package com.deal_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DealEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final DealEventPayloadBuilder payloadBuilder;

    private static final String TOPIC_CREATE = "deal-create";
    private static final String TOPIC_UPDATE = "deal-update";
    private static final String TOPIC_STAGE_CHANGE = "deal-stage-change";
    private static final String TOPIC_DELETE = "deal-delete";

    public void publishDealCreated(UUID dealId) {
        try {
            String payload = payloadBuilder.buildDealCreatedPayload(dealId);
            kafkaTemplate.send(TOPIC_CREATE, dealId.toString(), payload);
            log.info("Published deal-create key={}, value={}", dealId, payload);
        } catch (Exception e) {
            log.error("Failed to publish deal-create for {}: {}", dealId, e.getMessage(), e);
        }
    }

    public void publishDealDeleted(UUID dealId) {
        try {
            String payload = payloadBuilder.buildDealDeletedPayload(dealId);
            kafkaTemplate.send(TOPIC_DELETE, dealId.toString(), payload);
            log.info("Published deal-delete key={}, value={}", dealId, payload);
        } catch (Exception e) {
            log.error("Failed to publish deal-delete for {}: {}", dealId, e.getMessage(), e);
        }
    }

    public void publishDealStageChange(UUID dealId, Enum<?> stage) {
        try {
            String payload = payloadBuilder.buildStageChangePayload(dealId, stage);
            kafkaTemplate.send(TOPIC_STAGE_CHANGE, dealId.toString(), payload);
            log.info("Published deal-stage-change key={}, value={}", dealId, payload);
        } catch (Exception e) {
            log.error("Failed to publish deal-stage-change for {}: {}", dealId, e.getMessage(), e);
        }
    }

    public void publishDealUpdated(UUID dealId, UUID companyId, List<UUID> userIds) {
        try {
            String payload = payloadBuilder.buildDealUpdatedPayload(dealId, companyId, userIds);
            kafkaTemplate.send(TOPIC_UPDATE, dealId.toString(), payload);
            log.info("Published deal-update key={}, value={}", dealId, payload);
        } catch (Exception e) {
            log.error("Failed to publish deal-update for {}: {}", dealId, e.getMessage(), e);
        }
    }
}
