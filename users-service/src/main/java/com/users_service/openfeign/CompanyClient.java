package com.users_service.openfeign;

import com.users_service.dto.CompanyDTO;
import com.users_service.dto.CompanyListDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "COMPANY-SERVICE", path = "/api/company")
public interface CompanyClient {

    @GetMapping(path ="check/{company_id}")
    boolean companyExists(@PathVariable UUID company_id);

    @GetMapping
    CompanyListDTO getAllCompanies();

    @GetMapping(path="one/user/{company_id}")
    CompanyDTO getOneCompany (@PathVariable UUID company_id);
}
