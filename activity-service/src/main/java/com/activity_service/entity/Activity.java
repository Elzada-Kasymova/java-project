package com.activity_service.entity;

import com.activity_service.domain.ActivityStatus;
import com.activity_service.domain.ActivityType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType type;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityStatus status;

    private UUID userId;
    private UUID dealId;
    private UUID companyId;

    private Instant scheduledAt;
    private Instant completedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
