# RentKar Project Structure

## Root Directory
```
RentKar/
├── backend/              # Spring Boot backend application
├── frontend/             # React frontend application
├── .kiro/                # Kiro AI assistant configuration
├── start-backend.bat     # Windows quick-start script for backend
├── start-frontend.bat    # Windows quick-start script for frontend
└── [documentation files] # PRD.md, ARCHITECTURE.md, API_CONTRACT.md, etc.
```

## Backend Structure
```
backend/
├── src/main/java/com/rentkar/
│   ├── RentKarApplication.java    # Main Spring Boot application
│   ├── config/                     # Configuration classes (Security, CORS, etc.)
│   ├── controller/                 # REST API controllers
│   ├── service/                    # Business logic layer
│   ├── repository/                 # JPA repositories (data access)
│   ├── model/                      # Entity classes (User, Item, BorrowRequest)
│   ├── dto/                        # Data Transfer Objects
│   ├── security/                   # JWT, authentication filters
│   └── exception/                  # Custom exceptions and handlers
├── src/main/resources/
│   ├── application.properties      # Main configuration file
│   └── application-example.properties
├── pom.xml                         # Maven dependencies
└── target/                         # Build output (gitignored)
```

## Frontend Structure
```
frontend/
├── src/
│   ├── main.jsx                # Application entry point
│   ├── App.jsx                 # Root component with Router
│   ├── index.css               # Global styles (Tailwind imports)
│   ├── components/             # Reusable UI components
│   │   └── ui/                 # shadcn/ui components
│   ├── contexts/               # React Context providers
│   │   └── AuthContext.jsx    # Authentication state management
│   ├── pages/                  # Page components (routes)
│   ├── lib/                    # Utility functions
│   │   └── utils.js            # Helper functions (cn, etc.)
│   ├── hooks/                  # Custom React hooks
│   ├── services/               # API service functions
│   └── assets/                 # Static assets (images, etc.)
├── public/                     # Public static files
├── .env                        # Environment variables (gitignored)
├── .env.example                # Environment template
├── package.json                # NPM dependencies and scripts
├── vite.config.js              # Vite configuration
├── tailwind.config.js          # Tailwind CSS configuration
├── postcss.config.js           # PostCSS configuration
└── node_modules/               # Dependencies (gitignored)
```

## Code Organization Patterns

### Backend Conventions
- **Package naming**: `com.rentkar.[layer]` (e.g., `com.rentkar.controller`)
- **Entity classes**: Located in `model/` package, use JPA annotations
- **Controllers**: RESTful endpoints in `controller/`, use `@RestController`
- **Services**: Business logic in `service/`, use `@Service`
- **Repositories**: Data access in `repository/`, extend JpaRepository
- **DTOs**: Request/response objects in `dto/` package
- **Configuration**: Spring configuration classes in `config/`

### Frontend Conventions
- **Components**: Reusable UI in `components/`, shadcn/ui in `components/ui/`
- **Pages**: Route-level components in `pages/`
- **Contexts**: Global state providers in `contexts/` (e.g., AuthContext)
- **Services**: API calls organized in `services/` by domain
- **Hooks**: Custom hooks in `hooks/` with `use` prefix
- **Utilities**: Helper functions in `lib/utils.js`
- **Styling**: Tailwind utility classes, use `cn()` for conditional classes

## Key Files to Know
- `API_CONTRACT.md`: Complete API endpoint documentation
- `ARCHITECTURE.md`: System design and technical decisions
- `PRD.md`: Product requirements and feature specifications
- `PROJECT_STATUS.md`: Current development progress
- `QUICK_START.md`: Fast setup instructions
- `SETUP_GUIDE.md`: Detailed installation guide

## Database Schema
Three main tables: `users`, `items`, `borrow_requests`
- Foreign key relationships: items → users (owner), requests → users (borrower/lender) and items
- Status enums: User roles (USER, ADMIN), Item status (AVAILABLE, BORROWED, UNAVAILABLE), Request status (PENDING, APPROVED, REJECTED, RETURNED, COMPLETED)
