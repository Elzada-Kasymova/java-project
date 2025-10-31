package com.users_service.dto;

import java.time.Instant;
import java.util.UUID;

public record CompanyDTO(
        UUID id,
        String name,
        String industry,
        Double budget,
        String address,
        String country,
        UUID userId,
        Instant createdAt
) {}
