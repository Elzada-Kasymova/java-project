package com.company_service.service;

import com.company_service.dto.CompanyCreateDTO;
import com.company_service.dto.CompanyDTO;
import com.company_service.dto.CompanyUpdateDTO;

import java.util.List;
import java.util.UUID;

public interface CompanyService {

    List<CompanyDTO> getAllCompanies();

    CompanyDTO getCompanyById(UUID id);

    CompanyDTO createCompany(CompanyCreateDTO dto);

    CompanyDTO updateCompany(UUID id, CompanyUpdateDTO dto);

    void deleteCompany(UUID id);

    void clearUserId(UUID companyId);
}
