package com.company_service.service;

import com.company_service.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface CompanyService {
    List<CompanyDTO> getAllCompaniesDTO();
    CompanyDTO createCompany(CompanyCreateDTO companyCreateDTO);
    void deleteCompany(UUID id);
    CompanyDTO updateCompany(UUID id, String name, Double budget);
    CompanyWithUsersDTO getCompanyWithUsers(UUID id);
    Page<CompanyUsersDTO> getAllCompanyAndUsers(Pageable pageable);
    CompanyDTO getOneCompany(UUID id);
    boolean checkCompany(UUID id);
    void addUserToCompany(UserDTO userDTO);
}