package com.notification_service.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class TokenProvider {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${keycloak.token-uri}")
    private String tokenUri;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    // simple cache
    private final AtomicReference<CachedToken> cached = new AtomicReference<>();

    public TokenProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder().build();
    }

    public synchronized String getAccessToken() {
        CachedToken ct = cached.get();
        if (ct != null && ct.getExpiresAt().isAfter(Instant.now().plusSeconds(30))) {
            return ct.getToken();
        }
        // fetch new
        String resp = webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode node = objectMapper.readTree(resp);
            String token = node.get("access_token").asText();
            int expiresIn = node.get("expires_in").asInt(60);
            cached.set(new CachedToken(token, Instant.now().plusSeconds(expiresIn)));
            return token;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to obtain token from Keycloak", ex);
        }
    }

    private static class CachedToken {
        private final String token;
        private final Instant expiresAt;
        public CachedToken(String token, Instant expiresAt) { this.token = token; this.expiresAt = expiresAt; }
        public String getToken() { return token; }
        public Instant getExpiresAt() { return expiresAt; }
    }
}
