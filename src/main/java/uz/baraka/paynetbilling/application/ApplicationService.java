package uz.baraka.paynetbilling.application;

import uz.baraka.paynetbilling.domain.ApplicationPurpose;
import uz.baraka.paynetbilling.domain.ApplicationSource;

import java.math.BigDecimal;
import java.util.UUID;

public interface ApplicationService {
    record CreateRequest(UUID userId, String pinfl, String name,
                         ApplicationSource source, ApplicationPurpose purpose) {}

    record CreateResponse(String applicationId, String name, BigDecimal amount,
                          ApplicationSource source, ApplicationPurpose purpose) {}

    CreateResponse create(CreateRequest req);
}
