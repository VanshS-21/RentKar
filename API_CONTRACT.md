# RentKar - API Contract Documentation

## Base URL
- **Development**: `http://localhost:8080/api`
- **Production**: `TBD`

## Authentication
All protected endpoints require JWT token in header:
```
Authorization: Bearer <jwt_token>
```

---

## 1. Authentication APIs

### 1.1 Register User
**Endpoint**: `POST /auth/register`

**Request Body**:
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "fullName": "John Doe",
  "phone": "1234567890"
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "role": "USER"
  }
}
```

**Error Response** (400 Bad Request):
```json
{
  "success": false,
  "message": "Username already exists",
  "errors": ["username: Username is already taken"]
}
```

---

### 1.2 Login
**Endpoint**: `POST /auth/login`

**Request Body**:
```json
{
  "username": "john_doe",
  "password": "SecurePass123"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "user": {
      "id": 1,
      "username": "john_doe",
      "email": "john@example.com",
      "fullName": "John Doe",
      "role": "USER"
    }
  }
}
```

---

### 1.3 Get Current User
**Endpoint**: `GET /auth/me`

**Headers**: `Authorization: Bearer <token>`

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "phone": "1234567890",
    "role": "USER",
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

---

## 2. User APIs

### 2.1 Get User Profile
**Endpoint**: `GET /users/{userId}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "john_doe",
    "fullName": "John Doe",
    "email": "john@example.com",
    "phone": "1234567890",
    "totalItemsListed": 5,
    "totalItemsBorrowed": 3,
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

---

### 2.2 Update User Profile
**Endpoint**: `PUT /users/{userId}`

**Headers**: `Authorization: Bearer <token>`

**Request Body**:
```json
{
  "fullName": "John Updated Doe",
  "phone": "9876543210"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Profile updated successfully",
  "data": {
    "id": 1,
    "username": "john_doe",
    "fullName": "John Updated Doe",
    "phone": "9876543210"
  }
}
```

---

## 3. Item APIs

### 3.1 Create Item
**Endpoint**: `POST /items`

**Headers**: `Authorization: Bearer <token>`

**Request Body**:
```json
{
  "title": "Scientific Calculator",
  "description": "Casio FX-991EX, barely used, perfect condition",
  "category": "Electronics",
  "imageUrl": "https://res.cloudinary.com/..."
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "message": "Item created successfully",
  "data": {
    "id": 1,
    "title": "Scientific Calculator",
    "description": "Casio FX-991EX, barely used, perfect condition",
    "category": "Electronics",
    "imageUrl": "https://res.cloudinary.com/...",
    "status": "AVAILABLE",
    "owner": {
      "id": 1,
      "username": "john_doe",
      "fullName": "John Doe"
    },
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

---

### 3.2 Get All Items
**Endpoint**: `GET /items`

**Query Parameters**:
- `category` (optional): Filter by category
- `status` (optional): Filter by status (AVAILABLE, BORROWED, UNAVAILABLE)
- `search` (optional): Search in title and description
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)

**Example**: `GET /items?category=Electronics&status=AVAILABLE&page=0&size=10`

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": 1,
        "title": "Scientific Calculator",
        "description": "Casio FX-991EX, barely used",
        "category": "Electronics",
        "imageUrl": "https://res.cloudinary.com/...",
        "status": "AVAILABLE",
        "owner": {
          "id": 1,
          "username": "john_doe",
          "fullName": "John Doe"
        },
        "createdAt": "2024-01-15T10:30:00"
      }
    ],
    "pagination": {
      "currentPage": 0,
      "totalPages": 5,
      "totalItems": 50,
      "pageSize": 10
    }
  }
}
```

---

### 3.3 Get Item Details
**Endpoint**: `GET /items/{itemId}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Scientific Calculator",
    "description": "Casio FX-991EX, barely used, perfect condition",
    "category": "Electronics",
    "imageUrl": "https://res.cloudinary.com/...",
    "status": "AVAILABLE",
    "owner": {
      "id": 1,
      "username": "john_doe",
      "fullName": "John Doe",
      "email": "john@example.com",
      "phone": "1234567890"
    },
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
}
```

---

### 3.4 Update Item
**Endpoint**: `PUT /items/{itemId}`

**Headers**: `Authorization: Bearer <token>`

**Request Body**:
```json
{
  "title": "Scientific Calculator - Updated",
  "description": "Updated description",
  "category": "Electronics",
  "status": "AVAILABLE"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Item updated successfully",
  "data": {
    "id": 1,
    "title": "Scientific Calculator - Updated",
    "description": "Updated description",
    "status": "AVAILABLE"
  }
}
```

---

### 3.5 Delete Item
**Endpoint**: `DELETE /items/{itemId}`

**Headers**: `Authorization: Bearer <token>`

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Item deleted successfully"
}
```

---

### 3.6 Upload Item Image
**Endpoint**: `POST /items/upload-image`

**Headers**: 
- `Authorization: Bearer <token>`
- `Content-Type: multipart/form-data`

**Request Body** (Form Data):
- `image`: File (JPEG, PNG, max 5MB)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Image uploaded successfully",
  "data": {
    "imageUrl": "https://res.cloudinary.com/rentkar/image/upload/v1234567890/items/abc123.jpg",
    "publicId": "items/abc123"
  }
}
```

---

### 3.7 Generate Item Title (AI)
**Endpoint**: `POST /items/generate-title`

**Headers**: `Authorization: Bearer <token>`

**Description**: Generate an AI-powered title for an item using Google Gemini API. Rate limited to 10 requests per user per hour.

**Request Body**:
```json
{
  "itemName": "Scientific Calculator",
  "category": "Electronics",
  "additionalInfo": "Casio FX-991EX, barely used",
  "condition": "Like New",
  "specifications": "552 functions, solar powered"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Title generated successfully",
  "data": {
    "content": "Scientific Calculator - Casio FX-991EX (Like New)",
    "tokenCount": 45,
    "responseTimeMs": 1250
  }
}
```

**Error Response** (429 Too Many Requests):
```json
{
  "success": false,
  "message": "Rate limit exceeded. Please try again later.",
  "errors": ["You have reached the maximum of 10 AI generation requests per hour"],
  "retryAfter": 3600,
  "timestamp": "2024-01-15T10:30:00"
}
```

**Error Response** (503 Service Unavailable):
```json
{
  "success": false,
  "message": "AI generation service is currently unavailable",
  "errors": ["Gemini API is not configured or unavailable"],
  "timestamp": "2024-01-15T10:30:00"
}
```

**Error Response** (408 Request Timeout):
```json
{
  "success": false,
  "message": "AI generation request timed out",
  "errors": ["Request exceeded 30 second timeout"],
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### 3.8 Generate Item Description (AI)
**Endpoint**: `POST /items/generate-description`

**Headers**: `Authorization: Bearer <token>`

**Description**: Generate an AI-powered description for an item using Google Gemini API. Rate limited to 10 requests per user per hour.

**Request Body**:
```json
{
  "itemName": "Scientific Calculator",
  "category": "Electronics",
  "additionalInfo": "Casio FX-991EX, barely used",
  "condition": "Like New",
  "specifications": "552 functions, solar powered, natural textbook display"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Description generated successfully",
  "data": {
    "content": "High-quality scientific calculator perfect for engineering and mathematics students. This Casio FX-991EX features 552 functions, natural textbook display, and solar power for reliable performance. In like-new condition with barely any use. Ideal for exams, homework, and daily coursework. Save money by borrowing instead of buying!",
    "tokenCount": 128,
    "responseTimeMs": 1850
  }
}
```

**Error Response** (429 Too Many Requests):
```json
{
  "success": false,
  "message": "Rate limit exceeded. Please try again later.",
  "errors": ["You have reached the maximum of 10 AI generation requests per hour"],
  "retryAfter": 3600,
  "timestamp": "2024-01-15T10:30:00"
}
```

**Error Response** (400 Bad Request):
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    "itemName: Item name is required",
    "category: Category is required"
  ],
  "timestamp": "2024-01-15T10:30:00"
}
```

**Error Response** (503 Service Unavailable):
```json
{
  "success": false,
  "message": "AI generation service is currently unavailable",
  "errors": ["Gemini API is not configured or unavailable"],
  "timestamp": "2024-01-15T10:30:00"
}
```

**Rate Limiting Details**:
- **Limit**: 10 requests per user per hour
- **Tracking**: Based on authenticated user ID (or IP address for unauthenticated users)
- **Reset**: Rolling 1-hour window from first request
- **Headers**: Response includes `X-RateLimit-Remaining` and `X-RateLimit-Reset` headers
- **Retry-After**: Included in 429 responses (seconds until next request allowed)

---

## 4. Borrow Request APIs

### 4.1 Create Borrow Request
**Endpoint**: `POST /requests`

**Headers**: `Authorization: Bearer <token>`

**Description**: Create a new borrow request for an available item. The borrower must be authenticated and cannot request their own items. The borrow date must not be in the past, and the return date must be after the borrow date.

**Authorization**: Authenticated users only (borrower role)

**Request Body**:
```json
{
  "itemId": 1,
  "requestMessage": "Hi, I need this calculator for my exams next week. Will return in 5 days.",
  "borrowDate": "2024-01-20",
  "returnDate": "2024-01-25"
}
```

**Validation Rules**:
- `itemId`: Required, must exist and be AVAILABLE
- `borrowDate`: Required, must not be in the past
- `returnDate`: Required, must be after borrowDate
- `requestMessage`: Optional, max 500 characters
- Borrower cannot be the item owner

**Response** (201 Created):
```json
{
  "success": true,
  "message": "Borrow request sent successfully",
  "data": {
    "id": 1,
    "item": {
      "id": 1,
      "title": "Scientific Calculator",
      "imageUrl": "https://res.cloudinary.com/...",
      "category": "Electronics"
    },
    "borrower": {
      "id": 2,
      "username": "jane_doe",
      "fullName": "Jane Doe"
    },
    "lender": {
      "id": 1,
      "username": "john_doe",
      "fullName": "John Doe"
    },
    "status": "PENDING",
    "requestMessage": "Hi, I need this calculator for my exams...",
    "borrowDate": "2024-01-20",
    "returnDate": "2024-01-25",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
}
```

**Error Response** (400 Bad Request - Validation):
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    "borrowDate: Borrow date cannot be in the past",
    "returnDate: Return date must be after borrow date"
  ],
  "timestamp": "2024-01-15T10:30:00"
}
```

**Error Response** (400 Bad Request - Self-borrowing):
```json
{
  "success": false,
  "message": "Cannot borrow your own item",
  "errors": ["You cannot create a borrow request for your own item"],
  "timestamp": "2024-01-15T10:30:00"
}
```

**Error Response** (400 Bad Request - Item unavailable):
```json
{
  "success": false,
  "message": "Item is not available for borrowing",
  "errors": ["The requested item is currently borrowed or unavailable"],
  "timestamp": "2024-01-15T10:30:00"
}
```

**Error Response** (404 Not Found):
```json
{
  "success": false,
  "message": "Item not found",
  "errors": ["Item with ID 1 does not exist"],
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### 4.2 Get Sent Requests (Borrower View)
**Endpoint**: `GET /requests/sent`

**Headers**: `Authorization: Bearer <token>`

**Description**: Retrieve all borrow requests created by the authenticated user. Returns requests where the user is the borrower.

**Authorization**: Authenticated users only (borrower role)

**Query Parameters**:
- `status` (optional): Filter by status (PENDING, APPROVED, REJECTED, RETURNED, COMPLETED)

**Example**: `GET /requests/sent?status=PENDING`

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "item": {
        "id": 1,
        "title": "Scientific Calculator",
        "imageUrl": "https://res.cloudinary.com/...",
        "category": "Electronics",
        "status": "BORROWED"
      },
      "lender": {
        "id": 1,
        "username": "john_doe",
        "fullName": "John Doe",
        "email": "john@example.com",
        "phone": "1234567890"
      },
      "status": "APPROVED",
      "requestMessage": "Hi, I need this calculator...",
      "responseMessage": "Sure, you can borrow it!",
      "borrowDate": "2024-01-20",
      "returnDate": "2024-01-25",
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-16T10:30:00"
    }
  ]
}
```

**Note**: Lender contact information (email, phone) is only included for APPROVED requests.

---

### 4.3 Get Received Requests (Lender View)
**Endpoint**: `GET /requests/received`

**Headers**: `Authorization: Bearer <token>`

**Description**: Retrieve all borrow requests for items owned by the authenticated user. Returns requests where the user is the lender.

**Authorization**: Authenticated users only (lender role)

**Query Parameters**:
- `status` (optional): Filter by status (PENDING, APPROVED, REJECTED, RETURNED, COMPLETED)

**Example**: `GET /requests/received?status=PENDING`

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "item": {
        "id": 1,
        "title": "Scientific Calculator",
        "imageUrl": "https://res.cloudinary.com/...",
        "category": "Electronics",
        "status": "AVAILABLE"
      },
      "borrower": {
        "id": 2,
        "username": "jane_doe",
        "fullName": "Jane Doe",
        "email": "jane@example.com",
        "phone": "9876543210"
      },
      "status": "PENDING",
      "requestMessage": "Hi, I need this calculator...",
      "borrowDate": "2024-01-20",
      "returnDate": "2024-01-25",
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ]
}
```

**Note**: Borrower contact information (email, phone) is included for all requests to help lenders make decisions.

---

### 4.4 Get Request Details
**Endpoint**: `GET /requests/{requestId}`

**Headers**: `Authorization: Bearer <token>`

**Description**: Retrieve detailed information about a specific borrow request. Only accessible by the borrower or lender involved in the request.

**Authorization**: Authenticated users only (must be borrower or lender)

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "item": {
      "id": 1,
      "title": "Scientific Calculator",
      "description": "Casio FX-991EX, barely used",
      "imageUrl": "https://res.cloudinary.com/...",
      "category": "Electronics",
      "status": "BORROWED"
    },
    "borrower": {
      "id": 2,
      "username": "jane_doe",
      "fullName": "Jane Doe",
      "email": "jane@example.com",
      "phone": "9876543210"
    },
    "lender": {
      "id": 1,
      "username": "john_doe",
      "fullName": "John Doe",
      "email": "john@example.com",
      "phone": "1234567890"
    },
    "status": "APPROVED",
    "requestMessage": "Hi, I need this calculator for my exams...",
    "responseMessage": "Sure, you can borrow it!",
    "borrowDate": "2024-01-20",
    "returnDate": "2024-01-25",
    "returnedAt": null,
    "completedAt": null,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-16T10:30:00"
  }
}
```

**Error Response** (403 Forbidden):
```json
{
  "success": false,
  "message": "Access denied",
  "errors": ["You do not have permission to view this request"],
  "timestamp": "2024-01-15T10:30:00"
}
```

**Error Response** (404 Not Found):
```json
{
  "success": false,
  "message": "Request not found",
  "errors": ["Borrow request with ID 1 does not exist"],
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### 4.5 Approve Request
**Endpoint**: `POST /requests/{requestId}/approve`

**Headers**: `Authorization: Bearer <token>`

**Description**: Approve a pending borrow request. Changes request status to APPROVED and item status to BORROWED. Only the item owner (lender) can approve requests.

**Authorization**: Authenticated users only (must be the lender/item owner)

**Request Body** (optional):
```json
{
  "responseMessage": "Sure, you can borrow it! Please take good care of it."
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Request approved successfully",
  "data": {
    "id": 1,
    "status": "APPROVED",
    "responseMessage": "Sure, you can borrow it! Please take good care of it.",
    "updatedAt": "2024-01-16T10:30:00"
  }
}
```

**Error Response** (400 Bad Request - Invalid status):
```json
{
  "success": false,
  "message": "Cannot approve request",
  "errors": ["Only PENDING requests can be approved"],
  "timestamp": "2024-01-16T10:30:00"
}
```

**Error Response** (400 Bad Request - Item unavailable):
```json
{
  "success": false,
  "message": "Item is not available",
  "errors": ["The item is no longer available for borrowing"],
  "timestamp": "2024-01-16T10:30:00"
}
```

**Error Response** (403 Forbidden):
```json
{
  "success": false,
  "message": "Access denied",
  "errors": ["Only the item owner can approve this request"],
  "timestamp": "2024-01-16T10:30:00"
}
```

---

### 4.6 Reject Request
**Endpoint**: `POST /requests/{requestId}/reject`

**Headers**: `Authorization: Bearer <token>`

**Description**: Reject a pending borrow request. Changes request status to REJECTED and keeps item status as AVAILABLE. Only the item owner (lender) can reject requests.

**Authorization**: Authenticated users only (must be the lender/item owner)

**Request Body** (optional):
```json
{
  "responseMessage": "Sorry, I need the item during those dates"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Request rejected",
  "data": {
    "id": 1,
    "status": "REJECTED",
    "responseMessage": "Sorry, I need the item during those dates",
    "updatedAt": "2024-01-16T10:30:00"
  }
}
```

**Error Response** (400 Bad Request):
```json
{
  "success": false,
  "message": "Cannot reject request",
  "errors": ["Only PENDING requests can be rejected"],
  "timestamp": "2024-01-16T10:30:00"
}
```

**Error Response** (403 Forbidden):
```json
{
  "success": false,
  "message": "Access denied",
  "errors": ["Only the item owner can reject this request"],
  "timestamp": "2024-01-16T10:30:00"
}
```

---

### 4.7 Mark as Returned
**Endpoint**: `POST /requests/{requestId}/return`

**Headers**: `Authorization: Bearer <token>`

**Description**: Mark an approved request as returned. Changes request status to RETURNED and item status back to AVAILABLE. Records the return timestamp. Only the item owner (lender) can mark items as returned.

**Authorization**: Authenticated users only (must be the lender/item owner)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Item marked as returned",
  "data": {
    "id": 1,
    "status": "RETURNED",
    "returnedAt": "2024-01-25T14:30:00",
    "updatedAt": "2024-01-25T14:30:00"
  }
}
```

**Error Response** (400 Bad Request):
```json
{
  "success": false,
  "message": "Cannot mark as returned",
  "errors": ["Only APPROVED requests can be marked as returned"],
  "timestamp": "2024-01-25T14:30:00"
}
```

**Error Response** (403 Forbidden):
```json
{
  "success": false,
  "message": "Access denied",
  "errors": ["Only the item owner can mark this request as returned"],
  "timestamp": "2024-01-25T14:30:00"
}
```

---

### 4.8 Confirm Return (Complete Transaction)
**Endpoint**: `POST /requests/{requestId}/confirm`

**Headers**: `Authorization: Bearer <token>`

**Description**: Confirm that an item has been returned and complete the transaction. Changes request status to COMPLETED and records the completion timestamp. Only the borrower can confirm returns.

**Authorization**: Authenticated users only (must be the borrower)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Return confirmed successfully",
  "data": {
    "id": 1,
    "status": "COMPLETED",
    "completedAt": "2024-01-25T15:00:00",
    "updatedAt": "2024-01-25T15:00:00"
  }
}
```

**Error Response** (400 Bad Request):
```json
{
  "success": false,
  "message": "Cannot confirm return",
  "errors": ["Only RETURNED requests can be confirmed"],
  "timestamp": "2024-01-25T15:00:00"
}
```

**Error Response** (403 Forbidden):
```json
{
  "success": false,
  "message": "Access denied",
  "errors": ["Only the borrower can confirm the return"],
  "timestamp": "2024-01-25T15:00:00"
}
```

---

### 4.9 Cancel Request
**Endpoint**: `DELETE /requests/{requestId}`

**Headers**: `Authorization: Bearer <token>`

**Description**: Cancel a pending borrow request. Deletes the request from the system. Only the borrower can cancel their own pending requests. Does not affect item status.

**Authorization**: Authenticated users only (must be the borrower)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Request cancelled successfully"
}
```

**Error Response** (400 Bad Request):
```json
{
  "success": false,
  "message": "Cannot cancel request",
  "errors": ["Only PENDING requests can be cancelled"],
  "timestamp": "2024-01-16T10:30:00"
}
```

**Error Response** (403 Forbidden):
```json
{
  "success": false,
  "message": "Access denied",
  "errors": ["Only the borrower can cancel this request"],
  "timestamp": "2024-01-16T10:30:00"
}
```

---

### 4.10 Get Request Statistics
**Endpoint**: `GET /requests/statistics`

**Headers**: `Authorization: Bearer <token>`

**Description**: Get statistics about the authenticated user's borrow requests, including counts by status for both sent and received requests.

**Authorization**: Authenticated users only

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "sent": {
      "pending": 2,
      "approved": 3,
      "rejected": 1,
      "returned": 1,
      "completed": 5,
      "total": 12
    },
    "received": {
      "pending": 4,
      "approved": 2,
      "rejected": 2,
      "returned": 1,
      "completed": 8,
      "total": 17
    }
  }
}
```

---

## 5. Admin APIs (Optional)

### 5.1 Delete Item (Admin)
**Endpoint**: `DELETE /admin/items/{itemId}`

**Headers**: `Authorization: Bearer <admin_token>`

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Item removed by admin"
}
```

---

### 5.2 Get Platform Statistics
**Endpoint**: `GET /admin/stats`

**Headers**: `Authorization: Bearer <admin_token>`

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "totalUsers": 150,
    "totalItems": 320,
    "totalRequests": 450,
    "activeRequests": 25,
    "completedTransactions": 380
  }
}
```

---

## Error Response Format

All error responses follow this structure:

```json
{
  "success": false,
  "message": "Error message",
  "errors": ["Detailed error 1", "Detailed error 2"],
  "timestamp": "2024-01-15T10:30:00"
}
```

### Common HTTP Status Codes
- `200 OK` - Success
- `201 Created` - Resource created
- `400 Bad Request` - Validation error
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

---

## Request Status Flow

```
PENDING → APPROVED → RETURNED → COMPLETED
   ↓
REJECTED
```

## Item Status Flow

```
AVAILABLE → BORROWED → AVAILABLE
   ↓
UNAVAILABLE
```

---

## Implementation Status

### Implemented Endpoints ✅
- **Authentication APIs** (1.1 - 1.3): Fully implemented and tested
- **Item APIs** (3.1 - 3.8): Fully implemented and tested
  - All CRUD operations working
  - Image upload with Cloudinary integration
  - AI-powered title and description generation with Gemini API
  - Rate limiting for AI generation (10 requests/hour per user)
  - Search and filter functionality
  - Pagination support
  - Owner-based authorization
- **Borrow Request APIs** (4.1 - 4.10): Fully implemented and tested
  - Complete request lifecycle (create → approve/reject → return → complete)
  - Request filtering by status
  - Authorization checks for all actions
  - Statistics and analytics
  - Cancellation workflow

### Pending Endpoints 🔄
- **User APIs** (2.1 - 2.2): Planned for future implementation
- **Admin APIs** (5.1 - 5.2): Optional feature

---

## Borrow Request Workflow

### Status Transitions

```
PENDING → APPROVED → RETURNED → COMPLETED
   ↓
REJECTED
   ↓
CANCELLED (deleted)
```

### Authorization Matrix

| Action | Endpoint | Who Can Perform | Required Status |
|--------|----------|-----------------|-----------------|
| Create Request | POST /requests | Borrower (not owner) | Item: AVAILABLE |
| View Sent Requests | GET /requests/sent | Borrower | Any |
| View Received Requests | GET /requests/received | Lender | Any |
| View Request Details | GET /requests/{id} | Borrower or Lender | Any |
| Approve Request | POST /requests/{id}/approve | Lender only | PENDING |
| Reject Request | POST /requests/{id}/reject | Lender only | PENDING |
| Mark as Returned | POST /requests/{id}/return | Lender only | APPROVED |
| Confirm Return | POST /requests/{id}/confirm | Borrower only | RETURNED |
| Cancel Request | DELETE /requests/{id} | Borrower only | PENDING |
| Get Statistics | GET /requests/statistics | Any authenticated user | N/A |

### Item Status Changes

| Request Action | Item Status Change |
|----------------|-------------------|
| Create Request | No change (remains AVAILABLE) |
| Approve Request | AVAILABLE → BORROWED |
| Reject Request | No change (remains AVAILABLE) |
| Mark as Returned | BORROWED → AVAILABLE |
| Confirm Return | No change (remains AVAILABLE) |
| Cancel Request | No change |

---

*Last Updated: November 21, 2025 - Borrow Workflow Complete*
