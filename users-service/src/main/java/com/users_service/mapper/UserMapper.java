package com.users_service.mapper;

import com.users_service.dto.UserCreateDTO;
import com.users_service.dto.UserDTO;
import com.users_service.dto.UserUpdateDTO;
import com.users_service.entity.User;
import org.mapstruct.*;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(Instant.now())")
    User toEntity(UserCreateDTO dto);

}
