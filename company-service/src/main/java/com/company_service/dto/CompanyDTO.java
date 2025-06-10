package com.company_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class CompanyDTO {
    private UUID id;
    private String name;
    private Double budget;
    private List<UUID> users_id;

    public CompanyDTO() {}
}
