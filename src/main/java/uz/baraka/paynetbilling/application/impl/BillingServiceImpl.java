package uz.baraka.paynetbilling.application.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.baraka.paynetbilling.application.BillingService;
import uz.baraka.paynetbilling.application.event.PaymentCompletedEvent;
import uz.baraka.paynetbilling.application.validation.AmountValidator;
import uz.baraka.paynetbilling.domain.ApplicationStatus;
import uz.baraka.paynetbilling.domain.BankType;
import uz.baraka.paynetbilling.domain.entity.Application;
import uz.baraka.paynetbilling.domain.entity.PaymentTransaction;
import uz.baraka.paynetbilling.domain.entity.TxnState;
import uz.baraka.paynetbilling.exception.CannotCancelAfterOutcomeException;
import uz.baraka.paynetbilling.exception.NoSuchApplicationException;
import uz.baraka.paynetbilling.exception.TransactionAlreadyCancelledException;
import uz.baraka.paynetbilling.exception.TransactionAlreadyExistsException;
import uz.baraka.paynetbilling.exception.TransactionNotFoundException;
import uz.baraka.paynetbilling.port.ApplicationOutcomeRepository;
import uz.baraka.paynetbilling.port.ApplicationRepository;
import uz.baraka.paynetbilling.port.PaymentTransactionRepository;
import uz.baraka.paynetbilling.web.rpc.JsonRpcModels;
import uz.baraka.paynetbilling.web.rpc.ServiceIdMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BillingServiceImpl implements BillingService {
    private final PaymentTransactionRepository txRepo;
    private final ApplicationRepository appRepo;
    private final ApplicationOutcomeRepository outcomeRepo;
    private final AmountValidator amountValidator;
    private final ServiceIdMapper serviceIdMapper;
    private final ApplicationEventPublisher events;
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("GMT+5"));

    @Override
    public ApplicationInfo getApplicationInfo(String applicationId) {
        var app = mustBeActive(applicationId);

        boolean paid = txRepo.existsByApplicationIdAndState(applicationId, TxnState.PAID);
        return new ApplicationInfo(
                app.getApplicationId(),
                app.getName(),
                app.getAmount().divide(BigDecimal.valueOf(100)),
                paid
        );
    }

    @Override
    public boolean isAlreadyPaid(String applicationId) {
        return txRepo.existsByApplicationIdAndState(applicationId, TxnState.PAID);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatementRow> statementForReconciliation(LocalDateTime from, LocalDateTime to) {
        ZoneId APP_ZONE = ZoneId.of("Asia/Tashkent");
        OffsetDateTime fromUtc = from.atZone(APP_ZONE).toInstant().atOffset(ZoneOffset.UTC);
        OffsetDateTime toUtc   = to.atZone(APP_ZONE).toInstant().atOffset(ZoneOffset.UTC);

        var all = txRepo.findAllByCreatedAtBetween(fromUtc, toUtc);

        var paid = all.stream()
                .filter(t -> t.getState() == TxnState.PAID)
                .toList();

        DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(APP_ZONE);

        var out = new ArrayList<StatementRow>(paid.size());
        for (var t : paid) {
            out.add(new StatementRow(
                    t.getId(),
                    t.getTransactionId(),
                    t.getAmount(),
                    TS.format(t.getCreatedAt())
            ));
        }
        out.sort(Comparator.comparing(StatementRow::timestamp));
        return out;
    }

    @Override
    @Transactional
    public long cancelByTransactionId(long transactionId) {
        var tx = txRepo.findByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Транзакция не найдена"));

        var app = appRepo.findByApplicationId(tx.getApplicationId())
                .orElseThrow(() -> new NoSuchApplicationException("Клиент не найден"));

        boolean outcomeExists = outcomeRepo.existsByApplicationIdAndPurpose(
                app.getApplicationId(), app.getPurpose());

        if (outcomeExists) {throw new CannotCancelAfterOutcomeException("Недостаточно средств на счету клиента для отмены платежа");}


        if (tx.getState() == TxnState.CANCELLED) {
            throw new TransactionAlreadyCancelledException("Транзакция уже отменена");
        }

        tx.setState(TxnState.CANCELLED);
        txRepo.save(tx);

        return tx.getId();
    }

    @Override
    @Transactional
    public JsonRpcModels.ApplicationInfo perform(long transactionId, String applicationId, BigDecimal amount) {
        var app = mustBeActive(applicationId);

        amountValidator.assertFixed(amount);

        txRepo.findByTransactionId(transactionId).ifPresent(tx -> {
            throw new TransactionAlreadyExistsException("Транзакция уже существует");
        });

        var tx = new PaymentTransaction();
        tx.setTransactionId(transactionId);
        tx.setApplicationId(applicationId);
        tx.setAmount(amount);
        tx.setState(TxnState.PAID);
        txRepo.save(tx);

        events.publishEvent(new PaymentCompletedEvent(app.getApplicationId(), app.getUserId(), true));

        return new JsonRpcModels.ApplicationInfo(app.getApplicationId(), app.getName(), app.getAmount().divide(BigDecimal.valueOf(100)), true);
    }


    @Override
    @Transactional(readOnly = true)
    public JsonRpcModels.CheckByTxResult checkByTransactionId(long transactionId) {
        var tx = txRepo.findByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Транзакция не найдена"));

        int state = switch (tx.getState()) {
            case PAID     -> 1;
            case CANCELLED -> 2;
            default       -> 0;
        };

        long providerTrnId = tx.getId();
        return new JsonRpcModels.CheckByTxResult(providerTrnId, state);
    }

    @Transactional(readOnly = true)
    public Application requireAppAndValidateService(String applicationId, Integer serviceId) {
        var app = appRepo.findByApplicationId(applicationId)
                .orElseThrow(() -> new NoSuchApplicationException("Клиент не найден"));

        int expected = serviceIdMapper.toServiceId(app.getBankType());
        if (!Objects.equals(serviceId, expected))
            throw new NoSuchApplicationException("Клиент не найден");
        return app;
    }

    @Transactional(readOnly = true)
    public PaymentTransaction requireTxAndValidateService(long transactionId, Integer serviceId) {
        var tx = txRepo.findByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Транзакция не найдена"));

        var app = appRepo.findByApplicationId(tx.getApplicationId())
                .orElseThrow(() -> new NoSuchApplicationException("Клиент не найден"));
        int expected = serviceIdMapper.toServiceId(app.getBankType());
        if (!Objects.equals(serviceId, expected))
            throw new NoSuchApplicationException("Клиент не найден");
        return tx;
    }

    @Transactional(readOnly = true)
    public List<StatementRow> statementForReconciliation(LocalDateTime from, LocalDateTime to, Integer serviceId) {
        BankType filterBank = (serviceId != null) ? serviceIdMapper.toBankType(serviceId) : null;

        var all = txRepo.findAllByCreatedAtBetween(
                from.atOffset(ZoneOffset.UTC), to.atOffset(ZoneOffset.UTC));

        var out = new ArrayList<StatementRow>();
        for (var t : all) {
            if (t.getState() != TxnState.PAID) continue;
            var app = appRepo.findByApplicationId(t.getApplicationId()).orElse(null);
            if (app == null) continue;
            if (filterBank != null && app.getBankType() != filterBank) continue;

            out.add(new StatementRow(
                    t.getId(),
                    t.getTransactionId(),
                    t.getAmount(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                            .withZone(ZoneId.of("GMT+5"))
                            .format(t.getCreatedAt())
            ));
        }
        out.sort(Comparator.comparing(StatementRow::timestamp));
        return out;
    }

    private Application mustBeActive(String applicationId) {
        var app = appRepo.findByApplicationId(applicationId)
                .orElseThrow(() -> new NoSuchApplicationException("Клиент не найден"));
        if (app.getStatus() != ApplicationStatus.ACTIVE) {
            throw new NoSuchApplicationException("Клиент не найден");
        }
        return app;
    }
}
