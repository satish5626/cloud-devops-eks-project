package com.example.app.service;

import org.springframework.stereotype.Service;

@Service
public class GreetingService {

    public String greet(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name must not be empty");
        }
        return "Hello, " + name + "! Welcome to the CI/CD pipeline demo.";
    }
}
