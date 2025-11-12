package com.deal_service.dto;


import com.deal_service.domain.DealStage;
import jakarta.validation.constraints.NotNull;


public record DealPatchStageDTO(
        @NotNull DealStage stage,
        java.time.Instant closedAt
) {}

