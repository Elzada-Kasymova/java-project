package com.deal_service.kafka;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class DealEventPayloadBuilder {

    public String buildDealCreatedPayload(java.util.UUID dealId) {
        return dealId.toString();
    }

    public String buildDealDeletedPayload(java.util.UUID dealId) {
        return dealId.toString();
    }

    public String buildStageChangePayload(java.util.UUID dealId, Enum<?> stage) {
        if (stage == null) return dealId.toString();
        return "{\"dealId\":\"" + dealId + "\",\"stage\":\"" + stage.name() + "\"}";
    }

    public String buildDealUpdatedPayload(java.util.UUID dealId, java.util.UUID companyId, List<java.util.UUID> userIds) {

        boolean hasCompany = companyId != null;
        boolean hasUsers = userIds != null && !userIds.isEmpty();

        if (!hasCompany && !hasUsers) {
            return dealId.toString();
        }

        StringBuilder sb = new StringBuilder("{\"dealId\":\"").append(dealId).append("\"");

        if (hasCompany) {
            sb.append(",\"companyId\":\"").append(companyId).append("\"");
        }

        if (hasUsers) {
            String users = userIds.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(s -> "\"" + s + "\"")
                    .collect(Collectors.joining(","));
            sb.append(",\"userIds\":[").append(users).append("]");
        }

        sb.append("}");
        return sb.toString();
    }
}
