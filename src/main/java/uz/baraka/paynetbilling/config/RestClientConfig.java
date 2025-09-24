package uz.baraka.paynetbilling.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient restClient(AppProps props) {
        var cfg = props.webhook();
        var http = HttpClient.newBuilder()
                .connectTimeout(cfg.connectTimeout())
                .version(java.net.http.HttpClient.Version.HTTP_1_1)
                .build();
        var rf = new JdkClientHttpRequestFactory(http);
        rf.setReadTimeout(cfg.readTimeout());
        return RestClient.builder().requestFactory(rf).build();
    }
}
