package com.users_service.service;

import com.users_service.dto.*;
import com.users_service.entity.User;
import com.users_service.exception.ResourceNotModifiedException;
import com.users_service.exception.UserAlreadyExistsException;
import com.users_service.exception.UserNotFoundException;
import com.users_service.mapper.UserMapper;
import com.users_service.openfeign.CompanyClient;
import com.users_service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CompanyClient companyClient;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, CompanyClient companyClient, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.companyClient = companyClient;
        this.userMapper = userMapper;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        log.info("Fetching all users");
        List<UserDTO> users = userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
        log.info("Found {} users", users.size());
        return users;
    }

    @Override
    public void deleteUsersByCompanyId(UUID id) {
        log.info("Deleting users by companyId: {}", id);
        userRepository.deleteAllByCompanyId(id);
        log.info("Deleted users for companyId: {}", id);
    }

    @Override
    public void deleteUserById(UUID id) {
        log.info("Deleting user with id: {}", id);
        getUserOrThrow(id);
        userRepository.deleteById(id);
        log.info("Deleted user with id: {}", id);
    }

    @Override
    @Transactional
    public UserDTO updateUser(UUID id, UserUpdateDTO dto) {
        log.info("Updating user with id: {}", id);
        User user = getUserOrThrow(id);

        boolean isUpdated = updateUserFields(user, dto);

        if (!isUpdated) {
            log.warn("No changes detected for user with id {}", id);
            throw new ResourceNotModifiedException("No changes detected");
        }

        UserDTO updatedDto = userMapper.toDto(user);
        log.info("User with id {} updated", id);
        return updatedDto;
    }

    @Override
    public List<User> getUsersByCompanyId(UUID id) {
        log.info("Getting users by companyId: {}", id);
        List<User> users = userRepository.findAllByCompanyId(id);
        if (users.isEmpty()) {
            log.info("No users found for companyId: {}", id);
        } else {
            log.info("Found {} users for companyId: {}", users.size(), id);
        }
        return users;
    }

    @Override
    public UserDTO getUserById(UUID id) {
        log.info("Fetching user by id: {}", id);
        UserDTO dto = userRepository.findById(id)
                .map(userMapper::toDto)
                .orElse(null);
        log.info(dto != null ? "User found with id: {}" : "User not found with id: {}", id);
        return dto;
    }

    @Override
    public UserDTO createUser(UserCreateDTO dto) {
        log.info("Creating user: {} {}", dto.getFirst_name(), dto.getLast_name());

        userRepository.findByNameAndSurname(dto.getFirst_name(), dto.getLast_name())
                .ifPresent(existingUser -> {
                    log.warn("User {} {} already exists", dto.getFirst_name(), dto.getLast_name());
                    throw new UserAlreadyExistsException("User already exists");
                });

        if (!companyClient.companyExists(dto.getCompany_id())) {
            log.warn("Company with id {} not found, cannot create user", dto.getCompany_id());
            throw new UserNotFoundException("Company not found with ID: " + dto.getCompany_id());
        }

        User user = userMapper.toEntity(dto);
        User savedUser = userRepository.save(user);
        UserDTO userDTO = userMapper.toDto(savedUser);

        companyClient.getUserId(userDTO);

        log.info("User created with id: {}", savedUser.getId());
        return userDTO;
    }

    @Override
    public UserResponseDTO getUsersOneCompany(UUID id) {
        log.info("Fetching users for companyId: {}", id);
        List<UserDTO> userDTOs = getUsersByCompanyId(id).stream()
                .map(userMapper::toDto)
                .toList();

        UserResponseDTO dto = new UserResponseDTO();
        dto.setCompanyId(id);
        dto.setUsers(userDTOs);

        log.info("Found {} users for companyId {}", userDTOs.size(), id);
        return dto;
    }

    @Override
    public Page<UserWithCompanyDTO> getAllUsersAndCompany(Pageable pageable) {
        log.info("Fetching all users with their companies");

        List<CompanyDTO> companyList = companyClient.getAllCompanies();

        Page<User> userPage = userRepository.findAll(pageable);

        List<UserWithCompanyDTO> content = userPage.stream()
                .map(userMapper::toDto)
                .map(userDTO -> new UserWithCompanyDTO(userDTO, findCompanyById(userDTO.getCompany_id(), companyList)))
                .toList();

        Page<UserWithCompanyDTO> page = new PageImpl<>(content, pageable, userPage.getTotalElements());
        log.info("Fetched {} users with company data", page.getNumberOfElements());
        return page;
    }

    @Override
    public UserAndCompanyDTO getUserAndCompany(UUID id) {
        log.info("Fetching user and company for userId: {}", id);
        UserDTO user = getUserById(id);
        getUserOrThrow(id);

        CompanyDTO company = companyClient.getOneCompany(user.getCompany_id());

        UserAndCompanyDTO dto = new UserAndCompanyDTO(user, company);
        log.info("Fetched user and company for userId: {}", id);
        return dto;
    }


    private User getUserOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User with id {} not found", id);
                    return new UserNotFoundException("User not found with id: " + id);
                });
    }

    private boolean updateUserFields(User user, UserUpdateDTO dto) {
        boolean isUpdated = false;

        if (dto.getFirst_name() != null && !dto.getFirst_name().equals(user.getFirst_name())) {
            user.setFirst_name(dto.getFirst_name());
            isUpdated = true;
        }
        if (dto.getLast_name() != null && !dto.getLast_name().equals(user.getLast_name())) {
            user.setLast_name(dto.getLast_name());
            isUpdated = true;
        }
        if (dto.getPhone_number() != null && !dto.getPhone_number().equals(user.getPhone_number())) {
            user.setPhone_number(dto.getPhone_number());
            isUpdated = true;
        }

        return isUpdated;
    }

    private CompanyDTO findCompanyById(UUID companyId, List<CompanyDTO> companies) {
        return companies.stream()
                .filter(company -> company.getId().equals(companyId))
                .findFirst()
                .orElse(null);
    }
}
