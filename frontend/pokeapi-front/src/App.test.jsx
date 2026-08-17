import { render, screen } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { describe, expect, it } from 'vitest'
import App from './App.jsx'

function renderApp() {
  const queryClient = new QueryClient()
  return render(
    <BrowserRouter>
      <QueryClientProvider client={queryClient}>
        <App />
      </QueryClientProvider>
    </BrowserRouter>,
  )
}

describe('App', () => {
  it('renders the placeholder heading', () => {
    renderApp()
    expect(screen.getByText('Pokedex')).toBeInTheDocument()
  })
})
