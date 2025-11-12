package com.activity_service.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum ActivityType {
    TASK,
    CALL,
    MEETING;

    ActivityType() {
    }
}