package com.fintek.discovery;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "eureka.client.fetch-registry=false",
        "eureka.client.register-with-eureka=false"
})
@ActiveProfiles("test")
class DiscoveryServerApplicationTest {
    @Autowired
    private ApplicationContext context;

    @Test
    void shouldLoadDiscoveryServerContext() {
        assertNotNull(context.getBean(DiscoveryServerApplication.class),
                "Discovery server application bean should load");
    }

    @Test
    void shouldStartEurekaServerConfiguration() {
        assertNotNull(DiscoveryServerApplication.class.getAnnotation(EnableEurekaServer.class),
                "Discovery application should be annotated as Eureka server");
    }
}
