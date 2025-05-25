package com.users_service.dto;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class UserAndCompanyDTO {
    private UserDTO user;
    private CompanyDTO company;

    public UserAndCompanyDTO(UserDTO user, CompanyDTO company) {
        this.user = user;
        this.company = company;
    }

}

