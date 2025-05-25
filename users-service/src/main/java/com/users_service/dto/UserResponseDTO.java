package com.users_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UserResponseDTO {
    private UUID companyId;
    private List<UserDTO> users;
}
