package com.bachiep.sems.service;

import com.bachiep.sems.exception.EmailDeliveryException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.application.name:SEMS}")
    private String appName;

    @Value("${app.mail.from:}")
    private String mailFrom;

    @Value("${app.security.otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (mailFrom != null && !mailFrom.isBlank()) {
            message.setFrom(mailFrom);
        }
        message.setTo(to);
        message.setSubject(appName + " - Your OTP Code");
        message.setText("Your OTP code for logging into " + appName + " is: " + otp
                + "\nThis code will expire in " + otpExpirationMinutes + " minutes.");
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            throw new EmailDeliveryException("Could not send OTP email. Please check mail configuration.", exception);
        }
    }
}
