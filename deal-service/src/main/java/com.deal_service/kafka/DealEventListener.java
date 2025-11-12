package com.deal_service.kafka;

import com.deal_service.entity.Deal;
import com.deal_service.repository.DealRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class DealEventListener {

    private static final Logger logger = LoggerFactory.getLogger(DealEventListener.class);

    private final DealRepository dealRepository;
    private final ObjectMapper objectMapper;

    public DealEventListener(DealRepository dealRepository, ObjectMapper objectMapper) {
        this.dealRepository = dealRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "crm-events", groupId = "deal-service-group")
    public void handleEvent(ConsumerRecord<String, String> record) {
        try {
            Map<String, Object> event = objectMapper.readValue(record.value(), Map.class);
            String eventType = (String) event.get("eventType");
            Map<String, Object> payload = (Map<String, Object>) event.get("payload");

            switch (eventType) {
                case "UserDeleted" -> onUserDeleted(payload);
                case "CompanyDeleted" -> onCompanyDeleted(payload);
                case "CompanyCreated" -> onCompanyCreated(payload);
                case "CompanyUpdated" -> onCompanyUpdated(payload);
                default -> logger.warn("⚠️ Unknown event type received: {}", eventType);
            }
        } catch (Exception e) {
            logger.error("❌ Error processing Kafka event: {}", record.value(), e);
        }
    }

    private void onUserDeleted(Map<String, Object> payload) {
        String userIdStr = (String) payload.get("id");
        if (userIdStr == null) {
            logger.warn("⚠️ UserDeleted event missing 'id' field");
            return;
        }

        UUID userId = UUID.fromString(userIdStr);
        List<Deal> deals = dealRepository.findByUserId(userId);

        for (Deal deal : deals) {
            deal.setUserId(null);
        }
        dealRepository.saveAll(deals);

        logger.info("🧹 UserDeleted: cleared owner in {} deals (userId={})", deals.size(), userId);
    }

    private void onCompanyDeleted(Map<String, Object> payload) {
        String companyIdStr = (String) payload.get("id");
        if (companyIdStr == null) {
            logger.warn("⚠️ CompanyDeleted event missing 'id' field");
            return;
        }

        UUID companyId = UUID.fromString(companyIdStr);
        List<Deal> deals = dealRepository.findByCompanyId(companyId);

        for (Deal deal : deals) {
            deal.setCompanyId(null);
        }
        dealRepository.saveAll(deals);

        logger.info("🏢 CompanyDeleted: cleared company in {} deals (companyId={})", deals.size(), companyId);
    }

    private void onCompanyCreated(Map<String, Object> payload) {
        logger.info("🏢 CompanyCreated: {}", payload.get("name"));
    }

    private void onCompanyUpdated(Map<String, Object> payload) {
        logger.info("🏢 CompanyUpdated: {}", payload.get("name"));
    }
}
