package com.notification_service.dto;

import lombok.Getter;

@Getter
public enum ActivityType {
    TASK,
    CALL,
    MEETING;

    ActivityType() {
    }
}