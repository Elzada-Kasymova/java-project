package com.activity_service.service;


import com.activity_service.domain.ActivityStatus;
import com.activity_service.domain.ActivityType;
import com.activity_service.entity.Activity;
import com.activity_service.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityEventService {

    private final ActivityRepository repository;


    public void handleUserCreated(UUID userId) {
        Activity activity = new Activity();
        activity.setType(ActivityType.TASK);
        activity.setTitle("Onboarding task");
        activity.setStatus(ActivityStatus.PLANNED);
        activity.setUserId(userId);
        activity.setCreatedAt(Instant.now());
        repository.save(activity);

        log.info("Created onboarding activity for user {}", userId);
    }

    public void handleUserUpdated(UUID userId, UUID companyId) {
        if (companyId == null) return;

        List<Activity> activities = repository.findByUserId(userId);
        for (Activity a : activities) {
            a.setCompanyId(companyId);
        }
        repository.saveAll(activities);

        log.info("Updated companyId={} for {} activities of user {}", companyId, activities.size(), userId);
    }

    public void handleUserDeleted(UUID userId) {
        List<Activity> activities = repository.findByUserId(userId);
        repository.deleteAll(activities);

        log.info("Deleted {} activities for user {}", activities.size(), userId);
    }


    public void handleCompanyDeleted(UUID companyId) {
        List<Activity> activities = repository.findByCompanyId(companyId);
        repository.deleteAll(activities);

        log.info("Deleted {} activities for company {}", activities.size(), companyId);
    }

    public void handleDealCreated(UUID dealId) {
        Activity activity = new Activity();
        activity.setType(ActivityType.TASK);
        activity.setTitle("Follow-up task for deal");
        activity.setStatus(ActivityStatus.PLANNED);
        activity.setDealId(dealId);
        activity.setCreatedAt(Instant.now());
        repository.save(activity);

        log.info("Created follow-up activity for deal {}", dealId);
    }

    public void handleDealStageChange(UUID dealId, String stage) {
        List<Activity> activities = repository.findByDealId(dealId);
        for (Activity a : activities) {
            switch (stage) {
                case "WON" -> a.setStatus(ActivityStatus.COMPLETED);
                case "LOST" -> a.setStatus(ActivityStatus.CANCELED);
            }
        }
        repository.saveAll(activities);

        log.info("Updated {} activities for deal {} to stage {}", activities.size(), dealId, stage);
    }

    public void handleDealDeleted(UUID dealId) {
        List<Activity> activities = repository.findByDealId(dealId);
        repository.deleteAll(activities);

        log.info("Deleted {} activities for deal {}", activities.size(), dealId);
    }
}

