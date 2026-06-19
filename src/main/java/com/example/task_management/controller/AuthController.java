package com.example.task_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.task_management.model.User;
import com.example.task_management.repository.UserRepository;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public String registerUser(
            String name,
            String email,
            String password,
            String role,
            RedirectAttributes redirectAttributes) {

        if (userRepository.existsByEmail(email)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Email already registered.");

            return "redirect:/register";
        }

        User user = new User();

        user.setName(name);
        user.setUsername(email);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        userRepository.save(user);

        // Auto Login After Registration
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                email,
                null,
                AuthorityUtils.createAuthorityList(
                        "ROLE_" + role));

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        redirectAttributes.addFlashAttribute(
                "success",
                "Registration successful.");

        // Always go to dashboard
        return "redirect:/dashboard";
    }
}