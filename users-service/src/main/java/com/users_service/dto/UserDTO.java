package com.users_service.dto;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserDTO {

    private UUID id;
    private String first_name;
    private String last_name;
    private String phone_number;
    private UUID company_id;

    public UserDTO() {
    }
    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", first_name='" + first_name + '\'' +
                ", last_name='" + last_name + '\'' +
                ", phone_number='" + phone_number + '\'' +
                ", company_id=" + company_id +
                '}';
    }

    public UserDTO(UUID id, String first_name, String last_name, String phone_number, UUID company_id) {
        this.id = id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.phone_number = phone_number;
        this.company_id = company_id;
    }
}
