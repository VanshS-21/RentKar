package com.rentkar.dto;

import com.rentkar.model.ItemStatus;
import jakarta.validation.constraints.Size;

public class UpdateItemRequest {
    
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;
    
    private String description;
    
    private String category;
    
    private String imageUrl;
    
    private ItemStatus status;
    
    public UpdateItemRequest() {}
    
    public UpdateItemRequest(String title, String description, String category, String imageUrl, ItemStatus status) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.imageUrl = imageUrl;
        this.status = status;
    }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }
}
