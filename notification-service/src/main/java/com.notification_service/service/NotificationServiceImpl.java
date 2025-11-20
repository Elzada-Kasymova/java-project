package com.notification_service.service;

import com.notification_service.dto.ActivityDTO;
import com.notification_service.dto.DealResponseDTO;
import com.notification_service.dto.UserDTO;
import com.notification_service.keycloak.KeycloakClient;
import com.notification_service.keycloak.KeycloakTokenProvider;
import com.notification_service.openfeign.ActivityClient;
import com.notification_service.openfeign.DealClient;
import com.notification_service.openfeign.UserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final UserClient userClient;
    private final DealClient dealClient;
    private final ActivityClient activityClient;
    private final KeycloakClient keycloakClient;
    private final EmailService emailService;
    private final KeycloakTokenProvider keycloakTokenProvider;



    public void handleUserCreated(UUID userId) {
        log.info("Handling user-created for {}", userId);

        UserDTO user = userClient.getUserByIdInternal(userId);
        userClient.getUserByIdInternal(userId);

        String email = keycloakClient.getEmailByUserId(userId.toString());

        emailService.sendWelcomeEmail(email, user.firstName());
    }

    public void handleUserDeleted(UUID userId) {
        log.info("Handling user-deleted for {}", userId);

        emailService.cancelScheduledEmails(userId);
    }

    public void handleDealCreated(UUID dealId) {
        log.info("Handling deal-created for {}", dealId);

        DealResponseDTO deal = dealClient.getInternalDeal(dealId).getBody();
        if (deal == null) return;

        String email = keycloakClient.getEmailByUserId(deal.userId().toString());

        emailService.sendDealCreatedNotification(email, deal);
    }

    public void handleDealStageChanged(UUID dealId, String stage) {
        log.info("Handling deal-stage-change for {} stage={}", dealId, stage);

        if (!"WON".equalsIgnoreCase(stage)) {
            log.info("Stage is not WON, ignoring deal {}", dealId);
            return;
        }

        DealResponseDTO deal = dealClient.getInternalDeal(dealId).getBody();
        if (deal == null) return;

        String email = keycloakClient.getEmailByUserId(deal.userId().toString());

        emailService.sendCongratsEmail(email, deal);
    }

    public void handleActivityCompleted(UUID activityId) {
        log.info("Handling activity-completed for {}", activityId);

        ActivityDTO activity = activityClient.getByIdInternal(activityId);

        String email = keycloakClient.getEmailByUserId(activity.getUserId().toString());

        emailService.sendActivityCompletedEmail(email, activity);
    }

    @Override
    public void handleActivityCreated(UUID activityId) {
        log.info("Handling activity-created for {}", activityId);

        ActivityDTO activity = activityClient.getByIdInternal(activityId);
        if (activity == null) {
            log.warn("Activity not found: {}", activityId);
            return;
        }

        String email = keycloakClient.getEmailByUserId(activity.getUserId().toString());

        emailService.sendActivityCreatedEmail(email, activity);
    }

}
