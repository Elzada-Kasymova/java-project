package com.users_service.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserDTO(
        UUID id,
        String firstName,
        String lastName,
        String email,
        List<UUID> companyIds,
        Instant createdAt
) {}
