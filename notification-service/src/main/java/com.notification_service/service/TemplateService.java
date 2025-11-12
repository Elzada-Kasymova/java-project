package com.notification_service.service;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
public class TemplateService {

    private final SpringTemplateEngine templateEngine;

    public TemplateService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String render(String templateName, Map<String,Object> vars) {
        Context ctx = new Context();
        if (vars != null) ctx.setVariables(vars);
        return templateEngine.process(templateName, ctx);
    }

    public String getSubject(String templateName) {
        return switch (templateName) {
            case "welcome_email" -> "Добро пожаловать в CRM";
            case "deal_created_email" -> "Новая сделка в CRM";
            case "deal_won_congrats" -> "Поздравляем! Сделка выиграна";
            case "company_created_email" -> "Новый контрагент в CRM";
            default -> "Уведомление CRM";
        };
    }
}

