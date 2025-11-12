package com.company_service.openfiegn;

import com.company_service.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@FeignClient(name = "USERS-SERVICE", path = "/api/users")
public interface UserClient {

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    UserDTO getUserById(@PathVariable("id") UUID userId);

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping("/company/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    UUID deleteCompanyId(@PathVariable UUID id);
}
