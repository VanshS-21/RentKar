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

### 3.7 Generate Item Description (AI)
**Endpoint**: `POST /items/generate-description`

**Headers**: `Authorization: Bearer <token>`

**Request Body**:
```json
{
  "itemName": "Scientific Calculator",
  "category": "Electronics",
  "additionalInfo": "Casio FX-991EX"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "title": "Scientific Calculator - Casio FX-991EX",
    "description": "High-quality scientific calculator perfect for engineering and mathematics students. Features include natural textbook display, 552 functions, and solar power. Ideal for exams and daily coursework."
  }
}
```

---

## 4. Borrow Request APIs

### 4.1 Create Borrow Request
**Endpoint**: `POST /requests`

**Headers**: `Authorization: Bearer <token>`

**Request Body**:
```json
{
  "itemId": 1,
  "requestMessage": "Hi, I need this calculator for my exams next week. Will return in 5 days.",
  "borrowDate": "2024-01-20",
  "returnDate": "2024-01-25"
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "message": "Borrow request sent successfully",
  "data": {
    "id": 1,
    "item": {
      "id": 1,
      "title": "Scientific Calculator"
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
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

---

### 4.2 Get Sent Requests (Borrower View)
**Endpoint**: `GET /requests/sent`

**Headers**: `Authorization: Bearer <token>`

**Query Parameters**:
- `status` (optional): Filter by status

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
        "imageUrl": "https://res.cloudinary.com/..."
      },
      "lender": {
        "id": 1,
        "username": "john_doe",
        "fullName": "John Doe"
      },
      "status": "PENDING",
      "borrowDate": "2024-01-20",
      "returnDate": "2024-01-25",
      "createdAt": "2024-01-15T10:30:00"
    }
  ]
}
```

---

### 4.3 Get Received Requests (Lender View)
**Endpoint**: `GET /requests/received`

**Headers**: `Authorization: Bearer <token>`

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
        "imageUrl": "https://res.cloudinary.com/..."
      },
      "borrower": {
        "id": 2,
        "username": "jane_doe",
        "fullName": "Jane Doe",
        "phone": "9876543210"
      },
      "status": "PENDING",
      "requestMessage": "Hi, I need this calculator...",
      "borrowDate": "2024-01-20",
      "returnDate": "2024-01-25",
      "createdAt": "2024-01-15T10:30:00"
    }
  ]
}
```

---

### 4.4 Approve Request
**Endpoint**: `PUT /requests/{requestId}/approve`

**Headers**: `Authorization: Bearer <token>`

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Request approved successfully",
  "data": {
    "id": 1,
    "status": "APPROVED",
    "updatedAt": "2024-01-16T10:30:00"
  }
}
```

---

### 4.5 Reject Request
**Endpoint**: `PUT /requests/{requestId}/reject`

**Headers**: `Authorization: Bearer <token>`

**Request Body** (optional):
```json
{
  "rejectionReason": "Item not available for those dates"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Request rejected",
  "data": {
    "id": 1,
    "status": "REJECTED"
  }
}
```

---

### 4.6 Mark as Returned
**Endpoint**: `PUT /requests/{requestId}/return`

**Headers**: `Authorization: Bearer <token>`

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Item marked as returned",
  "data": {
    "id": 1,
    "status": "RETURNED"
  }
}
```

---

### 4.7 Complete Transaction
**Endpoint**: `PUT /requests/{requestId}/complete`

**Headers**: `Authorization: Bearer <token>`

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Transaction completed successfully",
  "data": {
    "id": 1,
    "status": "COMPLETED"
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

*Last Updated: Week 1*
