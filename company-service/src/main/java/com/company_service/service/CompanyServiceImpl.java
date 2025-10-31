package com.company_service.service;

import com.company_service.dto.CompanyCreateDTO;
import com.company_service.dto.CompanyDTO;
import com.company_service.dto.CompanyUpdateDTO;
import com.company_service.entity.Company;
import com.company_service.exception.CompanyNotFoundException;
import com.company_service.mapper.CompanyMapper;
import com.company_service.openfiegn.UserClient;
import com.company_service.repository.CompanyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final UserClient userClient;

    public CompanyServiceImpl(CompanyRepository companyRepository, CompanyMapper companyMapper, UserClient userClient) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
        this.userClient = userClient;
    }

    @Override
    public List<CompanyDTO> getAllCompanies() {
        log.info("Fetching all companies");
        return companyRepository.findAll().stream()
                .map(companyMapper::toDto)
                .toList();
    }

    @Override
    public CompanyDTO getCompanyById(UUID id) {
        log.info("Fetching company by id: {}", id);
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found: " + id));
        return companyMapper.toDto(company);
    }

    @Override
    @Transactional
    public CompanyDTO createCompany(CompanyCreateDTO dto) {
        log.info("Creating company: {}", dto.getName());
        Company company = companyMapper.toEntity(dto);
        company = companyRepository.save(company);
        return companyMapper.toDto(company);
    }

    @Override
    @Transactional
    public CompanyDTO updateCompany(UUID id, CompanyUpdateDTO dto) {
        log.info("Updating company {}", id);
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found: " + id));
        companyMapper.updateEntity(company, dto);
        company = companyRepository.save(company);
        return companyMapper.toDto(company);
    }

    @Override
    @Transactional
    public void deleteCompany(UUID id) {
        log.info("Deleting company {}", id);
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found: " + id));
        companyRepository.delete(company);
    }

    @Override
    @Transactional
    public void clearUserId(UUID companyId) {
        log.info("Clearing userIds for company {}", companyId);
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found: " + companyId));
        company.getUserIds().clear();
        companyRepository.save(company);
    }
}
