package com.fintek.notification.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationExecutorConfig {
    @Bean(destroyMethod = "close")
    ExecutorService notificationExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
