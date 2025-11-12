package com.notification_service.service;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailSender {

    private final JavaMailSender mailSender;

    @Value("${notification.mail.from:no-reply@example.com}")
    private String from;

    public void send(String to, String subject, String html) throws Exception {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, "utf-8");
        helper.setText(html, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setFrom(from);
        mailSender.send(msg);
    }
}
