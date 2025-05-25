package com.users_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CompanyListDTO {
    private List<CompanyDTO> companies;

    @Override
    public String toString() {
        return "CompanyListDTO{" +
                "companies=" + companies +
                '}';
    }
}
