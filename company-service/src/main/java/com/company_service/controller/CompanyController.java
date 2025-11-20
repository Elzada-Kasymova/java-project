package com.company_service.controller;

import com.company_service.dto.CompanyCreateDTO;
import com.company_service.dto.CompanyDTO;
import com.company_service.dto.CompanyUpdateDTO;
import com.company_service.service.CompanyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RequestMapping("/api/company")
@RestController
@Slf4j
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<CompanyDTO> getAllCompanies() {
        return companyService.getAllCompanies();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public CompanyDTO getCompanyById(@PathVariable UUID id) {
        return companyService.getCompanyById(id);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyDTO createCompany(@Valid @RequestBody CompanyCreateDTO dto) {
        return companyService.createCompany(dto);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/{id}")
    public CompanyDTO updateCompany(@PathVariable UUID id,
                                    @Valid @RequestBody CompanyUpdateDTO dto) {
        return companyService.updateCompany(id, dto);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompany(@PathVariable UUID id) {
        companyService.deleteCompany(id);
    }


    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/{companyId}/clear-user")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearUserId(@PathVariable UUID companyId) {
        log.info("Clearing userId for company {}", companyId);
        companyService.clearUserId(companyId);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/exists/{id}")
    public boolean companyExists(@PathVariable UUID id) {
        log.info("Checking if company {} exists", id);
        return companyService.existsById(id);
    }
}
