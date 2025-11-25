package com.projectAnupam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProjectAnupamApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectAnupamApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("  PROJECT ANUPAM - Backend Started!");
        System.out.println("  Server: http://localhost:8080");
        System.out.println("  API Endpoint: http://localhost:8080/api/products");
        System.out.println("========================================\n");
    }
}