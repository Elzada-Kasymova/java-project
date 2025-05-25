package com.users_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CompanyDTO {
    private UUID id;
    private String name;
    private Double budget;
}
