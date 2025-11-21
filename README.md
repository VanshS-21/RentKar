# RentKar 🎒

A peer-to-peer item sharing platform for college students.

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

### Frontend Configuration
Configuration is in `frontend/.env`

See `frontend/.env.example` for available options

## 📚 Documentation

- [Product Requirements Document](./PRD.md) - Project specifications and requirements
- [System Architecture](./ARCHITECTURE.md) - Technical architecture and design
- [API Contract](./API_CONTRACT.md) - API endpoints and specifications
- [Authentication System](./AUTHENTICATION.md) - Complete authentication documentation
- [Wireframes](./WIREFRAMES.md) - UI/UX designs
- [Team Roles](./TEAM_ROLES.md) - Team structure and responsibilities
- [Setup Guide](./SETUP_GUIDE.md) - Detailed installation instructions
- [Quick Start](./QUICK_START.md) - Fast setup and usage guide
- [Project Status](./PROJECT_STATUS.md) - Current progress and remaining tasks

## 🎯 Features

### Core Features
- ✅ **User Authentication** - Secure JWT-based authentication with registration and login
  - Email and password validation
  - BCrypt password hashing
  - Session persistence with localStorage
  - Protected routes with automatic redirect
  - Logout functionality
- 🔄 Item listing with image upload
- 🔄 AI-powered item descriptions
- 🔄 Borrow request workflow
- 🔄 Approve/reject requests
- 🔄 Item return tracking
- 🔄 User profiles

### Optional Features
- 🔄 Admin dashboard
- 🔄 In-app messaging
- 🔄 Search and filters
- 🔄 Notifications

**Current Status**: Week 2 Complete - See [PROJECT_STATUS.md](./PROJECT_STATUS.md) for detailed progress

## 🗓️ Development Timeline

| Week | Milestone | Status |
|------|-----------|--------|
| Week 1 | Project Planning & Setup | ✅ Complete |
| Week 2 | Authentication System | ✅ Complete |
| Week 3-4 | Item Management | ⏳ In Progress |
| Week 5 | AI Integration | 📅 Planned |
| Week 6-7 | Borrow Workflow | 📅 Planned |
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
