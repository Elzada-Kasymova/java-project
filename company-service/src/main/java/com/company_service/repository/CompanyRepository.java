package com.company_service.repository;

import com.company_service.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {


    @Query(value = "SELECT * FROM companies WHERE name = ?1 LIMIT 1", nativeQuery = true)
    Optional<Company> findByName(String name);

}
