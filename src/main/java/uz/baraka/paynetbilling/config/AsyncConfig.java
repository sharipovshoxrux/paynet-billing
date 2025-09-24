package uz.baraka.paynetbilling.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {
    @Bean(name = "webhookExecutor")
    public Executor webhookExecutor() {
        var t = new ThreadPoolTaskExecutor();
        t.setThreadNamePrefix("webhook-");
        t.setCorePoolSize(2);
        t.setMaxPoolSize(4);
        t.setQueueCapacity(100);
        t.initialize();
        return t;
    }
}
