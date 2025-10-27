package com.gateway_service.congif;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("users-service", r -> r.path("/users-service/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://users-service:8000"))
                .route("company-service", r -> r.path("/company-service/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://company-service:8090"))
                .build();
    }
}
