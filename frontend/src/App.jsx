import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import Navigation from './components/Navigation'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import ProtectedRoute from './components/ProtectedRoute'

// Placeholder Home component (will be replaced with actual home page later)
const HomePage = () => {
  return (
    <div className="min-h-screen bg-background">
      <Navigation />
      <div className="container mx-auto px-4 py-8">
        <h1 className="text-4xl font-bold text-center mb-4">
          🎒 RentKar
        </h1>
        <p className="text-center text-muted-foreground mb-8">
          Share. Borrow. Save Money.
        </p>
        <div className="max-w-2xl mx-auto bg-card p-8 rounded-lg shadow-lg">
          <h2 className="text-2xl font-semibold mb-4">
            Welcome to RentKar! 🚀
          </h2>
          <p className="text-muted-foreground mb-4">
            Your peer-to-peer item sharing platform is ready.
          </p>
          <div className="space-y-2">
            <p className="text-sm">✅ Frontend project initialized</p>
            <p className="text-sm">✅ TailwindCSS configured</p>
            <p className="text-sm">✅ shadcn/ui ready to use</p>
            <p className="text-sm">✅ React Router setup</p>
            <p className="text-sm">✅ Authentication system integrated</p>
            <p className="text-sm">✅ Protected routes configured</p>
          </div>
        </div>
      </div>
    </div>
  )
}

function App() {
  return (
    <Router>
      <Routes>
        {/* Public Routes */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        
        {/* Protected Routes */}
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <HomePage />
            </ProtectedRoute>
          }
        />
        
        {/* Catch all - redirect to home */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
      <Toaster position="top-right" />
    </Router>
  )
}

export default App
