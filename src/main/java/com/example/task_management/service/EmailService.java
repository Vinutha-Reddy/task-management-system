package com.example.task_management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendResetLink(String email, String link) {

        System.out.println("Sending email to: " + email);
        System.out.println("Reset link: " + link);

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("Reset Your Password");
        message.setText(
                "Click the link below to reset your password:\n\n"
                        + link);

        mailSender.send(message);

        System.out.println("Email sent successfully.");
    }

    public void sendEmail(
            String to,
            String subject,
            String body) {

        try {

            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            System.out.println(
                    "Email sent successfully to: "
                            + to);

        } catch (Exception e) {

            System.out.println(
                    "Error sending email to: "
                            + to);

            e.printStackTrace();

            throw e;
        }
    }
}
