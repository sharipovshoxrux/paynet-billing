package uz.baraka.paynetbilling.application;

import uz.baraka.paynetbilling.web.rpc.JsonRpcModels;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface BillingService {
    record ApplicationInfo(String applicationId, String name, BigDecimal amount, boolean paid) {}
    record ReconRow(String applicationId, String name, BigDecimal amount, String timestamp) {}

    ApplicationInfo getApplicationInfo(String applicationId);
    boolean isAlreadyPaid(String applicationId);
    List<ReconRow> statementForReconciliation(LocalDateTime from, LocalDateTime to);
    long cancelByTransactionId(long transactionId);

    JsonRpcModels.ApplicationInfo perform(long transactionId, String applicationId, BigDecimal amount);
    JsonRpcModels.CheckByTxResult checkByTransactionId(long transactionId);

}
