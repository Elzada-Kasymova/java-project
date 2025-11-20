package com.company_service.service;

import com.company_service.dto.CompanyCreateDTO;
import com.company_service.dto.CompanyDTO;
import com.company_service.dto.CompanyUpdateDTO;
import com.company_service.entity.Company;
import com.company_service.exception.CompanyNotFoundException;
import com.company_service.exception.UserNotFoundException;
import com.company_service.kafka.CompanyEventPublisher;
import com.company_service.mapper.CompanyMapper;
import com.company_service.openfiegn.UserClient;
import com.company_service.repository.CompanyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final UserClient userClient;
    private final CompanyEventPublisher eventPublisher;

    public CompanyServiceImpl(CompanyRepository companyRepository,
                              CompanyMapper companyMapper,
                              UserClient userClient,
                              CompanyEventPublisher eventPublisher) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
        this.userClient = userClient;
        this.eventPublisher = eventPublisher;
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
    public boolean existsById(UUID id) {
        log.info("Checking existence of company: {}", id);
        return companyRepository.existsById(id);
    }


    @Override
    @Transactional
    public CompanyDTO createCompany(CompanyCreateDTO dto) {
        log.info("Creating company: {}", dto.getName());

        if (dto.getUserId() != null && !dto.getUserId().isEmpty()) {
            for (UUID userId : dto.getUserId()) {
                if (!userClient.userExists(userId)) {
                    throw new UserNotFoundException("User not found: " + userId);
                }
            }
        }

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

        if (dto.getUserIds() != null && !dto.getUserIds().isEmpty()) {

            for (UUID newUserId : dto.getUserIds()) {
                if (!userClient.userExists(newUserId)) {
                    log.error("User {} does not exist, cannot add to company {}", newUserId, id);
                    throw new UserNotFoundException("User not found: " + newUserId);
                }
            }

            company.setUserIds(dto.getUserIds());
        }

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
        try {
            userClient.deleteCompanyId(id);
        } catch (Exception e) {
            log.warn("Failed to notify user-service to remove companyId {}: {}", id, e.getMessage(), e);
        }
        companyRepository.delete(company);
        log.info("Company deleted from DB: {}", id);
        eventPublisher.publishCompanyDeleted(id.toString());
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
