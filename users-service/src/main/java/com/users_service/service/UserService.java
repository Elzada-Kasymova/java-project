package com.users_service.service;

import com.users_service.dto.*;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<UserDTO> getAllUsers();
    UserDTO getUserById(UUID id);
    UserDTO createUser(UserCreateDTO dto);
    UserDTO updateUser(UUID id, UserUpdateDTO dto);
    void deleteUserById(UUID id);
    void deleteCompanyId(UUID id);
    boolean existsById(UUID id);
}
