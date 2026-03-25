package com.vaultcore.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.UUID;

@Service
@Profile("prod")
public class SmtpNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(SmtpNotificationService.class);

    private final JavaMailSender javaMailSender;

    public SmtpNotificationService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Async
    @Override
    public void sendChallenge(UUID userId, String otp) {
        log.info("[SMTP-2FA] Attempting to send OTP to userId: {}", userId);
        try {
            // Note: In a real app we would resolve user email from UserRepository
            String fakeEmailDest = "user-" + userId + "@example.com";
            
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(fakeEmailDest);
            helper.setSubject("VaultCore - Action Required: 2FA Authentication");

            String htmlMsg = "<h3>VaultCore Security Alert</h3>" +
                    "<p>A transaction exceeding your defined limit has been initiated.</p>" +
                    "<p>Your 6-digit OTP is: <strong>" + otp + "</strong></p>" +
                    "<p>This code will expire in 5 minutes.</p>";

            helper.setText(htmlMsg, true);
            javaMailSender.send(message);

            log.info("[SMTP-2FA] Successfully sent OTP to {}", fakeEmailDest);
        } catch (MessagingException e) {
            log.error("[SMTP-2FA] Failed to send email.", e);
        }

        // SMS fallback stub
        log.warn("TODO: Implement Twilio/AWS SNS SMS fallback send");
    }
}
