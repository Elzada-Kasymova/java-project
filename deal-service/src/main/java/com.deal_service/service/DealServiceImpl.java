package com.deal_service.service;

import com.deal_service.domain.DealStage;
import com.deal_service.dto.*;
import com.deal_service.entity.Deal;
import com.deal_service.kafka.DealEventPublisher;
import com.deal_service.mapper.DealMapper;
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

    public DealServiceImpl(DealRepository repository,
                           DealMapper mapper,
                           DealEventPublisher eventPublisher) {
        this.repository = repository;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public DealResponseDTO create(DealCreateDTO dto) {
        Deal toSave = mapper.toEntity(dto);
        if (toSave.getStage() == null) toSave.setStage(DealStage.LEAD);
        toSave.setCreatedAt(Instant.now());
        Deal saved = repository.save(toSave);

        Map<String, Object> payload = Map.of(
                "id", saved.getId().toString(),
                "stage", saved.getStage().name(),
                "companyId", saved.getCompanyId() != null ? saved.getCompanyId().toString() : null,
                "userId", saved.getUserId() != null ? saved.getUserId().toString() : null,
                "amount", saved.getAmount()
        );

        eventPublisher.publish("DealCreated", saved.getId(), payload);
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
    @Transactional
    public DealResponseDTO update(UUID id, DealUpdateDTO dto) {
        Deal existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Deal not found: " + id));

        mapper.updateFromDto(dto, existing);
        existing.setUpdatedAt(Instant.now());
        if (dto.closedAt() != null) existing.setClosedAt(dto.closedAt());
        Deal saved = repository.save(existing);

        Map<String, Object> payload = Map.of(
                "id", saved.getId().toString(),
                "stage", saved.getStage().name(),
                "companyId", saved.getCompanyId() != null ? saved.getCompanyId().toString() : null,
                "userId", saved.getUserId() != null ? saved.getUserId().toString() : null,
                "amount", saved.getAmount()
        );

        eventPublisher.publish("DealUpdated", saved.getId(), payload);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public DealResponseDTO changeStage(UUID id, DealPatchStageDTO req) {
        Deal deal = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Deal not found: " + id));
        DealStage old = deal.getStage();

        deal.setStage(req.stage());
        if (req.stage() == DealStage.WON || req.stage() == DealStage.LOST) {
            deal.setClosedAt(req.closedAt() == null ? Instant.now() : req.closedAt());
        }
        deal.setUpdatedAt(Instant.now());
        Deal saved = repository.save(deal);

        Map<String, Object> stagePayload = new HashMap<>();
        stagePayload.put("id", saved.getId().toString());
        stagePayload.put("oldStage", old != null ? old.name() : null);
        stagePayload.put("newStage", saved.getStage() != null ? saved.getStage().name() : null);
        stagePayload.put("companyId", saved.getCompanyId() != null ? saved.getCompanyId().toString() : null);
        stagePayload.put("userId", saved.getUserId() != null ? saved.getUserId().toString() : null);
        stagePayload.put("amount", saved.getAmount());
        stagePayload.put("closedAt", saved.getClosedAt() != null ? saved.getClosedAt().toString() : null);

        eventPublisher.publish("DealStageChanged", saved.getId(), stagePayload);

        if (saved.getStage() == DealStage.WON) {
            eventPublisher.publish("DealWon", saved.getId(), stagePayload);
        } else if (saved.getStage() == DealStage.LOST) {
            eventPublisher.publish("DealLost", saved.getId(), stagePayload);
        }

        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Deal existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Deal not found: " + id));

        repository.delete(existing);

        Map<String, Object> payload = Map.of("id", id.toString());
        eventPublisher.publish("DealDeleted", id, payload);
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
