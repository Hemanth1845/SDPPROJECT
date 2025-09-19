package com.crm.service.impl;

import com.crm.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender emailSender;

    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@crm-app.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error while sending simple email: " + e.getMessage());
        }
    }

    // NEW IMPLEMENTATION for sending HTML emails
    @Override
    public void sendHtmlMessage(String to, String subject, String htmlBody) {
        MimeMessage message = emailSender.createMimeMessage();
        try {
            // Use MimeMessageHelper to build a multipart message for HTML content
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("noreply@crm-app.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // Set the second parameter to 'true' to indicate HTML
            emailSender.send(message);
        } catch (MessagingException e) {
            // Using a proper logger is recommended in a real application
            System.err.println("Error while sending HTML email: " + e.getMessage());
        }
    }
}
