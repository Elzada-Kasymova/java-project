package com.company_service.mapper;

import com.company_service.dto.CompanyCreateDTO;
import com.company_service.dto.CompanyDTO;
import com.company_service.dto.CompanyUpdateDTO;
import com.company_service.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.sql.Timestamp;
import java.time.Instant;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "id", target = "id")
    CompanyDTO toDto(Company company);

    Company toEntity(CompanyCreateDTO dto);

    void updateEntity(@MappingTarget Company company, CompanyUpdateDTO dto);

    default Instant map(Timestamp timestamp) {
        return timestamp != null ? timestamp.toInstant() : null;
    }
}
