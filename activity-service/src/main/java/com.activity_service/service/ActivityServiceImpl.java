package com.activity_service.service;

import com.activity_service.domain.ActivityStatus;
import com.activity_service.dto.ActivityCreateDTO;
import com.activity_service.dto.ActivityDTO;
import com.activity_service.dto.ActivityUpdateDTO;
import com.activity_service.entity.Activity;
import com.activity_service.exception.ActivityNotFoundException;
import com.activity_service.kafka.ActivityEventPublisher;
import com.activity_service.mapper.ActivityMapper;
import com.activity_service.openfeign.CompanyClient;
import com.activity_service.openfeign.DealClient;
import com.activity_service.openfeign.UserClient;
import com.activity_service.repository.ActivityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository repository;
    private final ActivityMapper mapper;
    private final ActivityEventPublisher eventPublisher;
    private final DealClient dealClient;
    private final UserClient userClient;
    private final CompanyClient companyClient;

    public ActivityServiceImpl(
            ActivityRepository repository,
            ActivityMapper mapper,
            ActivityEventPublisher eventPublisher,
            DealClient dealClient,
            UserClient userClient,
            CompanyClient companyClient
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
        this.dealClient = dealClient;
        this.userClient = userClient;
        this.companyClient = companyClient;
    }

    @Override
    public List<ActivityDTO> getAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ActivityDTO get(UUID id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new ActivityNotFoundException("Activity not found with id: " + id));
    }

    @Override
    public ActivityDTO create(ActivityCreateDTO dto) {
        validateRelatedEntities(dto);

        Activity entity = mapper.toEntity(dto);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        Activity saved = repository.save(entity);

        ActivityDTO result = mapper.toDto(saved);

        // 📤 Публикуем событие
        Map<String, Object> payload = Map.ofEntries(
                Map.entry("id", saved.getId().toString()),
                Map.entry("title", saved.getTitle()),
                Map.entry("description", saved.getDescription()),
                Map.entry("type", saved.getType().name()),
                Map.entry("status", saved.getStatus().name()),
                Map.entry("userId", saved.getUserId() != null ? saved.getUserId().toString() : null),
                Map.entry("dealId", saved.getDealId() != null ? saved.getDealId().toString() : null),
                Map.entry("companyId", saved.getCompanyId() != null ? saved.getCompanyId().toString() : null),
                Map.entry("scheduledAt", saved.getScheduledAt() != null ? saved.getScheduledAt().toString() : null),
                Map.entry("createdAt", saved.getCreatedAt().toString())
        );


        eventPublisher.publishActivityCreated(payload);
        log.info("Activity created with id {}", saved.getId());

        return result;
    }

    @Override
    public ActivityDTO update(UUID id, ActivityUpdateDTO dto) {
        Activity existing = repository.findById(id)
                .orElseThrow(() -> new ActivityNotFoundException("Activity not found with id: " + id));

        if (dto.getTitle() != null) existing.setTitle(dto.getTitle());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
        if (dto.getType() != null) existing.setType(dto.getType());
        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
        if (dto.getScheduledAt() != null) existing.setScheduledAt(dto.getScheduledAt());
        if (dto.getCompletedAt() != null) existing.setCompletedAt(dto.getCompletedAt());
        if (dto.getDealId() != null) existing.setDealId(dto.getDealId());
        if (dto.getCompanyId() != null) existing.setCompanyId(dto.getCompanyId());
        if (dto.getUserId() != null) existing.setUserId(dto.getUserId());

        validateRelatedEntities(dto);

        existing.setUpdatedAt(Instant.now());
        Activity saved = repository.save(existing);

        ActivityDTO result = mapper.toDto(saved);

        // 📤 Kafka событие
        Map<String, Object> payload = Map.ofEntries(
                Map.entry("id", saved.getId().toString()),
                Map.entry("title", saved.getTitle()),
                Map.entry("status", saved.getStatus()),
                Map.entry("updatedAt", saved.getUpdatedAt().toString())
        );
        eventPublisher.publishActivityUpdated(payload);
        log.info("Activity updated: {}", saved.getId());

        return result;
    }

    @Override
    public void delete(UUID id) {
        Activity existing = repository.findById(id)
                .orElseThrow(() -> new ActivityNotFoundException("Activity not found with id: " + id));

        repository.delete(existing);
        log.info("Activity deleted with id {}", id);

        eventPublisher.publishActivityDeleted(Map.of(
                "id", id.toString(),
                "deletedAt", Instant.now().toString()
        ));
    }

    @Override
    public ActivityDTO complete(UUID id, UUID completedBy) {
        Activity existing = repository.findById(id)
                .orElseThrow(() -> new ActivityNotFoundException("Activity not found with id: " + id));

        userClient.getUserById(completedBy); // Проверяем существование пользователя

        existing.setStatus(ActivityStatus.COMPLETED);
        existing.setCompletedAt(Instant.now());
        existing.setUpdatedAt(Instant.now());

        Activity saved = repository.save(existing);
        ActivityDTO result = mapper.toDto(saved);

        eventPublisher.publishActivityCompleted(Map.ofEntries(
                Map.entry("id", saved.getId().toString()),
                Map.entry("completedBy", completedBy.toString()),
                Map.entry("completedAt", saved.getCompletedAt().toString())
        ));
        log.info("Activity {} completed by {}", id, completedBy);

        return result;
    }

    @Override
    public List<ActivityDTO> findByDealId(UUID dealId) {
        return repository.findByDealId(dealId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    private void validateRelatedEntities(Object dto) {
        try {
            if (dto instanceof ActivityCreateDTO r) {
                if (r.getDealId() != null) dealClient.getDealById(r.getDealId());
                if (r.getCompanyId() != null) companyClient.getCompanyById(r.getCompanyId());
                if (r.getUserId() != null) userClient.getUserById(r.getUserId());
            } else if (dto instanceof ActivityUpdateDTO r) {
                if (r.getDealId() != null) dealClient.getDealById(r.getDealId());
                if (r.getCompanyId() != null) companyClient.getCompanyById(r.getCompanyId());
                if (r.getUserId() != null) userClient.getUserById(r.getUserId());
            }
        } catch (Exception e) {
            throw new RuntimeException("Related entity not found: " + e.getMessage());
        }
    }
}
