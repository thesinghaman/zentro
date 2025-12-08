package com.zentro.feature.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Email Service implementation using Resend API
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    
    @Value("${app.email.resend.api-key}")
    private String resendApiKey;
    
    @Value("${app.email.resend.from-email}")
    private String fromEmail;
    
    @Value("${app.email.resend.from-name}")
    private String fromName;
    
    private static final String RESEND_API_URL = "https://api.resend.com/emails";
    
    @Override
    public void sendVerificationOtp(String toEmail, String firstName, String otp) {
        String subject = "Verify Your Zentro Account";
        String htmlContent = buildVerificationOtpEmail(firstName, otp);
        
        sendEmail(toEmail, subject, htmlContent);
        log.info("Verification OTP email sent to: {}", toEmail);
    }
    
    @Override
    public void sendPasswordResetOtp(String toEmail, String firstName, String otp) {
        String subject = "Reset Your Zentro Password";
        String htmlContent = buildPasswordResetOtpEmail(firstName, otp);
        
        sendEmail(toEmail, subject, htmlContent);
        log.info("Password reset OTP email sent to: {}", toEmail);
    }
    
    @Override
    public void sendWelcomeEmail(String toEmail, String firstName) {
        String subject = "Welcome to Zentro!";
        String htmlContent = buildWelcomeEmail(firstName);
        
        sendEmail(toEmail, subject, htmlContent);
        log.info("Welcome email sent to: {}", toEmail);
    }
    
    @Override
    public void sendPasswordResetConfirmation(String toEmail, String firstName) {
        String subject = "Password Reset Successful";
        String htmlContent = buildPasswordResetConfirmationEmail(firstName);
        
        sendEmail(toEmail, subject, htmlContent);
        log.info("Password reset confirmation email sent to: {}", toEmail);
    }
    
    /**
     * Send email via Resend API
     */
    private void sendEmail(String toEmail, String subject, String htmlContent) {
        try {
            WebClient webClient = WebClient.builder()
                    .baseUrl(RESEND_API_URL)
                    .defaultHeader("Authorization", "Bearer " + resendApiKey)
                    .defaultHeader("Content-Type", "application/json")
                    .build();
            
            Map<String, Object> emailRequest = Map.of(
                    "from", fromName + " <" + fromEmail + ">",
                    "to", new String[]{toEmail},
                    "subject", subject,
                    "html", htmlContent
            );
            
            webClient.post()
                    .bodyValue(emailRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(error -> {
                        log.error("Failed to send email to {}: {}", toEmail, error.getMessage());
                        return Mono.empty();
                    })
                    .subscribe(response -> log.debug("Email sent successfully: {}", response));
            
        } catch (Exception e) {
            log.error("Error sending email to {}: {}", toEmail, e.getMessage(), e);
        }
    }
    
    /**
     * Build verification OTP email HTML
     */
    private String buildVerificationOtpEmail(String firstName, String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 30px; border-radius: 10px 10px 0 0; text-align: center;">
                        <h1 style="color: white; margin: 0;">Verify Your Email</h1>
                    </div>
                    <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                        <p style="font-size: 16px;">Hi %s,</p>
                        <p style="font-size: 16px;">Welcome to Zentro! Use the code below to verify your email address:</p>
                        <div style="background: white; border: 2px dashed #667eea; padding: 20px; text-align: center; margin: 30px 0; border-radius: 8px;">
                            <span style="font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 8px;">%s</span>
                        </div>
                        <p style="font-size: 14px; color: #666;">This code will expire in <strong>5 minutes</strong>.</p>
                        <p style="font-size: 14px; color: #666;">If you didn't request this, please ignore this email.</p>
                        <hr style="border: none; border-top: 1px solid #ddd; margin: 30px 0;">
                        <p style="font-size: 12px; color: #999; text-align: center;">© 2025 Zentro. All rights reserved.</p>
                    </div>
                </body>
                </html>
                """.formatted(firstName, otp);
    }
    
    /**
     * Build password reset OTP email HTML
     */
    private String buildPasswordResetOtpEmail(String firstName, String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); padding: 30px; border-radius: 10px 10px 0 0; text-align: center;">
                        <h1 style="color: white; margin: 0;">Reset Your Password</h1>
                    </div>
                    <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                        <p style="font-size: 16px;">Hi %s,</p>
                        <p style="font-size: 16px;">We received a request to reset your Zentro password. Use the code below:</p>
                        <div style="background: white; border: 2px dashed #f5576c; padding: 20px; text-align: center; margin: 30px 0; border-radius: 8px;">
                            <span style="font-size: 32px; font-weight: bold; color: #f5576c; letter-spacing: 8px;">%s</span>
                        </div>
                        <p style="font-size: 14px; color: #666;">This code will expire in <strong>5 minutes</strong>.</p>
                        <p style="font-size: 14px; color: #666;">If you didn't request this, your account is secure. You can safely ignore this email.</p>
                        <hr style="border: none; border-top: 1px solid #ddd; margin: 30px 0;">
                        <p style="font-size: 12px; color: #999; text-align: center;">© 2025 Zentro. All rights reserved.</p>
                    </div>
                </body>
                </html>
                """.formatted(firstName, otp);
    }
    
    /**
     * Build welcome email HTML
     */
    private String buildWelcomeEmail(String firstName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 30px; border-radius: 10px 10px 0 0; text-align: center;">
                        <h1 style="color: white; margin: 0;">Welcome to Zentro! 🎉</h1>
                    </div>
                    <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                        <p style="font-size: 16px;">Hi %s,</p>
                        <p style="font-size: 16px;">Your account has been successfully verified! We're excited to have you join the Zentro community.</p>
                        <p style="font-size: 16px;">Start exploring our amazing products and enjoy your shopping experience.</p>
                        <div style="text-align: center; margin: 30px 0;">
                            <p style="font-size: 14px; color: #666;">Happy Shopping! 🛍️</p>
                        </div>
                        <hr style="border: none; border-top: 1px solid #ddd; margin: 30px 0;">
                        <p style="font-size: 12px; color: #999; text-align: center;">© 2025 Zentro. All rights reserved.</p>
                    </div>
                </body>
                </html>
                """.formatted(firstName);
    }
    
    /**
     * Build password reset confirmation email HTML
     */
    private String buildPasswordResetConfirmationEmail(String firstName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background: linear-gradient(135deg, #11998e 0%%, #38ef7d 100%%); padding: 30px; border-radius: 10px 10px 0 0; text-align: center;">
                        <h1 style="color: white; margin: 0;">Password Reset Successful ✓</h1>
                    </div>
                    <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                        <p style="font-size: 16px;">Hi %s,</p>
                        <p style="font-size: 16px;">Your Zentro password has been successfully reset.</p>
                        <p style="font-size: 14px; color: #666;">If you didn't make this change, please contact our support team immediately.</p>
                        <hr style="border: none; border-top: 1px solid #ddd; margin: 30px 0;">
                        <p style="font-size: 12px; color: #999; text-align: center;">© 2025 Zentro. All rights reserved.</p>
                    </div>
                </body>
                </html>
                """.formatted(firstName);
    }
}
