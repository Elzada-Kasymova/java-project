package com.deal_service.service;

import com.deal_service.domain.DealStage;
import com.deal_service.dto.*;
import com.deal_service.entity.Deal;
import com.deal_service.kafka.DealEventPublisher;
import com.deal_service.mapper.DealMapper;
import com.deal_service.openfeign.CompanyClient;
import com.deal_service.openfeign.UserClient;
import com.deal_service.repository.DealRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.ws.rs.NotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DealServiceImpl implements DealService {

    private final DealRepository repository;
    private final DealMapper mapper;
    private final DealEventPublisher eventPublisher;
    private final CompanyClient companyClient;
    private final UserClient userClient;

    public DealServiceImpl(DealRepository repository,
                           DealMapper mapper,
                           DealEventPublisher eventPublisher, CompanyClient companyClient, UserClient userClient) {
        this.repository = repository;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
        this.companyClient = companyClient;
        this.userClient = userClient;
    }

    @Override
    @Transactional
    public DealResponseDTO create(DealCreateDTO dto) {

        if (dto.companyId() != null && !companyClient.companyExists(dto.companyId())) {
            throw new NotFoundException("Company not found: " + dto.companyId());
        }

        if (dto.userId() != null && !userClient.userExists(dto.userId())) {
            throw new NotFoundException("User not found: " + dto.userId());
        }

        Deal deal = mapper.toEntity(dto);
        if (deal.getStage() == null) deal.setStage(DealStage.LEAD);

        deal.setCreatedAt(Instant.now());
        Deal saved = repository.save(deal);

        eventPublisher.publishDealCreated(saved.getId());

        return mapper.toDto(saved);
    }


    @Override
    public List<DealSummaryDTO> getAll() {
        List<Deal> list = repository.findAllByIsDeletedFalse();
        return list.stream().map(mapper::toSummaryDto).collect(Collectors.toList());
    }

    @Override
    public DealResponseDTO getById(UUID id) {
        Deal d = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Deal not found: " + id));
        return mapper.toDto(d);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.findByIdAndIsDeletedFalse(id).isPresent();
    }


    @Override
    @Transactional
    public DealResponseDTO update(UUID id, DealUpdateDTO dto) {

        Deal existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Deal not found: " + id));

        UUID oldCompanyId = existing.getCompanyId();
        UUID oldUserId = existing.getUserId();

        if (dto.companyId() != null &&
                !Objects.equals(dto.companyId(), oldCompanyId) &&
                !companyClient.companyExists(dto.companyId())) {

            throw new NotFoundException("Company not found: " + dto.companyId());
        }

        if (dto.userId() != null &&
                !Objects.equals(dto.userId(), oldUserId) &&
                !userClient.userExists(dto.userId())) {

            throw new NotFoundException("User not found: " + dto.userId());
        }

        mapper.updateFromDto(dto, existing);
        existing.setUpdatedAt(Instant.now());

        if (dto.closedAt() != null) {
            existing.setClosedAt(dto.closedAt());
        }

        Deal saved = repository.save(existing);

        boolean companyChanged = !Objects.equals(oldCompanyId, saved.getCompanyId());
        boolean userChanged = !Objects.equals(oldUserId, saved.getUserId());

        if (companyChanged || userChanged) {
            eventPublisher.publishDealUpdated(
                    saved.getId(),
                    companyChanged ? saved.getCompanyId() : null,
                    userChanged ? List.of(saved.getUserId()) : null
            );
        }

        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public DealResponseDTO changeStage(UUID id, DealPatchStageDTO req) {
        Deal deal = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Deal not found: " + id));


        deal.setStage(req.stage());
        if (req.stage() == DealStage.WON || req.stage() == DealStage.LOST) {
            deal.setClosedAt(req.closedAt() != null ? req.closedAt() : Instant.now());
        }
        deal.setUpdatedAt(Instant.now());

        Deal saved = repository.save(deal);

        if (saved.getStage() == DealStage.WON || saved.getStage() == DealStage.LOST) {
            eventPublisher.publishDealStageChange(saved.getId(), saved.getStage());
        }

        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Deal existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Deal not found: " + id));

        repository.delete(existing);

        eventPublisher.publishDealDeleted(existing.getId());
    }

    @Override
    public List<DealSummaryDTO> search(Optional<DealStage> stage,
                                       Optional<UUID> companyId,
                                       Optional<UUID> userId,
                                       Optional<String> dateFrom,
                                       Optional<String> dateTo) {
        Specification<Deal> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));
            stage.ifPresent(s -> predicates.add(cb.equal(root.get("stage"), s)));
            companyId.ifPresent(cid -> predicates.add(cb.equal(root.get("companyId"), cid)));
            userId.ifPresent(uid -> predicates.add(cb.equal(root.get("userId"), uid)));
            dateFrom.ifPresent(fromStr -> predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), Instant.parse(fromStr))));
            dateTo.ifPresent(toStr -> predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), Instant.parse(toStr))));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Deal> list = repository.findAll(spec);
        return list.stream().map(mapper::toSummaryDto).collect(Collectors.toList());
    }
}
