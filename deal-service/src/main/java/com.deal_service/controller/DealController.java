package com.deal_service.controller;

import com.deal_service.domain.DealStage;
import com.deal_service.dto.*;
import com.deal_service.service.DealService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/deals")
public class DealController {

    private final DealService service;

    public DealController(DealService service) {
        this.service = service;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<DealSummaryDTO>> getAllDeals() {
        return ResponseEntity.ok(service.getAll());
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<DealResponseDTO> getDeal(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping
    public ResponseEntity<DealResponseDTO> createDeal(@Valid @RequestBody DealCreateDTO dto) {
        DealResponseDTO res = service.create(dto);
        return ResponseEntity.status(201).body(res);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<DealResponseDTO> updateDeal(@PathVariable UUID id, @Valid @RequestBody DealUpdateDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PatchMapping("/{id}/stage")
    public ResponseEntity<DealResponseDTO> changeStage(@PathVariable UUID id, @Valid @RequestBody DealPatchStageDTO req) {
        return ResponseEntity.ok(service.changeStage(id, req));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeal(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<List<DealSummaryDTO>> search(
            @RequestParam Optional<DealStage> stage,
            @RequestParam Optional<UUID> companyId,
            @RequestParam Optional<UUID> userId,
            @RequestParam Optional<String> dateFrom,
            @RequestParam Optional<String> dateTo
    ) {
        return ResponseEntity.ok(service.search(stage, companyId, userId, dateFrom, dateTo));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/exists/{id}")
    public boolean dealExists(@PathVariable UUID id) {
        return service.existsById(id);
    }
}
