package com.company_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyEventPublisher {

    private static final String TOPIC = "crm-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publish(String eventType, String originAggregateId, Map<String, Object> payload) {
        try {
            Map<String, Object> envelope = Map.of(
                    "eventId", UUID.randomUUID().toString(),
                    "eventType", eventType,
                    "eventVersion", 1,
                    "eventTime", Instant.now().toString(),
                    "sourceService", "company-service",
                    "originAggregateId", originAggregateId,
                    "correlationId", null,
                    "causationId", null,
                    "payload", payload
            );

            String message = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send(TOPIC, originAggregateId, message);

            log.info("✅ Published {} event: {}", eventType, message);
        } catch (Exception e) {
            log.error("❌ Failed to publish {} event: {}", eventType, e.getMessage(), e);
        }
    }
}
