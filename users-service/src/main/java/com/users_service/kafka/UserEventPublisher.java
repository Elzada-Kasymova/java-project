package com.users_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC_CREATED = "user-created";
    private static final String TOPIC_UPDATED = "user-updated";
    private static final String TOPIC_DELETED = "user-deleted";

    public void publishUserCreated(String userId) {
        try {
            kafkaTemplate.send(TOPIC_CREATED, userId, userId);
            log.info("Published user-created key={}, value={}", userId, userId);
        } catch (Exception e) {
            log.error("Failed to publish user-created for {}: {}", userId, e.getMessage(), e);
        }
    }

    public void publishUserDeleted(String userId) {
        try {
            kafkaTemplate.send(TOPIC_DELETED, userId, userId);
            log.info("Published user-deleted key={}, value={}", userId, userId);
        } catch (Exception e) {
            log.error("Failed to publish user-deleted for {}: {}", userId, e.getMessage(), e);
        }
    }

    public void publishUserUpdated(String userId, List<UUID> companyIds) {
        try {
            String payload;

            if (companyIds == null || companyIds.isEmpty()) {
                payload = "{\"userId\":\"" + userId + "\"}";
            } else {
                List<String> companyIdsStr = companyIds.stream()
                        .filter(Objects::nonNull)
                        .map(UUID::toString)
                        .toList();

                if (companyIdsStr.size() == 1) {
                    payload = String.format(
                            "{\"userId\":\"%s\",\"companyId\":\"%s\"}",
                            userId, companyIdsStr.get(0)
                    );
                } else {
                    String idsJsonArray = companyIdsStr.stream()
                            .map(id -> "\"" + id + "\"")
                            .collect(Collectors.joining(","));

                    payload = String.format(
                            "{\"userId\":\"%s\",\"companyIds\":[%s]}",
                            userId,
                            idsJsonArray
                    );
                }
            }

            kafkaTemplate.send(TOPIC_UPDATED, userId, payload);
            log.info("Published user-updated key={}, value={}", userId, payload);

        } catch (Exception e) {
            log.error("Failed to publish user-updated for {}: {}", userId, e.getMessage(), e);
        }
    }
}
