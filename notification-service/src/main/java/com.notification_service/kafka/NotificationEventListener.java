package com.notification_service.kafka;

import com.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "activity-create", groupId = "notification-service")
    public void onActivityCreate(String message, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.info("Activity created. key={}, message={}", key, message);
        notificationService.handleActivityCreated(UUID.fromString(key));
    }

    @KafkaListener(topics = "activity-completed", groupId = "notification-service")
    public void onActivityCompleted(String message, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        UUID activityId = UUID.fromString(key);
        log.info("Activity completed. key={}, message={}", activityId, message);
        notificationService.handleActivityCompleted(activityId);
    }

    @KafkaListener(topics = "deal-create", groupId = "notification-service")
    public void onDealCreated(String message, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        UUID dealId = UUID.fromString(key);
        log.info("Deal created. dealId={}, message={}", dealId, message);
        notificationService.handleDealCreated(dealId);
    }

    @KafkaListener(topics = "deal-stage-change", groupId = "notification-service")
    public void onDealStageChange(String message, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        UUID dealId = UUID.fromString(key);
        log.info("Deal stage changed. dealId={}, raw={}", dealId, message);

        String stage = extractStage(message);
        if ("WON".equals(stage)) {
            log.info("Deal {} is WON! Processing...", dealId);
            notificationService.handleDealStageChanged(dealId, stage);
        }
    }

    @KafkaListener(topics = "user-created", groupId = "notification-service")
    public void onUserCreated(String message, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        UUID userId = UUID.fromString(key);
        log.info("User created. userId={}, message={}", userId, message);
        notificationService.handleUserCreated(userId);
    }

    @KafkaListener(topics = "user-deleted", groupId = "notification-service")
    public void onUserDeleted(String message, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        UUID userId = UUID.fromString(key);
        log.info("User deleted. userId={}, message={}", userId, message);
        notificationService.handleUserDeleted(userId);
    }

    private String extractStage(String message) {
        int start = message.indexOf("\"stage\":\"");
        if (start == -1) return null;

        start += 9;
        int end = message.indexOf("\"", start);
        if (end == -1) return null;

        return message.substring(start, end);
    }
}
