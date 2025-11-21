package com.rentkar.service;

import com.rentkar.dto.BorrowRequestDTO;
import com.rentkar.dto.ItemDTO;
import com.rentkar.dto.ItemOwnerDTO;
import com.rentkar.dto.UserDTO;
import com.rentkar.model.BorrowRequest;
import org.springframework.stereotype.Component;

@Component
public class BorrowRequestMapper {
    
    /**
     * Convert BorrowRequest entity to BorrowRequestDTO
     */
    public BorrowRequestDTO toDTO(BorrowRequest request) {
        if (request == null) {
            return null;
        }
        
        // Convert item to ItemDTO
        ItemDTO itemDTO = convertItemToDTO(request);
        
        // Convert borrower to UserDTO
        UserDTO borrowerDTO = convertUserToDTO(request.getBorrower());
        
        // Convert lender to UserDTO
        UserDTO lenderDTO = convertUserToDTO(request.getLender());
        
        return new BorrowRequestDTO(
                request.getId(),
                itemDTO,
                borrowerDTO,
                lenderDTO,
                request.getStatus(),
                request.getRequestMessage(),
                request.getResponseMessage(),
                request.getBorrowDate(),
                request.getReturnDate(),
                request.getReturnedAt(),
                request.getCompletedAt(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
    
    private ItemDTO convertItemToDTO(BorrowRequest request) {
        ItemOwnerDTO ownerDTO = new ItemOwnerDTO(
                request.getItem().getOwner().getId(),
                request.getItem().getOwner().getUsername(),
                request.getItem().getOwner().getFullName(),
                request.getItem().getOwner().getEmail(),
                request.getItem().getOwner().getPhone()
        );
        
        return new ItemDTO(
                request.getItem().getId(),
                request.getItem().getTitle(),
                request.getItem().getDescription(),
                request.getItem().getCategory(),
                request.getItem().getImageUrl(),
                request.getItem().getStatus(),
                ownerDTO,
                request.getItem().getCreatedAt(),
                request.getItem().getUpdatedAt()
        );
    }
    
    private UserDTO convertUserToDTO(com.rentkar.model.User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
