package com.users_service.controller;

import com.users_service.dto.*;
import com.users_service.repository.User;
import com.users_service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    //--------ЗАПРОСЫ , КОТОРЫЕ ИСПОЛЬЗУЮТЬСЯ ДЛЯ COMPANY-SERVICE

    //Получение пользователей конкретной компании
    @GetMapping(path = "one/company/{company_id}")
    public UserResponseDTO getUsers(@PathVariable UUID company_id) {
        return userService.getUsersOneCompany(company_id);
    }

    //Удаление пользователей конкретной компании
    @DeleteMapping(path = "delete/users/{company_id}")
    public ResponseEntity<String> deleteUsers (@PathVariable UUID company_id) {
        userService.deleteUsersByCompanyId(company_id);
        return ResponseEntity.status(HttpStatus.OK).body("Users deleted successfully");
    }

    // Получение всех пользователей без информации о компании
    @GetMapping
    public UserListDTO getAllUsers() {
        return userService.getAllUsers();
    }

    //--------------------------------


    //--------------ЗАПРОСЫ ДЛЯ USER-SERVICE

    //Удаление пользователя
    @DeleteMapping(path = "{id}")
    public ResponseEntity<String> deleteUser(@PathVariable UUID id) {
        userService.deleteUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully");
    }

    //Обновление пользователя
    @PutMapping(path = "{id}")
    public User updateUser(
            @PathVariable UUID id,
            @RequestBody UserUpdateDTO userUpdateDTO) {
        return userService.updateUser(id, userUpdateDTO.getFirst_name(),
                userUpdateDTO.getLast_name(), userUpdateDTO.getPhone_number());
    }

    //Создание нового пользователя - есть проверка на существование компании
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    //Получение всех пользователей с информацией о компании
    @GetMapping(path = "all")
    public List<UserWithCompanyDTO> getAllUsersAndCompany() {
        return userService.getAllUsersAndCompany();
    }

    //Получение одного пользователя с информацией о компании
    @GetMapping(path = "one/{user_id}")
    public UserAndCompanyDTO getUserAndCompany(@PathVariable UUID user_id) {
        return userService.getUserAndCompany(user_id);
    }

    //------------------------


}
