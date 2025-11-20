package com.users_service.keycloak;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
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


    public boolean userExistsByEmail(String email, String token) {
        String realm = getRealmFromIssuer();
        String url = String.format("%s/admin/realms/%s/users?email=%s",
                issuerUri.replace("/realms/" + realm, ""), realm, email);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            return response.getBody() != null && !response.getBody().isEmpty();
        } catch (HttpClientErrorException e) {
            log.error("Error checking user existence in Keycloak: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Error connecting to Keycloak");
        }
    }


    public String createUser(String username, String password, String email, String firstName, String lastName) {
        String token = getServiceAccessToken();
        String realm = getRealmFromIssuer();

        if (userExistsByEmail(email, token)) {
            log.warn("User with email {} already exists in Keycloak", email);
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User already exists with email: " + email
            );
        }

        String url = String.format("%s/admin/realms/%s/users",
                issuerUri.replace("/realms/" + realm, ""), realm);

        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("enabled", true);

        if (firstName != null && !firstName.isBlank()) {
            user.put("firstName", firstName);
        }
        if (lastName != null && !lastName.isBlank()) {
            user.put("lastName", lastName);
        }

        List<Map<String, Object>> credentials = List.of(Map.of(
                "type", "password",
                "value", password,
                "temporary", false
        ));
        user.put("credentials", credentials);


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        ResponseEntity<Void> response;
        try {
            response = restTemplate.postForEntity(url, new HttpEntity<>(user, headers), Void.class);
        } catch (HttpClientErrorException e) {
            log.error("Failed to create user in Keycloak: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to create user in Keycloak");
        }

        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Failed to create user in Keycloak: " + response.getStatusCode()
            );
        }

        String location = response.getHeaders().getLocation() != null
                ? response.getHeaders().getLocation().toString()
                : "unknown";
        log.info("Created new user in Keycloak: {}", location);
        return location.substring(location.lastIndexOf("/") + 1);
    }

    public void deleteUser(String keycloakUserId) {
        String token = getServiceAccessToken();
        String realm = getRealmFromIssuer();

        String url = String.format("%s/admin/realms/%s/users/%s",
                issuerUri.replace("/realms/" + realm, ""), realm, keycloakUserId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            log.info("Deleted user {} from Keycloak", keycloakUserId);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("User {} not found in Keycloak", keycloakUserId);
        } catch (HttpClientErrorException e) {
            log.error("Error deleting user {} from Keycloak: {}", keycloakUserId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Error deleting user from Keycloak");
        }
    }

}
