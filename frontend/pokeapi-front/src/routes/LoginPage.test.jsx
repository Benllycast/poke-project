import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import { describe, expect, it, vi, beforeEach } from 'vitest'
import LoginPage from './LoginPage.jsx'
import { AuthProvider } from '../context/AuthContext.jsx'
import * as authApi from '../api/authApi.js'

vi.mock('../api/authApi.js')

function renderAt(initialEntry) {
  return render(
    <AuthProvider>
      <MemoryRouter initialEntries={[initialEntry]}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/" element={<p>Home page</p>} />
          <Route path="/pokemon/:id/edit" element={<p>Edit page</p>} />
        </Routes>
      </MemoryRouter>
    </AuthProvider>,
  )
}

async function fillAndSubmit(user) {
  await user.type(screen.getByLabelText('Email'), 'trainer@example.com')
  await user.type(screen.getByLabelText('Password'), 'Password123!')
  await user.click(screen.getByRole('button', { name: 'Log in' }))
}

beforeEach(() => {
  localStorage.clear()
  vi.clearAllMocks()
})

describe('LoginPage', () => {
  it('renders email and password fields', () => {
    renderAt('/login')

    expect(screen.getByLabelText('Email')).toBeInTheDocument()
    expect(screen.getByLabelText('Password')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Log in' })).toBeInTheDocument()
  })

  it('logs in and navigates to / on success', async () => {
    authApi.login.mockResolvedValue({ token: 'jwt-token', email: 'trainer@example.com' })
    const user = userEvent.setup()

    renderAt('/login')
    await fillAndSubmit(user)

    await waitFor(() =>
      expect(authApi.login).toHaveBeenCalledWith({
        email: 'trainer@example.com',
        password: 'Password123!',
      }),
    )
    expect(await screen.findByText('Home page')).toBeInTheDocument()
  })

  it('redirects back to the originally attempted route on success', async () => {
    authApi.login.mockResolvedValue({ token: 'jwt-token', email: 'trainer@example.com' })
    const user = userEvent.setup()

    renderAt({ pathname: '/login', state: { from: { pathname: '/pokemon/5/edit' } } })
    await fillAndSubmit(user)

    expect(await screen.findByText('Edit page')).toBeInTheDocument()
  })

  it('shows an error message and stays on the page when login fails', async () => {
    authApi.login.mockRejectedValue({ status: 401, message: 'Invalid email or password' })
    const user = userEvent.setup()

    renderAt('/login')
    await fillAndSubmit(user)

    expect(await screen.findByRole('alert')).toHaveTextContent('Invalid email or password')
    expect(screen.getByRole('button', { name: 'Log in' })).toBeInTheDocument()
  })
})
