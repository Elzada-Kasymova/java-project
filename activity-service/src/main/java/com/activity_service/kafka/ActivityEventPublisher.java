package com.activity_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC_CREATE = "activity-create";
    private static final String TOPIC_COMPLETED = "activity-completed";

    public void publishActivityCreated(UUID activityId) {
        try {
            String id = activityId.toString();
            kafkaTemplate.send(TOPIC_CREATE, id, id);
            log.info("Published activity-create key={}, value={}", id, id);
        } catch (Exception e) {
            log.error("Failed to publish activity-create for {}: {}", activityId, e.getMessage(), e);
        }
    }

    public void publishActivityCompleted(UUID activityId) {
        try {
            String id = activityId.toString();
            kafkaTemplate.send(TOPIC_COMPLETED, id, id);
            log.info("Published activity-completed key={}, value={}", id, id);
        } catch (Exception e) {
            log.error("Failed to publish activity-completed for {}: {}", activityId, e.getMessage(), e);
        }
    }
}
