package com.rentkar.controller;

import com.rentkar.dto.AIGenerationRequest;
import com.rentkar.dto.AIGenerationResponse;
import com.rentkar.dto.CreateItemRequest;
import com.rentkar.dto.ItemDTO;
import com.rentkar.dto.UpdateItemRequest;
import com.rentkar.exception.RateLimitExceededException;
import com.rentkar.model.ItemStatus;
import com.rentkar.model.User;
import com.rentkar.repository.UserRepository;
import com.rentkar.service.AIService;
import com.rentkar.service.CloudinaryService;
import com.rentkar.service.ItemService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
public class ItemController {
    
    private final ItemService itemService;
    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;
    private final AIService aiService;
    
    public ItemController(ItemService itemService, CloudinaryService cloudinaryService, 
                         UserRepository userRepository, AIService aiService) {
        this.itemService = itemService;
        this.cloudinaryService = cloudinaryService;
        this.userRepository = userRepository;
        this.aiService = aiService;
    }
    
    @PostMapping
    public ResponseEntity<?> createItem(@Valid @RequestBody CreateItemRequest request) {
        try {
            Long userId = getCurrentUserId();
            ItemDTO item = itemService.createItem(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createSuccessResponse(item, "Item created successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(e.getMessage()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("Failed to create item"));
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getAllItems(
            @RequestParam(required = false) ItemStatus status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<ItemDTO> items = itemService.getAllItems(status, category, search, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("items", items.getContent());
            response.put("pagination", createPaginationMetadata(items));
            
            return ResponseEntity.ok(createSuccessResponse(response, "Items retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("Failed to retrieve items"));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getItemById(@PathVariable Long id) {
        try {
            ItemDTO item = itemService.getItemById(id);
            return ResponseEntity.ok(createSuccessResponse(item, "Item retrieved successfully"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("Failed to retrieve item"));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @Valid @RequestBody UpdateItemRequest request) {
        try {
            Long userId = getCurrentUserId();
            ItemDTO item = itemService.updateItem(id, request, userId);
            return ResponseEntity.ok(createSuccessResponse(item, "Item updated successfully"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(createErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("Failed to update item"));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            itemService.deleteItem(id, userId);
            return ResponseEntity.ok(createSuccessResponse(null, "Item deleted successfully"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("Failed to delete item"));
        }
    }
    
    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = cloudinaryService.uploadImage(file);
            Map<String, String> data = new HashMap<>();
            data.put("imageUrl", imageUrl);
            return ResponseEntity.ok(createSuccessResponse(data, "Image uploaded successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("Failed to upload image"));
        }
    }
    
    @GetMapping("/my-items")
    public ResponseEntity<?> getMyItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long userId = getCurrentUserId();
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<ItemDTO> items = itemService.getItemsByOwner(userId, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("items", items.getContent());
            response.put("pagination", createPaginationMetadata(items));
            
            return ResponseEntity.ok(createSuccessResponse(response, "User items retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("Failed to retrieve user items"));
        }
    }
    
    @PostMapping("/generate-title")
    public ResponseEntity<?> generateTitle(@Valid @RequestBody AIGenerationRequest request, 
                                          HttpServletRequest httpRequest) {
        try {
            String userId = getUserIdOrIp(httpRequest);
            AIGenerationResponse response = aiService.generateTitle(request, userId);
            
            if (!response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse(response.getErrorMessage()));
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("content", response.getContent());
            data.put("tokenCount", response.getTokenCount());
            data.put("responseTimeMs", response.getResponseTimeMs());
            data.put("remainingRequests", aiService.getRemainingRequests(userId));
            
            return ResponseEntity.ok(createSuccessResponse(data, "Title generated successfully"));
            
        } catch (RateLimitExceededException e) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Retry-After", String.valueOf(e.getRetryAfterSeconds()));
            
            Map<String, Object> errorResponse = createErrorResponse(e.getMessage());
            errorResponse.put("retryAfter", e.getRetryAfterSeconds());
            
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .headers(headers)
                    .body(errorResponse);
                    
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to generate title"));
        }
    }
    
    @PostMapping("/generate-description")
    public ResponseEntity<?> generateDescription(@Valid @RequestBody AIGenerationRequest request,
                                                 HttpServletRequest httpRequest) {
        try {
            String userId = getUserIdOrIp(httpRequest);
            AIGenerationResponse response = aiService.generateDescription(request, userId);
            
            if (!response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse(response.getErrorMessage()));
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("content", response.getContent());
            data.put("tokenCount", response.getTokenCount());
            data.put("responseTimeMs", response.getResponseTimeMs());
            data.put("remainingRequests", aiService.getRemainingRequests(userId));
            
            return ResponseEntity.ok(createSuccessResponse(data, "Description generated successfully"));
            
        } catch (RateLimitExceededException e) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Retry-After", String.valueOf(e.getRetryAfterSeconds()));
            
            Map<String, Object> errorResponse = createErrorResponse(e.getMessage());
            errorResponse.put("retryAfter", e.getRetryAfterSeconds());
            
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .headers(headers)
                    .body(errorResponse);
                    
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to generate description"));
        }
    }
    
    @GetMapping("/ai-available")
    public ResponseEntity<?> checkAIAvailability() {
        try {
            boolean available = aiService.isAvailable();
            
            Map<String, Object> data = new HashMap<>();
            data.put("available", available);
            
            return ResponseEntity.ok(createSuccessResponse(data, "AI availability checked"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to check AI availability"));
        }
    }
    
    /**
     * Get user ID for authenticated users, or IP address for unauthenticated users
     */
    private String getUserIdOrIp(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId();
            return "user_" + userId;
        } catch (Exception e) {
            // User not authenticated, use IP address
            String ipAddress = request.getRemoteAddr();
            return "ip_" + ipAddress;
        }
    }
    
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("User not authenticated");
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        return user.getId();
    }
    
    private Map<String, Object> createSuccessResponse(Object data, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
    
    private Map<String, Object> createPaginationMetadata(Page<?> page) {
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("currentPage", page.getNumber());
        pagination.put("totalPages", page.getTotalPages());
        pagination.put("totalItems", page.getTotalElements());
        pagination.put("pageSize", page.getSize());
        return pagination;
    }
}
