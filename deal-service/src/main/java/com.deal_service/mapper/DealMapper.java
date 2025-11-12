package com.deal_service.mapper;

import com.deal_service.domain.DealStage;
import com.deal_service.dto.*;
import com.deal_service.entity.Deal;
import org.mapstruct.*;
import java.time.Instant;

@Mapper(componentModel = "spring")
public interface DealMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "closedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true) // <- исправлено (was isDeleted)
    Deal toEntity(DealCreateDTO dto);

    DealResponseDTO toDto(Deal entity);
    DealDTO toDealDTO(Deal entity);

    DealSummaryDTO toSummaryDto(Deal entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deleted", ignore = true) // <- исправлено (was isDeleted)
    void updateFromDto(DealUpdateDTO dto, @MappingTarget Deal entity);

    @AfterMapping
    default void setDefaultStage(DealCreateDTO dto, @MappingTarget Deal entity) {
        if (entity.getStage() == null) {
            entity.setStage(DealStage.LEAD);
        }
    }

    @AfterMapping
    default void setTimestampsAfterCreate(DealCreateDTO dto, @MappingTarget Deal entity) {
        entity.setCreatedAt(Instant.now());
    }
}
