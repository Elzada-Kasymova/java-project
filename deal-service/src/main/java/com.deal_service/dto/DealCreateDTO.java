package com.deal_service.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

public record DealCreateDTO(
        @NotBlank String title,
        String description,
        @PositiveOrZero BigDecimal amount,
        @NotNull UUID companyId,
        @NotNull UUID userId
) {}