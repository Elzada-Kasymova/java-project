package com.users_service.controller;

import com.users_service.dto.UserCreateDTO;
import com.users_service.dto.UserDTO;
import com.users_service.dto.UserUpdateDTO;
import com.users_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // 3.1 Получить всех пользователей
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<UserDTO> getAllUsers() {
        log.info("GET /api/users/all — получение всех пользователей");
        return userService.getAllUsers();
    }

    // 3.2 Получить одного пользователя
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SYSTEM')")
    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable UUID id) {
        log.info("GET /api/users/{} — получение пользователя по ID", id);
        return userService.getUserById(id);
    }

    // 3.3 Создать пользователя
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO createUser(@RequestBody @Valid UserCreateDTO dto) {
        log.info("POST /api/users — создание пользователя {}", dto.getEmail());
        return userService.createUser(dto);
    }

    // 3.4 Обновить пользователя
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/{id}")
    public UserDTO updateUser(@PathVariable UUID id, @RequestBody @Valid UserUpdateDTO dto) {
        log.info("PUT /api/users/{} — обновление пользователя", id);
        return userService.updateUser(id, dto);
    }

    // 3.5 Удалить пользователя
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID id) {
        log.info("DELETE /api/users/{} — удаление пользователя", id);
        userService.deleteUserById(id);
    }

    // Удалить companyId из списка у всех пользователей
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping("/company/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompanyId(@PathVariable UUID id) {
        log.info("DELETE /api/users/company/{} — удаление компании из списка у пользователей", id);
        userService.deleteCompanyId(id);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/exists/{id}")
    public boolean userExists(@PathVariable UUID id) {
        log.info("Checking if user {} exists", id);
        return userService.existsById(id);
    }

}
