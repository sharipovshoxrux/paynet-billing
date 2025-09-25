package uz.baraka.paynetbilling.application;

import jakarta.annotation.Nullable;
import uz.baraka.paynetbilling.domain.ApplicationPurpose;
import uz.baraka.paynetbilling.domain.ApplicationSource;
import uz.baraka.paynetbilling.domain.BankType;
import uz.baraka.paynetbilling.domain.entity.TxnState;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ApplicationService {
    record CreateRequest(UUID userId, String pinfl, String name,
                         ApplicationSource source, ApplicationPurpose purpose, BankType bankType) {}

    record CreateResponse(String applicationId, String name, BigDecimal amount,
                          ApplicationSource source, ApplicationPurpose purpose) {}

    CreateResponse create(CreateRequest req);

    List<ApplicationStatusResponse> findStatusesByUserId(UUID userId,
                                                         @Nullable TxnState statusFilter,
                                                         @Nullable ApplicationPurpose purpose,
                                                         @Nullable ApplicationSource source);

    void recordOutcome(String applicationId, ApplicationPurpose purpose);

    record ApplicationStatusResponse(
            String applicationId,
            TxnState status,
            ApplicationSource source,
            ApplicationPurpose purpose
    ) {}
}
