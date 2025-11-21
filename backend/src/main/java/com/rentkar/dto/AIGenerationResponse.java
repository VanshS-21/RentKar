package com.rentkar.dto;

public class AIGenerationResponse {
    
    private String content;
    
    private int tokenCount;
    
    private long responseTimeMs;
    
    private boolean success;
    
    private String errorMessage;
    
    public AIGenerationResponse() {}
    
    public AIGenerationResponse(String content, int tokenCount, long responseTimeMs, 
                                boolean success, String errorMessage) {
        this.content = content;
        this.tokenCount = tokenCount;
        this.responseTimeMs = responseTimeMs;
        this.success = success;
        this.errorMessage = errorMessage;
    }
    
    // Success response constructor
    public AIGenerationResponse(String content, int tokenCount, long responseTimeMs) {
        this.content = content;
        this.tokenCount = tokenCount;
        this.responseTimeMs = responseTimeMs;
        this.success = true;
        this.errorMessage = null;
    }
    
    // Error response constructor
    public AIGenerationResponse(String errorMessage) {
        this.content = null;
        this.tokenCount = 0;
        this.responseTimeMs = 0;
        this.success = false;
        this.errorMessage = errorMessage;
    }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public int getTokenCount() { return tokenCount; }
    public void setTokenCount(int tokenCount) { this.tokenCount = tokenCount; }
    
    public long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
