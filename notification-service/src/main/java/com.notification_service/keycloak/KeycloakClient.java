package com.notification_service.keycloak;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.provider.keycloak.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String issuerUri;

    private String getRealmFromIssuer() {
        String[] parts = issuerUri.split("/realms/");
        return parts.length > 1 ? parts[1] : "crm-realm";
    }


    public String getServiceAccessToken() {
        String body = "grant_type=client_credentials"
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                tokenUri, HttpMethod.POST, new HttpEntity<>(body, headers), JsonNode.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Failed to get access token from Keycloak: " + response.getStatusCode()
            );
        }

        return response.getBody().get("access_token").asText();
    }

    public String getEmailByUserId(String userId) {
        String token = getServiceAccessToken();
        String realm = getRealmFromIssuer();

        String url = String.format("%s/admin/realms/%s/users/%s",
                issuerUri.replace("/realms/" + realm, ""), realm, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Failed to fetch user from Keycloak: " + response.getStatusCode()
            );
        }

        Map<String, Object> body = response.getBody();
        if (body == null || !body.containsKey("email")) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Email not found for userId: " + userId
            );
        }

        return body.get("email").toString();
    }

}
