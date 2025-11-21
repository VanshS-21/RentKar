# RentKar Documentation Index

Quick reference guide to all project documentation.

## 🚀 Getting Started

- **[README.md](./README.md)** - Start here! Project overview, quick start, and setup instructions
- **[SETUP_GUIDE.md](./SETUP_GUIDE.md)** - Detailed installation and configuration guide

## 📋 Project Planning

- **[PRD.md](./PRD.md)** - Product Requirements Document (project scope and objectives)
- **[PROJECT_STATUS.md](./PROJECT_STATUS.md)** - Current progress and weekly milestones
- **[CHANGELOG.md](./CHANGELOG.md)** - Detailed change history and completed features
- **[TEAM_ROLES.md](./TEAM_ROLES.md)** - Team structure and responsibilities

## 🏗️ Technical Documentation

- **[ARCHITECTURE.md](./ARCHITECTURE.md)** - System architecture and technology stack
- **[API_CONTRACT.md](./API_CONTRACT.md)** - Complete API endpoint specifications
- **[AUTHENTICATION.md](./AUTHENTICATION.md)** - Authentication system documentation (Week 2)

## 🎨 Design

- **[WIREFRAMES.md](./WIREFRAMES.md)** - UI/UX wireframes and mockups

## 🤖 Feature Guides

- **[AI_GENERATION_GUIDE.md](./AI_GENERATION_GUIDE.md)** - Complete guide to AI-powered item descriptions
- **[BORROW_WORKFLOW_GUIDE.md](./BORROW_WORKFLOW_GUIDE.md)** - User guide for borrowing and lending items

## 📦 Week-by-Week Specs

### Week 2: Authentication (Complete ✅)
- **Documentation**: [AUTHENTICATION.md](./AUTHENTICATION.md)
- **Status**: Production-ready, all tests passing (27 tests)

### Week 3-4: Item Management (Complete ✅)
- **Location**: `.kiro/specs/item-management/`
- **Files**:
  - `requirements.md` - 12 requirements with 60 acceptance criteria
  - `design.md` - Architecture and 39 correctness properties
  - `tasks.md` - Implementation plan (all tasks complete)
- **Status**: Production-ready, all tests passing (56 backend + 28 frontend)
- **Features**: Full CRUD, Cloudinary upload, search/filter, pagination, authorization

### Week 5: AI Integration (Complete ✅)
- **Location**: `.kiro/specs/ai-description-generation/`
- **Files**:
  - `requirements.md` - 12 requirements with 60 acceptance criteria
  - `design.md` - Architecture and 33 correctness properties
  - `tasks.md` - Implementation plan (all tasks complete)
- **Documentation**: [AI_GENERATION_GUIDE.md](./AI_GENERATION_GUIDE.md)
- **Status**: Production-ready, all tests passing (97 backend + 34 frontend)
- **Features**: Google Gemini AI integration, category-specific prompts, rate limiting, regeneration, error handling

### Week 6-7: Borrow Workflow (Complete ✅)
- **Location**: `.kiro/specs/borrow-workflow/`
- **Files**:
  - `requirements.md` - 15 requirements with 75 acceptance criteria
  - `design.md` - Architecture and 32 correctness properties
  - `tasks.md` - Implementation plan (all tasks complete)
- **Documentation**: [BORROW_WORKFLOW_GUIDE.md](./BORROW_WORKFLOW_GUIDE.md)
- **Status**: Production-ready, all tests passing
- **Features**: Request lifecycle, approve/reject/return workflow, borrower/lender views, notifications, authorization, statistics

## 🔧 Quick Commands

### Start Application
```bash
# Windows
start-backend.bat
start-frontend.bat

# Or manually
cd backend && mvn spring-boot:run
cd frontend && npm run dev
```

### Run Tests
```bash
# Backend
cd backend && mvn test

# Frontend
cd frontend && npm test
```

## 📍 Access Points

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080/api
- **API Docs**: http://localhost:8080/swagger-ui.html

## 🎯 Current Focus

**Week 8+: Polish and Optional Features (Next)**

**Completed**: Borrow Workflow ✅
- Complete request lifecycle implementation
- All 32 borrow workflow correctness properties validated
- Borrower and lender views with filtering
- Authorization and validation
- Statistics and notifications
- Complete user guide available

## 📚 External Resources

- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [React Docs](https://react.dev/)
- [TailwindCSS](https://tailwindcss.com/docs)
- [Cloudinary](https://cloudinary.com/documentation)
- [Gemini API](https://ai.google.dev/docs)

---

**Last Updated**: November 21, 2025  
**Project Status**: Week 6-7 Complete (Core Features Done), Polish Phase Ready
