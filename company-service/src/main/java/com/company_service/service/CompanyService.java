package com.company_service.service;

import com.company_service.dto.*;
import com.company_service.openfiegn.UserClient;
import com.company_service.repository.Company;
import com.company_service.repository.CompanyRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserClient userClient;
    private final ModelMapper modelMapper;

    public CompanyService(CompanyRepository companyRepository, UserClient userClient, ModelMapper modelMapper) {
        this.companyRepository = companyRepository;
        this.userClient = userClient;
        this.modelMapper = modelMapper;
    }

    public CompanyListDTO getAllCompaniesDTO() {
        List<Company> companies = companyRepository.findAll();
        List<CompanyDTO> companyDTOList = companies.stream()
                .map(company -> modelMapper.map(company, CompanyDTO.class)).toList();
        CompanyListDTO companyListDTO = new CompanyListDTO();
        companyListDTO.setCompanies(companyDTOList);
        return companyListDTO;
    }

    public void createCompany (Company company) {
        Optional<Company> optionalCompany = companyRepository.findByName(company.getName());
        if (optionalCompany.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }
        companyRepository.save(company);
    }

    public void deleteCompany(UUID id) {
        Optional<Company> optionalCompany = companyRepository.findById(id);
        if (optionalCompany.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found");
        }
        companyRepository.deleteById(id);
        userClient.deleteUsers(id);
    }

    public Optional<Company> getCompanyById(UUID id) {
        Optional<Company> company = companyRepository.findById(id);
        if (company.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found");
        }
        return company;
    }

    @Transactional
    public Optional<Company> updateCompany(UUID id, String name, Double budget) {
        Optional<Company> optionalCompany = companyRepository.findById(id);
        if (optionalCompany.isEmpty()) {
            log.warn("Couldn't update company info, company with id {} does not exist", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found");
        }

        Company company = optionalCompany.get();
        boolean isUpdated = false;
        if (name != null && !name.equals(company.getName())) {
            Optional<Company> existingCompany = companyRepository.findByName(name);
            if (existingCompany.isPresent()) {
                log.warn("Company with name '{}' already exists", name);
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Company with this name already exists");
            }
            company.setName(name);
            isUpdated = true;
        }

        if (budget != null && !budget.equals(company.getBudget())) {
            company.setBudget(budget);
            isUpdated = true;
        }
        if (!isUpdated) {
            log.info("No changes detected for company id {}, nothing to update.", id);
            return Optional.empty();
        }
        return Optional.of(company);
    }

    public CompanyWithUsersDTO getCompanyWithUsers(UUID id) {
        Optional<Company> optionalCompany = getCompanyById(id);
        if (optionalCompany.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found");
        }
        Company company = optionalCompany.get();

        CompanyDTO companyDTO = modelMapper.map(company, CompanyDTO.class);

        UsersResponseDTO responseDTO = userClient.getUsers(id);
        List<UserDTO> users = responseDTO.getUsers();

        return new CompanyWithUsersDTO(companyDTO, users);
    }

    public List<CompanyUsersDTO> getAllCompanyAndUsers(){
        UserListDTO userListDTO = userClient.getAllUsers();
        Map<UUID, List<UserDTO>> usersByCompanyId = userListDTO.getUsers().stream()
                .collect(Collectors.groupingBy(UserDTO::getCompanyId));

        List<CompanyUsersDTO> result = new ArrayList<>();

        List<Company> allCompanies = companyRepository.findAll();

        for (Company company : allCompanies) {
            List<UserDTO> users = usersByCompanyId.getOrDefault(company.getId(), new ArrayList<>());

            CompanyUsersDTO companyUsersDTO = createCompanyUsersDTO(company, users);
            result.add(companyUsersDTO);
        }

        return result;
    }

    private CompanyUsersDTO createCompanyUsersDTO(Company company, List<UserDTO> users) {
        CompanyDTO companyDTO = new CompanyDTO();
        companyDTO.setId(company.getId());
        companyDTO.setName(company.getName());
        companyDTO.setBudget(company.getBudget());

        CompanyUsersDTO companyUsersDTO = new CompanyUsersDTO();
        companyUsersDTO.setCompany(companyDTO);
        companyUsersDTO.setUsers(users);

        return companyUsersDTO;
    }

    public CompanyDTO getOneCompany (UUID id) {
        Optional<Company> optionalCompany = companyRepository.findById(id);
        if (optionalCompany.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found");
        }
        Company company = optionalCompany.get();
        return modelMapper.map(company, CompanyDTO.class);
    }

    public boolean checkCompany (UUID id){
        Optional<Company> optionalCompany = getCompanyById(id);
        boolean exists = optionalCompany.isPresent();
        if (!exists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found");
        }
        return true;
    }
}
