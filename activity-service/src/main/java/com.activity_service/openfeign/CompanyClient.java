package com.activity_service.openfeign;

import com.activity_service.dto.CompanyDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "COMPANY-SERVICE", path = "/api/company")
public interface CompanyClient {

    // Проверяем, что компания существует
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    CompanyDTO getCompanyById(@PathVariable("id") UUID id);
}
