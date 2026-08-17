import { Routes, Route } from 'react-router-dom'
import PokemonListPage from './routes/PokemonListPage.jsx'
import PokemonDetailPage from './routes/PokemonDetailPage.jsx'

function App() {
  return (
    <div id="app-shell">
      <Routes>
        <Route path="/" element={<PokemonListPage />} />
        <Route path="/pokemon/:id" element={<PokemonDetailPage />} />
      </Routes>
    </div>
  )
}

export default App
