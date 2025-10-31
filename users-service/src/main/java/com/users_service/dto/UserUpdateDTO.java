package com.users_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UserUpdateDTO {

    @Size(max = 50)
    private String first_name;

    @Size(max = 50)
    private String last_name;

    @Email
    private String email;

    private List<UUID> companyIds;

}
