# RentKar 🎒

A peer-to-peer item sharing platform for college students.

> 📚 **Documentation**: See [DOCS_SUMMARY.md](./DOCS_SUMMARY.md) for a complete documentation overview | [DOCS_INDEX.md](./DOCS_INDEX.md) for navigation

## 📋 Project Overview

RentKar enables students to borrow and lend items within their campus community. Built with modern web technologies and AI-powered features.

**Domain**: Web Development + AI  
**Team Size**: 3 Members  
**Duration**: 10-12 Weeks

## 🛠️ Tech Stack

### Frontend
- **Framework**: React 18+ with Vite
- **Styling**: TailwindCSS
- **UI Components**: shadcn/ui
- **State Management**: React Context API
- **Form Handling**: React Hook Form
- **HTTP Client**: Axios
- **Routing**: React Router v6

### Backend
- **Framework**: Spring Boot 3.x
- **Language**: Java 25
- **Security**: Spring Security + JWT
- **ORM**: Spring Data JPA
- **Database**: MySQL 8.0+
- **Build Tool**: Maven

### External Services
- **Image Storage**: Cloudinary
- **AI Service**: Google Gemini API

## 📁 Project Structure

```
RentKar/
├── backend/                 # Spring Boot backend
│   ├── src/main/java/       # Java source code
│   ├── src/main/resources/  # Configuration files
│   ├── pom.xml              # Maven dependencies
│   └── README.md
├── frontend/                # React frontend
│   ├── src/                 # React components
│   ├── package.json         # npm dependencies
│   └── README.md
├── start-backend.bat        # Quick start script for backend
├── start-frontend.bat       # Quick start script for frontend
├── PRD.md                   # Product requirements
├── ARCHITECTURE.md          # System architecture
├── API_CONTRACT.md          # API documentation
├── WIREFRAMES.md            # UI designs
├── TEAM_ROLES.md            # Team structure
├── SETUP_GUIDE.md           # Installation guide
└── README.md                # This file
```

## 🚀 Quick Start

### Prerequisites
- ✅ Java 25
- ✅ Maven
- ✅ Node.js & npm
- ✅ MySQL 8.0
- ✅ Database: `rentkar_db` (already configured)

### Run the Application

**Option 1: Using Batch Files (Windows)**
1. Double-click `start-backend.bat`
2. Double-click `start-frontend.bat`
3. Open http://localhost:5173

**Option 2: Manual Start**

Terminal 1 - Backend:
```bash
cd backend
mvn spring-boot:run
```

Terminal 2 - Frontend:
```bash
cd frontend
npm run dev
```

### First Time Setup

If you haven't installed dependencies yet:

```bash
# Backend
cd backend
mvn clean install

# Frontend
cd frontend
npm install
```

### Access the Application
- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080/api
- **API Docs**: http://localhost:8080/swagger-ui.html

## 🔑 Configuration

### Backend Configuration
Configuration is in `backend/src/main/resources/application.properties`

**Pre-configured for development:**
- Database: `rentkar_db` on localhost:3306
- User: `rentkar_user` / `RentKar@2024`
- JWT, Cloudinary, and Gemini API keys included

⚠️ **For production**: Update all credentials and API keys

### AI Generation Setup (Google Gemini API)

The AI-powered item description generation feature requires a Google Gemini API key.

#### Getting Your Gemini API Key

1. **Visit Google AI Studio**
   - Go to https://makersuite.google.com/app/apikey
   - Sign in with your Google account

2. **Create API Key**
   - Click "Create API Key"
   - Select or create a Google Cloud project
   - Copy the generated API key

3. **Add to Configuration**
   - Open `backend/src/main/resources/application.properties`
   - Find the line: `gemini.api-key=your_gemini_api_key_here`
   - Replace `your_gemini_api_key_here` with your actual API key
   - Save the file

#### Configuration Options

The following AI generation parameters can be customized in `application.properties`:

```properties
# Required: Your Gemini API key
gemini.api-key=your_gemini_api_key_here

# Optional: API endpoint (default shown)
gemini.api-endpoint=https://generativelanguage.googleapis.com/v1beta

# Optional: Model to use (default: gemini-pro)
gemini.model=gemini-pro

# Optional: Enable/disable AI features (default: true)
ai.generation.enabled=true

# Optional: Rate limit per user per hour (default: 10)
ai.rate-limit.per-hour=10

# Optional: Request timeout in milliseconds (default: 30000)
ai.request.timeout-ms=30000

# Optional: Generation temperature 0.0-1.0 (default: 0.7)
# Higher = more creative, Lower = more focused
ai.temperature=0.7

# Optional: Max tokens for title generation (default: 200)
ai.max-tokens.title=200

# Optional: Max tokens for description generation (default: 500)
ai.max-tokens.description=500
```

#### Disabling AI Generation

If you don't want to use AI generation:
- Set `ai.generation.enabled=false` in `application.properties`
- The application will work normally without AI features
- Users can still create items manually

#### Troubleshooting AI Generation

**"AI generation service is currently unavailable"**
- Verify your API key is correct in `application.properties`
- Check that `ai.generation.enabled=true`
- Ensure you have internet connectivity
- Verify the Gemini API is accessible from your network

**"Rate limit exceeded"**
- Each user is limited to 10 AI generations per hour
- Wait for the cooldown period (shown in the error message)
- Adjust `ai.rate-limit.per-hour` if needed for development

**"Request timed out"**
- The default timeout is 30 seconds
- Check your internet connection
- Increase `ai.request.timeout-ms` if needed

**API Key Issues**
- Ensure there are no extra spaces in the API key
- Verify the key is valid at https://makersuite.google.com/app/apikey
- Check that your Google Cloud project has the Generative Language API enabled

For more details, see [AI_GENERATION_GUIDE.md](./AI_GENERATION_GUIDE.md)

### Frontend Configuration
Configuration is in `frontend/.env`

See `frontend/.env.example` for available options

## 📚 Documentation

- [Product Requirements Document](./PRD.md) - Project specifications and requirements
- [System Architecture](./ARCHITECTURE.md) - Technical architecture and design
- [API Contract](./API_CONTRACT.md) - API endpoints and specifications
- [Authentication System](./AUTHENTICATION.md) - Complete authentication documentation
- [AI Generation Guide](./AI_GENERATION_GUIDE.md) - How to use AI-powered item descriptions
- [Borrow Workflow Guide](./BORROW_WORKFLOW_GUIDE.md) - Complete guide for borrowing and lending items
- [Wireframes](./WIREFRAMES.md) - UI/UX designs
- [Team Roles](./TEAM_ROLES.md) - Team structure and responsibilities
- [Setup Guide](./SETUP_GUIDE.md) - Detailed installation instructions
- [Project Status](./PROJECT_STATUS.md) - Current progress and remaining tasks

## 🎯 Features

### Core Features
- ✅ **User Authentication** - Secure JWT-based authentication with registration and login
  - Email and password validation
  - BCrypt password hashing
  - Session persistence with localStorage
  - Protected routes with automatic redirect
  - Logout functionality
- ✅ **Item Management** - Complete CRUD operations for item listings
  - Create, read, update, delete items with authorization
  - Cloudinary image upload (5MB limit, image validation)
  - Advanced search (title and description matching)
  - Multi-criteria filtering (category, status, search)
  - Pagination with metadata
  - Owner-based authorization
  - Image preview before upload
  - "My Items" page for managing user's listings
- ✅ **AI-Powered Item Descriptions** - Google Gemini API integration
  - Automatic title generation (3-200 characters)
  - Automatic description generation (50-1000 characters)
  - Category-specific content optimization
  - Regeneration for alternative suggestions
  - Rate limiting (10 requests/hour per user)
  - Graceful degradation when unavailable
  - User-friendly error handling
- ✅ **Borrow Workflow** - Complete peer-to-peer borrowing system
  - Create borrow requests with dates and messages
  - Borrower view (My Requests) with status tracking
  - Lender view (Incoming Requests) for managing requests
  - Approve/reject workflow with optional messages
  - Return confirmation process (lender marks returned, borrower confirms)
  - Request cancellation for pending requests
  - Status tracking (PENDING → APPROVED → RETURNED → COMPLETED)
  - Real-time statistics dashboard
  - Status filtering for both views
  - Authorization and validation
  - Toast notifications for status changes
  - Contact information sharing (only for approved requests)
- 🔄 User profiles and ratings

### Optional Features
- 🔄 Admin dashboard
- 🔄 In-app messaging
- 🔄 Notifications

**Current Status**: Week 6-7 Complete (70% done) - See [PROJECT_STATUS.md](./PROJECT_STATUS.md) for detailed progress

## 🗓️ Development Timeline

| Week | Milestone | Status |
|------|-----------|--------|
| Week 1 | Project Planning & Setup | ✅ Complete |
| Week 2 | Authentication System | ✅ Complete |
| Week 3-4 | Item Management | ✅ Complete |
| Week 5 | AI Integration | ✅ Complete |
| Week 6-7 | Borrow Workflow | ✅ Complete |
| Week 8-9 | Optional Features | 📅 Planned |
| Week 10 | UI Polish | 📅 Planned |
| Week 11 | Testing & Debugging | 📅 Planned |
| Week 12 | Documentation & Presentation | 📅 Planned |

## 👥 Team

- **Backend Lead**: [Name]
- **Frontend Lead**: [Name]
- **Integration Specialist**: [Name]

## 📝 API Documentation

API documentation is available at:
- Development: `http://localhost:8080/swagger-ui.html`
- See [API_CONTRACT.md](./API_CONTRACT.md) for detailed endpoint documentation

## 🧪 Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend
npm run test
```

## 🚢 Deployment

Deployment instructions will be added in Week 11-12.

**Planned Deployment**:
- Frontend: Vercel/Netlify
- Backend: Railway/Render
- Database: Railway/PlanetScale

## 🤝 Contributing

This is an academic project. For team members:

1. Create a feature branch
2. Make your changes
3. Submit a pull request
4. Get review from at least one team member
5. Merge after approval

## 📄 License

This project is created for educational purposes as part of PW IOI – School of Technology coursework.

## 🙏 Acknowledgments

- PW IOI – School of Technology
- Course Instructors
- Open source libraries and tools used

## 📞 Support

For questions or issues:
- Create an issue in the repository
- Contact team members via WhatsApp/Slack
- Refer to documentation in `/docs` folder

---

**Built with ❤️ by Team RentKar**
