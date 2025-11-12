package com.deal_service.openfeign;


import com.deal_service.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "USERS-SERVICE", path = "/api/users")
public interface UserClient {

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    UserDTO getUserById(@PathVariable("id") UUID userId);

}