package com.users_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserWithCompanyDTO {

    private UserDTO user;
    private CompanyDTO company;

    public UserWithCompanyDTO(UserDTO user, CompanyDTO company) {
        this.user = user;
        this.company = company;
    }

    @Override
    public String toString() {
        return "UserWithCompanyDTO{" +
                "user=" + user +
                ", company=" + company +
                '}';
    }
}
