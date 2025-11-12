package com.notification_service.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification_service.dto.NotificationRequest;
import com.notification_service.entity.Notification;
import com.notification_service.exception.NotificationSendException;
import com.notification_service.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repo;
    private final TemplateService templateService;
    private final EmailSender emailSender;
    private final ObjectMapper objectMapper;


    @Value("${notification.max-attempts:3}")
    private int maxAttempts;

    @Override
    @Transactional
    public void handleEnvelope(Map<String, Object> envelope) {
        if (envelope == null) return;
        String eventId = asString(envelope.get("eventId"));
        String eventType = asString(envelope.get("eventType"));
        Map<String, Object> payload = asMap(envelope.get("payload"));

        if (eventId == null || eventType == null) {
            // invalid envelope — ignore
            return;
        }

        // idempotency: if already SENT for this eventId -> skip
        Optional<Notification> existingOpt = repo.findByExternalId(eventId);
        if (existingOpt.isPresent() && "SENT".equalsIgnoreCase(existingOpt.get().getStatus())) {
            return;
        }

        // dispatch by eventType
        switch (eventType) {
            case "UserCreated" -> handleUserCreated(eventId, payload);
            case "DealCreated" -> handleDealCreated(eventId, payload);
            case "DealStageChanged" -> handleDealStageChanged(eventId, payload);
            case "CompanyCreated" -> handleCompanyCreated(eventId, payload);
            default -> {
                // ignore other events
            }
        }
    }

    private void handleUserCreated(String eventId, Map<String, Object> payload) {
        if (payload == null) return;

        // payload from your example has fields: id, firstName, lastName, email, companyIds
        String email = extractEmailFromPayload(payload);
        if (email == null) return;

        Map<String, Object> templateVars = Map.of(
                "firstName", payload.getOrDefault("firstName", ""),
                "lastName", payload.getOrDefault("lastName", "")
        );

        NotificationRequest req = NotificationRequest.builder()
                .externalId(eventId)
                .eventType("UserCreated")
                .template("welcome_email")
                .recipientEmail(email)
                .payload(templateVars)
                .build();

        // Use provided eventId (avoid generating new one)
        req.setExternalId(eventId);
        enqueueAndSend(req);
    }

    private void handleDealCreated(String eventId, Map<String, Object> payload) {
        if (payload == null) return;

        // Try to find assignee email in payload under common keys
        String email = extractEmailFromPayload(payload);
        // If no direct email, check for assigneeEmail or nested assignee object
        if (email == null) {
            email = asString(payload.get("assigneeEmail"));
            if (email == null) {
                Map<String, Object> assignee = asMap(payload.get("assignee"));
                if (assignee != null) email = extractEmailFromPayload(assignee);
            }
        }
        if (email == null) return;

        Map<String, Object> templateVars = Map.of(
                "assigneeName", payload.getOrDefault("assigneeName", ""),
                "dealId", payload.getOrDefault("dealId", payload.getOrDefault("id", "")),
                "amount", payload.getOrDefault("amount", "")
        );

        NotificationRequest req = NotificationRequest.builder()
                .externalId(eventId)
                .eventType("DealCreated")
                .template("deal_created_email")
                .recipientEmail(email)
                .payload(templateVars)
                .build();

        enqueueAndSend(req);
    }

    private void handleDealStageChanged(String eventId, Map<String, Object> payload) {
        if (payload == null) return;
        String stageTo = asString(payload.get("stageTo"));
        if (!"WON".equalsIgnoreCase(stageTo)) return;

        // Find assignee email similarly to DealCreated
        String email = extractEmailFromPayload(payload);
        if (email == null) {
            email = asString(payload.get("assigneeEmail"));
            if (email == null) {
                Map<String, Object> assignee = asMap(payload.get("assignee"));
                if (assignee != null) email = extractEmailFromPayload(assignee);
            }
        }
        if (email == null) return;

        Map<String, Object> templateVars = Map.of(
                "assigneeName", payload.getOrDefault("assigneeName", ""),
                "dealId", payload.getOrDefault("dealId", payload.getOrDefault("id", "")),
                "amount", payload.getOrDefault("amount", "")
        );

        NotificationRequest req = NotificationRequest.builder()
                .externalId(eventId)
                .eventType("DealStageChanged")
                .template("deal_won_congrats")
                .recipientEmail(email)
                .payload(templateVars)
                .build();

        enqueueAndSend(req);

        // Optional: create accounting task — stubbed for later
        // createAccountingTask(payload);
    }

    private void handleCompanyCreated(String eventId, Map<String, Object> payload) {
        if (payload == null) return;

        // Account manager email may be present as accountManagerEmail, managerEmail, or nested manager object
        String email = asString(payload.get("accountManagerEmail"));
        if (email == null) email = asString(payload.get("managerEmail"));
        if (email == null) {
            Map<String, Object> manager = asMap(payload.get("accountManager"));
            if (manager != null) email = extractEmailFromPayload(manager);
        }
        if (email == null) {
            // fallback to any email in payload
            email = extractEmailFromPayload(payload);
        }
        if (email == null) return;

        Map<String, Object> templateVars = Map.of(
                "accountManagerName", payload.getOrDefault("accountManagerName", ""),
                "companyName", payload.getOrDefault("companyName", payload.getOrDefault("name", ""))
        );

        NotificationRequest req = NotificationRequest.builder()
                .externalId(eventId)
                .eventType("CompanyCreated")
                .template("company_created_email")
                .recipientEmail(email)
                .payload(templateVars)
                .build();

        enqueueAndSend(req);
    }

    private void enqueueAndSend(NotificationRequest req) {
        if (req == null || req.getExternalId() == null) return;

        // create DB row (if not exists)
        Notification n = repo.findByExternalId(req.getExternalId()).orElseGet(() -> {
            Notification nn = Notification.builder()
                    .externalId(req.getExternalId())
                    .eventType(req.getEventType())
                    .templateName(req.getTemplate())
                    .payload(serialize(req.getPayload()))
                    .recipientEmail(req.getRecipientEmail())
                    .status("PENDING")
                    .attempts(0)
                    .createdAt(Instant.now())
                    .build();
            return repo.save(nn);
        });

        // send in background
        sendAsync(req);
    }

    @Async("notificationExecutor")
    public void sendAsync(NotificationRequest req) {
        // fetch DB row
        Notification n = repo.findByExternalId(req.getExternalId()).orElseThrow();
        try {
            String body = templateService.render(req.getTemplate(), req.getPayload());
            String subject = templateService.getSubject(req.getTemplate());
            emailSender.send(req.getRecipientEmail(), subject, body);

            n.setStatus("SENT");
            n.setSentAt(Instant.now());
            n.setAttempts(n.getAttempts() + 1);
            repo.save(n);
        } catch (Exception ex) {
            n.setAttempts(n.getAttempts() + 1);
            n.setError(ex.getMessage());
            if (n.getAttempts() >= maxAttempts) n.setStatus("DLQ"); else n.setStatus("RETRY");
            repo.save(n);
            throw new NotificationSendException("Failed to send email", ex);
        }
    }

    /* ---------- Helpers ---------- */

    // Safely cast to Map<String,Object>
    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object o) {
        if (o instanceof Map) {
            return (Map<String, Object>) o;
        }
        return null;
    }

    private String asString(Object o) {
        if (o == null) return null;
        return o.toString();
    }

    /**
     * Try common locations for email inside payload or nested map.
     * Looks for keys: email, recipientEmail, assigneeEmail, userEmail
     * If contains nested maps (e.g. "assignee": {email: ...}) it will attempt them as well.
     */
    private String extractEmailFromPayload(Map<String, Object> payload) {
        if (payload == null) return null;

        // direct keys
        String[] keys = new String[] {"email", "recipientEmail", "assigneeEmail", "userEmail", "managerEmail"};
        for (String k : keys) {
            Object v = payload.get(k);
            if (v != null && v.toString().contains("@")) return v.toString();
        }

        // sometimes payload has nested objects with email
        for (Object value : payload.values()) {
            if (value instanceof Map) {
                Map<String, Object> nested = asMap(value);
                String nestedEmail = extractEmailFromPayload(nested);
                if (nestedEmail != null) return nestedEmail;
            }
            // if value is a JSON string that might contain email - skip (not expected)
        }

        return null;
    }

    private String serialize(Object o) {
        try { return objectMapper.writeValueAsString(o); } catch (Exception e) { return "{}"; }
    }
}

