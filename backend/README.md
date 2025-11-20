# RentKar Backend

Spring Boot REST API for RentKar platform.

## 🛠️ Tech Stack

- **Framework**: Spring Boot 3.2.1
- **Language**: Java 17+
- **Database**: MySQL 8.0+
- **Security**: Spring Security + JWT
- **ORM**: Spring Data JPA
- **Build Tool**: Maven

## 📁 Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/rentkar/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── repository/      # JPA repositories
│   │   │   ├── service/         # Business logic
│   │   │   ├── security/        # Security config & JWT
│   │   │   ├── exception/       # Custom exceptions
│   │   │   └── util/            # Utility classes
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-example.properties
│   └── test/                    # Unit and integration tests
├── pom.xml
└── README.md
```

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.8+
- MySQL 8.0+

### Setup

1. **Create Database**
   ```sql
   CREATE DATABASE rentkar_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'rentkar_user'@'localhost' IDENTIFIED BY 'RentKar@2024';
   GRANT ALL PRIVILEGES ON rentkar_db.* TO 'rentkar_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

2. **Configure Application**
   ```bash
   # Copy example properties
   cp src/main/resources/application-example.properties src/main/resources/application.properties
   
   # Edit application.properties with your credentials
   ```

3. **Install Dependencies**
   ```bash
   mvn clean install
   ```

4. **Run Application**
   ```bash
   mvn spring-boot:run
   ```

The API will be available at `http://localhost:8080/api`

## 📚 API Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs JSON**: http://localhost:8080/api-docs

## 🔑 Environment Variables

Configure these in `application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/rentkar_db
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT
jwt.secret=your_secret_key
jwt.expiration=86400000

# Cloudinary
cloudinary.cloud-name=your_cloud_name
cloudinary.api-key=your_api_key
cloudinary.api-secret=your_api_secret

# Gemini AI
gemini.api-key=your_gemini_api_key
```

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=UserServiceTest

# Run with coverage
mvn clean test jacoco:report
```

## 📦 Build

```bash
# Build JAR file
mvn clean package

# Skip tests
mvn clean package -DskipTests

# Run JAR
java -jar target/rentkar-backend-1.0.0.jar
```

## 🔐 Security

- JWT-based authentication
- BCrypt password hashing
- CORS configuration
- Input validation
- SQL injection prevention via JPA

## 📝 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token
- `GET /api/auth/me` - Get current user

### Items
- `GET /api/items` - Get all items
- `GET /api/items/{id}` - Get item by ID
- `POST /api/items` - Create new item
- `PUT /api/items/{id}` - Update item
- `DELETE /api/items/{id}` - Delete item
- `POST /api/items/upload-image` - Upload item image
- `POST /api/items/generate-description` - AI generate description

### Borrow Requests
- `POST /api/requests` - Create borrow request
- `GET /api/requests/sent` - Get sent requests
- `GET /api/requests/received` - Get received requests
- `PUT /api/requests/{id}/approve` - Approve request
- `PUT /api/requests/{id}/reject` - Reject request
- `PUT /api/requests/{id}/return` - Mark as returned
- `PUT /api/requests/{id}/complete` - Complete transaction

See [API_CONTRACT.md](../API_CONTRACT.md) for detailed documentation.

## 🐛 Debugging

Enable debug logging in `application.properties`:

```properties
logging.level.com.rentkar=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

## 📊 Database Schema

Tables:
- `users` - User accounts
- `items` - Listed items
- `borrow_requests` - Borrow transactions

See [ARCHITECTURE.md](../ARCHITECTURE.md) for detailed schema.

## 🤝 Contributing

1. Create feature branch
2. Make changes
3. Write tests
4. Submit pull request

## 📄 License

Educational project for PW IOI – School of Technology.
