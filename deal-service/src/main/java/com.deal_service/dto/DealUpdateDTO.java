package com.deal_service.dto;
import com.deal_service.domain.DealStage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DealUpdateDTO(
        @NotBlank String title,
        String description,
        @PositiveOrZero BigDecimal amount,
        DealStage stage,
        UUID companyId,
        UUID userId,
        Instant closedAt
) {}