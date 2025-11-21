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
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class ItemServicePropertyTest {
    
    private final Validator validator;
    
    public ItemServicePropertyTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }
    
    // Feature: item-management, Property 1: Valid item creation stores item with owner
    // Validates: Requirements 1.1, 1.5
    @Property(tries = 100)
    void validItemCreationStoresItemWithOwner(
            @ForAll("validTitle") String title,
            @ForAll("validDescription") String description,
            @ForAll("validCategory") String category,
            @ForAll("validImageUrl") String imageUrl,
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
        request.setDescription(description);
        request.setCategory(category);
        request.setImageUrl(imageUrl);
        
        ItemDTO result = service.createItem(request, ownerId);
        
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(title);
        assertThat(result.getDescription()).isEqualTo(description);
        assertThat(result.getCategory()).isEqualTo(category);
        assertThat(result.getImageUrl()).isEqualTo(imageUrl);
        assertThat(result.getStatus()).isEqualTo(ItemStatus.AVAILABLE);
        assertThat(result.getOwner()).isNotNull();
        assertThat(result.getOwner().getId()).isEqualTo(ownerId);
        
        Item savedItem = itemDb.get(result.getId());
        assertThat(savedItem).isNotNull();
        assertThat(savedItem.getOwner().getId()).isEqualTo(ownerId);
    }
    
    // Feature: item-management, Property 2: Short titles are rejected
    // Validates: Requirements 1.2
    @Property(tries = 100)
    void shortTitlesAreRejected(
            @ForAll("shortTitle") String title,
            @ForAll("validDescription") String description,
            @ForAll("validCategory") String category,
            @ForAll("validImageUrl") String imageUrl) {
        
        CreateItemRequest request = new CreateItemRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setCategory(category);
        request.setImageUrl(imageUrl);
        
        Set<ConstraintViolation<CreateItemRequest>> violations = validator.validate(request);
        
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> 
            v.getPropertyPath().toString().equals("title") && 
            v.getMessage().toLowerCase().contains("between")
        );
    }
    
    // Feature: item-management, Property 3: Long titles are rejected
    // Validates: Requirements 1.3
    @Property(tries = 100)
    void longTitlesAreRejected(
            @ForAll("longTitle") String title,
            @ForAll("validDescription") String description,
            @ForAll("validCategory") String category,
            @ForAll("validImageUrl") String imageUrl) {
        
        CreateItemRequest request = new CreateItemRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setCategory(category);
        request.setImageUrl(imageUrl);
        
        Set<ConstraintViolation<CreateItemRequest>> violations = validator.validate(request);
        
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> 
            v.getPropertyPath().toString().equals("title") && 
            v.getMessage().toLowerCase().contains("between")
        );
    }
    
    // Feature: item-management, Property 4: Missing title is rejected
    // Validates: Requirements 1.4
    @Property(tries = 100)
    void missingTitleIsRejected(
            @ForAll("validDescription") String description,
            @ForAll("validCategory") String category,
            @ForAll("validImageUrl") String imageUrl) {
        
        CreateItemRequest request = new CreateItemRequest();
        request.setTitle(null);
        request.setDescription(description);
        request.setCategory(category);
        request.setImageUrl(imageUrl);
        
        Set<ConstraintViolation<CreateItemRequest>> violations = validator.validate(request);
        
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> 
            v.getPropertyPath().toString().equals("title") && 
            v.getMessage().toLowerCase().contains("required")
        );
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
    
    // Helper methods
    private User createUser(Long id, String username, String email) {
        User user = new User();
        setUserField(user, "id", id);
        setUserField(user, "username", username);
        setUserField(user, "email", email);
        setUserField(user, "password", "hashedPassword");
        setUserField(user, "fullName", "Test User");
        setUserField(user, "role", Role.USER);
        setUserField(user, "createdAt", LocalDateTime.now());
        setUserField(user, "updatedAt", LocalDateTime.now());
        return user;
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
    
    // Feature: item-management, Property 8: Item list returns available items by default
    // Validates: Requirements 3.1, 6.5
    @Property(tries = 100)
    void itemListReturnsAvailableItemsByDefault(
            @ForAll("itemList") List<Item> items) {
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        // Filter only AVAILABLE items
        List<Item> availableItems = items.stream()
                .filter(i -> i.getStatus() == ItemStatus.AVAILABLE)
                .toList();
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> page = new PageImpl<>(availableItems, pageable, availableItems.size());
        
        when(mockItemRepo.findWithFilters(ItemStatus.AVAILABLE, null, null, pageable))
                .thenReturn(page);
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        Page<ItemDTO> result = service.getAllItems(null, null, null, pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent()).allMatch(item -> item.getStatus() == ItemStatus.AVAILABLE);
    }
    
    // Feature: item-management, Property 9: Pagination returns correct page size
    // Validates: Requirements 3.2
    @Property(tries = 100)
    void paginationReturnsCorrectPageSize(
            @ForAll("itemList") List<Item> items,
            @ForAll @IntRange(min = 1, max = 50) int pageSize) {
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        Pageable pageable = PageRequest.of(0, pageSize);
        List<Item> pageItems = items.stream().limit(pageSize).toList();
        Page<Item> page = new PageImpl<>(pageItems, pageable, items.size());
        
        when(mockItemRepo.findWithFilters(any(), any(), any(), any()))
                .thenReturn(page);
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        Page<ItemDTO> result = service.getAllItems(ItemStatus.AVAILABLE, null, null, pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent().size()).isLessThanOrEqualTo(pageSize);
    }
    
    // Feature: item-management, Property 10: Pagination includes metadata
    // Validates: Requirements 3.3
    @Property(tries = 100)
    void paginationIncludesMetadata(
            @ForAll("itemList") List<Item> items,
            @ForAll @IntRange(min = 1, max = 20) int pageSize,
            @ForAll @IntRange(min = 0, max = 5) int pageNumber) {
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        int start = pageNumber * pageSize;
        int end = Math.min(start + pageSize, items.size());
        List<Item> pageItems = start < items.size() ? items.subList(start, end) : List.of();
        Page<Item> page = new PageImpl<>(pageItems, pageable, items.size());
        
        when(mockItemRepo.findWithFilters(any(), any(), any(), any()))
                .thenReturn(page);
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        Page<ItemDTO> result = service.getAllItems(ItemStatus.AVAILABLE, null, null, pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getNumber()).isEqualTo(pageNumber);
        assertThat(result.getSize()).isEqualTo(pageSize);
        assertThat(result.getTotalElements()).isEqualTo(items.size());
        assertThat(result.getTotalPages()).isEqualTo((int) Math.ceil((double) items.size() / pageSize));
    }
    
    // Feature: item-management, Property 11: Item list includes owner information
    // Validates: Requirements 3.4
    @Property(tries = 100)
    void itemListIncludesOwnerInformation(
            @ForAll("itemList") List<Item> items) {
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> page = new PageImpl<>(items, pageable, items.size());
        
        when(mockItemRepo.findWithFilters(any(), any(), any(), any()))
                .thenReturn(page);
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        Page<ItemDTO> result = service.getAllItems(ItemStatus.AVAILABLE, null, null, pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent()).allMatch(item -> {
            return item.getOwner() != null &&
                   item.getOwner().getId() != null &&
                   item.getOwner().getUsername() != null &&
                   item.getOwner().getFullName() != null;
        });
    }
    
    // Feature: item-management, Property 12: Empty list returns empty array
    // Validates: Requirements 3.5
    @Property(tries = 100)
    void emptyListReturnsEmptyArray(
            @ForAll @IntRange(min = 1, max = 20) int pageSize) {
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        
        Pageable pageable = PageRequest.of(0, pageSize);
        Page<Item> page = new PageImpl<>(List.of(), pageable, 0);
        
        when(mockItemRepo.findWithFilters(any(), any(), any(), any()))
                .thenReturn(page);
        
        ItemService service = new ItemServiceImpl(mockItemRepo, mockUserRepo);
        
        Page<ItemDTO> result = service.getAllItems(ItemStatus.AVAILABLE, null, null, pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(0);
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
}
