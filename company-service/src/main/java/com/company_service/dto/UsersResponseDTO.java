package com.company_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class UsersResponseDTO {
    private UUID companyId;
    private List<UserDTO> users;

}
