# RentKar - Quick Start Guide

## 🚀 Start the Application

### Method 1: Batch Files (Easiest)
1. Double-click `start-backend.bat`
2. Double-click `start-frontend.bat`
3. Open http://localhost:5173 in your browser

### Method 2: Command Line

**Terminal 1 - Backend:**
```bash
cd backend
mvn spring-boot:run
```

**Terminal 2 - Frontend:**
```bash
cd frontend
npm run dev
```

---

## 🌐 Access Points

- **Application**: http://localhost:5173
- **Backend API**: http://localhost:8080/api
- **API Documentation**: http://localhost:8080/swagger-ui.html

---

## 👤 First Time User

### Registration
1. Go to http://localhost:5173
2. Click **"Create Account"** or navigate to `/register`
3. Fill in the registration form:
   - Username (required)
   - Email (required, valid format)
   - Password (required, minimum 8 characters)
   - Full Name (required)
   - Phone (optional)
4. Click **"Create Account"**
5. You'll be redirected to the login page

### Login
1. Navigate to `/login` or click **"Log in"**
2. Enter your username and password
3. Click **"Log In"**
4. You'll be redirected to the home page

### Features Available
- ✅ Secure authentication with JWT tokens
- ✅ Session persistence (stays logged in on refresh)
- ✅ Protected routes (requires login)
- ✅ Logout functionality
- 🔄 Item management (Week 3-4 - in progress)

---

## 🛑 Stop the Servers

Press `Ctrl + C` in each terminal window

---

## 🔧 Troubleshooting

### Backend won't start
- Check if MySQL is running: `net start MySQL80`
- Verify database exists: `rentkar_db`

### Frontend won't start
- Run: `cd frontend && npm install`

### Port already in use
- Backend: Change port in `backend/src/main/resources/application.properties`
- Frontend: Vite will suggest an alternative port

---

## 📝 Database Credentials

- **Database**: rentkar_db
- **User**: rentkar_user
- **Password**: RentKar@2024
- **Host**: localhost:3306

---

---

## 🎯 Week 3: Item Management (Current Focus)

### Prerequisites
- ✅ All systems configured and ready
- ✅ Cloudinary upload preset created: `rentkar_items`
- ✅ Database running
- ✅ Authentication working

### Getting Started with Week 3
1. Open `.kiro/specs/item-management/tasks.md`
2. Click "Start task" next to task 1.1
3. Follow the incremental implementation plan

### What You'll Build
- Item CRUD operations (Create, Read, Update, Delete)
- Image upload to Cloudinary
- Search and filter functionality
- Pagination for item lists
- Owner-based authorization

### Cloudinary Configuration
- **Cloud Name**: dkoemrt4r
- **Upload Preset**: rentkar_items (unsigned)
- **Folder**: rentkar/items
- **Max File Size**: 5MB

---

## 📚 More Information

- Full documentation: [README.md](./README.md)
- Detailed setup: [SETUP_GUIDE.md](./SETUP_GUIDE.md)
- API details: [API_CONTRACT.md](./API_CONTRACT.md)
- Project status: [PROJECT_STATUS.md](./PROJECT_STATUS.md)
- Week 3 spec: `.kiro/specs/item-management/`
