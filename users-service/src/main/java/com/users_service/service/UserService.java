package com.users_service.service;

import com.users_service.dto.*;
import com.users_service.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<UserDTO> getAllUsers();
    void deleteUsersByCompanyId(UUID id);
    void deleteUserById(UUID id);
    UserDTO updateUser(UUID id, UserUpdateDTO userUpdateDTO);
    List<User> getUsersByCompanyId(UUID id);
    UserDTO getUserById(UUID id);
    UserDTO createUser(UserCreateDTO UserCreateDTO);
    UserResponseDTO getUsersOneCompany(UUID id);
    Page<UserWithCompanyDTO> getAllUsersAndCompany(Pageable pageable);
    UserAndCompanyDTO getUserAndCompany(UUID id);
}
