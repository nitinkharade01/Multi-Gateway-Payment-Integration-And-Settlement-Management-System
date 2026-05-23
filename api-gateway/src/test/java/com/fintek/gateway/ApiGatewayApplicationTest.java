package com.fintek.gateway;

import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class ApiGatewayApplicationTest {
    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldLoadGatewayApplicationContextAndExposeActuatorHealthEndpoint() {
        webTestClient.get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldRouteBackendServicePathsToConfiguredServices() {
        Map<String, URI> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectMap(route -> route.getId(), route -> route.getUri())
                .block();

        assertNotNull(routes, "Gateway route definitions should be available");
        assertEquals("lb://auth-service", routes.get("auth").toString(), "Auth path should route to auth-service");
        assertEquals("lb://merchant-service", routes.get("merchant").toString(), "Merchant path should route to merchant-service");
        assertEquals("lb://payment-service", routes.get("payment").toString(), "Payment path should route to payment-service");
        assertEquals("lb://refund-service", routes.get("refunds").toString(), "Refund path should route to refund-service");
        assertEquals("lb://settlement-service", routes.get("settlements").toString(), "Settlement path should route to settlement-service");
        assertEquals("lb://webhook-service", routes.get("webhook").toString(), "Webhook path should route to webhook-service");
    }

    @Test
    void shouldAllowPublicAuthEndpointsThroughSecurityLayer() {
        webTestClient.post().uri("/api/auth/login")
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
