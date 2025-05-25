package com.company_service.openfiegn;


import com.company_service.dto.UserListDTO;
import com.company_service.dto.UsersResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "USERS-SERVICE", path = "/api/users")
public interface UserClient {

    @GetMapping(path = "one/company/{company_id}")
    UsersResponseDTO getUsers(@PathVariable UUID company_id);

    @DeleteMapping(path = "delete/users/{company_id}")
    void deleteUsers (@PathVariable UUID company_id);

    @GetMapping
    UserListDTO getAllUsers();
}
