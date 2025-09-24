package uz.baraka.paynetbilling.infra.webhook;

import java.util.UUID;

public interface WebhookClient {
    void sendPaymentUpdate(String applicationId, UUID userId, boolean paid);
}
