package com.notification_service.openfeign;

import com.notification_service.keycloak.KeycloakTokenProvider;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor(KeycloakTokenProvider tokenProvider) {
        return template -> {

            String token = tokenProvider.getToken();
            template.header("Authorization", "Bearer " + token);
        };
    }
}

