# RentKar Frontend

React application for RentKar platform built with Vite, TailwindCSS, and shadcn/ui.

## 🛠️ Tech Stack

- **Framework**: React 18
- **Build Tool**: Vite
- **Styling**: TailwindCSS
- **UI Components**: shadcn/ui
- **State Management**: React Context API
- **Form Handling**: React Hook Form + Zod
- **HTTP Client**: Axios
- **Routing**: React Router v6
- **Icons**: Lucide React

## 📁 Project Structure

```
frontend/
├── public/                  # Static assets
├── src/
│   ├── components/          # Reusable components
│   │   ├── ui/             # shadcn/ui components
│   │   ├── layout/         # Layout components
│   │   └── features/       # Feature-specific components
│   ├── contexts/           # React Context providers
│   ├── hooks/              # Custom React hooks
│   ├── lib/                # Utility functions
│   ├── pages/              # Page components
│   ├── services/           # API service functions
│   ├── App.jsx             # Main App component
│   ├── main.jsx            # Entry point
│   └── index.css           # Global styles
├── .env.example            # Environment variables example
├── package.json
├── vite.config.js
├── tailwind.config.js
└── README.md
```

## 🚀 Getting Started

### Prerequisites

- Node.js 18+ and npm

### Installation

1. **Install Dependencies**
   ```bash
   npm install
   ```

2. **Setup Environment Variables**
   ```bash
   # Copy example env file
   cp .env.example .env
   
   # Edit .env with your values
   ```

3. **Run Development Server**
   ```bash
   npm run dev
   ```

The app will be available at `http://localhost:5173`

## 🎨 Adding shadcn/ui Components

shadcn/ui components are added on-demand. To add a new component:

```bash
# Example: Add Button component
npx shadcn-ui@latest add button

# Example: Add Card component
npx shadcn-ui@latest add card

# Example: Add Form components
npx shadcn-ui@latest add form input label
```

Available components: https://ui.shadcn.com/docs/components

## 📦 Available Scripts

```bash
# Development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Lint code
npm run lint
```

## 🔑 Environment Variables

Create a `.env` file:

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_CLOUDINARY_CLOUD_NAME=your_cloud_name
VITE_CLOUDINARY_UPLOAD_PRESET=rentkar_items
```

## 🎨 Styling with TailwindCSS

This project uses TailwindCSS with shadcn/ui's design system:

```jsx
// Example component
import { Button } from '@/components/ui/button'

function MyComponent() {
  return (
    <div className="container mx-auto p-4">
      <Button variant="default" size="lg">
        Click me
      </Button>
    </div>
  )
}
```

## 🔐 Authentication

Authentication is handled via Context API:

```jsx
import { useAuth } from '@/contexts/AuthContext'

function MyComponent() {
  const { user, login, logout, isAuthenticated } = useAuth()
  
  // Use authentication methods
}
```

## 📝 Form Handling

Forms use React Hook Form with Zod validation:

```jsx
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import * as z from 'zod'

const schema = z.object({
  email: z.string().email(),
  password: z.string().min(6),
})

function LoginForm() {
  const form = useForm({
    resolver: zodResolver(schema),
  })
  
  // Handle form submission
}
```

## 🌐 API Integration

API calls are made using Axios:

```jsx
import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL

// Example API call
const getItems = async () => {
  const response = await axios.get(`${API_BASE_URL}/items`)
  return response.data
}
```

## 🎨 Theme Customization

Theme colors are defined in `tailwind.config.js` and `src/index.css`.

To customize:
1. Edit CSS variables in `src/index.css`
2. Update Tailwind config in `tailwind.config.js`

## 📱 Responsive Design

The app is fully responsive using Tailwind's responsive utilities:

```jsx
<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
  {/* Responsive grid */}
</div>
```

## 🧪 Testing

Testing setup will be added in Week 11.

## 📦 Build for Production

```bash
# Create production build
npm run build

# Preview production build locally
npm run preview
```

Build output will be in the `dist/` directory.

## 🚢 Deployment

Recommended platforms:
- **Vercel**: https://vercel.com (Recommended)
- **Netlify**: https://netlify.com
- **GitHub Pages**: For static hosting

## 🤝 Contributing

1. Create feature branch
2. Make changes
3. Test thoroughly
4. Submit pull request

## 📄 License

Educational project for PW IOI – School of Technology.

## 🔗 Useful Links

- [React Documentation](https://react.dev/)
- [Vite Documentation](https://vitejs.dev/)
- [TailwindCSS Documentation](https://tailwindcss.com/)
- [shadcn/ui Documentation](https://ui.shadcn.com/)
- [React Hook Form](https://react-hook-form.com/)
- [Axios Documentation](https://axios-http.com/)
