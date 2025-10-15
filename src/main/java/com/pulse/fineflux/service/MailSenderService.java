package com.pulse.fineflux.service;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailSenderService {
    private final JavaMailSender mailSender;
    // For more security, make this configurable with @Value later
    private final String from = "reset@fineflux.com";

    public void sendUsernameReminder(String to, String username, String firstName) {
        String subject = "Username Reminder";
        String body = "Hello " + (firstName != null ? firstName : "") + ",\n\nYour username is: " + username +
                "\nIf you did not request this, ignore this mail.\n\n- FineFlux Team";
        log.info("Preparing to send username reminder mail to {} for username {}", to, username);
        sendSimple(subject, body, to);
    }

    public void sendPasswordResetMail(@Email @NotBlank String emailId, @NotBlank String username, String link) {
        String subject = "FineFlux Password Reset Request";
        String body =
                "Hello " + username + ",\n\n" +
                        "We received a request to reset your FineFlux password.\n" +
                        "To set a new password, please click the link below (valid for 1 hour):\n" +
                        link + "\n\n" +
                        "If you did not request a password reset, simply ignore this email and your password will remain unchanged.\n\n" +
                        "Best regards,\nFineFlux Team";
        log.info("Preparing to send password reset mail to {} with reset link {}", emailId, link);
        sendSimple(subject, body, emailId);
    }

    private void sendSimple(String subject, String body, String to) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, false);
            helper.setTo(to);
            helper.setFrom(from);
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(message);
            log.info("Email sent to {} with subject '{}'", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send mail", e);
        }
    }
}
