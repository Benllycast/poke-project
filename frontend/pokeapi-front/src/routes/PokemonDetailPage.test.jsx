import { render, screen } from '@testing-library/react'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { describe, expect, it, vi, beforeEach } from 'vitest'
import PokemonDetailPage from './PokemonDetailPage.jsx'
import * as pokemonApi from '../api/pokemonApi.js'

vi.mock('../api/pokemonApi.js')

function renderAt(id) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={[`/pokemon/${id}`]}>
        <Routes>
          <Route path="/pokemon/:id" element={<PokemonDetailPage />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

const detail = {
  id: 35,
  name: 'clefairy',
  spriteUrl: 'clefairy.png',
  category: 'Fairy Pokémon',
  description: 'A cute Pokemon.',
  stats: [{ name: 'speed', baseStat: 35, effort: 0 }],
  evolutionChain: [{ speciesId: 173, name: 'cleffa', minLevel: null }],
}

beforeEach(() => {
  vi.clearAllMocks()
})

describe('PokemonDetailPage', () => {
  it('renders image, stats, description, and evolution chain', async () => {
    pokemonApi.getById.mockResolvedValue(detail)

    renderAt(35)

    expect(await screen.findByRole('heading', { name: 'clefairy' })).toBeInTheDocument()
    expect(screen.getByText('A cute Pokemon.')).toBeInTheDocument()
    expect(screen.getByText('speed')).toBeInTheDocument()
    expect(screen.getByText('cleffa')).toBeInTheDocument()
    expect(pokemonApi.getById).toHaveBeenCalledWith('35')
  })

  it('renders a not-found state on a 404 error', async () => {
    pokemonApi.getById.mockRejectedValue({ status: 404, message: 'not found' })

    renderAt(9999)

    expect(await screen.findByRole('alert')).toHaveTextContent('Pokemon not found.')
  })
})
