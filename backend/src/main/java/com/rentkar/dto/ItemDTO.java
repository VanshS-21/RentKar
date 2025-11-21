package com.rentkar.dto;

import com.rentkar.model.ItemStatus;

import java.time.LocalDateTime;

public class ItemDTO {
    
    private Long id;
    private String title;
    private String description;
    private String category;
    private String imageUrl;
    private ItemStatus status;
    private ItemOwnerDTO owner;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public ItemDTO() {}
    
    public ItemDTO(Long id, String title, String description, String category, String imageUrl, 
                   ItemStatus status, ItemOwnerDTO owner, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.imageUrl = imageUrl;
        this.status = status;
        this.owner = owner;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
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
    
    public ItemOwnerDTO getOwner() { return owner; }
    public void setOwner(ItemOwnerDTO owner) { this.owner = owner; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
