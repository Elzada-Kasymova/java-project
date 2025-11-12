package com.users_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class UserEventPublisher {

    private static final String TOPIC = "crm-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public UserEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishUserCreated(Map<String, Object> payload) {
        publishEvent("UserCreated", payload, (String) payload.get("id"));
    }

    public void publishUserUpdated(Map<String, Object> payload) {
        publishEvent("UserUpdated", payload, (String) payload.get("id"));
    }

    public void publishUserDeleted(Map<String, Object> payload) {
        publishEvent("UserDeleted", payload, (String) payload.get("id"));
    }

    private void publishEvent(String eventType, Map<String, Object> payload, String originAggregateId) {
        try {
            Map<String, Object> envelope = Map.of(
                    "eventId", UUID.randomUUID().toString(),
                    "eventType", eventType,
                    "eventVersion", 1,
                    "eventTime", Instant.now().toString(),
                    "sourceService", "users-service",
                    "originAggregateId", originAggregateId,
                    "correlationId", null,
                    "causationId", null,
                    "payload", payload
            );

            String value = objectMapper.writeValueAsString(envelope);
            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, originAggregateId, value);
            kafkaTemplate.send(record);

            log.info("✅ Published {} event: {}", eventType, value);
        } catch (Exception e) {
            log.error("❌ Failed to publish {} event: {}", eventType, e.getMessage(), e);
        }
    }
}
