package com.notification_service.dto;


import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserDTO (
        UUID id,
        String firstName,
        String lastName,
        String phoneNumber,
        UUID companyId,
        Instant createdAt,
        List<UUID> roles
) {}
