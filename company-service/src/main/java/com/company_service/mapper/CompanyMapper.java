package com.company_service.mapper;

import com.company_service.dto.CompanyCreateDTO;
import com.company_service.dto.CompanyDTO;
import com.company_service.entity.Company;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    CompanyDTO toDto(Company company);
    Company toEntity(CompanyDTO companyDTO);

    Company toEntity(CompanyCreateDTO companyCreateDTO);
}
