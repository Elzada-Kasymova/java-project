package com.activity_service.openfeign;

import com.activity_service.dto.DealResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "DEAL-SERVICE", path = "/api/v1/deals")
public interface DealClient {

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    DealResponseDTO getDealById(@PathVariable("id") UUID id);
}
