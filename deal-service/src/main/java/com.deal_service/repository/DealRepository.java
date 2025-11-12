package com.deal_service.repository;

import com.deal_service.entity.Deal;
import org.springframework.data.jpa.repository.*;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface DealRepository extends JpaRepository<Deal, UUID>, JpaSpecificationExecutor<Deal> {
    List<Deal> findAllByIsDeletedFalse();
    Optional<Deal> findByIdAndIsDeletedFalse(UUID id);
    List<Deal> findByUserId(UUID userId);
    List<Deal> findByCompanyId(UUID companyId);
}
