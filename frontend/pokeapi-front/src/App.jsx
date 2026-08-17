import { Routes, Route } from 'react-router-dom'
import NavBar from './components/NavBar.jsx'
import PokemonListPage from './routes/PokemonListPage.jsx'
import PokemonDetailPage from './routes/PokemonDetailPage.jsx'
import LoginPage from './routes/LoginPage.jsx'
import RegisterPage from './routes/RegisterPage.jsx'

function App() {
  return (
    <div id="app-shell">
      <NavBar />
      <Routes>
        <Route path="/" element={<PokemonListPage />} />
        <Route path="/pokemon/:id" element={<PokemonDetailPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
      </Routes>
    </div>
  )
}

export default App
