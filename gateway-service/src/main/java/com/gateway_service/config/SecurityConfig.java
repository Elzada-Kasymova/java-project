package com.gateway_service.config;

import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.core.convert.converter.Converter;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(exchange -> exchange

                        .pathMatchers(HttpMethod.POST, "/api/users").permitAll()

                        .pathMatchers(HttpMethod.OPTIONS).permitAll()

                        .pathMatchers("/internal/**").hasAuthority("ROLE_service.internal")

                        .anyExchange().authenticated()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthoritiesConverter()))
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
                )

                .build();
    }



    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthoritiesConverter() {
        return new ReactiveJwtAuthenticationConverterAdapter(keycloakJwtAuthenticationConverter());
    }

    private JwtAuthenticationConverter keycloakJwtAuthenticationConverter() {

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            Set<String> roles = new HashSet<>();

            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.get("roles") instanceof List<?> realmRoles) {
                realmRoles.forEach(r -> roles.add(normalizeRole(r.toString())));
            }

            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess != null) {
                resourceAccess.values().forEach(obj -> {
                    Map<?, ?> client = (Map<?, ?>) obj;
                    Object rolesObj = client.get("roles");

                    if (rolesObj instanceof List<?> clientRoles) {
                        clientRoles.forEach(r -> roles.add(normalizeRole(r.toString())));
                    }
                });
            }

            log.debug("Extracted roles from JWT: {}", roles);

            return roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        });

        return converter;
    }


    private String normalizeRole(String role) {
        role = role.trim();
        return role.startsWith("ROLE_") ? role : "ROLE_" + role;
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        String jwkSetUri = "http://keycloak:8080/realms/crm-realm/protocol/openid-connect/certs";
        NimbusReactiveJwtDecoder jwtDecoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();

        OAuth2TokenValidator<Jwt> customValidator = token -> {
            String iss = token.getIssuer() != null ? token.getIssuer().toString() : "";
            if ("http://keycloak:8080/realms/crm-realm".equals(iss)
                    || "http://localhost:9090/realms/crm-realm".equals(iss)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Invalid issuer: " + iss, null)
            );
        };

        jwtDecoder.setJwtValidator(customValidator);
        return jwtDecoder;
    }


}
