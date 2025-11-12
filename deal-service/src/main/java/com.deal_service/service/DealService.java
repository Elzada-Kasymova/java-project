package com.deal_service.service;


import com.deal_service.domain.DealStage;
import com.deal_service.dto.*;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DealService {
    DealResponseDTO create(DealCreateDTO dto);

    List<DealSummaryDTO> getAll();

    DealResponseDTO getById(UUID id);

    DealResponseDTO update(UUID id, DealUpdateDTO dto);

    DealResponseDTO changeStage(UUID id, DealPatchStageDTO req);

    void delete(UUID id);

    List<DealSummaryDTO> search(
            Optional<DealStage> stage,
            Optional<UUID> companyId,
            Optional<UUID> userId,
            Optional<String> dateFrom,
            Optional<String> dateTo
    );
}
