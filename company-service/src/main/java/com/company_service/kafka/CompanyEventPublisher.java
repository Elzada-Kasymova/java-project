package com.company_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyEventPublisher {

    private static final String TOPIC_COMPANY_DELETE = "company-delete";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publishCompanyDeleted(String companyId) {
        try {
            kafkaTemplate.send(TOPIC_COMPANY_DELETE, companyId, companyId);
            log.info("Published company-delete key={}, value={}", companyId, companyId);
        } catch (Exception e) {
            log.error("Failed to publish company-delete for {}: {}", companyId, e.getMessage(), e);
        }
    }
}
