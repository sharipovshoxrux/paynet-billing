package uz.baraka.paynetbilling.domain.factory;

import org.springframework.stereotype.Component;
import uz.baraka.paynetbilling.domain.ApplicationPurpose;
import uz.baraka.paynetbilling.domain.ApplicationSource;
import uz.baraka.paynetbilling.domain.entity.Application;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class ApplicationFactory {
    private final Clock clock;

    public ApplicationFactory(Clock clock) {
        this.clock = clock;
    }

    public Application newApplication(String applicationId,
                                      UUID userId,
                                      String pinfl,
                                      String name,
                                      BigDecimal amount,
                                      ApplicationSource source,
                                      ApplicationPurpose purpose) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        if (pinfl == null || pinfl.isBlank()) throw new IllegalArgumentException("pinfl is required");
        String effectiveName = (name == null || name.isBlank()) ? "Unknown" : name.trim();

        Application app = new Application();
        app.setApplicationId(applicationId);
        app.setUserId(userId);
        app.setPinfl(pinfl.trim());
        app.setName(effectiveName);
        app.setAmount(amount);
        app.setSource(source);
        app.setPurpose(purpose);
        app.setCreatedAt(OffsetDateTime.now(clock));
        return app;
    }
}
