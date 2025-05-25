package com.company_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CompanyListDTO {
    private List<CompanyDTO> companies;

    @Override
    public String toString() {
        return "CompanyListDTO{" +
                "companies=" + companies +
                '}';
    }
}
