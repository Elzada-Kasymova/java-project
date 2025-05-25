package com.company_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class UserListDTO {
    private List<UserDTO> users;

    @Override
    public String toString() {
        return "UserListDTO{" +
                "users=" + users +
                '}';
    }
}
