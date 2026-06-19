package com.example.task_management;

import java.awt.Desktop;
import java.net.URI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {
        "com.example.task_management.controller",
        "com.example.task_management.service",
        "com.example.task_management.repository",
        "com.example.task_management.model",
        "com.example.task_management.config"
})
@EnableJpaRepositories(basePackages = "com.example.task_management.repository")
public class TaskManagementApplication {

    public static void main(String[] args) {
        System.out.println("  Application starting...");

        SpringApplication.run(TaskManagementApplication.class, args);

        System.out.println("  Application started. Trying to open browser...");

        try {
            Desktop.getDesktop().browse(new URI("http://localhost:8080"));
        } catch (Exception e) {
            System.err.println("  Failed to open browser: " + e.getMessage());
        }
    }
}
