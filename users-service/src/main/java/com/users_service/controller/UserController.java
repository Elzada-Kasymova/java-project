package com.users_service.controller;

import com.users_service.dto.*;
import com.users_service.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = "api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ----------- COMPANY-SERVICE -----------
    // Получение пользователей конкретной компании
    @GetMapping(path = "one/company/{company_id}")
    public UserResponseDTO getUsers(@PathVariable UUID company_id) {
        log.info("Запрошены пользователи компании с ID: {}", company_id);
        return userService.getUsersOneCompany(company_id);
    }

    // Удаление всех пользователей конкретной компании
    @DeleteMapping(path = "delete/users/{company_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUsers(@PathVariable UUID company_id) {
        log.info("Удаление всех пользователей компании с ID: {}", company_id);
        userService.deleteUsersByCompanyId(company_id);
    }


    // Получение всех пользователей
    @GetMapping
    public List<UserDTO> getAllUsers() {
        log.info("Запрошены все пользователи для Company-service");
        return userService.getAllUsers();
    }

    // ----------- USER-SERVICE -----------
    // Удаление пользователя
    @DeleteMapping(path = "{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID id) {
        log.info("Удаление пользователя с ID: {}", id);
        userService.deleteUserById(id);
    }

    // Обновление пользователя
    @PutMapping(path = "{id}")
    public UserDTO updateUser(
            @PathVariable UUID id,
            @RequestBody @Valid UserUpdateDTO userUpdateDTO) {
        log.info("Обновление пользователя с ID: {}", id);
        return userService.updateUser(id, userUpdateDTO);
    }

    // Создание нового пользователя
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO createUser(@RequestBody @Valid UserCreateDTO userCreateDTO) {
        log.info("Создание нового пользователя: {} {}", userCreateDTO.getFirst_name(), userCreateDTO.getLast_name());
        return userService.createUser(userCreateDTO);
    }

    // Получение всех пользователей с информацией о компании
    @GetMapping(path = "all")
    public Page<UserWithCompanyDTO> getAllUsersAndCompany(Pageable pageable) {
        log.info("Получение всех пользователями и компаний с пагинацией: {}", pageable);
        return userService.getAllUsersAndCompany(pageable);
    }

    // Получение одного пользователя с информацией о компании
    @GetMapping(path = "one/{user_id}")
    public UserAndCompanyDTO getUserAndCompany(@PathVariable UUID user_id) {
        log.info("Запрошен пользователь с ID: {} и его компания", user_id);
        return userService.getUserAndCompany(user_id);
    }
    //------------------------


}
