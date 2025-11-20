package com.activity_service.service;

import com.activity_service.dto.ActivityCreateDTO;
import com.activity_service.dto.ActivityDTO;
import com.activity_service.dto.ActivityUpdateDTO;

import java.util.List;
import java.util.UUID;

public interface ActivityService {
    ActivityDTO create(ActivityCreateDTO dto);
    ActivityDTO get(UUID id);
    List<ActivityDTO> getAll();
    ActivityDTO update(UUID id, ActivityUpdateDTO dto);
    void delete(UUID id);
    ActivityDTO complete(UUID id, UUID completedBy);
    List<ActivityDTO> findByDealId(UUID dealId);
}
