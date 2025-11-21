package com.rentkar.dto;

import com.rentkar.validation.FutureOrPresent;
import com.rentkar.validation.ValidDateRange;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@ValidDateRange(message = "Return date must be after borrow date")
public class CreateBorrowRequestDTO {
    
    @NotNull(message = "Borrow date is required")
    @FutureOrPresent(message = "Borrow date cannot be in the past")
    private LocalDate borrowDate;
    
    @NotNull(message = "Return date is required")
    private LocalDate returnDate;
    
    @Size(max = 500, message = "Request message must not exceed 500 characters")
    private String requestMessage;
    
    public CreateBorrowRequestDTO() {}
    
    public CreateBorrowRequestDTO(LocalDate borrowDate, LocalDate returnDate, String requestMessage) {
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.requestMessage = requestMessage;
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
    
    public String getRequestMessage() {
        return requestMessage;
    }
    
    public void setRequestMessage(String requestMessage) {
        this.requestMessage = requestMessage;
    }
}
