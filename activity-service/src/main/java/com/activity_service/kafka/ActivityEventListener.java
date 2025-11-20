package com.activity_service.kafka;

import com.activity_service.service.ActivityEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityEventListener {

    private final ActivityEventService eventService;

    @KafkaListener(
            topics = {
                    "user-created",
                    "user-updated",
                    "user-deleted",
                    "company-delete",
                    "deal-create",
                    "deal-stage-change",
                    "deal-delete"
            },
            groupId = "activity-service-group"
    )
    public void handleEvent(
            String message,
            @Header("kafka_receivedTopic") String topic
    ) {
        try {
            log.info("Received event from topic={} message={}", topic, message);

            switch (topic) {

                case "user-created" ->
                        eventService.handleUserCreated(UUID.fromString(message));

                case "user-updated" -> {
                    if (!message.startsWith("{")) {
                        eventService.handleUserUpdated(UUID.fromString(message), null);
                        break;
                    }

                    String userIdStr = extract(message, "userId");
                    String companyIdStr = extract(message, "companyId");
                    UUID userId = UUID.fromString(userIdStr);
                    UUID companyId = companyIdStr != null ? UUID.fromString(companyIdStr) : null;

                    eventService.handleUserUpdated(userId, companyId);
                }

                case "user-deleted" ->
                        eventService.handleUserDeleted(UUID.fromString(message));

                case "company-delete" ->
                        eventService.handleCompanyDeleted(UUID.fromString(message));

                case "deal-create" ->
                        eventService.handleDealCreated(UUID.fromString(message));

                case "deal-stage-change" -> {
                    String dealId = extract(message, "dealId");
                    String stage = extract(message, "stage");

                    eventService.handleDealStageChange(
                            UUID.fromString(dealId),
                            stage
                    );
                }

                case "deal-delete" ->
                        eventService.handleDealDeleted(UUID.fromString(message));

                default ->
                        log.warn("Unhandled topic: {}", topic);
            }

        } catch (Exception e) {
            log.error("Failed to process Kafka event from topic={} message={}", topic, message, e);
        }
    }

    private String extract(String json, String field) {
        try {
            int start = json.indexOf("\"" + field + "\"");
            if (start == -1) return null;

            int colon = json.indexOf(":", start);
            int quote1 = json.indexOf("\"", colon + 1);
            int quote2 = json.indexOf("\"", quote1 + 1);

            return json.substring(quote1 + 1, quote2);
        } catch (Exception e) {
            log.error("Failed to extract '{}' from {}", field, json);
            return null;
        }
    }
}
