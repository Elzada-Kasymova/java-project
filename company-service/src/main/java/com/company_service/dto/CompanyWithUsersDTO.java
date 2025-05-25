package com.company_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CompanyWithUsersDTO {
    private CompanyDTO company;
    private List<UserDTO> users;

    public CompanyWithUsersDTO(CompanyDTO company, List<UserDTO> users) {
        this.company = company;
        this.users = users;
    }
}
