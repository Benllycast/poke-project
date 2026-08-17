import { render, screen, waitFor, fireEvent } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { describe, expect, it, vi, beforeEach } from 'vitest'
import PokemonListPage from './PokemonListPage.jsx'
import * as pokemonApi from '../api/pokemonApi.js'

vi.mock('../api/pokemonApi.js')

function renderPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <PokemonListPage />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

function page(pageNumber) {
  return {
    items: [
      { id: 35, name: 'clefairy', spriteUrl: 'c.png', category: 'Fairy', weight: 75, abilities: [], moves: [] },
    ],
    page: pageNumber,
    size: 20,
    totalElements: 40,
    totalPages: 2,
  }
}

beforeEach(() => {
  vi.clearAllMocks()
})

describe('PokemonListPage', () => {
  it('renders one card per item in the fetched page', async () => {
    pokemonApi.list.mockResolvedValue(page(0))

    renderPage()

    expect(await screen.findByText('clefairy')).toBeInTheDocument()
    expect(pokemonApi.list).toHaveBeenCalledWith(0, 20)
  })

  it('requests the next page on pagination click', async () => {
    pokemonApi.list.mockResolvedValue(page(0))
    renderPage()
    await screen.findByText('clefairy')

    fireEvent.click(screen.getByRole('button', { name: 'Next' }))

    await waitFor(() => expect(pokemonApi.list).toHaveBeenCalledWith(1, 20))
  })
})
