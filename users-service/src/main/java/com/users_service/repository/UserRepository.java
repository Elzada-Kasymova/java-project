package com.users_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query(value = "SELECT * FROM users WHERE company_id = ?1", nativeQuery = true)
    List<User> findAllByCompanyId(UUID id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM users WHERE company_id = ?1", nativeQuery = true)
    void deleteAllByCompanyId(UUID companyId);

    @Query(value = "SELECT * FROM users WHERE first_name = ?1 AND last_name = ?2" , nativeQuery = true)
    Optional<User> findByNameAndSurname(String name, String surname);
}
