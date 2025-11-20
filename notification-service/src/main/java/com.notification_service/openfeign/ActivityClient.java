package com.notification_service.openfeign;

import com.notification_service.dto.ActivityDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;
@FeignClient(name = "ACTIVITY-SERVICE")
public interface ActivityClient {

    @GetMapping("/internal/activity/{id}")
    ActivityDTO getByIdInternal(@PathVariable UUID id);
}
