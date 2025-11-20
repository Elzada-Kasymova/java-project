package com.notification_service.service;

import com.notification_service.dto.ActivityDTO;
import com.notification_service.dto.DealResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    private void sendTemplate(String to, String subject, String templateName, Context context) {
        try {
            String htmlContent = templateEngine.process("email/" + templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
        }
    }


    @Override
    public void sendWelcomeEmail(String email, String username) {
        Context ctx = new Context();
        ctx.setVariable("username", username);

        sendTemplate(email, "Welcome to CRM!", "welcome", ctx);
    }


    @Override
    public void cancelScheduledEmails(UUID userId) {
        log.info("Cancelled scheduled emails for {}", userId);
    }


    @Override
    public void sendDealCreatedNotification(String email, DealResponseDTO deal) {
        Context ctx = new Context();
        ctx.setVariable("title", deal.title());
        ctx.setVariable("amount", deal.amount());
        ctx.setVariable("stage", deal.stage());

        sendTemplate(email, "New Deal Created", "deal-created", ctx);
    }


    @Override
    public void sendCongratsEmail(String email, DealResponseDTO deal) {
        Context ctx = new Context();
        ctx.setVariable("title", deal.title());
        ctx.setVariable("amount", deal.amount());

        sendTemplate(email, "🎉 Deal WON!", "deal-won", ctx);
    }


    @Override
    public void sendActivityCompletedEmail(String email, ActivityDTO activity) {
        Context ctx = new Context();
        ctx.setVariable("title", activity.getTitle());
        ctx.setVariable("description", activity.getDescription());
        ctx.setVariable("completedAt", activity.getCompletedAt());

        sendTemplate(email, "Activity Completed", "activity-completed", ctx);
    }

    @Override
    public void sendActivityCreatedEmail(String email, ActivityDTO activity) {

        Context ctx = new Context();
        ctx.setVariable("title", activity.getTitle());
        ctx.setVariable("description", activity.getDescription());
        ctx.setVariable("createdAt", activity.getCreatedAt());

        sendTemplate(email, "New Activity Created", "activity-created", ctx);
    }

}
