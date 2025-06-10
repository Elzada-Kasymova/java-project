package com.company_service.controller;

import com.company_service.dto.*;
import com.company_service.service.CompanyService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("api/company")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    // ----------- COMPANY-SERVICE -----------

    // Получение информации об одной компании и всех ее пользователей
    @GetMapping("one/{id}")
    public CompanyWithUsersDTO getCompanyWithUsers(@PathVariable UUID id) {
        log.info("Получение компании с пользователями по id: {}", id);
        return companyService.getCompanyWithUsers(id);
    }

    // Создание компании
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyDTO createCompany(@RequestBody @Valid CompanyCreateDTO companyCreateDTO) {
        log.info("Создание компании с данными: {}", companyCreateDTO);
        return companyService.createCompany(companyCreateDTO);
    }

    // Обновление информации о компании
    @PutMapping("/{id}")
    public CompanyDTO updateCompany(@PathVariable UUID id,
                                    @RequestBody @Valid CompanyUpdateDTO companyUpdateDTO) {
        log.info("Обновление компании id {} с данными: {}", id, companyUpdateDTO);
        return companyService.updateCompany(id, companyUpdateDTO.getName(), companyUpdateDTO.getBudget());
    }


    // Удаление компании
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompany(@PathVariable UUID id) {
        log.info("Удаление компании id: {}", id);
        companyService.deleteCompany(id);
    }

    // Получение всех компаний
    @GetMapping("all")
    public Page<CompanyUsersDTO> getAllCompaniesWithUsers(Pageable pageable) {
        log.info("Получение всех компаний с пользователями с пагинацией: {}", pageable);
        return  companyService.getAllCompanyAndUsers(pageable);

    }

    //Получение usersId
    @PutMapping("/userid")
    public ResponseEntity<Void> addUserToCompany(@RequestBody UserDTO userDTO) {
        if (userDTO == null || userDTO.getId() == null || userDTO.getCompanyId() == null) {
            return ResponseEntity.badRequest().build();
        }

        companyService.addUserToCompany(userDTO);
        return ResponseEntity.ok().build();
    }



    // ----------- USER-SERVICE -----------

    // Проверка валидности компании (для user-service)
    @GetMapping("check/{companyId}")
    public boolean companyExists(@PathVariable UUID companyId) {
        log.info("Проверка существования компании с id: {}", companyId);
        return companyService.checkCompany(companyId);
    }

    // Получение списка всех компаний
    @GetMapping
    public List<CompanyDTO> getAllCompanies() {
        log.info("Получение списка всех компаний");
        return companyService.getAllCompaniesDTO();
    }

    // Получение одной компании по id (для пользователя)
    @GetMapping("one/user/{companyId}")
    public CompanyDTO getOneCompany(@PathVariable UUID companyId) {
        log.info("Получение компании по id: {}", companyId);
        return companyService.getOneCompany(companyId);
    }

}
