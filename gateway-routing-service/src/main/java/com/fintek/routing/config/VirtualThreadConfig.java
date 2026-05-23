package com.fintek.routing.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VirtualThreadConfig {
    @Bean(destroyMethod = "close")
    ExecutorService gatewayExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
