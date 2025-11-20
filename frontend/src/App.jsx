import { BrowserRouter as Router } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import { AuthProvider } from './contexts/AuthContext'

function App() {
  return (
    <Router>
      <AuthProvider>
        <div className="min-h-screen bg-background">
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
                Your peer-to-peer item sharing platform is being set up.
              </p>
              <div className="space-y-2">
                <p className="text-sm">✅ Frontend project initialized</p>
                <p className="text-sm">✅ TailwindCSS configured</p>
                <p className="text-sm">✅ shadcn/ui ready to use</p>
                <p className="text-sm">✅ React Router setup</p>
                <p className="text-sm">✅ Context API ready</p>
                <p className="text-sm">⏳ Backend setup in progress...</p>
              </div>
            </div>
          </div>
        </div>
        <Toaster position="top-right" />
      </AuthProvider>
    </Router>
  )
}

export default App
