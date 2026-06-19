package com.example.task_management.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.task_management.model.PasswordResetToken;
import com.example.task_management.model.User;
import com.example.task_management.repository.PasswordResetTokenRepository;
import com.example.task_management.repository.UserRepository;
import com.example.task_management.service.EmailService;

@Controller
public class ForgotPasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.base-url}")
    private String baseUrl;

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @RequestParam String email,
            Model model) {

        System.out.println("FORGOT PASSWORD REQUEST RECEIVED");
        System.out.println("EMAIL = " + email);
        User user = userRepository.findByEmail(email);

        if (user == null) {
            model.addAttribute(
                    "message",
                    "Account does not exist. Please register.");
            return "forgot-password";
        }

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = tokenRepository.findByEmail(email);

        if (resetToken == null) {
            resetToken = new PasswordResetToken();
        }

        resetToken.setEmail(email);
        resetToken.setToken(token);
        resetToken.setExpiryDate(
                LocalDateTime.now().plusMinutes(30));

        tokenRepository.save(resetToken);

        String resetLink = baseUrl + "/reset-password?token=" + token;

        emailService.sendResetLink(email, resetLink);

        return "redirect:/login?mailSent=true";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(
            @RequestParam String token,
            Model model) {

        model.addAttribute("token", token);

        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @RequestParam String token,
            @RequestParam String email,
            @RequestParam String password,
            Model model) {

        PasswordResetToken resetToken = tokenRepository.findByToken(token);

        if (resetToken == null) {
            model.addAttribute(
                    "message",
                    "Invalid reset link.");
            return "reset-password";
        }

        if (resetToken.getExpiryDate()
                .isBefore(LocalDateTime.now())) {

            model.addAttribute(
                    "message",
                    "Reset link has expired.");
            return "reset-password";
        }

        User user = userRepository.findByEmail(email);

        if (user == null) {

            model.addAttribute(
                    "message",
                    "User not found.");
            return "reset-password";
        }

        user.setPassword(
                passwordEncoder.encode(password));

        userRepository.save(user);

        tokenRepository.delete(resetToken);

        return "redirect:/login?resetSuccess=true";
    }
}