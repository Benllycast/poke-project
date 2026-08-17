import { Routes, Route } from 'react-router-dom'
import NavBar from './components/NavBar.jsx'
import ProtectedRoute from './components/ProtectedRoute.jsx'
import PokemonListPage from './routes/PokemonListPage.jsx'
import PokemonDetailPage from './routes/PokemonDetailPage.jsx'
import PokemonEditPage from './routes/PokemonEditPage.jsx'
import LoginPage from './routes/LoginPage.jsx'
import RegisterPage from './routes/RegisterPage.jsx'
import NotFoundPage from './routes/NotFoundPage.jsx'

function App() {
  return (
    <div id="app-shell">
      <NavBar />
      <Routes>
        <Route path="/" element={<PokemonListPage />} />
        <Route path="/pokemon/:id" element={<PokemonDetailPage />} />
        <Route
          path="/pokemon/:id/edit"
          element={
            <ProtectedRoute>
              <PokemonEditPage />
            </ProtectedRoute>
          }
        />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </div>
  )
}

export default App
