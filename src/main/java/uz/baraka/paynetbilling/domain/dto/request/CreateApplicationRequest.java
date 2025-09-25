package uz.baraka.paynetbilling.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import uz.baraka.paynetbilling.domain.ApplicationPurpose;
import uz.baraka.paynetbilling.domain.ApplicationSource;
import uz.baraka.paynetbilling.domain.BankType;

import java.util.UUID;

public record CreateApplicationRequest(
        @NotNull UUID userId,
        @NotNull String pinfl,
        @NotNull String name,
        @NotNull ApplicationSource source,
        @NotNull ApplicationPurpose purpose,
        @NotNull BankType bankType
) {}
