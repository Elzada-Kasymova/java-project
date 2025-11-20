package com.activity_service.controller;

import com.activity_service.dto.ActivityCreateDTO;
import com.activity_service.dto.ActivityDTO;
import com.activity_service.dto.ActivityUpdateDTO;
import com.activity_service.service.ActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService service;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<ActivityDTO> getAll() {
        log.info("Fetching all activities");
        return service.getAll();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SYSTEM')")
    @GetMapping("/{id}")
    public ActivityDTO getById(@PathVariable UUID id) {
        log.info("Fetching activity by id: {}", id);
        return service.get(id);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping
    public ActivityDTO create(@RequestBody ActivityCreateDTO dto) {
        log.info("Creating new activity: {}", dto.getTitle());
        return service.create(dto);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ActivityDTO update(@PathVariable UUID id, @RequestBody ActivityUpdateDTO dto) {
        log.info("Updating activity with id {}", id);
        return service.update(id, dto);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        log.info("Deleting activity with id {}", id);
        service.delete(id);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/{id}/complete")
    public ActivityDTO complete(@PathVariable UUID id, @RequestParam UUID completedBy) {
        log.info("Completing activity {} by user {}", id, completedBy);
        return service.complete(id, completedBy);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/deal/{dealId}")
    public List<ActivityDTO> getByDealId(@PathVariable UUID dealId) {
        log.info("Fetching activities by dealId {}", dealId);
        return service.findByDealId(dealId);
    }
}
