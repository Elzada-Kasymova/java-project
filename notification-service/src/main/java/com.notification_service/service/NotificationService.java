package com.notification_service.service;

import java.util.UUID;

public interface NotificationService {
    void handleUserCreated(UUID userId);
    void handleUserDeleted(UUID userId);
    void handleDealCreated(UUID dealId);
    void handleDealStageChanged(UUID dealId, String stage);
    void handleActivityCompleted(UUID activityId);
    void handleActivityCreated(UUID activityId);

}
