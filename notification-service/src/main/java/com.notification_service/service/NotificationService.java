package com.notification_service.service;

import java.util.Map;

public interface NotificationService {
    /**
     * Handle a generic crm-events envelope.
     */
    void handleEnvelope(Map<String,Object> envelope);
}
