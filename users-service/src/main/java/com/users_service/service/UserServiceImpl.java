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

import java.time.Instant;
import java.util.*;

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

        // Проверяем компании
        if (dto.getCompanyIds() != null && !dto.getCompanyIds().isEmpty()) {
            for (UUID companyId : dto.getCompanyIds()) {
                companyClient.getCompanyById(companyId);
            }
        }

        // Создаём пользователя в Keycloak
        String keycloakUserId = keycloakClient.createUser(dto.getUsername(), dto.getPassword(), dto.getEmail());

        // Сохраняем в БД
        User user = userMapper.toEntity(dto);
        user.setId(UUID.fromString(keycloakUserId));
        userRepository.save(user);

        UserDTO userDTO = userMapper.toDto(user);

        // 📤 Публикуем событие в Kafka
        Map<String, Object> payload = Map.of(
                "id", user.getId().toString(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "email", user.getEmail(),
                "companyIds", user.getCompanyIds()
        );
        eventPublisher.publishUserCreated(payload);

        return userDTO;
    }

    @Override
    public UserDTO getUserById(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
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

        userRepository.delete(user);
        log.info("User deleted with id {}", id);

        // 📤 Публикуем событие удаления
        Map<String, Object> payload = Map.of(
                "id", id.toString(),
                "deletedAt", Instant.now().toString()
        );
        eventPublisher.publishUserDeleted(payload);
    }

    @Override
    @Transactional
    public UserDTO updateUser(UUID id, UserUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (dto.getFirst_name() != null) user.setFirstName(dto.getFirst_name());
        if (dto.getLast_name() != null) user.setLastName(dto.getLast_name());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());

        // Проверяем и обновляем компании
        if (dto.getCompanyIds() != null) {
            for (UUID companyId : dto.getCompanyIds()) {
                companyClient.getCompanyById(companyId);
            }
            user.setCompanyIds(dto.getCompanyIds());
        }

        userRepository.save(user);
        log.info("User updated: {}", user);

        // 📤 Публикуем событие обновления
        Map<String, Object> payload = Map.of(
                "id", id.toString(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "email", user.getEmail(),
                "companyIds", user.getCompanyIds()
        );
        eventPublisher.publishUserUpdated(payload);

        return userMapper.toDto(user);
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
