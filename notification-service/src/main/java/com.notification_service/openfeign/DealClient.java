package com.notification_service.openfeign;

import com.notification_service.dto.DealResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "DEAL-SERVICE")
public interface DealClient {

    @GetMapping("/internal/deal/{id}")
    ResponseEntity<DealResponseDTO> getInternalDeal(@PathVariable UUID id);
}
