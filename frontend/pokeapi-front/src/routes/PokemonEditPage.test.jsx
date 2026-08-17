import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { describe, expect, it, vi, beforeEach } from 'vitest'
import PokemonEditPage from './PokemonEditPage.jsx'
import * as pokemonApi from '../api/pokemonApi.js'

vi.mock('../api/pokemonApi.js')

const detail = {
  id: 35,
  name: 'clefairy',
  spriteUrl: 'c.png',
  category: 'Fairy',
  weight: 75,
  height: 6,
  abilities: [],
  moves: [],
  stats: [],
  types: [],
  description: '',
  evolutionChain: [],
  localizedName: '',
  region: '',
  tags: '',
}

function renderPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={['/pokemon/35/edit']}>
        <Routes>
          <Route path="/pokemon/:id/edit" element={<PokemonEditPage />} />
          <Route path="/pokemon/:id" element={<p>Detail page</p>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

beforeEach(() => {
  vi.clearAllMocks()
})

describe('PokemonEditPage', () => {
  it('saves and navigates to the detail page on success', async () => {
    pokemonApi.getById.mockResolvedValue(detail)
    pokemonApi.update.mockResolvedValue({ ...detail, region: 'Kanto' })
    const user = userEvent.setup()

    renderPage()

    await screen.findByLabelText('Name')
    await user.click(screen.getByRole('button', { name: 'Save' }))

    await waitFor(() =>
      expect(pokemonApi.update).toHaveBeenCalledWith('35', expect.objectContaining({ name: 'clefairy' })),
    )
    expect(await screen.findByText('Detail page')).toBeInTheDocument()
  })

  it('renders a validation message on a 400 error', async () => {
    pokemonApi.getById.mockResolvedValue(detail)
    pokemonApi.update.mockRejectedValue({ status: 400, message: 'name must not be blank' })
    const user = userEvent.setup()

    renderPage()

    await screen.findByLabelText('Name')
    await user.click(screen.getByRole('button', { name: 'Save' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('name must not be blank')
  })
})
