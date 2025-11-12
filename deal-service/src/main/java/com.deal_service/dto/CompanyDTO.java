package com.deal_service.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CompanyDTO(
        UUID id,
        String name,
        String industry,
        Double budget,
        String address,
        String country,
        List<UUID> userIds,
        Instant createdAt
) {}
