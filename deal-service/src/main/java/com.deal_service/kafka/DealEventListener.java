package com.deal_service.kafka;

import com.deal_service.service.DealEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DealEventListener {

    private final DealEventService dealEventService;

    @KafkaListener(
            topics = {
                    "user-deleted",
                    "company-delete",
                    "user-updated"
            },
            groupId = "deal-service-group"
    )
    public void handleEvent(String message, @Header("kafka_receivedTopic") String topic) {
        try {
            switch (topic) {
                case "user-deleted" ->
                        dealEventService.handleUserDeleted(UUID.fromString(message).toString());

                case "company-delete" ->
                        dealEventService.handleCompanyDeleted(UUID.fromString(message).toString());

                case "user-updated" ->
                        dealEventService.handleUserUpdated(UUID.fromString(message).toString());
            }
        } catch (Exception e) {
            log.error("Error processing Kafka message. topic={} message={}", topic, message, e);
        }
    }

}
