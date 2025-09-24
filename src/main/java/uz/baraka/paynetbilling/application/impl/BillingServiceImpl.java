package uz.baraka.paynetbilling.application.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.baraka.paynetbilling.application.BillingService;
import uz.baraka.paynetbilling.application.event.PaymentCompletedEvent;
import uz.baraka.paynetbilling.application.validation.AmountValidator;
import uz.baraka.paynetbilling.domain.entity.PaymentTransaction;
import uz.baraka.paynetbilling.domain.entity.TxnState;
import uz.baraka.paynetbilling.port.ApplicationRepository;
import uz.baraka.paynetbilling.port.PaymentTransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public ApplicationInfo getApplicationInfo(String applicationId) {
        var app = appRepo.findByApplicationId(applicationId)
                .orElseThrow(() -> new NoSuchElementException("Application not found: " + applicationId));
        boolean paid = txRepo.existsByApplicationIdAndState(applicationId, TxnState.PAID);
        return new ApplicationInfo(app.getApplicationId(), app.getName(), app.getAmount(), paid);
    }

    @Override
    public boolean isAlreadyPaid(String applicationId) {
        return txRepo.existsByApplicationIdAndState(applicationId, TxnState.PAID);
    }

    @Override
    @Transactional
    public ApplicationInfo perform(String applicationId, BigDecimal amount) {
        var app = appRepo.findByApplicationId(applicationId)
                .orElseThrow(() -> new NoSuchElementException("Application not found: " + applicationId));

        amountValidator.assertFixed(amount);

        if (txRepo.existsByApplicationIdAndState(applicationId, TxnState.PAID)) {
            return new ApplicationInfo(app.getApplicationId(), app.getName(), app.getAmount(), true);
        }

        var tx = new PaymentTransaction();
        tx.setApplicationId(applicationId);
        tx.setAmount(amount);
        tx.setState(TxnState.PAID);
        txRepo.save(tx);

        events.publishEvent(new PaymentCompletedEvent(app.getApplicationId(), app.getUserId(), true));

        return new ApplicationInfo(app.getApplicationId(), app.getName(), app.getAmount(), true);
    }

    @Override
    @Transactional
    public void cancelByApplicationId(String applicationId) {
        txRepo.findTopByApplicationIdOrderByCreatedAtDesc(applicationId)
                .ifPresent(tx -> {
                    if (tx.getState() == TxnState.PAID) {
                        tx.setState(TxnState.CANCELLED);
                        txRepo.save(tx);
                    }
                });
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
}
