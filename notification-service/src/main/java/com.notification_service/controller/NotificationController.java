package com.notification_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification_service.entity.Notification;
import com.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationRepository repo;
    private final ObjectMapper mapper;

    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody Map<String,Object> envelope) {
        String eventId = (String) envelope.getOrDefault("eventId", UUID.randomUUID().toString());
        // idempotency
        if (repo.findByExternalId(eventId).isPresent()) {
            return ResponseEntity.accepted().body(Map.of("externalId", eventId, "status", "already_registered"));
        }

        Map<String,Object> payload = (Map<String,Object>) envelope.get("payload");
        String recipient = payload != null ? (String) payload.get("recipient") : null;

        Notification n = Notification.builder()
                .externalId(eventId)
                .templateName((String) envelope.getOrDefault("template", "default_notification"))
                .recipientEmail((String) envelope.get("recipient_email"))
                .payload(write(payload))
                .status("PENDING")
                .attempts(0)
                .createdAt(Instant.now())
                .build();
        repo.save(n);


        return ResponseEntity.accepted().body(Map.of("externalId", eventId, "status", "queued"));
    }

    private String write(Object o) {
        try { return mapper.writeValueAsString(o); } catch(Exception ex) { return "{}"; }
    }
}

