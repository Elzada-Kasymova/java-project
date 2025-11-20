package com.deal_service.controller;

import com.deal_service.dto.DealResponseDTO;
import com.deal_service.service.DealService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/deal")
public class DealInternalController {
    private final DealService service;

    @GetMapping("/{id}")
    public ResponseEntity<DealResponseDTO> getInternalDeal(@PathVariable UUID id) {
        log.info("GET /internal/deal/{} — internal request", id);
        return ResponseEntity.ok(service.getById(id));
    }
}
