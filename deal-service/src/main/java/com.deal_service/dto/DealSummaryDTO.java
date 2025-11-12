package com.deal_service.dto;


import com.deal_service.domain.DealStage;

import java.math.BigDecimal;
import java.util.UUID;

public record DealSummaryDTO(
        UUID id,
        String title,
        BigDecimal amount,
        DealStage stage,
        UUID companyId,
        UUID userId
) {}
