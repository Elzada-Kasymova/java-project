package com.company_service.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class CompanyUpdateDTO {

    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @Positive(message = "Budget must be a positive number")
    private Double budget;

    @Size(max = 100)
    private String industry;

    @Size(max = 255)
    private String address;

    @Size(max = 100)
    private String country;

    private List<UUID> userIds;
}
