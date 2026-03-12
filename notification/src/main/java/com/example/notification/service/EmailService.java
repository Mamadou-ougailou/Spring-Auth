package com.example.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Value("${app.verification.base-url}")
    private String baseUrl;

    /**
     * Sends a verification e-mail containing the verification link.
     *
     * @param to         recipient e-mail address
     * @param tokenId    public token identifier
     * @param tokenClear raw token value (used as query parameter {@code t})
     */
    public void sendVerificationEmail(String to, String tokenId, String tokenClear) {
        String verificationLink = String.format(
                "%s/verify?tokenId=%s&t=%s", baseUrl, tokenId, tokenClear);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@auth-service.local");
        message.setTo(to);
        message.setSubject("Verify your e-mail address");
        message.setText("Welcome!\n\nPlease click the link below to verify your e-mail:\n\n"
                + verificationLink
                + "\n\nThis link expires in 30 minutes.\n");

        mailSender.send(message);

        log.info("[EmailService] Verification e-mail sent to '{}' (tokenId={}).", to, tokenId);
    }
}
