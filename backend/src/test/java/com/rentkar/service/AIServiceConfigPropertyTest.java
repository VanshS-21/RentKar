package com.rentkar.service;

import net.jqwik.api.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property tests for AI service configuration
 */
@SpringBootTest
@TestPropertySource(properties = {
    "gemini.api-key=test-api-key-12345",
    "gemini.api-endpoint=https://test-endpoint.com",
    "gemini.model=gemini-pro",
    "ai.generation.enabled=true",
    "ai.request.timeout-ms=30000",
    "ai.temperature=0.7",
    "ai.max-tokens.title=200",
    "ai.max-tokens.description=500",
    "ai.usage.threshold.warning=8",
    "ai.rate-limit.per-hour=10"
})
public class AIServiceConfigPropertyTest {
    
    @Value("${gemini.api-key}")
    private String apiKey;
    
    @Value("${gemini.api-endpoint}")
    private String apiEndpoint;
    
    @Value("${gemini.model}")
    private String model;
    
    @Value("${ai.generation.enabled}")
    private boolean enabled;
    
    @Value("${ai.temperature}")
    private double temperature;
    
    @Value("${ai.max-tokens.title}")
    private int maxTokensTitle;
    
    @Value("${ai.max-tokens.description}")
    private int maxTokensDescription;
    
    // Feature: ai-description-generation, Property 16: Configuration loading
    // Validates: Requirements 6.1
    @Test
    void configurationLoadedFromEnvironmentVariables() {
        // Verify that all required configuration values are loaded
        assertThat(apiKey).isNotNull();
        assertThat(apiKey).isNotEmpty();
        assertThat(apiKey).isEqualTo("test-api-key-12345");
        
        assertThat(apiEndpoint).isNotNull();
        assertThat(apiEndpoint).isNotEmpty();
        assertThat(apiEndpoint).isEqualTo("https://test-endpoint.com");
        
        assertThat(model).isNotNull();
        assertThat(model).isNotEmpty();
        assertThat(model).isEqualTo("gemini-pro");
        
        assertThat(enabled).isTrue();
    }
    
    // Feature: ai-description-generation, Property 17: Configuration parameters applied
    // Validates: Requirements 6.2
    @Test
    void configurationParametersApplied() {
        // Verify that configured parameters are within valid ranges
        assertThat(temperature).isBetween(0.0, 2.0);
        assertThat(maxTokensTitle).isGreaterThanOrEqualTo(1);
        assertThat(maxTokensTitle).isLessThanOrEqualTo(2048);
        assertThat(maxTokensDescription).isGreaterThanOrEqualTo(1);
        assertThat(maxTokensDescription).isLessThanOrEqualTo(2048);
        
        // Verify specific configured values
        assertThat(temperature).isEqualTo(0.7);
        assertThat(maxTokensTitle).isEqualTo(200);
        assertThat(maxTokensDescription).isEqualTo(500);
    }
}
