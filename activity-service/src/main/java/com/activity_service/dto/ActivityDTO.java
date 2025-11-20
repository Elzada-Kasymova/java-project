package com.activity_service.dto;

import com.activity_service.domain.ActivityStatus;
import com.activity_service.domain.ActivityType;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityDTO {
    private UUID id;
    private ActivityType type;
    private String title;
    private String description;
    private ActivityStatus status;
    private UUID userId;
    private UUID dealId;
    private UUID companyId;
    private Instant scheduledAt;
    private Instant completedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
