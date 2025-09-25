package uz.baraka.paynetbilling.web.rpc;

import org.springframework.stereotype.Component;
import uz.baraka.paynetbilling.domain.BankType;
import uz.baraka.paynetbilling.exception.ServiceNotFoundException;

@Component
public final class ServiceIdMapper {
    public int toServiceId(BankType bankType) {
        return switch (bankType) {
            case ALOQA -> 1;
            case XALQ -> 2;
        };
    }

    public BankType toBankType(int serviceId) {
        return switch (serviceId) {
            case 1 -> BankType.ALOQA;
            case 2 -> BankType.XALQ;
            default -> throw new ServiceNotFoundException("Услуга не найдена");
        };
    }
}
