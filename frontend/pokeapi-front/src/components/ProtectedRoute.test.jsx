import { render, screen } from '@testing-library/react'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import { describe, expect, it, beforeEach } from 'vitest'
import ProtectedRoute from './ProtectedRoute.jsx'
import { AuthProvider } from '../context/AuthContext.jsx'

function renderProtected(initialEntry) {
  return render(
    <AuthProvider>
      <MemoryRouter initialEntries={[initialEntry]}>
        <Routes>
          <Route path="/login" element={<p>Login page</p>} />
          <Route
            path="/secret"
            element={
              <ProtectedRoute>
                <p>Secret content</p>
              </ProtectedRoute>
            }
          />
        </Routes>
      </MemoryRouter>
    </AuthProvider>,
  )
}

beforeEach(() => {
  localStorage.clear()
})

describe('ProtectedRoute', () => {
  it('redirects to /login when logged out', () => {
    renderProtected('/secret')
    expect(screen.getByText('Login page')).toBeInTheDocument()
  })

  it('renders children when logged in', () => {
    localStorage.setItem('pokeapp_token', 'token-abc')
    renderProtected('/secret')
    expect(screen.getByText('Secret content')).toBeInTheDocument()
  })
})
