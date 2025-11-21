package com.rentkar.dto;

import com.rentkar.model.RequestStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BorrowRequestDTO {
    
    private Long id;
    private ItemDTO item;
    private UserDTO borrower;
    private UserDTO lender;
    private RequestStatus status;
    private String requestMessage;
    private String responseMessage;
    private LocalDate borrowDate;
    private LocalDate returnDate;
    private LocalDateTime returnedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public BorrowRequestDTO() {}
    
    public BorrowRequestDTO(Long id, ItemDTO item, UserDTO borrower, UserDTO lender,
                           RequestStatus status, String requestMessage, String responseMessage,
                           LocalDate borrowDate, LocalDate returnDate, LocalDateTime returnedAt,
                           LocalDateTime completedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.item = item;
        this.borrower = borrower;
        this.lender = lender;
        this.status = status;
        this.requestMessage = requestMessage;
        this.responseMessage = responseMessage;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.returnedAt = returnedAt;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public ItemDTO getItem() {
        return item;
    }
    
    public void setItem(ItemDTO item) {
        this.item = item;
    }
    
    public UserDTO getBorrower() {
        return borrower;
    }
    
    public void setBorrower(UserDTO borrower) {
        this.borrower = borrower;
    }
    
    public UserDTO getLender() {
        return lender;
    }
    
    public void setLender(UserDTO lender) {
        this.lender = lender;
    }
    
    public RequestStatus getStatus() {
        return status;
    }
    
    public void setStatus(RequestStatus status) {
        this.status = status;
    }
    
    public String getRequestMessage() {
        return requestMessage;
    }
    
    public void setRequestMessage(String requestMessage) {
        this.requestMessage = requestMessage;
    }
    
    public String getResponseMessage() {
        return responseMessage;
    }
    
    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }
    
    public LocalDate getBorrowDate() {
        return borrowDate;
    }
    
    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }
    
    public LocalDate getReturnDate() {
        return returnDate;
    }
    
    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }
    
    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }
    
    public void setReturnedAt(LocalDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
