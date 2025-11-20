# RentKar - System Architecture

## System Overview
RentKar is a peer-to-peer item sharing platform built with a modern web stack.

## Architecture Pattern
**Three-Tier Architecture**
- **Presentation Layer**: React + TailwindCSS
- **Business Logic Layer**: Spring Boot REST API
- **Data Layer**: MySQL Database

---

## Technology Stack

### Frontend
- **Framework**: React 18+
- **Build Tool**: Vite
- **Styling**: TailwindCSS
- **UI Components**: shadcn/ui
- **State Management**: React Context API
- **HTTP Client**: Axios
- **Routing**: React Router v6
- **Form Handling**: React Hook Form + Zod
- **Icons**: Lucide React
- **Image Upload**: Cloudinary SDK

### Backend
- **Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **Security**: Spring Security + JWT
- **ORM**: Spring Data JPA
- **Validation**: Jakarta Validation
- **API Documentation**: SpringDoc OpenAPI

### Database
- **RDBMS**: MySQL 8.0+
- **Connection Pool**: HikariCP

### External Services
- **Image Storage**: Cloudinary
- **AI Service**: Google Gemini API

### Development Tools
- **Version Control**: Git + GitHub
- **API Testing**: Postman
- **Build Tools**: Maven (Backend), Vite (Frontend)

---

## System Components

### 1. Authentication Service
- User registration
- Login with JWT token generation
- Token validation and refresh
- Password encryption (BCrypt)

### 2. User Management Service
- User profile CRUD
- Role management (User, Admin)
- User search and listing

### 3. Item Management Service
- Item CRUD operations
- Image upload to Cloudinary
- Item search and filtering
- Category management
- Item status management

### 4. AI Service
- Integration with LLM API
- Auto-generate item titles
- Auto-generate item descriptions
- Prompt engineering for better results

### 5. Borrow Request Service
- Create borrow requests
- Approve/reject requests
- Track request status
- Return confirmation workflow

### 6. Admin Service (Optional)
- Remove inappropriate items
- User management
- Platform analytics

---

## Database Schema Design

### Users Table
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    phone VARCHAR(15),
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Items Table
```sql
CREATE TABLE items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    image_url VARCHAR(500),
    status ENUM('AVAILABLE', 'BORROWED', 'UNAVAILABLE') DEFAULT 'AVAILABLE',
    owner_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### Borrow Requests Table
```sql
CREATE TABLE borrow_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    item_id BIGINT NOT NULL,
    borrower_id BIGINT NOT NULL,
    lender_id BIGINT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'RETURNED', 'COMPLETED') DEFAULT 'PENDING',
    request_message TEXT,
    borrow_date DATE,
    return_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    FOREIGN KEY (borrower_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (lender_id) REFERENCES users(id) ON DELETE CASCADE
);
```

---

## API Endpoints Design

### Authentication Endpoints
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token
- `POST /api/auth/refresh` - Refresh JWT token
- `GET /api/auth/me` - Get current user info

### User Endpoints
- `GET /api/users/{id}` - Get user profile
- `PUT /api/users/{id}` - Update user profile
- `GET /api/users/{id}/items` - Get user's items

### Item Endpoints
- `POST /api/items` - Create new item
- `GET /api/items` - Get all available items (with filters)
- `GET /api/items/{id}` - Get item details
- `PUT /api/items/{id}` - Update item
- `DELETE /api/items/{id}` - Delete item
- `POST /api/items/upload-image` - Upload item image
- `POST /api/items/generate-description` - AI generate description

### Borrow Request Endpoints
- `POST /api/requests` - Create borrow request
- `GET /api/requests/sent` - Get requests sent by user
- `GET /api/requests/received` - Get requests received by user
- `PUT /api/requests/{id}/approve` - Approve request
- `PUT /api/requests/{id}/reject` - Reject request
- `PUT /api/requests/{id}/return` - Mark as returned
- `PUT /api/requests/{id}/complete` - Complete transaction

### Admin Endpoints (Optional)
- `DELETE /api/admin/items/{id}` - Remove item
- `DELETE /api/admin/users/{id}` - Remove user
- `GET /api/admin/stats` - Get platform statistics

---

## Security Considerations

1. **Authentication**: JWT-based stateless authentication
2. **Authorization**: Role-based access control (RBAC)
3. **Password Security**: BCrypt hashing with salt
4. **API Security**: CORS configuration, rate limiting
5. **Input Validation**: Server-side validation for all inputs
6. **SQL Injection Prevention**: Parameterized queries via JPA
7. **XSS Prevention**: Input sanitization and output encoding

---

## Deployment Architecture (Future)

```
[Client Browser] 
    ↓
[React App - Vercel/Netlify]
    ↓
[Spring Boot API - Railway/Render]
    ↓
[MySQL Database - Railway/PlanetScale]
    ↓
[Cloudinary CDN]
```

---

## Development Workflow

1. **Feature Branch Strategy**
   - `main` - Production-ready code
   - `develop` - Integration branch
   - `feature/*` - Feature branches
   - `bugfix/*` - Bug fix branches

2. **Code Review Process**
   - All changes via Pull Requests
   - At least 1 team member review
   - Pass all tests before merge

3. **Testing Strategy**
   - Unit tests for services
   - Integration tests for APIs
   - Manual testing for UI flows

---

## Performance Optimization

1. **Backend**
   - Database indexing on frequently queried columns
   - Connection pooling with HikariCP
   - Caching for frequently accessed data

2. **Frontend**
   - Lazy loading for routes
   - Image optimization before upload
   - Debouncing for search inputs

3. **Database**
   - Proper indexing strategy
   - Query optimization
   - Regular maintenance

---

## Monitoring & Logging

- **Backend Logging**: SLF4J + Logback
- **Error Tracking**: Console logs (production: Sentry/similar)
- **API Monitoring**: Spring Boot Actuator

---

*Last Updated: 20-11-2025*
