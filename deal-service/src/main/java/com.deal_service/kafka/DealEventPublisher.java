package com.deal_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DealEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topic:crm-events}")
    private String topic;

    public void publish(String eventType, UUID originAggregateId, Map<String, Object> payload) {
        try {
            Map<String, Object> envelope = Map.of(
                    "eventId", UUID.randomUUID().toString(),
                    "eventType", eventType,
                    "eventVersion", 1,
                    "eventTime", Instant.now().toString(),
                    "sourceService", "deal-service",
                    "originAggregateId", originAggregateId != null ? originAggregateId.toString() : null,
                    "correlationId", null,
                    "causationId", null,
                    "payload", payload
            );

            String jsonMessage = objectMapper.writeValueAsString(envelope);
            String key = originAggregateId != null ? originAggregateId.toString() : UUID.randomUUID().toString();

            kafkaTemplate.send(topic, key, jsonMessage);
            log.info("✅ Kafka event sent: {} | Key: {} | Topic: {}", eventType, key, topic);
        } catch (Exception e) {
            log.error("❌ Error publishing Kafka event: {}", eventType, e);
        }
    }
}
