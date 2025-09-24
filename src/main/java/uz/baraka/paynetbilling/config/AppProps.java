package uz.baraka.paynetbilling.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.Duration;

@ConfigurationProperties(prefix = "app")
@Validated
public record AppProps(
        @Positive BigDecimal fixedAmount,
        Webhook webhook
) {
    @Validated
    public record Webhook(
            boolean enabled,
            @NotBlank String url,
            @NotBlank String tokenUrl,
            @NotBlank String clientId,
            @NotBlank String clientSecret,
            @DurationMin(seconds = 1) Duration connectTimeout,
            @DurationMin(seconds = 1) Duration readTimeout,
            @Min(0) int maxRetries
    ) {}

}
