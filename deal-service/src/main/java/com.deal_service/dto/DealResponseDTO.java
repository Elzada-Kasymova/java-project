package com.deal_service.dto;

import com.deal_service.domain.DealStage;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DealResponseDTO(
        UUID id,
        String title,
        String description,
        BigDecimal amount,
        DealStage stage,
        UUID companyId,
        UUID userId,
        Instant createdAt,
        Instant updatedAt,
        Instant closedAt
) {}
