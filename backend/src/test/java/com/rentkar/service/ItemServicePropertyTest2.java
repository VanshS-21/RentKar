package com.rentkar.service;

import com.rentkar.dto.CreateItemRequest;
import com.rentkar.dto.ItemDTO;
import com.rentkar.dto.UpdateItemRequest;
import com.rentkar.model.Item;
import com.rentkar.model.ItemStatus;
import com.rentkar.model.Role;
import com.rentkar.model.User;
import com.rentkar.repository.ItemRepository;
import com.rentkar.repository.UserRepository;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Additional property-based tests for ItemService (Properties 13-39)
 */
public class ItemServicePropertyTest2 {
    
    // Feature: item-management, Property 13: Search matches title and description
    // Validates: Requirements 4.1, 4.3
    @Property(tries = 100)
    void searchMatchesTitleAndDescription(
            @ForAll("searchKeyword") String keyword,
            @ForAll("itemListWithKeyword") List<Item> items) {
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        // Filter items that contain the keyword in title or description
        List<Item> matchingItems = items.stream()
                .filter(i -> i.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                           (i.getDescription() != null && i.getDescription().toLowerCase().contains(keyword.toLowerCase())))
                .toList();
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> page = new PageImpl<>(matchingItems, pageable, matchingItems.size());
        
        when(mockItemRepo.findWithFilters(any(), any(), any(), any()))
                .thenReturn(page);
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        Page<ItemDTO> result = service.getAllItems(ItemStatus.AVAILABLE, null, keyword, pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent()).allMatch(item -> 
            item.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
            (item.getDescription() != null && item.getDescription().toLowerCase().contains(keyword.toLowerCase()))
        );
    }
    
    // Feature: item-management, Property 14: Empty search returns all items
    // Validates: Requirements 4.2
    @Property(tries = 100)
    void emptySearchReturnsAllItems(@ForAll("itemList") List<Item> items) {
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        Pageable pageable = PageRequest.of(0, 10);
        List<Item> pageItems = items.stream().limit(10).toList();
        Page<Item> page = new PageImpl<>(pageItems, pageable, items.size());
        
        when(mockItemRepo.findWithFilters(any(), any(), any(), any()))
                .thenReturn(page);
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        // Test with null keyword
        Page<ItemDTO> result1 = service.getAllItems(ItemStatus.AVAILABLE, null, null, pageable);
        assertThat(result1).isNotNull();
        assertThat(result1.getContent().size()).isEqualTo(Math.min(10, items.size()));
        
        // Test with empty keyword
        Page<ItemDTO> result2 = service.getAllItems(ItemStatus.AVAILABLE, null, "", pageable);
        assertThat(result2).isNotNull();
    }
    
    // Feature: item-management, Property 15: Category filter returns matching items
    // Validates: Requirements 5.1
    @Property(tries = 100)
    void categoryFilterReturnsMatchingItems(
            @ForAll("validCategory") String category,
            @ForAll("itemListWithCategories") List<Item> items) {
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        List<Item> matchingItems = items.stream()
                .filter(i -> i.getCategory().equals(category))
                .toList();
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> page = new PageImpl<>(matchingItems, pageable, matchingItems.size());
        
        when(mockItemRepo.findWithFilters(any(), any(), any(), any()))
                .thenReturn(page);
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        Page<ItemDTO> result = service.getAllItems(ItemStatus.AVAILABLE, category, null, pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent()).allMatch(item -> item.getCategory().equals(category));
    }
    
    // Feature: item-management, Property 16: Combined filters work together
    // Validates: Requirements 5.2
    @Property(tries = 100)
    void combinedFiltersWorkTogether(
            @ForAll("validCategory") String category,
            @ForAll("searchKeyword") String keyword,
            @ForAll("itemListWithCategories") List<Item> items) {
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        List<Item> matchingItems = items.stream()
                .filter(i -> i.getCategory().equals(category))
                .filter(i -> i.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                           (i.getDescription() != null && i.getDescription().toLowerCase().contains(keyword.toLowerCase())))
                .toList();
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> page = new PageImpl<>(matchingItems, pageable, matchingItems.size());
        
        when(mockItemRepo.findWithFilters(any(), any(), any(), any()))
                .thenReturn(page);
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        Page<ItemDTO> result = service.getAllItems(ItemStatus.AVAILABLE, category, keyword, pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent()).allMatch(item -> 
            item.getCategory().equals(category) &&
            (item.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
             (item.getDescription() != null && item.getDescription().toLowerCase().contains(keyword.toLowerCase())))
        );
    }
    
    // Feature: item-management, Property 17: Status filter returns matching items
    // Validates: Requirements 6.1, 6.2, 6.3
    @Property(tries = 100)
    void statusFilterReturnsMatchingItems(
            @ForAll("itemStatus") ItemStatus status,
            @ForAll("itemListWithStatuses") List<Item> items) {
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        List<Item> matchingItems = items.stream()
                .filter(i -> i.getStatus() == status)
                .toList();
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> page = new PageImpl<>(matchingItems, pageable, matchingItems.size());
        
        when(mockItemRepo.findWithFilters(any(), any(), any(), any()))
                .thenReturn(page);
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        Page<ItemDTO> result = service.getAllItems(status, null, null, pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent()).allMatch(item -> item.getStatus() == status);
    }
    
    // Feature: item-management, Property 18: Multiple filters combine correctly
    // Validates: Requirements 6.4
    @Property(tries = 100)
    void multipleFiltersCombineCorrectly(
            @ForAll("itemStatus") ItemStatus status,
            @ForAll("validCategory") String category,
            @ForAll("searchKeyword") String keyword,
            @ForAll("itemListWithStatuses") List<Item> items) {
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        List<Item> matchingItems = items.stream()
                .filter(i -> i.getStatus() == status)
                .filter(i -> i.getCategory().equals(category))
                .filter(i -> i.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                           (i.getDescription() != null && i.getDescription().toLowerCase().contains(keyword.toLowerCase())))
                .toList();
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> page = new PageImpl<>(matchingItems, pageable, matchingItems.size());
        
        when(mockItemRepo.findWithFilters(any(), any(), any(), any()))
                .thenReturn(page);
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        Page<ItemDTO> result = service.getAllItems(status, category, keyword, pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent()).allMatch(item -> 
            item.getStatus() == status &&
            item.getCategory().equals(category) &&
            (item.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
             (item.getDescription() != null && item.getDescription().toLowerCase().contains(keyword.toLowerCase())))
        );
    }
    
    // Feature: item-management, Property 19: Item details include complete information
    // Validates: Requirements 7.1, 7.3, 7.4, 7.5
    @Property(tries = 100)
    void itemDetailsIncludeCompleteInformation(
            @ForAll("validUserId") Long itemId,
            @ForAll("validTitle") String title,
            @ForAll("validDescription") String description,
            @ForAll("validCategory") String category,
            @ForAll("validImageUrl") String imageUrl,
            @ForAll("validUserId") Long ownerId) {
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        User owner = createUser(ownerId, "owner" + ownerId, "owner" + ownerId + "@test.com");
        Item item = createItem(itemId, title, description, category, imageUrl, ItemStatus.AVAILABLE, owner);
        
        when(mockItemRepo.findById(itemId)).thenReturn(Optional.of(item));
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        ItemDTO result = service.getItemById(itemId);
        
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(itemId);
        assertThat(result.getTitle()).isEqualTo(title);
        assertThat(result.getDescription()).isEqualTo(description);
        assertThat(result.getCategory()).isEqualTo(category);
        assertThat(result.getImageUrl()).isEqualTo(imageUrl);
        assertThat(result.getStatus()).isEqualTo(ItemStatus.AVAILABLE);
        assertThat(result.getOwner()).isNotNull();
        assertThat(result.getOwner().getId()).isEqualTo(ownerId);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }
    
    // Feature: item-management, Property 20: Non-existent item returns 404
    // Validates: Requirements 7.2
    @Property(tries = 100)
    void nonExistentItemReturns404(@ForAll("validUserId") Long itemId) {
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        when(mockItemRepo.findById(itemId)).thenReturn(Optional.empty());
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        assertThatThrownBy(() -> service.getItemById(itemId))
                .isInstanceOf(EntityNotFoundException.class);
    }
    
    // Feature: item-management, Property 21: Owner can update their item
    // Validates: Requirements 8.1
    @Property(tries = 100)
    void ownerCanUpdateTheirItem(
            @ForAll("validUserId") Long itemId,
            @ForAll("validUserId") Long ownerId,
            @ForAll("validTitle") String newTitle,
            @ForAll("validDescription") String newDescription) {
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        User owner = createUser(ownerId, "owner" + ownerId, "owner" + ownerId + "@test.com");
        Item item = createItem(itemId, "Old Title", "Old Description", "Electronics", 
                              "https://example.com/old.jpg", ItemStatus.AVAILABLE, owner);
        
        when(mockItemRepo.findById(itemId)).thenReturn(Optional.of(item));
        when(mockItemRepo.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        UpdateItemRequest request = new UpdateItemRequest();
        request.setTitle(newTitle);
        request.setDescription(newDescription);
        
        ItemDTO result = service.updateItem(itemId, request, ownerId);
        
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(newTitle);
        assertThat(result.getDescription()).isEqualTo(newDescription);
    }
    
    // Feature: item-management, Property 22: Non-owner cannot update item
    // Validates: Requirements 8.2
    @Property(tries = 100)
    void nonOwnerCannotUpdateItem(
            @ForAll("validUserId") Long itemId,
            @ForAll("validUserId") Long ownerId,
            @ForAll("validUserId") Long nonOwnerId) {
        
        Assume.that(!ownerId.equals(nonOwnerId));
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        User owner = createUser(ownerId, "owner" + ownerId, "owner" + ownerId + "@test.com");
        Item item = createItem(itemId, "Title", "Description", "Electronics", 
                              "https://example.com/img.jpg", ItemStatus.AVAILABLE, owner);
        
        when(mockItemRepo.findById(itemId)).thenReturn(Optional.of(item));
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        UpdateItemRequest request = new UpdateItemRequest();
        request.setTitle("New Title");
        
        assertThatThrownBy(() -> service.updateItem(itemId, request, nonOwnerId))
                .isInstanceOf(AccessDeniedException.class);
    }
    
    // Feature: item-management, Property 23: Title validation on update
    // Validates: Requirements 8.3
    @Property(tries = 100)
    void titleValidationOnUpdate(@ForAll("invalidTitle") String invalidTitle) {
        
        UpdateItemRequest request = new UpdateItemRequest();
        request.setTitle(invalidTitle);
        
        jakarta.validation.ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory();
        jakarta.validation.Validator validator = factory.getValidator();
        
        Set<jakarta.validation.ConstraintViolation<UpdateItemRequest>> violations = validator.validate(request);
        
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> 
            v.getPropertyPath().toString().equals("title") &&
            v.getMessage().toLowerCase().contains("between")
        );
    }
    
    // Feature: item-management, Property 24: Status validation on update
    // Validates: Requirements 8.4
    @Property(tries = 100)
    void statusValidationOnUpdate(
            @ForAll("validUserId") Long itemId,
            @ForAll("validUserId") Long ownerId,
            @ForAll("itemStatus") ItemStatus newStatus) {
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        User owner = createUser(ownerId, "owner" + ownerId, "owner" + ownerId + "@test.com");
        Item item = createItem(itemId, "Title", "Description", "Electronics", 
                              "https://example.com/img.jpg", ItemStatus.AVAILABLE, owner);
        
        when(mockItemRepo.findById(itemId)).thenReturn(Optional.of(item));
        when(mockItemRepo.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        UpdateItemRequest request = new UpdateItemRequest();
        request.setStatus(newStatus);
        
        ItemDTO result = service.updateItem(itemId, request, ownerId);
        
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(newStatus);
    }
    
    // Feature: item-management, Property 25: Update modifies timestamp
    // Validates: Requirements 8.5
    @Property(tries = 100)
    void updateModifiesTimestamp(
            @ForAll("validUserId") Long itemId,
            @ForAll("validUserId") Long ownerId,
            @ForAll("validTitle") String newTitle) {
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        User owner = createUser(ownerId, "owner" + ownerId, "owner" + ownerId + "@test.com");
        LocalDateTime oldTimestamp = LocalDateTime.now().minusDays(1);
        Item item = createItem(itemId, "Old Title", "Description", "Electronics", 
                              "https://example.com/img.jpg", ItemStatus.AVAILABLE, owner);
        setItemField(item, "updatedAt", oldTimestamp);
        
        when(mockItemRepo.findById(itemId)).thenReturn(Optional.of(item));
        when(mockItemRepo.save(any(Item.class))).thenAnswer(inv -> {
            Item savedItem = inv.getArgument(0);
            setItemField(savedItem, "updatedAt", LocalDateTime.now());
            return savedItem;
        });
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        UpdateItemRequest request = new UpdateItemRequest();
        request.setTitle(newTitle);
        
        ItemDTO result = service.updateItem(itemId, request, ownerId);
        
        assertThat(result).isNotNull();
        assertThat(result.getUpdatedAt()).isAfter(oldTimestamp);
    }
    
    // Feature: item-management, Property 26: Owner can delete their item
    // Validates: Requirements 9.1, 9.4
    @Property(tries = 100)
    void ownerCanDeleteTheirItem(
            @ForAll("validUserId") Long itemId,
            @ForAll("validUserId") Long ownerId) {
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        User owner = createUser(ownerId, "owner" + ownerId, "owner" + ownerId + "@test.com");
        Item item = createItem(itemId, "Title", "Description", "Electronics", 
                              "https://example.com/img.jpg", ItemStatus.AVAILABLE, owner);
        
        when(mockItemRepo.findById(itemId)).thenReturn(Optional.of(item));
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        // Should not throw exception
        service.deleteItem(itemId, ownerId);
    }
    
    // Feature: item-management, Property 27: Non-owner cannot delete item
    // Validates: Requirements 9.2
    @Property(tries = 100)
    void nonOwnerCannotDeleteItem(
            @ForAll("validUserId") Long itemId,
            @ForAll("validUserId") Long ownerId,
            @ForAll("validUserId") Long nonOwnerId) {
        
        Assume.that(!ownerId.equals(nonOwnerId));
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        User owner = createUser(ownerId, "owner" + ownerId, "owner" + ownerId + "@test.com");
        Item item = createItem(itemId, "Title", "Description", "Electronics", 
                              "https://example.com/img.jpg", ItemStatus.AVAILABLE, owner);
        
        when(mockItemRepo.findById(itemId)).thenReturn(Optional.of(item));
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        assertThatThrownBy(() -> service.deleteItem(itemId, nonOwnerId))
                .isInstanceOf(AccessDeniedException.class);
    }
    
    // Feature: item-management, Property 28: Deleted item returns 404
    // Validates: Requirements 9.5
    @Property(tries = 100)
    void deletedItemReturns404(@ForAll("validUserId") Long itemId) {
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        when(mockItemRepo.findById(itemId)).thenReturn(Optional.empty());
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        assertThatThrownBy(() -> service.getItemById(itemId))
                .isInstanceOf(EntityNotFoundException.class);
    }
    
    // Feature: item-management, Property 29: User can view their own items
    // Validates: Requirements 10.1, 10.2
    @Property(tries = 100)
    void userCanViewTheirOwnItems(
            @ForAll("validUserId") Long ownerId,
            @ForAll("itemListForOwner") List<Item> items) {
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        // Set all items to have the same owner
        User owner = createUser(ownerId, "owner" + ownerId, "owner" + ownerId + "@test.com");
        items.forEach(item -> setItemField(item, "owner", owner));
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> page = new PageImpl<>(items, pageable, items.size());
        
        when(mockItemRepo.findByOwnerId(ownerId, pageable)).thenReturn(page);
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        Page<ItemDTO> result = service.getItemsByOwner(ownerId, pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent()).allMatch(item -> item.getOwner().getId().equals(ownerId));
    }
    
    // Feature: item-management, Property 30: User items support pagination
    // Validates: Requirements 10.3
    @Property(tries = 100)
    void userItemsSupportPagination(
            @ForAll("validUserId") Long ownerId,
            @ForAll("itemListForOwner") List<Item> items,
            @ForAll @IntRange(min = 1, max = 20) int pageSize) {
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        User owner = createUser(ownerId, "owner" + ownerId, "owner" + ownerId + "@test.com");
        items.forEach(item -> setItemField(item, "owner", owner));
        
        Pageable pageable = PageRequest.of(0, pageSize);
        List<Item> pageItems = items.stream().limit(pageSize).toList();
        Page<Item> page = new PageImpl<>(pageItems, pageable, items.size());
        
        when(mockItemRepo.findByOwnerId(ownerId, pageable)).thenReturn(page);
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        Page<ItemDTO> result = service.getItemsByOwner(ownerId, pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent().size()).isLessThanOrEqualTo(pageSize);
        assertThat(result.getTotalElements()).isEqualTo(items.size());
    }
    
    // Feature: item-management, Property 31: User with no items returns empty array
    // Validates: Requirements 10.4
    @Property(tries = 100)
    void userWithNoItemsReturnsEmptyArray(@ForAll("validUserId") Long ownerId) {
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> page = new PageImpl<>(List.of(), pageable, 0);
        
        when(mockItemRepo.findByOwnerId(ownerId, pageable)).thenReturn(page);
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        Page<ItemDTO> result = service.getItemsByOwner(ownerId, pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }
    
    // Feature: item-management, Property 32: User items ordered by date
    // Validates: Requirements 10.5
    @Property(tries = 100)
    void userItemsOrderedByDate(
            @ForAll("validUserId") Long ownerId,
            @ForAll("itemListForOwner") List<Item> items) {
        
        Assume.that(items.size() >= 2);
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        User owner = createUser(ownerId, "owner" + ownerId, "owner" + ownerId + "@test.com");
        
        // Set different creation dates
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            setItemField(item, "owner", owner);
            setItemField(item, "createdAt", LocalDateTime.now().minusDays(items.size() - i));
        }
        
        // Sort by creation date descending (newest first)
        List<Item> sortedItems = items.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> page = new PageImpl<>(sortedItems, pageable, sortedItems.size());
        
        when(mockItemRepo.findByOwnerId(ownerId, pageable)).thenReturn(page);
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        Page<ItemDTO> result = service.getItemsByOwner(ownerId, pageable);
        
        assertThat(result).isNotNull();
        // Verify items are in descending order by creation date
        List<LocalDateTime> dates = result.getContent().stream()
                .map(ItemDTO::getCreatedAt)
                .toList();
        
        for (int i = 0; i < dates.size() - 1; i++) {
            assertThat(dates.get(i)).isAfterOrEqualTo(dates.get(i + 1));
        }
    }
    
    // Feature: item-management, Property 33: Required fields are validated
    // Validates: Requirements 11.1
    @Property(tries = 100)
    void requiredFieldsAreValidated(@ForAll("validCategory") String category) {
        
        CreateItemRequest request = new CreateItemRequest();
        request.setTitle(null); // Missing required field
        request.setCategory(category);
        
        jakarta.validation.ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory();
        jakarta.validation.Validator validator = factory.getValidator();
        
        Set<jakarta.validation.ConstraintViolation<CreateItemRequest>> violations = validator.validate(request);
        
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("title"));
    }
    
    // Feature: item-management, Property 34: Field formats are validated
    // Validates: Requirements 11.2
    @Property(tries = 100)
    void fieldFormatsAreValidated(
            @ForAll("invalidTitle") String invalidTitle,
            @ForAll("validCategory") String category) {
        
        CreateItemRequest request = new CreateItemRequest();
        request.setTitle(invalidTitle);
        request.setCategory(category);
        
        jakarta.validation.ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory();
        jakarta.validation.Validator validator = factory.getValidator();
        
        Set<jakarta.validation.ConstraintViolation<CreateItemRequest>> violations = validator.validate(request);
        
        assertThat(violations).isNotEmpty();
    }
    
    // Feature: item-management, Property 35: Validation errors are specific
    // Validates: Requirements 11.3
    @Property(tries = 100)
    void validationErrorsAreSpecific(@ForAll("shortTitle") String shortTitle) {
        
        CreateItemRequest request = new CreateItemRequest();
        request.setTitle(shortTitle);
        
        jakarta.validation.ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory();
        jakarta.validation.Validator validator = factory.getValidator();
        
        Set<jakarta.validation.ConstraintViolation<CreateItemRequest>> violations = validator.validate(request);
        
        assertThat(violations).isNotEmpty();
        assertThat(violations).allMatch(v -> 
            v.getPropertyPath().toString().equals("title") &&
            v.getMessage() != null &&
            !v.getMessage().isEmpty()
        );
    }
    
    // Feature: item-management, Property 36: Item stores owner ID
    // Validates: Requirements 12.1
    @Property(tries = 100)
    void itemStoresOwnerId(
            @ForAll("validTitle") String title,
            @ForAll("validUserId") Long ownerId) {
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        Map<Long, Item> itemDb = new HashMap<>();
        AtomicLong idCounter = new AtomicLong(1);
        
        User owner = createUser(ownerId, "owner" + ownerId, "owner" + ownerId + "@test.com");
        
        when(mockUserRepo.findById(ownerId)).thenReturn(Optional.of(owner));
        when(mockItemRepo.save(any(Item.class))).thenAnswer(inv -> {
            Item item = inv.getArgument(0);
            if (item.getId() == null) {
                setItemField(item, "id", idCounter.getAndIncrement());
            }
            setItemField(item, "createdAt", LocalDateTime.now());
            setItemField(item, "updatedAt", LocalDateTime.now());
            itemDb.put(item.getId(), item);
            return item;
        });
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        CreateItemRequest request = new CreateItemRequest();
        request.setTitle(title);
        request.setDescription("Description");
        request.setCategory("Electronics");
        request.setImageUrl("https://example.com/img.jpg");
        
        ItemDTO result = service.createItem(request, ownerId);
        
        Item savedItem = itemDb.get(result.getId());
        assertThat(savedItem).isNotNull();
        assertThat(savedItem.getOwner().getId()).isEqualTo(ownerId);
    }
    
    // Feature: item-management, Property 37: Item responses include owner info
    // Validates: Requirements 12.2
    @Property(tries = 100)
    void itemResponsesIncludeOwnerInfo(
            @ForAll("validUserId") Long itemId,
            @ForAll("validUserId") Long ownerId) {
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        User owner = createUser(ownerId, "owner" + ownerId, "owner" + ownerId + "@test.com");
        Item item = createItem(itemId, "Title", "Description", "Electronics", 
                              "https://example.com/img.jpg", ItemStatus.AVAILABLE, owner);
        
        when(mockItemRepo.findById(itemId)).thenReturn(Optional.of(item));
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        ItemDTO result = service.getItemById(itemId);
        
        assertThat(result).isNotNull();
        assertThat(result.getOwner()).isNotNull();
        assertThat(result.getOwner().getId()).isEqualTo(ownerId);
        assertThat(result.getOwner().getUsername()).isEqualTo("owner" + ownerId);
        assertThat(result.getOwner().getFullName()).isNotNull();
    }
    
    // Feature: item-management, Property 38: Owner-based queries work correctly
    // Validates: Requirements 12.4
    @Property(tries = 100)
    void ownerBasedQueriesWorkCorrectly(
            @ForAll("validUserId") Long ownerId,
            @ForAll("itemListForOwner") List<Item> ownerItems,
            @ForAll("itemListForOwner") List<Item> otherItems) {
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        User owner = createUser(ownerId, "owner" + ownerId, "owner" + ownerId + "@test.com");
        User otherUser = createUser(ownerId + 1000, "other", "other@test.com");
        
        ownerItems.forEach(item -> setItemField(item, "owner", owner));
        otherItems.forEach(item -> setItemField(item, "owner", otherUser));
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> page = new PageImpl<>(ownerItems, pageable, ownerItems.size());
        
        when(mockItemRepo.findByOwnerId(ownerId, pageable)).thenReturn(page);
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        Page<ItemDTO> result = service.getItemsByOwner(ownerId, pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent()).allMatch(item -> item.getOwner().getId().equals(ownerId));
        assertThat(result.getContent()).noneMatch(item -> item.getOwner().getId().equals(ownerId + 1000));
    }
    
    // Feature: item-management, Property 39: Ownership is used for authorization
    // Validates: Requirements 12.5
    @Property(tries = 100)
    void ownershipIsUsedForAuthorization(
            @ForAll("validUserId") Long itemId,
            @ForAll("validUserId") Long ownerId,
            @ForAll("validUserId") Long nonOwnerId) {
        
        Assume.that(!ownerId.equals(nonOwnerId));
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        User owner = createUser(ownerId, "owner" + ownerId, "owner" + ownerId + "@test.com");
        Item item = createItem(itemId, "Title", "Description", "Electronics", 
                              "https://example.com/img.jpg", ItemStatus.AVAILABLE, owner);
        
        when(mockItemRepo.findById(itemId)).thenReturn(Optional.of(item));
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        // Owner should be able to update
        when(mockItemRepo.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));
        UpdateItemRequest request = new UpdateItemRequest();
        request.setTitle("New Title");
        
        ItemDTO result = service.updateItem(itemId, request, ownerId);
        assertThat(result).isNotNull();
        
        // Non-owner should not be able to update
        assertThatThrownBy(() -> service.updateItem(itemId, request, nonOwnerId))
                .isInstanceOf(AccessDeniedException.class);
    }
    
    // Arbitraries
    @Provide
    Arbitrary<String> validTitle() {
        return Arbitraries.strings().alpha().numeric().withChars(' ', '-', '_')
                .ofMinLength(3).ofMaxLength(200);
    }
    
    @Provide
    Arbitrary<String> shortTitle() {
        return Arbitraries.strings().alpha().numeric().ofMinLength(0).ofMaxLength(2);
    }
    
    @Provide
    Arbitrary<String> longTitle() {
        return Arbitraries.strings().alpha().numeric().ofMinLength(201).ofMaxLength(300);
    }
    
    @Provide
    Arbitrary<String> invalidTitle() {
        return Arbitraries.oneOf(shortTitle(), longTitle());
    }
    
    @Provide
    Arbitrary<String> validDescription() {
        return Arbitraries.strings().alpha().numeric().withChars(' ', '.', ',', '\n')
                .ofMinLength(0).ofMaxLength(1000);
    }
    
    @Provide
    Arbitrary<String> validCategory() {
        return Arbitraries.of("Electronics", "Books", "Accessories", "Sports Equipment", 
                              "Musical Instruments", "Tools", "Other");
    }
    
    @Provide
    Arbitrary<String> validImageUrl() {
        return Arbitraries.strings().alpha().numeric()
                .ofMinLength(10).ofMaxLength(100)
                .map(s -> "https://cloudinary.com/images/" + s + ".jpg");
    }
    
    @Provide
    Arbitrary<Long> validUserId() {
        return Arbitraries.longs().between(1L, 10000L);
    }
    
    @Provide
    Arbitrary<String> searchKeyword() {
        return Arbitraries.strings().alpha().numeric().ofMinLength(1).ofMaxLength(20);
    }
    
    @Provide
    Arbitrary<ItemStatus> itemStatus() {
        return Arbitraries.of(ItemStatus.AVAILABLE, ItemStatus.BORROWED, ItemStatus.UNAVAILABLE);
    }
    
    @Provide
    Arbitrary<List<Item>> itemList() {
        return Combinators.combine(
                validUserId(),
                validTitle(),
                validDescription(),
                validCategory(),
                validImageUrl()
        ).as((userId, title, desc, cat, img) -> {
            User owner = createUser(userId, "user" + userId, "user" + userId + "@test.com");
            Item item = new Item();
            setItemField(item, "id", userId);
            setItemField(item, "title", title);
            setItemField(item, "description", desc);
            setItemField(item, "category", cat);
            setItemField(item, "imageUrl", img);
            setItemField(item, "status", ItemStatus.AVAILABLE);
            setItemField(item, "owner", owner);
            setItemField(item, "createdAt", LocalDateTime.now());
            setItemField(item, "updatedAt", LocalDateTime.now());
            return item;
        }).list().ofMinSize(0).ofMaxSize(20);
    }
    
    @Provide
    Arbitrary<List<Item>> itemListWithKeyword() {
        return Combinators.combine(
                validUserId(),
                searchKeyword(),
                validDescription(),
                validCategory(),
                validImageUrl()
        ).as((userId, keyword, desc, cat, img) -> {
            User owner = createUser(userId, "user" + userId, "user" + userId + "@test.com");
            Item item = new Item();
            setItemField(item, "id", userId);
            setItemField(item, "title", "Title with " + keyword + " inside");
            setItemField(item, "description", desc);
            setItemField(item, "category", cat);
            setItemField(item, "imageUrl", img);
            setItemField(item, "status", ItemStatus.AVAILABLE);
            setItemField(item, "owner", owner);
            setItemField(item, "createdAt", LocalDateTime.now());
            setItemField(item, "updatedAt", LocalDateTime.now());
            return item;
        }).list().ofMinSize(1).ofMaxSize(20);
    }
    
    @Provide
    Arbitrary<List<Item>> itemListWithCategories() {
        return Combinators.combine(
                validUserId(),
                validTitle(),
                validDescription(),
                validCategory(),
                validImageUrl()
        ).as((userId, title, desc, cat, img) -> {
            User owner = createUser(userId, "user" + userId, "user" + userId + "@test.com");
            Item item = new Item();
            setItemField(item, "id", userId);
            setItemField(item, "title", title);
            setItemField(item, "description", desc);
            setItemField(item, "category", cat);
            setItemField(item, "imageUrl", img);
            setItemField(item, "status", ItemStatus.AVAILABLE);
            setItemField(item, "owner", owner);
            setItemField(item, "createdAt", LocalDateTime.now());
            setItemField(item, "updatedAt", LocalDateTime.now());
            return item;
        }).list().ofMinSize(1).ofMaxSize(20);
    }
    
    @Provide
    Arbitrary<List<Item>> itemListWithStatuses() {
        return Combinators.combine(
                validUserId(),
                validTitle(),
                validDescription(),
                validCategory(),
                validImageUrl(),
                itemStatus()
        ).as((userId, title, desc, cat, img, status) -> {
            User owner = createUser(userId, "user" + userId, "user" + userId + "@test.com");
            Item item = new Item();
            setItemField(item, "id", userId);
            setItemField(item, "title", title);
            setItemField(item, "description", desc);
            setItemField(item, "category", cat);
            setItemField(item, "imageUrl", img);
            setItemField(item, "status", status);
            setItemField(item, "owner", owner);
            setItemField(item, "createdAt", LocalDateTime.now());
            setItemField(item, "updatedAt", LocalDateTime.now());
            return item;
        }).list().ofMinSize(1).ofMaxSize(20);
    }
    
    @Provide
    Arbitrary<List<Item>> itemListForOwner() {
        return Combinators.combine(
                validUserId(),
                validTitle(),
                validDescription(),
                validCategory(),
                validImageUrl()
        ).as((userId, title, desc, cat, img) -> {
            // Owner will be set in the test
            Item item = new Item();
            setItemField(item, "id", userId);
            setItemField(item, "title", title);
            setItemField(item, "description", desc);
            setItemField(item, "category", cat);
            setItemField(item, "imageUrl", img);
            setItemField(item, "status", ItemStatus.AVAILABLE);
            setItemField(item, "createdAt", LocalDateTime.now());
            setItemField(item, "updatedAt", LocalDateTime.now());
            return item;
        }).list().ofMinSize(0).ofMaxSize(20);
    }
    
    // Helper methods
    private User createUser(Long id, String username, String email) {
        User user = new User();
        setUserField(user, "id", id);
        setUserField(user, "username", username);
        setUserField(user, "email", email);
        setUserField(user, "password", "hashedPassword");
        setUserField(user, "fullName", "Test User " + id);
        setUserField(user, "role", Role.USER);
        setUserField(user, "createdAt", LocalDateTime.now());
        setUserField(user, "updatedAt", LocalDateTime.now());
        return user;
    }
    
    private Item createItem(Long id, String title, String description, String category, 
                           String imageUrl, ItemStatus status, User owner) {
        Item item = new Item();
        setItemField(item, "id", id);
        setItemField(item, "title", title);
        setItemField(item, "description", description);
        setItemField(item, "category", category);
        setItemField(item, "imageUrl", imageUrl);
        setItemField(item, "status", status);
        setItemField(item, "owner", owner);
        setItemField(item, "createdAt", LocalDateTime.now());
        setItemField(item, "updatedAt", LocalDateTime.now());
        return item;
    }
    
    private void setUserField(User user, String fieldName, Object value) {
        try {
            var field = User.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(user, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private void setItemField(Item item, String fieldName, Object value) {
        try {
            var field = Item.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(item, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
