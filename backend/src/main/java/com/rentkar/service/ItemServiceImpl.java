package com.rentkar.service;

import com.rentkar.dto.CreateItemRequest;
import com.rentkar.dto.ItemDTO;
import com.rentkar.dto.ItemOwnerDTO;
import com.rentkar.dto.UpdateItemRequest;
import com.rentkar.model.Item;
import com.rentkar.model.ItemStatus;
import com.rentkar.model.User;
import com.rentkar.repository.ItemRepository;
import com.rentkar.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ItemServiceImpl implements ItemService {
    
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    public ItemDTO createItem(CreateItemRequest request, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + ownerId));
        
        Item item = new Item();
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setImageUrl(request.getImageUrl());
        item.setStatus(ItemStatus.AVAILABLE);
        item.setOwner(owner);
        
        Item savedItem = itemRepository.save(item);
        return convertToDTO(savedItem);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ItemDTO getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + itemId));
        return convertToDTO(item);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ItemDTO> getAllItems(ItemStatus status, String category, String search, Pageable pageable) {
        // Default to AVAILABLE status if not specified
        ItemStatus filterStatus = (status != null) ? status : ItemStatus.AVAILABLE;
        
        Page<Item> items = itemRepository.findWithFilters(filterStatus, category, search, pageable);
        return items.map(this::convertToDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ItemDTO> getItemsByOwner(Long ownerId, Pageable pageable) {
        Page<Item> items = itemRepository.findByOwnerId(ownerId, pageable);
        return items.map(this::convertToDTO);
    }
    
    @Override
    public ItemDTO updateItem(Long itemId, UpdateItemRequest request, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + itemId));
        
        // Check authorization - only owner can update
        if (!item.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to update this item");
        }
        
        // Update fields if provided
        if (request.getTitle() != null) {
            item.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            item.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            item.setCategory(request.getCategory());
        }
        if (request.getImageUrl() != null) {
            item.setImageUrl(request.getImageUrl());
        }
        if (request.getStatus() != null) {
            item.setStatus(request.getStatus());
        }
        
        Item updatedItem = itemRepository.save(item);
        return convertToDTO(updatedItem);
    }
    
    @Override
    public void deleteItem(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + itemId));
        
        // Check authorization - only owner can delete
        if (!item.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to delete this item");
        }
        
        itemRepository.delete(item);
    }
    
    private ItemDTO convertToDTO(Item item) {
        ItemOwnerDTO ownerDTO = new ItemOwnerDTO(
                item.getOwner().getId(),
                item.getOwner().getUsername(),
                item.getOwner().getFullName(),
                item.getOwner().getEmail(),
                item.getOwner().getPhone()
        );
        
        return new ItemDTO(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getCategory(),
                item.getImageUrl(),
                item.getStatus(),
                ownerDTO,
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
