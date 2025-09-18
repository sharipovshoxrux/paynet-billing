package uz.baraka.paynetbilling.domain.dto.response;

import uz.baraka.paynetbilling.domain.ApplicationPurpose;
import uz.baraka.paynetbilling.domain.ApplicationSource;

import java.math.BigDecimal;

public record CreateApplicationResponse(
        String applicationId,
        String name,
        BigDecimal amount,
        ApplicationSource source,
        ApplicationPurpose purpose
) {}
