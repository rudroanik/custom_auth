package com.example.custom_auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendVerificationEmail(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Email Verification - Custom Auth");

            String verificationLink = baseUrl + "/api/auth/verify?token=" + token;

            String htmlContent = buildVerificationEmailHtml(verificationLink);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Verification email sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    private String buildVerificationEmailHtml(String verificationLink) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }" +
                ".content { background-color: #f9f9f9; padding: 30px; border-radius: 5px; margin-top: 20px; }" +
                ".button { display: inline-block; padding: 12px 24px; background-color: #4CAF50; color: white; " +
                "text-decoration: none; border-radius: 5px; margin: 20px 0; }" +
                ".footer { margin-top: 20px; text-align: center; color: #666; font-size: 12px; }" +
                ".warning { color: #d32f2f; font-weight: bold; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>Email Verification</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<h2>Welcome to Custom Auth!</h2>" +
                "<p>Thank you for registering. Please verify your email address to complete your registration.</p>" +
                "<p>Click the button below to verify your email:</p>" +
                "<a href='" + verificationLink + "' class='button'>Verify Email</a>" +
                "<p>Or copy and paste this link in your browser:</p>" +
                "<p style='word-break: break-all;'>" + verificationLink + "</p>" +
                "<p class='warning'>⚠️ This link will expire in 10 minutes.</p>" +
                "<p>If you didn't create an account, please ignore this email.</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>© 2026 Custom Auth. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
