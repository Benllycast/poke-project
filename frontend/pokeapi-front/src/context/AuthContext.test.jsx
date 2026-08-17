import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi, beforeEach } from 'vitest'
import { AuthProvider, useAuth } from './AuthContext.jsx'
import * as authApi from '../api/authApi.js'

vi.mock('../api/authApi.js')

function Consumer() {
  const { isAuthenticated, email, login } = useAuth()
  return (
    <div>
      <p>{isAuthenticated ? `logged in as ${email}` : 'logged out'}</p>
      <button onClick={() => login({ email: 'trainer@example.com', password: 'x' }).catch(() => {})}>
        Log in
      </button>
    </div>
  )
}

beforeEach(() => {
  localStorage.clear()
  vi.clearAllMocks()
})

describe('AuthContext', () => {
  it('updates state and localStorage on successful login', async () => {
    authApi.login.mockResolvedValue({ token: 'jwt-token', email: 'trainer@example.com' })
    const user = userEvent.setup()

    render(
      <AuthProvider>
        <Consumer />
      </AuthProvider>,
    )

    await user.click(screen.getByRole('button', { name: 'Log in' }))

    expect(await screen.findByText('logged in as trainer@example.com')).toBeInTheDocument()
    expect(localStorage.getItem('pokeapp_token')).toBe('jwt-token')
  })

  it('leaves state unauthenticated on failed login', async () => {
    authApi.login.mockRejectedValue({ status: 401, message: 'Invalid credentials' })
    const user = userEvent.setup()

    render(
      <AuthProvider>
        <Consumer />
      </AuthProvider>,
    )

    await user.click(screen.getByRole('button', { name: 'Log in' }))

    await waitFor(() => expect(authApi.login).toHaveBeenCalled())
    expect(screen.getByText('logged out')).toBeInTheDocument()
    expect(localStorage.getItem('pokeapp_token')).toBeNull()
  })
})
