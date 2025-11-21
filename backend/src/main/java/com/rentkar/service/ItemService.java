package com.rentkar.service;

import com.rentkar.dto.CreateItemRequest;
import com.rentkar.dto.ItemDTO;
import com.rentkar.dto.UpdateItemRequest;
import com.rentkar.model.ItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemService {
    
    ItemDTO createItem(CreateItemRequest request, Long ownerId);
    
    ItemDTO getItemById(Long itemId);
    
    Page<ItemDTO> getAllItems(ItemStatus status, String category, String search, Pageable pageable);
    
    Page<ItemDTO> getItemsByOwner(Long ownerId, Pageable pageable);
    
    ItemDTO updateItem(Long itemId, UpdateItemRequest request, Long userId);
    
    void deleteItem(Long itemId, Long userId);
}
