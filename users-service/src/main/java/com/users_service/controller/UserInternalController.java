package com.users_service.controller;

import com.users_service.dto.UserDTO;
import com.users_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/users")
public class UserInternalController {

    private final UserService userService;

    @GetMapping("/{id}")
    public UserDTO getUserByIdInternal(@PathVariable UUID id) {
        log.info("GET /internal/users/{} — internal request", id);
        return userService.getUserById(id);
    }
}
