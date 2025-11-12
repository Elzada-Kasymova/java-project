package com.activity_service.kafka;

import com.activity_service.repository.ActivityRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityEventListener {

    private final ObjectMapper objectMapper;
    private final ActivityRepository repository;

    @KafkaListener(topics = "crm-events", groupId = "activity-service-group")
    public void handleEvent(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            String eventType = event.get("eventType").asText();
            JsonNode payload = event.get("payload");

            switch (eventType) {
                case "UserDeleted" -> {
                    UUID userId = UUID.fromString(payload.get("id").asText());
                    repository.findAll().stream()
                            .filter(a -> userId.equals(a.getUserId()))
                            .forEach(a -> repository.deleteById(a.getId()));
                    log.info("🗑 Deleted all activities for userId: {}", userId);
                }
                case "CompanyDeleted" -> {
                    UUID companyId = UUID.fromString(payload.get("id").asText());
                    repository.findAll().stream()
                            .filter(a -> companyId.equals(a.getCompanyId()))
                            .forEach(a -> repository.deleteById(a.getId()));
                    log.info("🗑 Deleted all activities for companyId: {}", companyId);
                }
                case "DealDeleted" -> {
                    UUID dealId = UUID.fromString(payload.get("id").asText());
                    repository.findAll().stream()
                            .filter(a -> dealId.equals(a.getDealId()))
                            .forEach(a -> repository.deleteById(a.getId()));
                    log.info("🗑 Deleted all activities for dealId: {}", dealId);
                }
                default -> log.debug("ℹ️ Ignored event type: {}", eventType);
            }

        } catch (Exception e) {
            log.error("❌ Failed to process Kafka event: {}", message, e);
        }
    }
}
