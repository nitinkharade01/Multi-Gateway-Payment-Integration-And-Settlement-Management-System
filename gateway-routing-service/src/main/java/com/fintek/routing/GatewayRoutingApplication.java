package com.fintek.routing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.fintek")
public class GatewayRoutingApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayRoutingApplication.class, args);
    }
}
