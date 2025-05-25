package com.company_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CompanyUsersDTO {
    private CompanyDTO company;
    private List<UserDTO> users;

}

