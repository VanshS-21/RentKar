# Implementation Plan - Item Management System

## Status: COMPLETE ✅

All tasks for the Item Management System have been successfully implemented and tested. The system includes:
- Complete backend API with Spring Boot (entities, repositories, services, controllers)
- Comprehensive property-based tests using jqwik (Properties 1-39)
- Integration tests for all API endpoints
- Full frontend UI with React (listing, detail, create, edit, delete pages)
- Frontend property tests using fast-check
- Image upload with Cloudinary integration and validation
- Search, filtering, and pagination functionality

All requirements from the requirements document have been satisfied.

- [x] 1. Set up backend item infrastructure
  - [x] 1.1 Create Item entity with JPA annotations
    - Define Item class with id, title, description, category, imageUrl, status fields
    - Add JPA annotations (@Entity, @Table, @Column with constraints)
    - Add ManyToOne relationship to User (owner)
    - Add timestamps (createdAt, updatedAt) with automatic management
    - Create ItemStatus enum (AVAILABLE, BORROWED, UNAVAILABLE)
    - _Requirements: 1.1, 1.5, 12.1_
  
  - [x] 1.2 Create ItemRepository interface
    - Extend JpaRepository<Item, Long>
    - Add findByStatus method with Pageable
    - Add findByOwnerId method with Pageable
    - Add findByCategory method with Pageable
    - Add custom query method for search (title and description)
    - Add custom query method for combined filters
    - _Requirements: 3.1, 4.1, 5.1, 6.1, 10.1, 12.4_
  
  - [x] 1.3 Add Cloudinary dependencies to pom.xml
    - Add cloudinary-http44 dependency
    - Configure Cloudinary properties in application.properties
    - _Requirements: 2.1_
  
  - [x] 1.4 Create CloudinaryService for image upload
    - Implement uploadImage method
    - Implement deleteImage method
    - Load Cloudinary credentials from application.properties
    - Add file size validation (max 5MB)
    - Add file type validation (images only)
    - _Requirements: 2.1, 2.2, 2.3_
  
  - [x] 1.5 Write property test for image upload
    - **Property 5: Image upload returns Cloudinary URL**
    - **Validates: Requirements 2.1, 2.4**
    - Generate random valid image files, verify Cloudinary URL returned
  
  - [x] 1.6 Write property test for file size validation
    - **Property 6: Large files are rejected**
    - **Validates: Requirements 2.2**
    - Generate files > 5MB, verify rejection
  
  - [x] 1.7 Write property test for file type validation
    - **Property 7: Non-image files are rejected**
    - **Validates: Requirements 2.3**
    - Generate non-image files, verify rejection

  - [x] 2.1 Create DTOs for item operations
    - Create CreateItemRequest DTO with validation annotations
    - Create UpdateItemRequest DTO
    - Create ItemDTO for responses
    - Create ItemOwnerDTO for owner information
    - _Requirements: 1.1, 7.1, 8.1_
  
  - [x] 2.2 Create ItemService interface and implementation
    - Implement createItem method with validation
    - Implement getItemById method
    - Implement getAllItems method with filters and pagination
    - Implement getItemsByOwner method
    - Implement updateItem method with authorization
    - Implement deleteItem method with authorization
    - _Requirements: 1.1, 3.1, 7.1, 8.1, 9.1, 10.1_
  
  - [x] 2.3 Write property test for item creation
    - **Property 1: Valid item creation stores item with owner**
    - **Validates: Requirements 1.1, 1.5**
    - Generate random valid item data, verify creation with owner
  
  - [x] 2.4 Write property test for title validation
    - **Property 2: Short titles are rejected**
    - **Property 3: Long titles are rejected**
    - **Property 4: Missing title is rejected**
    - **Validates: Requirements 1.2, 1.3, 1.4**
    - Generate invalid titles, verify rejection
  
  - [x] 2.5 Write property test for item listing
    - **Property 8: Item list returns available items by default**
    - **Validates: Requirements 3.1, 6.5**
    - Query items without filters, verify only AVAILABLE returned
  
  - [x] 2.6 Write property test for pagination
    - **Property 9: Pagination returns correct page size**
    - **Property 10: Pagination includes metadata**
    - **Validates: Requirements 3.2, 3.3**
    - Generate random page sizes, verify correct pagination
  
  - [x] 2.7 Write property test for owner information
    - **Property 11: Item list includes owner information**
    - **Validates: Requirements 3.4**
    - Retrieve items, verify owner info present
  
  - [x] 2.8 Write property test for empty results
    - **Property 12: Empty list returns empty array**
    - **Validates: Requirements 3.5**
    - Query with no matches, verify empty array with metadata
  
  - [x] 2.9 Write property test for search functionality
    - **Property 13: Search matches title and description**
    - **Property 14: Empty search returns all items**
    - **Validates: Requirements 4.1, 4.2, 4.3**
    - Generate search keywords, verify matching results
  
  - [x] 2.10 Write property test for category filtering
    - **Property 15: Category filter returns matching items**
    - **Property 16: Combined filters work together**
    - **Validates: Requirements 5.1, 5.2**
    - Generate category filters, verify results
  
  - [x] 2.11 Write property test for status filtering
    - **Property 17: Status filter returns matching items**
    - **Property 18: Multiple filters combine correctly**
    - **Validates: Requirements 6.1, 6.2, 6.3, 6.4**
    - Generate status filters, verify results
  
  - [x] 2.12 Write property test for item details
    - **Property 19: Item details include complete information**
    - **Property 20: Non-existent item returns 404**
    - **Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5**
    - Generate item IDs, verify detail retrieval
  
  - [x] 2.13 Write property test for item updates
    - **Property 21: Owner can update their item**
    - **Property 22: Non-owner cannot update item**
    - **Property 23: Title validation on update**
    - **Property 24: Status validation on update**
    - **Property 25: Update modifies timestamp**
    - **Validates: Requirements 8.1, 8.2, 8.3, 8.4, 8.5**
    - Generate update requests, verify authorization and validation
  
  - [x] 2.14 Write property test for item deletion
    - **Property 26: Owner can delete their item**
    - **Property 27: Non-owner cannot delete item**
    - **Property 28: Deleted item returns 404**
    - **Validates: Requirements 9.1, 9.2, 9.4, 9.5**
    - Generate delete requests, verify authorization
  
  - [x] 2.15 Write property test for user items
    - **Property 29: User can view their own items**
    - **Property 30: User items support pagination**
    - **Property 31: User with no items returns empty array**
    - **Property 32: User items ordered by date**
    - **Validates: Requirements 10.1, 10.2, 10.3, 10.4, 10.5**
    - Generate owner queries, verify results
  
  - [x] 2.16 Write property test for validation
    - **Property 33: Required fields are validated**
    - **Property 34: Field formats are validated**
    - **Property 35: Validation errors are specific**
    - **Validates: Requirements 11.1, 11.2, 11.3**
    - Generate invalid inputs, verify validation errors
  
  - [x] 2.17 Write property test for ownership
    - **Property 36: Item stores owner ID**
    - **Property 37: Item responses include owner info**
    - **Property 38: Owner-based queries work correctly**
    - **Property 39: Ownership is used for authorization**
    - **Validates: Requirements 12.1, 12.2, 12.4, 12.5**
    - Generate ownership scenarios, verify behavior

- [x] 3. Create item REST controller
  - [x] 3.1 Create ItemController with endpoints
    - Implement POST /api/items endpoint (create item)
    - Implement GET /api/items endpoint (list with filters)
    - Implement GET /api/items/{id} endpoint (get details)
    - Implement PUT /api/items/{id} endpoint (update item)
    - Implement DELETE /api/items/{id} endpoint (delete item)
    - Implement POST /api/items/upload-image endpoint (upload image)
    - Implement GET /api/items/my-items endpoint (user's items)
    - Add proper error handling and validation
    - Return standardized API responses
    - _Requirements: 1.1, 2.1, 3.1, 7.1, 8.1, 9.1, 10.1_
  
  - [x] 3.2 Write integration tests for ItemController
    - Test create item endpoint with valid/invalid data
    - Test list items endpoint with various filters
    - Test get item details endpoint
    - Test update item endpoint with owner/non-owner
    - Test delete item endpoint with owner/non-owner
    - Test image upload endpoint
    - Test my-items endpoint
    - Verify proper HTTP status codes and response formats
    - _Requirements: 1.1, 3.1, 7.1, 8.1, 9.1_

- [x] 4. Checkpoint - Ensure all backend tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Set up frontend item infrastructure
  - [x] 5.1 Install required npm packages
    - Verify axios is installed
    - Verify react-router-dom is installed
    - Install react-hook-form for form handling
    - Install zod for validation
    - _Requirements: 1.1, 3.1_
  
  - [x] 5.2 Create itemService API client
    - Implement createItem function
    - Implement getItems function with filters
    - Implement getItemById function
    - Implement updateItem function
    - Implement deleteItem function
    - Implement uploadImage function
    - Implement getMyItems function
    - Handle API errors and return formatted responses
    - _Requirements: 1.1, 2.1, 3.1, 7.1, 8.1, 9.1, 10.1_
  
  - [x] 5.3 Create Cloudinary upload utility
    - Implement image upload to Cloudinary
    - Add file size validation (max 5MB)
    - Add file type validation (images only)
    - Show upload progress
    - Handle upload errors
    - _Requirements: 2.1, 2.2, 2.3_

- [x] 6. Create item listing and browsing UI
  - [x] 6.1 Create ItemListPage component
    - Display items in grid layout
    - Add search bar with debouncing
    - Add category filter dropdown
    - Add status filter dropdown
    - Implement pagination controls
    - Show loading state while fetching
    - Handle empty state (no items)
    - _Requirements: 3.1, 4.1, 5.1, 6.1_
  
  - [x] 6.2 Create ItemCard component
    - Display item image, title, category, status
    - Show owner name
    - Add click handler to view details
    - Style based on item status
    - Add placeholder for missing images
    - _Requirements: 3.4, 7.1_
  
  - [x] 6.3 Create SearchBar component
    - Input field with search icon
    - Debounce search input (300ms)
    - Clear search button
    - Show search results count
    - _Requirements: 4.1_
  
  - [x] 6.4 Create FilterPanel component
    - Category dropdown with all categories
    - Status dropdown (Available, Borrowed, Unavailable)
    - Clear filters button
    - Show active filter count
    - _Requirements: 5.1, 6.1_
  
  - [x] 6.5 Create Pagination component
    - Previous/Next buttons
    - Page number display
    - Jump to page input
    - Disable buttons at boundaries
    - _Requirements: 3.2, 3.3_
  
  - [x] 6.6 Write property test for search UI
    - Generate random search queries, verify UI updates
    - Verify debouncing works correctly
    - _Requirements: 4.1_
  
  - [x] 6.7 Write property test for filter UI
    - Generate random filter combinations, verify results
    - Verify filters combine correctly
    - _Requirements: 5.1, 6.1_
  
  - [x] 6.8 Write property test for pagination UI
    - Generate random page numbers, verify navigation
    - Verify page boundaries handled correctly
    - _Requirements: 3.2_

- [x] 7. Create item detail and management UI
  - [x] 7.1 Create ItemDetailPage component
    - Display full item information
    - Show large item image
    - Display owner contact information
    - Show item status badge
    - Add "Borrow" button (if not owner and available)
    - Add "Edit" button (if owner)
    - Add "Delete" button (if owner)
    - Handle loading and error states
    - _Requirements: 7.1, 7.3, 7.4, 7.5_
  
  - [x] 7.2 Create AddItemPage component
    - Build item creation form
    - Add title input with validation
    - Add description textarea
    - Add category dropdown
    - Add image upload component
    - Show image preview
    - Add client-side validation
    - Display loading state during submission
    - Display error messages from API
    - Redirect to item detail on success
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.1_
  
  - [x] 7.3 Create EditItemPage component
    - Pre-fill form with existing item data
    - Allow updating title, description, category, status
    - Allow changing image
    - Add client-side validation
    - Display loading state during submission
    - Display error messages from API
    - Redirect to item detail on success
    - _Requirements: 8.1, 8.3, 8.4_
  
  - [x] 7.4 Create MyItemsPage component
    - Display user's own items
    - Show all statuses (Available, Borrowed, Unavailable)
    - Add "Add New Item" button
    - Add edit/delete actions for each item
    - Support pagination
    - Handle empty state (no items)
    - _Requirements: 10.1, 10.2, 10.3, 10.4_
  
  - [x] 7.5 Create ImageUpload component
    - File input with drag-and-drop
    - Image preview before upload
    - Upload progress indicator
    - File size validation (max 5MB)
    - File type validation (images only)
    - Error handling for upload failures
    - _Requirements: 2.1, 2.2, 2.3_
  
  - [x] 7.6 Create DeleteConfirmation modal
    - Confirmation dialog for item deletion
    - Show item title in confirmation
    - Cancel and confirm buttons
    - Handle deletion and redirect
    - _Requirements: 9.1_
  
  - [x] 7.7 Write property test for form validation
    - Generate random form inputs, verify validation
    - Verify error messages displayed correctly
    - _Requirements: 1.2, 1.3, 1.4_
  
  - [x] 7.8 Write property test for image upload UI
    - Generate various file types and sizes
    - Verify validation and error handling
    - _Requirements: 2.2, 2.3_
  
  - [x] 7.9 Write property test for authorization UI
    - Verify edit/delete buttons only show for owners
    - Verify non-owners see borrow button
    - _Requirements: 8.2, 9.2_

- [x] 8. Integrate item management into application
  - [x] 8.1 Update navigation component
    - Add "Browse Items" link
    - Add "My Items" link
    - Add "Add Item" button (for authenticated users)
    - _Requirements: 3.1, 10.1_
  
  - [x] 8.2 Set up routing for item pages
    - Add route for /items (ItemListPage)
    - Add route for /items/:id (ItemDetailPage)
    - Add route for /items/new (AddItemPage)
    - Add route for /items/:id/edit (EditItemPage)
    - Add route for /my-items (MyItemsPage)
    - Protect add/edit/delete routes with authentication
    - _Requirements: 1.1, 3.1, 7.1, 8.1, 10.1_
  
  - [x] 8.3 Update home page
    - Show featured/recent items
    - Add "Browse All Items" button
    - Add quick search bar
    - _Requirements: 3.1_

- [x] 9. Configure environment variables and settings
  - [x] 9.1 Configure backend application.properties
    - Set Cloudinary cloud name
    - Set Cloudinary API key
    - Set Cloudinary API secret
    - Configure file upload max size (5MB)
    - _Requirements: 2.1, 2.2_
  
  - [x] 9.2 Configure frontend environment variables
    - Set Cloudinary cloud name
    - Set Cloudinary upload preset (if using unsigned upload)
    - _Requirements: 2.1_

- [x] 10. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
