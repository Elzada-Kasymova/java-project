package com.company_service.controller;

import com.company_service.dto.*;
import com.company_service.repository.Company;
import com.company_service.service.CompanyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.*;

@Slf4j
@RestController
@RequestMapping(path = "api/company")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    //---------ЗАПРОСЫ ДЛЯ COMPANY-SERVICE

    //Получение информации об одной компании и всех его пользователей
    @GetMapping(path = "one/{id}")
    public CompanyWithUsersDTO getUsersByCompanyId(@PathVariable UUID id) {
        return companyService.getCompanyWithUsers(id);
    }

    //Создание компаний
    @PostMapping
    public Company createCompany(@RequestBody Company company) {
        companyService.createCompany(company);
        return company;
    }

    //Обновление информации о компании
    @PutMapping(path = "/{id}")
    public ResponseEntity<Company> updateCompany(@PathVariable UUID id,
                                                 @RequestBody CompanyUpdateDTO companyDTO) {
        Optional<Company> updatedCompany = companyService.
                updateCompany(id, companyDTO.getName(), companyDTO.getBudget());
        return updatedCompany.map(ResponseEntity::ok).orElseGet(()
                -> ResponseEntity.status(HttpStatus.NOT_MODIFIED).build());
    }

    //Удаление компании, также удаляться все пользователи
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<String> deleteCompany(@PathVariable UUID id) {
        companyService.deleteCompany(id);
        return ResponseEntity.status(HttpStatus.OK).body("Company deleted successfully");
    }

    //Получение всех компаний и всех пользователей
    @GetMapping(path = "all")
    public List<CompanyUsersDTO> getAllUsersGroupedByCompany() {
        return companyService.getAllCompanyAndUsers();
    }
    //--------------------


    //-----------ЗАПРОСЫ , КОТОРЫЕ ИСПОЛЬЗУЮТЬСЯ ДЛЯ USER-SERVICE

    //Проверка валидности компании
    @GetMapping(path ="check/{company_id}")
    public boolean companyExists(@PathVariable UUID company_id) {
        return companyService.checkCompany(company_id);
    }

    //Получение только информации о компаниях
    @GetMapping
    public CompanyListDTO getAllCompanies() {
        return companyService.getAllCompaniesDTO();
    }

    //Получение компании одного пользователя
    @GetMapping(path="one/user/{company_id}")
    public CompanyDTO getOneCompany (@PathVariable UUID company_id) {
        return companyService.getOneCompany(company_id);
    }

    //-------------------------------
}
