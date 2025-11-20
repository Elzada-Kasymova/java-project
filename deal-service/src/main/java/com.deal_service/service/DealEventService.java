package com.deal_service.service;

import com.deal_service.entity.Deal;
import com.deal_service.repository.DealRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DealEventService {

    private static final Logger log = LoggerFactory.getLogger(DealEventService.class);

    private final DealRepository dealRepository;
    private final ObjectMapper objectMapper;

    public DealEventService(DealRepository dealRepository, ObjectMapper objectMapper) {
        this.dealRepository = dealRepository;
        this.objectMapper = objectMapper;
    }

    public void handleUserDeleted(String userIdStr) {
        UUID userId = UUID.fromString(userIdStr);

        List<Deal> deals = dealRepository.findByUserId(userId);
        for (Deal d : deals) {
            d.setUserId(null);
        }
        dealRepository.saveAll(deals);

        log.info("Handled user-delete. Cleared userId in {} deals for user {}", deals.size(), userId);
    }


    public void handleCompanyDeleted(String companyIdStr) {
        UUID companyId = UUID.fromString(companyIdStr);

        List<Deal> deals = dealRepository.findByCompanyId(companyId);
        for (Deal d : deals) {
            d.setCompanyId(null);
        }
        dealRepository.saveAll(deals);

        log.info("Handled company-delete. Cleared companyId in {} deals", deals.size());
    }


    public void handleUserUpdated(String raw) {
        try {
            if (!raw.trim().startsWith("{")) {
                log.info("Received user-update without company info for user {}", raw);
                return;
            }

            Map<String, Object> payload = objectMapper.readValue(raw, Map.class);

            String userIdStr = (String) payload.get("userId");
            if (userIdStr == null) {
                log.warn("user-update payload missing 'userId': {}", raw);
                return;
            }

            UUID userId = UUID.fromString(userIdStr);

            if (payload.containsKey("companyId")) {
                String companyIdStr = (String) payload.get("companyId");
                UUID companyId = UUID.fromString(companyIdStr);

                updateDealsWithCompany(userId, companyId);
                return;
            }

            if (payload.containsKey("companyIds")) {
                List<String> ids = (List<String>) payload.get("companyIds");
                if (!ids.isEmpty()) {
                    UUID first = UUID.fromString(ids.get(0));
                    updateDealsWithCompany(userId, first);
                }
                return;
            }

            log.info("user-update contains no companyId/companyIds");

        } catch (Exception e) {
            log.error("Error parsing user-update payload: {}", raw, e);
        }
    }

    private void updateDealsWithCompany(UUID userId, UUID companyId) {
        List<Deal> deals = dealRepository.findByUserId(userId);

        for (Deal d : deals) {
            d.setCompanyId(companyId);
        }
        dealRepository.saveAll(deals);

        log.info("Updated companyId={} in {} deals for user {}", companyId, deals.size(), userId);
    }
}

