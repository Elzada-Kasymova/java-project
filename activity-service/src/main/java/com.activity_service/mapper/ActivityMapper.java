package com.activity_service.mapper;

import com.activity_service.dto.ActivityCreateDTO;
import com.activity_service.dto.ActivityDTO;
import com.activity_service.dto.ActivityUpdateDTO;
import com.activity_service.entity.Activity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ActivityMapper {

    ActivityDTO toDto(Activity entity);

    Activity toEntity(ActivityCreateDTO dto);

    void updateEntityFromDto(ActivityUpdateDTO dto, @MappingTarget Activity entity);
}

