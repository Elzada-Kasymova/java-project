package com.deal_service.dto;

import com.deal_service.domain.DealStage;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class DealDTO {
    private UUID id;
    private String title;
    private String description;
    private BigDecimal amount;
    private DealStage stage;
    private UUID companyId;
    private UUID userId;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant closedAt;

    public DealDTO() {
    }

}
