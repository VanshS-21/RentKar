package com.rentkar.dto;

import jakarta.validation.constraints.NotBlank;

public class AIGenerationRequest {
    
    @NotBlank(message = "Item name is required")
    private String itemName;
    
    @NotBlank(message = "Category is required")
    private String category;
    
    private String additionalInfo;
    
    private String condition;
    
    private String specifications;
    
    public AIGenerationRequest() {}
    
    public AIGenerationRequest(String itemName, String category, String additionalInfo, 
                               String condition, String specifications) {
        this.itemName = itemName;
        this.category = category;
        this.additionalInfo = additionalInfo;
        this.condition = condition;
        this.specifications = specifications;
    }
    
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }
    
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    
    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }
}
