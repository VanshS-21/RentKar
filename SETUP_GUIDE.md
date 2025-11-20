# RentKar - Setup Guide

## ✅ Current Status

All prerequisites are installed and configured:
- **Java**: 25.0.1
- **Maven**: Installed
- **Node.js & npm**: Installed
- **MySQL**: 8.0 (Running)
- **Database**: rentkar_db (Created)
- **Backend**: Built and ready
- **Frontend**: Dependencies installed

---

## Installation Instructions for Windows

### 1. Install Git

**Download**: https://git-scm.com/download/win

**Steps**:
1. Download Git for Windows installer
2. Run the installer
3. Use default settings (recommended)
4. Verify installation:
   ```powershell
   git --version
   ```

---

### 2. Install Node.js & npm

**Download**: https://nodejs.org/ (LTS version recommended)

**Steps**:
1. Download Node.js LTS (v20.x or v18.x)
2. Run the installer
3. Check "Automatically install necessary tools" option
4. Verify installation:
   ```powershell
   node --version
   npm --version
   ```

---

### 3. Install Maven

**Option A: Using Chocolatey (Recommended)**
```powershell
# Install Chocolatey first (if not installed)
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Install Maven
choco install maven
```

**Option B: Manual Installation**
1. Download from: https://maven.apache.org/download.cgi
2. Extract to `C:\Program Files\Apache\maven`
3. Add to PATH:
   - Open System Properties → Environment Variables
   - Add `C:\Program Files\Apache\maven\bin` to PATH
4. Verify:
   ```powershell
   mvn --version
   ```

---

### 4. Install MySQL

**Download**: https://dev.mysql.com/downloads/installer/

**Steps**:
1. Download MySQL Installer for Windows
2. Choose "Custom" installation
3. Select:
   - MySQL Server 8.0
   - MySQL Workbench
   - MySQL Shell (optional)
4. Set root password (remember this!)
5. Complete installation
6. Verify:
   ```powershell
   mysql --version
   ```

**Create Database**:
```sql
-- Open MySQL Workbench or MySQL Shell
CREATE DATABASE rentkar_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user (optional, for security)
CREATE USER 'rentkar_user'@'localhost' IDENTIFIED BY 'RentKar@2024';
GRANT ALL PRIVILEGES ON rentkar_db.* TO 'rentkar_user'@'localhost';
FLUSH PRIVILEGES;
```

---

## Quick Install Script (Run as Administrator)

```powershell
# Install Chocolatey
Set-ExecutionPolicy Bypass -Scope Process -Force
[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Install all tools
choco install git nodejs maven mysql -y

# Verify installations
git --version
node --version
npm --version
mvn --version
mysql --version
```

---

## After Installation - Verify Everything

Run this verification script:

```powershell
Write-Host "=== RentKar Setup Verification ===" -ForegroundColor Cyan
Write-Host ""

# Check Java
Write-Host "Java:" -ForegroundColor Yellow
java -version
Write-Host ""

# Check Maven
Write-Host "Maven:" -ForegroundColor Yellow
mvn --version
Write-Host ""

# Check Node.js
Write-Host "Node.js:" -ForegroundColor Yellow
node --version
Write-Host ""

# Check npm
Write-Host "npm:" -ForegroundColor Yellow
npm --version
Write-Host ""

# Check Git
Write-Host "Git:" -ForegroundColor Yellow
git --version
Write-Host ""

# Check MySQL
Write-Host "MySQL:" -ForegroundColor Yellow
mysql --version
Write-Host ""

Write-Host "=== Verification Complete ===" -ForegroundColor Green
```

---

## External Services Setup

### 1. Cloudinary Setup

1. Go to: https://cloudinary.com/users/register/free
2. Sign up for free account
3. After login, go to Dashboard
4. Note down:
   - Cloud Name
   - API Key
   - API Secret
5. Create upload preset:
   - Settings → Upload → Upload presets
   - Add upload preset
   - Name: `rentkar_items`
   - Signing Mode: Unsigned
   - Folder: `rentkar/items`

### 2. Google Gemini API Setup

1. Go to: https://makersuite.google.com/app/apikey
2. Sign in with Google account
3. Click "Create API Key"
4. Copy the API key
5. Store securely (we'll use in .env file)

**Free Tier Limits**:
- 60 requests per minute
- 1,500 requests per day
- Perfect for development!

---

## IDE Setup (Optional but Recommended)

### Backend IDE: IntelliJ IDEA Community Edition
- Download: https://www.jetbrains.com/idea/download/
- Free and excellent for Spring Boot

### Frontend IDE: VS Code
- Download: https://code.visualstudio.com/
- Install extensions:
  - ES7+ React/Redux/React-Native snippets
  - Tailwind CSS IntelliSense
  - Prettier - Code formatter
  - ESLint

---

## Next Steps

After all installations are complete:

1. ✅ Verify all tools are installed
2. ✅ Create database in MySQL
3. ✅ Get Cloudinary credentials
4. ✅ Get Gemini API key
5. ✅ Initialize Git repository
6. ✅ Create Spring Boot project
7. ✅ Create React project
8. ✅ Configure environment variables

---

*Once installations are complete, run the project initialization scripts!*
