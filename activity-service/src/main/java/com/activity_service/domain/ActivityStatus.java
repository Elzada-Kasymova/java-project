package com.activity_service.domain;

import lombok.Getter;

@Getter
public enum ActivityStatus {
    PLANNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELED
}
