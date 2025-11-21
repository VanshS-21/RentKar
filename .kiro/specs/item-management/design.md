# Design Document - Item Management System

## Overview

The Item Management System provides comprehensive CRUD operations for items on the RentKar platform. The system consists of backend components (Spring Boot with Spring Data JPA) and frontend components (React with TailwindCSS) that work together to enable users to create, browse, search, update, and delete item listings.

The backend implements a layered architecture with Item entity, ItemRepository for data access, ItemService for business logic, and ItemController for REST endpoints. Images are uploaded to Cloudinary for cloud storage. The system supports pagination, search, and filtering capabilities.

The frontend provides forms for creating and editing items, a grid/list view for browsing items, detailed item pages, and search/filter functionality. Images are uploaded via Cloudinary's upload widget or direct API integration.

## Architecture

### Backend Architecture

**Layered Structure:**
```
ItemController (REST API Layer)
    ↓
ItemService (Business Logic Layer)
    ↓
ItemRepository (Data Access Layer)
    ↓
MySQL Database
```

**Cloudinary Integration:**
```
Frontend/Backend
    ↓
Cloudinary Upload API
    ↓
Cloudinary CDN (Image Storage)
```

**Key Components:**
- **Item Entity**: JPA entity representing item data with relationships to User
- **ItemRepository**: Spring Data JPA repository with custom query methods
- **ItemService**: Business logic for CRUD operations, validation, and authorization
- **ItemController**: REST endpoints for item operations
- **CloudinaryService**: Utility service for image upload and management
- **ItemStatus Enum**: Enumeration for item availability states

### Frontend Architecture

**Component Hierarchy:**
```
App
    ↓
ItemListPage (Browse Items)
    ↓
ItemCard (Individual Item Display)

AddItemPage (Create Item)
    ↓
ItemForm (Form Component)

ItemDetailPage (View Item)
    ↓
ItemActions (Edit/Delete)

MyItemsPage (User's Items)
    ↓
ItemList (Reusable List Component)
```

**Key Components:**
- **ItemListPage**: Main browsing page with search, filters, and pagination
- **ItemDetailPage**: Detailed view of a single item
- **AddItemPage**: Form for creating new items
- **EditItemPage**: Form for updating existing items
- **MyItemsPage**: User's own item listings
- **ItemCard**: Reusable component for displaying item summary
- **SearchBar**: Search input with debouncing
- **FilterPanel**: Category and status filters
- **Pagination**: Page navigation controls
- **ImageUpload**: Cloudinary image upload component

## Components and Interfaces

### Backend Components

#### Item Entity
```java
@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(length = 50)
    private String category;
    
    @Column(length = 500)
    private String imageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemStatus status = ItemStatus.AVAILABLE;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### ItemRepository Interface
```java
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByStatus(ItemStatus status, Pageable pageable);
    List<Item> findByOwnerId(Long ownerId, Pageable pageable);
    List<Item> findByCategory(String category, Pageable pageable);
    
    @Query("SELECT i FROM Item i WHERE " +
           "(LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Item> searchItems(String keyword, Pageable pageable);
    
    @Query("SELECT i FROM Item i WHERE " +
           "i.status = :status AND " +
           "(:category IS NULL OR i.category = :category) AND " +
           "(:keyword IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Item> findWithFilters(ItemStatus status, String category, 
                                String keyword, Pageable pageable);
}
```

#### ItemService Interface
```java
public interface ItemService {
    ItemDTO createItem(CreateItemRequest request, Long ownerId);
    ItemDTO getItemById(Long itemId);
    Page<ItemDTO> getAllItems(ItemStatus status, String category, 
                               String search, Pageable pageable);
    Page<ItemDTO> getItemsByOwner(Long ownerId, Pageable pageable);
    ItemDTO updateItem(Long itemId, UpdateItemRequest request, Long userId);
    void deleteItem(Long itemId, Long userId);
    String uploadImage(MultipartFile file);
}
```

#### ItemController Endpoints
```java
@RestController
@RequestMapping("/api/items")
public class ItemController {
    POST / - Create new item
    GET / - Get all items with filters
    GET /{id} - Get item by ID
    PUT /{id} - Update item
    DELETE /{id} - Delete item
    POST /upload-image - Upload image to Cloudinary
    GET /my-items - Get current user's items
}
```

#### CloudinaryService Interface
```java
public interface CloudinaryService {
    String uploadImage(MultipartFile file);
    void deleteImage(String publicId);
}
```

### Frontend Components

#### Item Data Model
```typescript
interface Item {
    id: number;
    title: string;
    description: string;
    category: string;
    imageUrl: string;
    status: 'AVAILABLE' | 'BORROWED' | 'UNAVAILABLE';
    owner: {
        id: number;
        username: string;
        fullName: string;
        email?: string;
        phone?: string;
    };
    createdAt: string;
    updatedAt: string;
}
```

#### Create Item Request
```typescript
interface CreateItemRequest {
    title: string;
    description: string;
    category: string;
    imageUrl: string;
}
```

#### Update Item Request
```typescript
interface UpdateItemRequest {
    title?: string;
    description?: string;
    category?: string;
    status?: ItemStatus;
}
```

#### Pagination Response
```typescript
interface PaginatedResponse<T> {
    items: T[];
    pagination: {
        currentPage: number;
        totalPages: number;
        totalItems: number;
        pageSize: number;
    };
}
```

## Data Models

### Item Status Enum
```java
public enum ItemStatus {
    AVAILABLE,
    BORROWED,
    UNAVAILABLE
}
```

### Categories
Common categories (not enforced at database level):
- Electronics
- Books
- Accessories
- Sports Equipment
- Musical Instruments
- Tools
- Other

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Valid item creation stores item with owner
*For any* valid item data (title 3-200 chars, valid category, valid image URL) and authenticated user, creating an item should store it in the database with the user as owner and status AVAILABLE.
**Validates: Requirements 1.1, 1.5**

### Property 2: Short titles are rejected
*For any* title with length less than 3 characters, item creation should be rejected with a validation error.
**Validates: Requirements 1.2**

### Property 3: Long titles are rejected
*For any* title with length greater than 200 characters, item creation should be rejected with a validation error.
**Validates: Requirements 1.3**

### Property 4: Missing title is rejected
*For any* item creation request without a title, the request should be rejected with a validation error.
**Validates: Requirements 1.4**

### Property 5: Image upload returns Cloudinary URL
*For any* valid image file (JPEG/PNG, < 5MB), uploading should return a Cloudinary URL and public ID.
**Validates: Requirements 2.1, 2.4**

### Property 6: Large files are rejected
*For any* file larger than 5MB, upload should be rejected with a file size error.
**Validates: Requirements 2.2**

### Property 7: Non-image files are rejected
*For any* file that is not an image format, upload should be rejected with a file type error.
**Validates: Requirements 2.3**

### Property 8: Item list returns available items by default
*For any* request to list items without status filter, the system should return only items with status AVAILABLE.
**Validates: Requirements 3.1, 6.5**

### Property 9: Pagination returns correct page size
*For any* page size parameter, the system should return at most that many items per page.
**Validates: Requirements 3.2**

### Property 10: Pagination includes metadata
*For any* paginated request, the response should include current page, total pages, total items, and page size.
**Validates: Requirements 3.3**

### Property 11: Item list includes owner information
*For any* item in the list, the response should include owner ID, username, and full name.
**Validates: Requirements 3.4**

### Property 12: Empty list returns empty array
*For any* query that matches no items, the system should return an empty array with pagination metadata.
**Validates: Requirements 3.5**

### Property 13: Search matches title and description
*For any* search keyword, the system should return items where the keyword appears in either title or description (case-insensitive).
**Validates: Requirements 4.1, 4.3**

### Property 14: Empty search returns all items
*For any* empty or null search keyword, the system should return all items (subject to other filters).
**Validates: Requirements 4.2**

### Property 15: Category filter returns matching items
*For any* category filter, the system should return only items belonging to that category.
**Validates: Requirements 5.1**

### Property 16: Combined filters work together
*For any* combination of category and search filters, the system should return items matching both criteria.
**Validates: Requirements 5.2**

### Property 17: Status filter returns matching items
*For any* status filter (AVAILABLE, BORROWED, UNAVAILABLE), the system should return only items with that status.
**Validates: Requirements 6.1, 6.2, 6.3**

### Property 18: Multiple filters combine correctly
*For any* combination of status, category, and search filters, the system should return items matching all criteria.
**Validates: Requirements 6.4**

### Property 19: Item details include complete information
*For any* valid item ID, retrieving item details should return title, description, category, image, status, owner info, and timestamps.
**Validates: Requirements 7.1, 7.3, 7.4, 7.5**

### Property 20: Non-existent item returns 404
*For any* non-existent item ID, the system should return a 404 Not Found error.
**Validates: Requirements 7.2**

### Property 21: Owner can update their item
*For any* item and its owner, the owner should be able to update the item and receive the updated data.
**Validates: Requirements 8.1**

### Property 22: Non-owner cannot update item
*For any* item and a user who is not the owner, update attempts should be rejected with 403 Forbidden.
**Validates: Requirements 8.2**

### Property 23: Title validation on update
*For any* update request with invalid title (too short/long), the system should reject with validation error.
**Validates: Requirements 8.3**

### Property 24: Status validation on update
*For any* update request with invalid status value, the system should reject with validation error.
**Validates: Requirements 8.4**

### Property 25: Update modifies timestamp
*For any* successful item update, the updatedAt timestamp should be changed to the current time.
**Validates: Requirements 8.5**

### Property 26: Owner can delete their item
*For any* item and its owner, the owner should be able to delete the item successfully.
**Validates: Requirements 9.1, 9.4**

### Property 27: Non-owner cannot delete item
*For any* item and a user who is not the owner, delete attempts should be rejected with 403 Forbidden.
**Validates: Requirements 9.2**

### Property 28: Deleted item returns 404
*For any* deleted item ID, subsequent requests should return 404 Not Found.
**Validates: Requirements 9.5**

### Property 29: User can view their own items
*For any* authenticated user, requesting their own items should return all items they own regardless of status.
**Validates: Requirements 10.1, 10.2**

### Property 30: User items support pagination
*For any* request for user's items with pagination parameters, the system should return paginated results.
**Validates: Requirements 10.3**

### Property 31: User with no items returns empty array
*For any* user with no items, the system should return an empty array with pagination metadata.
**Validates: Requirements 10.4**

### Property 32: User items ordered by date
*For any* user's items, they should be ordered by creation date in descending order (newest first).
**Validates: Requirements 10.5**

### Property 33: Required fields are validated
*For any* item submission, all required fields (title) must be present or the request is rejected.
**Validates: Requirements 11.1**

### Property 34: Field formats are validated
*For any* item submission, field lengths and formats must be valid or the request is rejected.
**Validates: Requirements 11.2**

### Property 35: Validation errors are specific
*For any* validation failure, the error response should include specific messages for each invalid field.
**Validates: Requirements 11.3**

### Property 36: Item stores owner ID
*For any* created item, the owner ID should match the authenticated user's ID.
**Validates: Requirements 12.1**

### Property 37: Item responses include owner info
*For any* item retrieval, the response should include owner username, full name, and ID.
**Validates: Requirements 12.2**

### Property 38: Owner-based queries work correctly
*For any* owner ID, querying items by owner should return only items belonging to that owner.
**Validates: Requirements 12.4**

### Property 39: Ownership is used for authorization
*For any* update or delete operation, the system should verify the requesting user is the owner.
**Validates: Requirements 12.5**

## Error Handling

### Backend Error Handling

**Validation Errors:**
- Title length validation (3-200 characters)
- Required field validation
- Status enum validation
- Return 400 Bad Request with detailed error messages

**Authorization Errors:**
- Non-owner attempting to update/delete item
- Return 403 Forbidden with appropriate message

**Not Found Errors:**
- Item ID does not exist
- Return 404 Not Found

**File Upload Errors:**
- File too large (> 5MB)
- Invalid file type (not image)
- Cloudinary upload failure
- Return 400 Bad Request with specific error

**Database Errors:**
- Catch DataAccessException
- Return 500 Internal Server Error
- Log detailed error for debugging

### Frontend Error Handling

**Form Validation:**
- Client-side validation before submission
- Title length validation
- Required field validation
- Display inline error messages

**API Error Handling:**
- Display user-friendly error messages
- Handle 400, 403, 404, 500 errors appropriately
- Show toast notifications for errors

**Image Upload Errors:**
- File size validation before upload
- File type validation
- Display upload progress
- Handle upload failures gracefully

**Loading States:**
- Show loading spinners during API calls
- Disable form submission while loading
- Show skeleton loaders for item lists

## Testing Strategy

### Unit Testing

**Backend Unit Tests:**
- Test ItemService CRUD operations
- Test validation logic
- Test authorization checks
- Test search and filter logic
- Mock ItemRepository and CloudinaryService

**Frontend Unit Tests:**
- Test form validation logic
- Test search/filter state management
- Test pagination logic
- Mock API calls

### Property-Based Testing

The system will use property-based testing to verify correctness properties across a wide range of inputs. For Java backend testing, we will use **jqwik**. For JavaScript/TypeScript frontend testing, we will use **fast-check**.

**Property Test Configuration:**
- Each property test should run a minimum of 100 iterations
- Use smart generators that constrain inputs to valid ranges
- Each property test must include a comment tag referencing the design document property
- Tag format: `// Feature: item-management, Property X: <property description>`

**Backend Property Tests (using jqwik):**
- Property 1: Generate random valid item data, verify creation with owner
- Property 2-4: Generate invalid titles, verify rejection
- Property 5-7: Generate various file types and sizes, verify upload behavior
- Property 8-12: Generate various queries, verify list behavior
- Property 13-14: Generate search keywords, verify search results
- Property 15-18: Generate filter combinations, verify filtering
- Property 19-20: Generate item IDs, verify detail retrieval
- Property 21-25: Generate update requests, verify update behavior
- Property 26-28: Generate delete requests, verify deletion
- Property 29-32: Generate owner queries, verify owner-based retrieval
- Property 33-39: Generate various inputs, verify validation and authorization

**Frontend Property Tests (using fast-check):**
- Generate random form inputs, verify validation
- Generate random search queries, verify UI updates
- Generate random filter combinations, verify results
- Generate random pagination parameters, verify page navigation

### Integration Testing

**Backend Integration Tests:**
- Test complete item creation flow
- Test image upload integration with Cloudinary
- Test search and filter with database
- Test authorization with Spring Security
- Use H2 in-memory database for test isolation

**Frontend Integration Tests:**
- Test complete item creation flow with form
- Test item browsing with search and filters
- Test item detail view and navigation
- Test edit and delete flows
- Mock backend API responses

### End-to-End Testing

- Test user creates item with image upload
- Test user browses and searches items
- Test user views item details
- Test user edits their own item
- Test user deletes their own item
- Test authorization (non-owner cannot edit/delete)

## Performance Considerations

**Database Optimization:**
- Index on owner_id for fast owner-based queries
- Index on status for fast status filtering
- Index on category for fast category filtering
- Full-text index on title and description for search
- Use pagination to limit result set size

**Image Optimization:**
- Cloudinary automatic image optimization
- Lazy loading for images in list view
- Image compression before upload
- Use Cloudinary transformations for thumbnails

**Frontend Performance:**
- Debounce search input (300ms)
- Lazy load item images
- Virtual scrolling for large lists (optional)
- Cache API responses (optional)
- Optimize re-renders with React.memo

**API Performance:**
- Use pagination for all list endpoints
- Implement caching for frequently accessed items (optional)
- Optimize database queries with proper joins
- Use connection pooling

## Deployment Considerations

**Environment Variables:**
- `CLOUDINARY_CLOUD_NAME`: Cloudinary cloud name
- `CLOUDINARY_API_KEY`: Cloudinary API key
- `CLOUDINARY_API_SECRET`: Cloudinary API secret
- `CLOUDINARY_UPLOAD_PRESET`: Upload preset for unsigned uploads (frontend)

**Database Setup:**
- Items table must be created
- Foreign key constraint to users table
- Indexes on owner_id, status, category
- Full-text index on title and description

**Cloudinary Setup:**
- Create Cloudinary account
- Configure upload preset
- Set up folder structure (items/)
- Configure image transformations

**Frontend Configuration:**
- Cloudinary cloud name in environment variables
- API base URL configured
- Image upload widget or direct upload configured

**Security Checklist:**
- Validate file types and sizes on backend
- Sanitize user input for XSS prevention
- Verify ownership before update/delete
- Use parameterized queries to prevent SQL injection
- Implement rate limiting on upload endpoint (optional)
