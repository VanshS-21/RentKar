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

1. Go to http://localhost:5173
2. Click **"Register"** or **"Sign Up"**
3. Create your account
4. Login with your credentials
5. Start using RentKar!

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

## 📚 More Information

- Full documentation: [README.md](./README.md)
- Detailed setup: [SETUP_GUIDE.md](./SETUP_GUIDE.md)
- API details: [API_CONTRACT.md](./API_CONTRACT.md)
