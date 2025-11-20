package com.activity_service.controller;

import com.activity_service.dto.ActivityDTO;
import com.activity_service.service.ActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/activity")
public class ActivityInternalController {
    private final ActivityService service;

    @GetMapping("/{id}")
    public ActivityDTO getByIdInternal(@PathVariable UUID id) {
        log.info("GET /internal/activity/{} — internal request", id);
        return service.get(id);
    }
}
