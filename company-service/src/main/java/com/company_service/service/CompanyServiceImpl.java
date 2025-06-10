package com.company_service.service;

import com.company_service.dto.*;
import com.company_service.entity.Company;
import com.company_service.exception.CompanyAlreadyExistsException;
import com.company_service.exception.CompanyNotFoundException;
import com.company_service.exception.ResourceNotModifiedException;
import com.company_service.exception.UserAlreadyExistsException;
import com.company_service.mapper.CompanyMapper;
import com.company_service.openfiegn.UserClient;
import com.company_service.repository.CompanyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final UserClient userClient;
    private final CompanyMapper companyMapper;

    public CompanyServiceImpl(CompanyRepository companyRepository, UserClient userClient, CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.userClient = userClient;
        this.companyMapper = companyMapper;
    }

    @Override
    public List<CompanyDTO> getAllCompaniesDTO() {
        log.info("Fetching all companies");
        List<CompanyDTO> dtos = companyRepository.findAll().stream()
                .map(companyMapper::toDto)
                .toList();
        log.info("Found {} companies", dtos.size());
        return dtos;
    }

    @Override
    public CompanyDTO createCompany(CompanyCreateDTO dto) {
        log.info("Creating company with name '{}'", dto.getName());
        checkCompanyNameExists(dto.getName());

        Company company = companyMapper.toEntity(dto);
        Company saved = companyRepository.save(company);
        CompanyDTO result = companyMapper.toDto(saved);

        log.info("Company created with id {}", saved.getId());
        return result;
    }

    @Override
    public void deleteCompany(UUID id) {
        log.info("Deleting company with id {}", id);
        Company company = findCompanyOrThrow(id);

        companyRepository.delete(company);
        userClient.deleteUsers(id);

        log.info("Company {} and its users deleted", id);
    }

    @Override
    @Transactional
    public CompanyDTO updateCompany(UUID id, String name, Double budget) {
        log.info("Updating company id {}, name: {}, budget: {}", id, name, budget);
        Company company = findCompanyOrThrow(id);

        boolean isUpdated = updateCompanyFields(company, name, budget);
        if (!isUpdated) {
            log.info("No changes for company {}", id);
            throw new ResourceNotModifiedException("Компания не обновлена");
        }

        Company updated = companyRepository.save(company);
        CompanyDTO dto = companyMapper.toDto(updated);

        log.info("Company {} updated", id);
        return dto;
    }

    @Override
    public CompanyWithUsersDTO getCompanyWithUsers(UUID id) {
        log.info("Fetching company with users, id {}", id);
        CompanyDTO companyDTO = companyMapper.toDto(findCompanyOrThrow(id));
        List<UserDTO> users = userClient.getUsers(id).getUsers();

        CompanyWithUsersDTO result = new CompanyWithUsersDTO(companyDTO, users);

        // Лог в конце
        log.info("Company {} has {} users", id, users.size());
        return result;
    }

    @Override
    public Page<CompanyUsersDTO> getAllCompanyAndUsers(Pageable pageable) {
        log.info("Fetching all companies with users, page: {}", pageable.getPageNumber());

        Map<UUID, List<UserDTO>> usersByCompany = userClient.getAllUsers().stream()
                .collect(Collectors.groupingBy(UserDTO::getCompanyId));

        Page<CompanyUsersDTO> page = companyRepository.findAll(pageable)
                .map(company -> {
                    List<UserDTO> users = usersByCompany.getOrDefault(company.getId(), Collections.emptyList());
                    return new CompanyUsersDTO(companyMapper.toDto(company), users);
                });

        log.info("Fetched {} companies with users on page {}", page.getNumberOfElements(), pageable.getPageNumber());
        return page;
    }

    @Override
    public CompanyDTO getOneCompany(UUID id) {
        log.info("Fetching single company with id {}", id);
        CompanyDTO dto = companyMapper.toDto(findCompanyOrThrow(id));
        log.info("Company {} fetched successfully", id);
        return dto;
    }

    @Override
    public boolean checkCompany(UUID id) {
        log.info("Checking if company exists with id {}", id);
        findCompanyOrThrow(id);
        log.info("Company {} exists", id);
        return true;
    }

    public void addUserToCompany(UserDTO userDTO) {
        log.info("Добавление пользователя {} в компанию {}", userDTO.getId(), userDTO.getCompanyId());
        Company company = findCompanyOrThrow(userDTO.getCompanyId());

        List<UUID> userIds = company.getUsers_id();
        if (userIds == null) {
            userIds = new ArrayList<>();
        }

        if (!userIds.contains(userDTO.getId())) {
            userIds.add(userDTO.getId());
            company.setUsers_id(userIds);
            companyRepository.save(company);
            log.info("Пользователь {} успешно добавлен в компанию {}", userDTO.getId(), userDTO.getCompanyId());
        } else {
            throw new UserAlreadyExistsException("User already exists");
        }
    }


    private Company findCompanyOrThrow(UUID id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Company with id {} not found", id);
                    return new CompanyNotFoundException("Company not found");
                });
    }

    private void checkCompanyNameExists(String name) {
        companyRepository.findByName(name).ifPresent(c -> {
            log.warn("Company with name '{}' already exists", name);
            throw new CompanyAlreadyExistsException("Company already exists");
        });
    }

    private boolean updateCompanyFields(Company company, String name, Double budget) {
        boolean updated = false;

        if (name != null && !name.equals(company.getName())) {
            checkCompanyNameExists(name);
            company.setName(name);
            updated = true;
        }

        if (budget != null && !budget.equals(company.getBudget())) {
            company.setBudget(budget);
            updated = true;
        }

        return updated;
    }
}
