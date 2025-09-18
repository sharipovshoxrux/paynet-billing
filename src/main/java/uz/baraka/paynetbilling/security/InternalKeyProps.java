package uz.baraka.paynetbilling.security;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "internal.api")
@Validated
public record InternalKeyProps(
        @NotBlank String key,          // the secret value
        String headerName              // optional override; default in config
) {}
