package com.rentkar.dto;

public class ItemOwnerDTO {
    
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    
    public ItemOwnerDTO() {}
    
    public ItemOwnerDTO(Long id, String username, String fullName, String email, String phone) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
