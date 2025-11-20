package com.notification_service.openfeign;

import com.notification_service.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "USERS-SERVICE")
public interface UserClient {

    @GetMapping("/internal/users/{id}")
    UserDTO getUserByIdInternal(
            @PathVariable UUID id
    );
}
