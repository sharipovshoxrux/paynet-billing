package uz.baraka.paynetbilling.application.event;

import java.util.UUID;

public record PaymentCompletedEvent(
        String applicationId,
        UUID userId,
        boolean paid
) {}
