package com.notification_service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class NotificationRequest {
    private String externalId;
    private String eventType;
    private String template;
    private String recipientEmail;
    private Map<String,Object> payload;
}

