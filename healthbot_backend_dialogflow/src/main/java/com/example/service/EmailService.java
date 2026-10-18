package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendSimpleEmail(String to, String subject, String body) {
        // Fallback: Always print to console so you can view reset/verify links directly in Render logs
        System.out.println("================ [EMAIL NOTIFICATION] ================");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
        System.out.println("=====================================================");

        if (mailSender == null) {
            System.err.println("JavaMailSender bean is not configured; email dispatch skipped.");
            return;
        }

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            System.out.println("Email successfully dispatched via SMTP.");
        } catch (Exception e) {
            System.err.println("SMTP delivery failed (Render blocks port 587): " + e.getMessage());
        }
    }
}
