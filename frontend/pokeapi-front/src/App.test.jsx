import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { describe, expect, it, vi } from 'vitest'
import App from './App.jsx'
import { AuthProvider } from './context/AuthContext.jsx'
import * as pokemonApi from './api/pokemonApi.js'

vi.mock('./api/pokemonApi.js')

function renderApp(initialEntry = '/') {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <MemoryRouter initialEntries={[initialEntry]}>
          <App />
        </MemoryRouter>
      </AuthProvider>
    </QueryClientProvider>,
  )
}

describe('App', () => {
  it('renders the nav and the Pokemon list at /', async () => {
    pokemonApi.list.mockResolvedValue({ items: [], page: 0, size: 20, totalElements: 0, totalPages: 0 })

    renderApp()

    expect(screen.getByRole('link', { name: 'Pokedex' })).toBeInTheDocument()
    expect(await screen.findByText('No Pokemon yet. Sync one to get started.')).toBeInTheDocument()
  })

  it('renders a not-found page for unknown routes', () => {
    renderApp('/does-not-exist')

    expect(screen.getByRole('heading', { name: 'Page not found' })).toBeInTheDocument()
  })
})
