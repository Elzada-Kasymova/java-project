package com.notification_service.service;

import com.notification_service.dto.ActivityDTO;
import com.notification_service.dto.DealResponseDTO;

import java.util.UUID;

public interface EmailService {
    void sendWelcomeEmail(String email, String username);
    void cancelScheduledEmails(UUID userId);
    void sendDealCreatedNotification(String email, DealResponseDTO deal);
    void sendCongratsEmail(String email, DealResponseDTO deal);
    void sendActivityCompletedEmail(String email, ActivityDTO activity);
    void sendActivityCreatedEmail(String email, ActivityDTO activity);

}


