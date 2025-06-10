package com.users_service.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class UserCreateDTO {

    @NotBlank(message = "First name must not be blank")
    @Size(max = 50, message = "First name must be at most 50 characters")
    private String first_name;

    @NotBlank(message = "Last name must not be blank")
    @Size(max = 50, message = "Last name must be at most 50 characters")
    private String last_name;

    @Pattern(regexp = "\\+?[0-9]{7,15}", message = "Invalid phone number format")
    private String phone_number;

    @NotNull(message = "Company ID is required")
    private UUID company_id;
}
