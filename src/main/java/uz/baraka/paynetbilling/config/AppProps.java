package uz.baraka.paynetbilling.config;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "app")
@Validated
public record AppProps(
        @Positive BigDecimal fixedAmount
) {

}
