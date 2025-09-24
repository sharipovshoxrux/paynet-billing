package uz.baraka.paynetbilling.application.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.baraka.paynetbilling.config.AppProps;
import uz.baraka.paynetbilling.exception.InvalidAmountException;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class AmountValidator {
    private final AppProps props;
    public void assertFixed(BigDecimal incoming) {
        if (incoming == null || props.fixedAmount().compareTo(incoming) != 0)
            throw new InvalidAmountException("Неверная сумма");
    }
}
