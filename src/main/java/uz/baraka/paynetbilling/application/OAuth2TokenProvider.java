package uz.baraka.paynetbilling.application;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import uz.baraka.paynetbilling.config.AppProps;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OAuth2TokenProvider {
    private final RestClient restClient;
    private final AppProps props;

    private volatile String token;
    private volatile long expiryEpochSeconds = 0;

    public synchronized String getAccessToken() {
        long now = System.currentTimeMillis() / 1000;
        if (token != null && now < expiryEpochSeconds - 30) {
            return token;
        }

        var body = Map.of(
                "grant_type", "client_credentials",
                "client_id", props.webhook().clientId(),
                "client_secret", props.webhook().clientSecret()
        );

        var resp = restClient.post()
                .uri(props.webhook().tokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body.entrySet()
                        .stream()
                        .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                        .collect(Collectors.joining("&")))
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        this.token = (String) resp.get("access_token");
        int expiresIn = ((Number) resp.get("expires_in")).intValue();
        this.expiryEpochSeconds = now + expiresIn;
        return token;
    }
}
