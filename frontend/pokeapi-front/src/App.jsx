import { Routes, Route } from 'react-router-dom'
import PokemonListPage from './routes/PokemonListPage.jsx'

function App() {
  return (
    <div id="app-shell">
      <Routes>
        <Route path="/" element={<PokemonListPage />} />
      </Routes>
    </div>
  )
}

export default App
