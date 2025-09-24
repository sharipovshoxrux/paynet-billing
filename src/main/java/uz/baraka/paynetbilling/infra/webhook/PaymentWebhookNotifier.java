package uz.baraka.paynetbilling.infra.webhook;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uz.baraka.paynetbilling.application.event.PaymentCompletedEvent;


@Component
@RequiredArgsConstructor
public class PaymentWebhookNotifier {

    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookNotifier.class);
    private final WebhookClient webhookClient;

    @Async("webhookExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentCompleted(PaymentCompletedEvent e) {
        try {
            webhookClient.sendPaymentUpdate(e.applicationId(), e.userId(), e.paid());
        } catch (Exception ex) {
            log.error("Webhook client threw appId={}", e.applicationId(), ex);
        }
    }
}
