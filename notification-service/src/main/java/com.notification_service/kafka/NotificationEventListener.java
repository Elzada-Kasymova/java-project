package com.notification_service.kafka;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${notification.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(String raw) throws Exception {
        // envelope is expected to be a JSON string as per provided template
        Map<String,Object> envelope = objectMapper.readValue(raw, Map.class);
        notificationService.handleEnvelope(envelope);
    }
}


