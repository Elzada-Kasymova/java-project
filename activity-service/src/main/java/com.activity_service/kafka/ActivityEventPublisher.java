package com.activity_service.kafka;

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
public class ActivityEventPublisher {

    private static final String TOPIC = "crm-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ActivityEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishActivityCreated(Map<String, Object> payload) {
        publishEvent("ActivityCreated", payload, (String) payload.get("id"));
    }

    public void publishActivityUpdated(Map<String, Object> payload) {
        publishEvent("ActivityUpdated", payload, (String) payload.get("id"));
    }

    public void publishActivityDeleted(Map<String, Object> payload) {
        publishEvent("ActivityDeleted", payload, (String) payload.get("id"));
    }

    public void publishActivityCompleted(Map<String, Object> payload) {
        publishEvent("ActivityCompleted", payload, (String) payload.get("id"));
    }

    private void publishEvent(String eventType, Map<String, Object> payload, String originAggregateId) {
        try {
            Map<String, Object> envelope = Map.of(
                    "eventId", UUID.randomUUID().toString(),
                    "eventType", eventType,
                    "eventVersion", 1,
                    "eventTime", Instant.now().toString(),
                    "sourceService", "activity-service",
                    "originAggregateId", originAggregateId,
                    "correlationId", null,
                    "causationId", null,
                    "payload", payload
            );

            String value = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send(new ProducerRecord<>(TOPIC, originAggregateId, value));
            log.info("✅ Published {}: {}", eventType, value);
        } catch (Exception e) {
            log.error("❌ Failed to publish {} event: {}", eventType, e.getMessage(), e);
        }
    }
}
