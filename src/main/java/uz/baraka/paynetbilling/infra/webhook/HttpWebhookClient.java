package uz.baraka.paynetbilling.infra.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import uz.baraka.paynetbilling.application.OAuth2TokenProvider;
import uz.baraka.paynetbilling.config.AppProps;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class HttpWebhookClient implements WebhookClient{
    private static final Logger log = LoggerFactory.getLogger(HttpWebhookClient.class);

    private final RestClient restClient;
    private final AppProps props;
    private final ObjectMapper om;
    private final OAuth2TokenProvider tokenProvider;

    @Override
    public void sendPaymentUpdate(String applicationId, UUID userId, boolean paid) {
        var cfg = props.webhook();
        if (cfg == null || !cfg.enabled()) {
            log.debug("Webhook disabled; skip appId={}", applicationId);
            return;
        }

        final String body;
        try {
            body = om.writeValueAsString(Map.of(
                    "applicationId", applicationId,
                    "userId", userId.toString(),
                    "paid", paid
            ));
        } catch (Exception e) {
            log.error("Webhook serialize failed appId={}", applicationId, e);
            return;
        }

        log.info("Webhook sending payment update, body={}", body);

        final String accessToken;
        try {
            accessToken = tokenProvider.getAccessToken();
        } catch (Exception e) {
            log.error("OAuth token fetch failed; skip appId={}", applicationId, e);
            return;
        }

        final String idempotencyKey = UUID.randomUUID().toString();
        final int attempts = Math.max(1, cfg.maxRetries() + 1);

        for (int i = 1; i <= attempts; i++) {
            Integer httpCode = null;
            try {
                restClient.post()
                        .uri(cfg.url())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .header("X-Idempotency-Key", idempotencyKey)
                        .header("X-Event-Type", "payment.updated")
                        .header("User-Agent", "paynet-billing")
                        .body(body)
                        .retrieve()
                        .toBodilessEntity();

                log.info("Webhook success appId={} attempt={} idem={}", applicationId, i, idempotencyKey);
                return;

            } catch (org.springframework.web.client.RestClientResponseException e) {
                httpCode = e.getStatusCode().value();
                log.warn("Webhook non-2xx appId={} http={} attempt={}/{} body={}",
                        applicationId, httpCode, i, attempts, snippet(e.getResponseBodyAsString()));
            } catch (org.springframework.web.client.RestClientException e) {
                log.warn("Webhook IO error appId={} attempt={}/{}: {}",
                        applicationId, i, attempts, e.toString());
            }

            if (!isTransient(httpCode)) break;
            sleepBackoff(i);
        }

        log.error("Webhook failed after {} attempts appId={} idem={}", attempts, applicationId, idempotencyKey);
    }

    private static boolean isTransient(Integer httpCode) {
        return httpCode == null || (httpCode >= 500 && httpCode < 600);
    }

    private static void sleepBackoff(int attempt) {
        long base = Math.min(200L * (1L << Math.min(3, attempt - 1)), 2000L);
        long jitter = ThreadLocalRandom.current().nextLong(0, 150);
        try { Thread.sleep(base + jitter); } catch (InterruptedException ignored) {}
    }

    private static String snippet(String s) {
        if (s == null) return "";
        return s.length() <= 512 ? s : s.substring(0, 512) + "...";
    }
}
