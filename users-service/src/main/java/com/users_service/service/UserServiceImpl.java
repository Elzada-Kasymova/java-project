package com.users_service.service;

import com.users_service.dto.UserCreateDTO;
import com.users_service.dto.UserDTO;
import com.users_service.dto.UserUpdateDTO;
import com.users_service.entity.User;
import com.users_service.exception.UserAlreadyExistsException;
import com.users_service.exception.UserNotFoundException;
import com.users_service.kafka.UserEventPublisher;
import com.users_service.keycloak.KeycloakClient;
import com.users_service.mapper.UserMapper;
import com.users_service.openfeign.CompanyClient;
import com.users_service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CompanyClient companyClient;
    private final KeycloakClient keycloakClient;
    private final UserEventPublisher eventPublisher;

    public UserServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper,
            CompanyClient companyClient,
            KeycloakClient keycloakClient,
            UserEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.companyClient = companyClient;
        this.keycloakClient = keycloakClient;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public UserDTO createUser(UserCreateDTO dto) {
        userRepository.findByEmail(dto.getEmail()).ifPresent(u -> {
            throw new UserAlreadyExistsException("User with email already exists");
        });

        if (dto.getCompanyIds() != null && !dto.getCompanyIds().isEmpty()) {
            for (UUID companyId : dto.getCompanyIds()) {
                if (!companyClient.companyExists(companyId)) {
                    throw new RuntimeException("Company not found: " + companyId);
                }
            }
        }

        String keycloakUserId = keycloakClient.createUser(
                dto.getUsername(),
                dto.getPassword(),
                dto.getEmail(),
                dto.getFirstName(),
                dto.getLastName()
        );

        User user = userMapper.toEntity(dto);
        user.setId(UUID.fromString(keycloakUserId));
        userRepository.save(user);

        UserDTO userDTO = userMapper.toDto(user);


        eventPublisher.publishUserCreated(user.getId().toString());

        return userDTO;
    }

    @Override
    public UserDTO getUserById(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Override
    public boolean existsById(UUID id) {
        return userRepository.existsById(id);
    }

    @Transactional
    public void deleteUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        for (UUID companyId : user.getCompanyIds()) {
            try {
                companyClient.clearUserId(companyId);
                log.info("Cleared userId for company {}", companyId);
            } catch (Exception e) {
                log.warn("Failed to clear userId for company {}", companyId, e);
            }
        }

        try {
            keycloakClient.deleteUser(id.toString());
        } catch (Exception e) {
            log.error("Failed to delete user {} from Keycloak: {}", id, e.getMessage());
        }

        userRepository.delete(user);
        log.info("User deleted with id {}", id);

        eventPublisher.publishUserDeleted(id.toString());
    }


    @Override
    @Transactional
    public UserDTO updateUser(UUID id, UserUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (dto.getFirst_name() != null) user.setFirstName(dto.getFirst_name());
        if (dto.getLast_name() != null) user.setLastName(dto.getLast_name());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());

        Set<UUID> oldSet = listToSet(user.getCompanyIds());
        Set<UUID> newSet = listToSet(dto.getCompanyIds());

        if (dto.getCompanyIds() != null) {
            for (UUID companyId : dto.getCompanyIds()) {
                if (!companyClient.companyExists(companyId)) {
                    throw new RuntimeException("Company not found: " + companyId);
                }
            }
            user.setCompanyIds(new ArrayList<>(dto.getCompanyIds()));
        }

        userRepository.save(user);
        log.info("User updated: {}", user);

        if (!Objects.equals(oldSet, newSet)) {
            eventPublisher.publishUserUpdated(id.toString(), dto.getCompanyIds());
        }

        return userMapper.toDto(user);
    }
    private Set<UUID> listToSet(List<UUID> list) {
        if (list == null) return Collections.emptySet();
        return list.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public void deleteCompanyId(UUID id) {
        userRepository.findAll().forEach(user -> {
            if (user.getCompanyIds().contains(id)) {
                user.getCompanyIds().remove(id);
                userRepository.save(user);
                log.info("Removed company {} from user {}", id, user.getId());
            }
        });
    }
}
