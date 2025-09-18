package uz.baraka.paynetbilling.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface BillingService {
    record ApplicationInfo(String applicationId, String name, BigDecimal amount, boolean paid) {}
    record ReconRow(String applicationId, String name, BigDecimal amount, String timestamp) {}

    ApplicationInfo getApplicationInfo(String applicationId);
    boolean isAlreadyPaid(String applicationId);
    ApplicationInfo perform(String applicationId, BigDecimal amount);
    void cancelByApplicationId(String applicationId);
    List<ReconRow> statementForReconciliation(LocalDateTime from, LocalDateTime to);
}
