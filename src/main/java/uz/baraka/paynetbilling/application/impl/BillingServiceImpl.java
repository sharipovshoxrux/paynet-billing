package uz.baraka.paynetbilling.application.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.baraka.paynetbilling.application.BillingService;
import uz.baraka.paynetbilling.application.validation.AmountValidator;
import uz.baraka.paynetbilling.domain.entity.PaymentTransaction;
import uz.baraka.paynetbilling.domain.entity.TxnState;
import uz.baraka.paynetbilling.exception.NoSuchApplicationException;
import uz.baraka.paynetbilling.exception.TransactionAlreadyExistsException;
import uz.baraka.paynetbilling.exception.TransactionNotFoundException;
import uz.baraka.paynetbilling.port.ApplicationRepository;
import uz.baraka.paynetbilling.port.PaymentTransactionRepository;
import uz.baraka.paynetbilling.web.rpc.JsonRpcModels;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BillingServiceImpl implements BillingService {
    private final PaymentTransactionRepository txRepo;
    private final ApplicationRepository appRepo;
    private final AmountValidator amountValidator;
    private final ApplicationEventPublisher events;
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("GMT+5"));

    @Override
    public ApplicationInfo getApplicationInfo(String applicationId) {
        var app = appRepo.findByApplicationId(applicationId)
                .orElseThrow(() -> new NoSuchApplicationException("Клиент не найден"));
        boolean paid = txRepo.existsByApplicationIdAndState(applicationId, TxnState.PAID);
        return new ApplicationInfo(app.getApplicationId(), app.getName(), app.getAmount().divide(BigDecimal.valueOf(100)), paid);
    }

    @Override
    public boolean isAlreadyPaid(String applicationId) {
        return txRepo.existsByApplicationIdAndState(applicationId, TxnState.PAID);
    }

    @Override
    public List<ReconRow> statementForReconciliation(LocalDateTime from, LocalDateTime to) {
        var all = txRepo.findAllByCreatedAtBetween(from.atOffset(ZoneOffset.UTC), to.atOffset(ZoneOffset.UTC));
        var paid = all.stream().filter(t -> t.getState() == TxnState.PAID).toList();

        var out = new ArrayList<ReconRow>(paid.size());
        for (var t : paid) {
            var app = appRepo.findByApplicationId(t.getApplicationId()).orElse(null);
            String name = app != null ? app.getName() : "";
            out.add(new ReconRow(t.getApplicationId(), name, t.getAmount(),
                    TS.format(t.getCreatedAt().toLocalDateTime())));
        }
        out.sort(Comparator.comparing(ReconRow::timestamp));
        return out;
    }

    @Override
    @Transactional
    public long cancelByTransactionId(long transactionId) {
        var tx = txRepo.findByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Транзакция не найдена"));

        if (tx.getState() != TxnState.CANCELLED) {
            tx.setState(TxnState.CANCELLED);
            txRepo.save(tx);
        }

        return tx.getId();
    }

    @Override
    @Transactional
    public JsonRpcModels.ApplicationInfo perform(long transactionId, String applicationId, BigDecimal amount) {
        var app = appRepo.findByApplicationId(applicationId)
                .orElseThrow(() -> new NoSuchApplicationException("Клиент не найден"));

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

        //events.publishEvent(new PaymentCompletedEvent(app.getApplicationId(), app.getUserId(), true));

        return new JsonRpcModels.ApplicationInfo(app.getApplicationId(), app.getName(), app.getAmount().divide(BigDecimal.valueOf(100)), true);
    }


    @Override
    @Transactional(readOnly = true)
    public JsonRpcModels.CheckByTxResult checkByTransactionId(long transactionId) {
        var tx = txRepo.findByTransactionId(transactionId)
                .orElseThrow(() -> new NoSuchElementException("Tx not found"));

        int state = switch (tx.getState()) {
            case PAID     -> 1;
            case CANCELLED -> 2;
            default       -> 0;
        };

        long providerTrnId = tx.getId();
        return new JsonRpcModels.CheckByTxResult(providerTrnId, state);
    }
}
