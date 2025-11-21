# Requirements Document - Item Management System

## Introduction

The Item Management System enables users to list, browse, search, and manage items available for borrowing on the RentKar platform. This system provides the core functionality for the peer-to-peer sharing experience, allowing item owners to showcase their items with images and descriptions, while potential borrowers can discover and view detailed information about available items.

## Glossary

- **Item**: A physical object that a user lists on the platform for lending to other users
- **Item Owner**: The user who created and owns an item listing
- **Item Listing**: The complete information about an item including title, description, category, image, and status
- **Item Status**: The current availability state of an item (AVAILABLE, BORROWED, UNAVAILABLE)
- **Category**: A classification label for items (e.g., Electronics, Books, Accessories)
- **Cloudinary**: The cloud-based image storage service used for item images
- **ItemRepository**: The data access layer interface for Item entity operations
- **ItemService**: The service layer component handling item business logic
- **ItemController**: The REST API controller exposing item endpoints
- **Pagination**: The technique of dividing large result sets into smaller pages

## Requirements

### Requirement 1

**User Story:** As a user, I want to create a new item listing with title, description, category, and image, so that other users can discover and borrow my items.

#### Acceptance Criteria

1. WHEN a user submits an item creation form with valid data THEN the Item Management System SHALL create a new Item entity and store it in the database with status AVAILABLE
2. WHEN a user submits an item creation form with a title shorter than 3 characters THEN the Item Management System SHALL reject the submission and return a validation error
3. WHEN a user submits an item creation form with a title longer than 200 characters THEN the Item Management System SHALL reject the submission and return a validation error
4. WHEN a user submits an item creation form without a title THEN the Item Management System SHALL reject the submission and return a validation error
5. WHEN a user creates an item THEN the Item Management System SHALL associate the item with the authenticated user as the owner

### Requirement 2

**User Story:** As a user, I want to upload an image for my item listing, so that borrowers can see what the item looks like.

#### Acceptance Criteria

1. WHEN a user uploads an image file THEN the Item Management System SHALL upload the image to Cloudinary and return the image URL
2. WHEN a user uploads a file larger than 5MB THEN the Item Management System SHALL reject the upload and return a file size error
3. WHEN a user uploads a file that is not an image format THEN the Item Management System SHALL reject the upload and return a file type error
4. WHEN an image upload succeeds THEN the Item Management System SHALL return the Cloudinary URL and public ID
5. WHEN an image upload fails THEN the Item Management System SHALL return an error message without creating the item

### Requirement 3

**User Story:** As a user, I want to browse all available items on the platform, so that I can find items I need to borrow.

#### Acceptance Criteria

1. WHEN a user requests the item list THEN the Item Management System SHALL return all items with status AVAILABLE by default
2. WHEN a user requests the item list with pagination parameters THEN the Item Management System SHALL return items in pages with the specified page size
3. WHEN a user requests a specific page THEN the Item Management System SHALL return items for that page along with pagination metadata
4. WHEN the item list is returned THEN the Item Management System SHALL include owner information for each item
5. WHEN the item list is empty THEN the Item Management System SHALL return an empty array with pagination metadata

### Requirement 4

**User Story:** As a user, I want to search for items by keyword, so that I can quickly find specific items I need.

#### Acceptance Criteria

1. WHEN a user provides a search keyword THEN the Item Management System SHALL return items where the keyword appears in the title or description
2. WHEN a user searches with an empty keyword THEN the Item Management System SHALL return all items
3. WHEN a user searches with a keyword THEN the Item Management System SHALL perform case-insensitive matching
4. WHEN search results are returned THEN the Item Management System SHALL include pagination support
5. WHEN no items match the search keyword THEN the Item Management System SHALL return an empty result set

### Requirement 5

**User Story:** As a user, I want to filter items by category, so that I can browse items of a specific type.

#### Acceptance Criteria

1. WHEN a user selects a category filter THEN the Item Management System SHALL return only items belonging to that category
2. WHEN a user combines category filter with search THEN the Item Management System SHALL return items matching both criteria
3. WHEN a user selects a non-existent category THEN the Item Management System SHALL return an empty result set
4. WHEN a user requests items without category filter THEN the Item Management System SHALL return items from all categories
5. WHEN category filtering is applied THEN the Item Management System SHALL maintain pagination support

### Requirement 6

**User Story:** As a user, I want to filter items by status, so that I can see only available or borrowed items.

#### Acceptance Criteria

1. WHEN a user filters by AVAILABLE status THEN the Item Management System SHALL return only items with status AVAILABLE
2. WHEN a user filters by BORROWED status THEN the Item Management System SHALL return only items with status BORROWED
3. WHEN a user filters by UNAVAILABLE status THEN the Item Management System SHALL return only items with status UNAVAILABLE
4. WHEN a user combines status filter with other filters THEN the Item Management System SHALL return items matching all criteria
5. WHEN no status filter is provided THEN the Item Management System SHALL default to showing AVAILABLE items

### Requirement 7

**User Story:** As a user, I want to view detailed information about a specific item, so that I can decide whether to borrow it.

#### Acceptance Criteria

1. WHEN a user requests an item by ID THEN the Item Management System SHALL return the complete item details including title, description, category, image, status, and owner information
2. WHEN a user requests a non-existent item ID THEN the Item Management System SHALL return a 404 Not Found error
3. WHEN item details are returned THEN the Item Management System SHALL include owner contact information
4. WHEN item details are returned THEN the Item Management System SHALL include creation and update timestamps
5. WHEN a user views item details THEN the Item Management System SHALL return the current status of the item

### Requirement 8

**User Story:** As an item owner, I want to edit my item listing, so that I can update information or correct mistakes.

#### Acceptance Criteria

1. WHEN an item owner updates their item THEN the Item Management System SHALL save the changes and return the updated item
2. WHEN a non-owner attempts to update an item THEN the Item Management System SHALL reject the request and return a 403 Forbidden error
3. WHEN an item owner updates the title THEN the Item Management System SHALL validate the new title meets length requirements
4. WHEN an item owner updates the status THEN the Item Management System SHALL accept only valid status values
5. WHEN an item is updated THEN the Item Management System SHALL update the updatedAt timestamp

### Requirement 9

**User Story:** As an item owner, I want to delete my item listing, so that I can remove items I no longer want to lend.

#### Acceptance Criteria

1. WHEN an item owner deletes their item THEN the Item Management System SHALL remove the item from the database
2. WHEN a non-owner attempts to delete an item THEN the Item Management System SHALL reject the request and return a 403 Forbidden error
3. WHEN an item with active borrow requests is deleted THEN the Item Management System SHALL cascade delete the associated requests
4. WHEN an item is deleted THEN the Item Management System SHALL return a success confirmation
5. WHEN a deleted item is requested THEN the Item Management System SHALL return a 404 Not Found error

### Requirement 10

**User Story:** As a user, I want to see my own item listings, so that I can manage the items I've listed.

#### Acceptance Criteria

1. WHEN a user requests their own items THEN the Item Management System SHALL return all items owned by that user
2. WHEN a user views their own items THEN the Item Management System SHALL include items of all statuses
3. WHEN a user's item list is returned THEN the Item Management System SHALL support pagination
4. WHEN a user has no items THEN the Item Management System SHALL return an empty array
5. WHEN a user views their items THEN the Item Management System SHALL order them by creation date descending

### Requirement 11

**User Story:** As a developer, I want proper validation on all item fields, so that data integrity is maintained.

#### Acceptance Criteria

1. WHEN item data is submitted THEN the Item Management System SHALL validate all required fields are present
2. WHEN item data is submitted THEN the Item Management System SHALL validate field lengths and formats
3. WHEN validation fails THEN the Item Management System SHALL return specific error messages for each invalid field
4. WHEN an invalid status value is provided THEN the Item Management System SHALL reject the request with a validation error
5. WHEN item data passes validation THEN the Item Management System SHALL proceed with the requested operation

### Requirement 12

**User Story:** As a system administrator, I want items to be properly associated with their owners, so that ownership is always clear.

#### Acceptance Criteria

1. WHEN an item is created THEN the Item Management System SHALL store the owner ID from the authenticated user
2. WHEN an item is retrieved THEN the Item Management System SHALL include owner information in the response
3. WHEN a user is deleted THEN the Item Management System SHALL cascade delete all items owned by that user
4. WHEN querying items by owner THEN the Item Management System SHALL return only items belonging to that owner
5. WHEN ownership is checked THEN the Item Management System SHALL use the stored owner ID for authorization
