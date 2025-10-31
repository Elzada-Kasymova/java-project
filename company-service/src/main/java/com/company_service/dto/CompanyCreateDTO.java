package com.company_service.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CompanyCreateDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @Size(max = 100, message = "Industry must be at most 100 characters")
    private String industry;

    @NotNull(message = "Budget is required")
    @Positive(message = "Budget must be a positive number")
    private Double budget;

    @Size(max = 255, message = "Address must be at most 255 characters")
    private String address;

    @Size(max = 100, message = "Country must be at most 100 characters")
    private String country;

    @NotNull(message = "UserId is required")
    private List<UUID> userId = new ArrayList<>();
}
