# RentKar Technology Stack

## Architecture
Three-tier architecture: React frontend → Spring Boot REST API → MySQL database

## Frontend Stack
- **Framework**: React 18+ with Vite build tool
- **Styling**: TailwindCSS with shadcn/ui components
- **State Management**: React Context API (see AuthContext pattern)
- **Routing**: React Router v6
- **Forms**: React Hook Form with Zod validation
- **HTTP Client**: Axios with interceptors
- **Icons**: Lucide React
- **Notifications**: react-hot-toast

## Backend Stack
- **Framework**: Spring Boot 3.2.1
- **Language**: Java 17
- **Security**: Spring Security with JWT authentication
- **ORM**: Spring Data JPA with Hibernate
- **Database**: MySQL 8.0+
- **Build Tool**: Maven
- **API Docs**: SpringDoc OpenAPI (Swagger)

## External Services
- **Image Storage**: Cloudinary
- **AI Service**: Google Gemini API for item description generation

## Development Environment
- **OS**: Windows with cmd shell
- **Database**: MySQL on localhost:3306, database `rentkar_db`
- **Ports**: Backend on 8080, Frontend on 5173

## Common Commands

### Backend
```bash
# Navigate to backend
cd backend

# Install dependencies
mvn clean install

# Run application
mvn spring-boot:run

# Run tests
mvn test

# Build JAR
mvn clean package
```

### Frontend
```bash
# Navigate to frontend
cd frontend

# Install dependencies
npm install

# Run dev server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Lint code
npm run lint
```

### Quick Start (Windows)
- Use `start-backend.bat` to launch backend
- Use `start-frontend.bat` to launch frontend

## Configuration Files
- Backend: `backend/src/main/resources/application.properties`
- Frontend: `frontend/.env` (see `.env.example`)
- Maven: `backend/pom.xml`
- NPM: `frontend/package.json`

## API Access
- Backend API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html
- Frontend: http://localhost:5173
