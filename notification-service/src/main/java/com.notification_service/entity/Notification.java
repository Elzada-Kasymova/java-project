package com.notification_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true)
    private String externalId;

    private String eventType;
    private String templateName;

    @Column(columnDefinition = "text")
    private String payload;

    private String recipientEmail;

    private String status;

    @Column(columnDefinition = "text")
    private String error;

    private int attempts;

    private Instant createdAt;
    private Instant sentAt;
}
