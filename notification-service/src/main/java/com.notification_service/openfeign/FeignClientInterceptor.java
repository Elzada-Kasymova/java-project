package com.notification_service.openfeign;

import com.notification_service.keycloak.KeycloakClient;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;


@Component
public class FeignClientInterceptor implements RequestInterceptor {

    private final KeycloakClient keycloakClient;

    public FeignClientInterceptor(KeycloakClient keycloakClient) {
        this.keycloakClient = keycloakClient;
    }

    @Override
    public void apply(RequestTemplate template) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();


        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String token = jwtAuth.getToken().getTokenValue();
            template.header("Authorization", "Bearer " + token);
        } else {
            String serviceToken = keycloakClient.getServiceAccessToken();
            template.header("Authorization", "Bearer " + serviceToken);
        }
    }
}
