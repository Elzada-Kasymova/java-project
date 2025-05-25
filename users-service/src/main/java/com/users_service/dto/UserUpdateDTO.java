package com.users_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserUpdateDTO {
    private String first_name;
    private String last_name;
    private String phone_number;
    private UUID company_id;
}
