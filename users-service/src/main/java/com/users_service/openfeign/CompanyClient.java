package com.users_service.openfeign;

import com.users_service.dto.CompanyDTO;
import com.users_service.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "COMPANY-SERVICE", path = "/api/company")
public interface CompanyClient {

    // Проверка существования компании
    @GetMapping("/{id}")
    CompanyDTO getCompanyById(@PathVariable UUID id);

    // Получение списка всех компаний
    @GetMapping("/all")
    List<CompanyDTO> getAllCompanies();

    // Очистить userId в компании (при удалении пользователя)
    @PutMapping("/{companyId}/clear-user")
    void clearUserId(@PathVariable UUID companyId);
}
