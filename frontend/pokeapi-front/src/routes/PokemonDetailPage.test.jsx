import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { describe, expect, it, vi, beforeEach } from 'vitest'
import PokemonDetailPage from './PokemonDetailPage.jsx'
import * as pokemonApi from '../api/pokemonApi.js'
import { AuthProvider } from '../context/AuthContext.jsx'

vi.mock('../api/pokemonApi.js')

function renderAt(id) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <MemoryRouter initialEntries={[`/pokemon/${id}`]}>
          <Routes>
            <Route path="/" element={<p>List page</p>} />
            <Route path="/pokemon/:id" element={<PokemonDetailPage />} />
          </Routes>
        </MemoryRouter>
      </AuthProvider>
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
  localStorage.clear()
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

  it('renders proprietary fields when present', async () => {
    pokemonApi.getById.mockResolvedValue({
      ...detail,
      localizedName: 'Pikachu (Kanto)',
      region: 'Kanto',
      tags: 'starter,electric',
    })

    renderAt(35)

    expect(await screen.findByText('Pikachu (Kanto)')).toBeInTheDocument()
    expect(screen.getByText('Kanto')).toBeInTheDocument()
    expect(screen.getByText('starter,electric')).toBeInTheDocument()
  })

  it('shows Edit/Delete only when authenticated', async () => {
    pokemonApi.getById.mockResolvedValue(detail)

    renderAt(35)

    await screen.findByRole('heading', { name: 'clefairy' })
    expect(screen.queryByRole('button', { name: 'Delete' })).not.toBeInTheDocument()
  })

  it('deletes the Pokemon and navigates to the list on confirm', async () => {
    localStorage.setItem('pokeapp_token', 'token-abc')
    pokemonApi.getById.mockResolvedValue(detail)
    pokemonApi.remove.mockResolvedValue(null)
    vi.spyOn(window, 'confirm').mockReturnValue(true)
    const user = userEvent.setup()

    renderAt(35)

    await user.click(await screen.findByRole('button', { name: 'Delete' }))

    await waitFor(() => expect(pokemonApi.remove).toHaveBeenCalledWith('35'))
    expect(await screen.findByText('List page')).toBeInTheDocument()
  })

  it('does not delete when the confirm dialog is cancelled', async () => {
    localStorage.setItem('pokeapp_token', 'token-abc')
    pokemonApi.getById.mockResolvedValue(detail)
    vi.spyOn(window, 'confirm').mockReturnValue(false)
    const user = userEvent.setup()

    renderAt(35)

    await user.click(await screen.findByRole('button', { name: 'Delete' }))

    expect(pokemonApi.remove).not.toHaveBeenCalled()
  })
})
