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

    public ActivityServiceImpl(ActivityRepository repository,
                               ActivityMapper mapper,
                               ActivityEventPublisher eventPublisher,
                               DealClient dealClient,
                               UserClient userClient,
                               CompanyClient companyClient) {
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

        validateRelatedEntities(dto.getDealId(), dto.getCompanyId(), dto.getUserId());

        Activity entity = mapper.toEntity(dto);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        Activity saved = repository.save(entity);

        eventPublisher.publishActivityCreated(saved.getId());
        log.info("Activity created {}", saved.getId());

        return mapper.toDto(saved);
    }

    @Override
    public ActivityDTO update(UUID id, ActivityUpdateDTO dto) {

        Activity existing = repository.findById(id)
                .orElseThrow(() -> new ActivityNotFoundException("Activity not found: " + id));

        validateRelatedEntities(dto.getDealId(), dto.getCompanyId(), dto.getUserId());

        if (dto.getTitle() != null) existing.setTitle(dto.getTitle());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
        if (dto.getType() != null) existing.setType(dto.getType());
        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
        if (dto.getScheduledAt() != null) existing.setScheduledAt(dto.getScheduledAt());
        if (dto.getCompletedAt() != null) existing.setCompletedAt(dto.getCompletedAt());
        if (dto.getDealId() != null) existing.setDealId(dto.getDealId());
        if (dto.getCompanyId() != null) existing.setCompanyId(dto.getCompanyId());
        if (dto.getUserId() != null) existing.setUserId(dto.getUserId());

        existing.setUpdatedAt(Instant.now());

        Activity saved = repository.save(existing);
        return mapper.toDto(saved);
    }

    @Override
    public void delete(UUID id) {
        Activity existing = repository.findById(id)
                .orElseThrow(() -> new ActivityNotFoundException("Activity not found: " + id));

        repository.delete(existing);
        log.info("Activity deleted {}", id);
    }

    @Override
    public ActivityDTO complete(UUID id, UUID completedBy) {

        if (!userClient.userExists(completedBy)) {
            throw new RuntimeException("User does not exist: " + completedBy);
        }

        Activity existing = repository.findById(id)
                .orElseThrow(() -> new ActivityNotFoundException("Activity not found: " + id));

        existing.setStatus(ActivityStatus.COMPLETED);
        existing.setCompletedAt(Instant.now());
        existing.setUpdatedAt(Instant.now());

        Activity saved = repository.save(existing);

        eventPublisher.publishActivityCompleted(saved.getId());
        log.info("Activity {} completed by {}", id, completedBy);

        return mapper.toDto(saved);
    }

    @Override
    public List<ActivityDTO> findByDealId(UUID dealId) {
        return repository.findByDealId(dealId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }


    private void validateRelatedEntities(UUID dealId, UUID companyId, UUID userId) {

        if (dealId != null && !dealClient.dealExists(dealId)) {
            throw new RuntimeException("Deal does not exist: " + dealId);
        }

        if (companyId != null && !companyClient.companyExists(companyId)) {
            throw new RuntimeException("Company does not exist: " + companyId);
        }

        if (userId != null && !userClient.userExists(userId)) {
            throw new RuntimeException("User does not exist: " + userId);
        }
    }
}
