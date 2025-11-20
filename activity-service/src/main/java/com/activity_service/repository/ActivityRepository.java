package com.activity_service.repository;

import com.activity_service.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {
    List<Activity> findByDealId(UUID dealId);
    List<Activity> findByCompanyId(UUID companyId);
    List<Activity> findByUserId(UUID userId);
}
