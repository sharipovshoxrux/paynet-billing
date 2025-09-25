package uz.baraka.paynetbilling.application;

import uz.baraka.paynetbilling.domain.entity.Application;
import uz.baraka.paynetbilling.domain.entity.PaymentTransaction;
import uz.baraka.paynetbilling.web.rpc.JsonRpcModels;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface BillingService {
    record ApplicationInfo(String applicationId, String name, BigDecimal amount, boolean paid) {}
    record StatementRow(
            long providerTrnId,
            Long transactionId,
            BigDecimal amount,
            String timestamp
    ) {}

    ApplicationInfo getApplicationInfo(String applicationId);
    boolean isAlreadyPaid(String applicationId);
    List<StatementRow> statementForReconciliation(LocalDateTime from, LocalDateTime to);
    long cancelByTransactionId(long transactionId);

    JsonRpcModels.ApplicationInfo perform(long transactionId, String applicationId, BigDecimal amount);
    JsonRpcModels.CheckByTxResult checkByTransactionId(long transactionId);

    Application requireAppAndValidateService(String applicationId, Integer serviceId);
    PaymentTransaction requireTxAndValidateService(long transactionId, Integer serviceId);
    List<StatementRow> statementForReconciliation(LocalDateTime from, LocalDateTime to, Integer serviceId);

}
