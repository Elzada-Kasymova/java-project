package com.deal_service.openfeign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "COMPANY-SERVICE", path = "/api/company")
public interface CompanyClient {

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/exists/{id}")
    boolean companyExists(@PathVariable UUID id);

}
