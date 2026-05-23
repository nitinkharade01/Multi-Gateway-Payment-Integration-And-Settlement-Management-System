package com.fintek.fraud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.fintek")
public class FraudMonitoringApplication {
    public static void main(String[] args) {
        SpringApplication.run(FraudMonitoringApplication.class, args);
    }
}
