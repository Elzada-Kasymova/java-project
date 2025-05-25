package com.company_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class CompanyDTO {
    private UUID id;
    private String name;
    private Double budget;

    public CompanyDTO() {}

    public CompanyDTO(UUID id, String name, Double budget) {
        this.id = id;
        this.name = name;
        this.budget = budget;
    }

}
