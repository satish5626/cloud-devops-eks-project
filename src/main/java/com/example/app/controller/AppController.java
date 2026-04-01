package com.example.app.controller;

import com.example.app.model.ApiResponse;
import com.example.app.service.GreetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AppController {

    @Autowired
    private GreetingService greetingService;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse> health() {
        return ResponseEntity.ok(new ApiResponse("UP", "Application is running"));
    }

    @GetMapping("/greet/{name}")
    public ResponseEntity<ApiResponse> greet(@PathVariable String name) {
        String message = greetingService.greet(name);
        return ResponseEntity.ok(new ApiResponse("OK", message));
    }

    @GetMapping("/version")
    public ResponseEntity<ApiResponse> version() {
        return ResponseEntity.ok(new ApiResponse("OK", "v1.0.0 - Spring Boot CI/CD Demo"));
    }
}
