package com.example.app;

import com.example.app.service.GreetingService;
import com.example.app.controller.AppController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GreetingService greetingService;

    // --- Unit Tests ---

    @Test
    @DisplayName("GreetingService returns correct message")
    void greetingService_returnsCorrectMessage() {
        String result = greetingService.greet("Alice");
        assertEquals("Hello, Alice! Welcome to the CI/CD pipeline demo.", result);
    }

    @Test
    @DisplayName("GreetingService throws on empty name")
    void greetingService_throwsOnEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> greetingService.greet(""));
    }

    @Test
    @DisplayName("GreetingService throws on null name")
    void greetingService_throwsOnNullName() {
        assertThrows(IllegalArgumentException.class, () -> greetingService.greet(null));
    }

    // --- Integration Tests ---

    @Test
    @DisplayName("GET /api/health returns UP")
    void healthEndpoint_returnsUp() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("GET /api/greet/{name} returns greeting")
    void greetEndpoint_returnsGreeting() throws Exception {
        mockMvc.perform(get("/api/greet/Bob"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("OK"))
            .andExpect(jsonPath("$.message").value("Hello, Bob! Welcome to the CI/CD pipeline demo."));
    }

    @Test
    @DisplayName("GET /api/version returns version info")
    void versionEndpoint_returnsVersion() throws Exception {
        mockMvc.perform(get("/api/version"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("OK"));
    }

    @Test
    @DisplayName("Application context loads successfully")
    void contextLoads() {
        assertNotNull(greetingService);
    }
}
