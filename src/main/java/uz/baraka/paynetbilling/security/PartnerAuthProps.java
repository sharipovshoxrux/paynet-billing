package uz.baraka.paynetbilling.security;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@ConfigurationProperties(prefix = "security.creds")
@Validated
public record PartnerAuthProps(List<Partner> partners) {

    public record Partner(
            @NotBlank String username,
            @NotBlank String passwordHash,
            boolean enabled
    ) {}
}
