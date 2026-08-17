import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import { describe, expect, it, vi, beforeEach } from 'vitest'
import RegisterPage from './RegisterPage.jsx'
import { AuthProvider } from '../context/AuthContext.jsx'
import * as authApi from '../api/authApi.js'

vi.mock('../api/authApi.js')

function renderPage() {
  return render(
    <AuthProvider>
      <MemoryRouter initialEntries={['/register']}>
        <Routes>
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/" element={<p>Home page</p>} />
        </Routes>
      </MemoryRouter>
    </AuthProvider>,
  )
}

async function fillAndSubmit(user) {
  await user.type(screen.getByLabelText('Email'), 'trainer@example.com')
  await user.type(screen.getByLabelText('Password'), 'Password123!')
  await user.click(screen.getByRole('button', { name: 'Register' }))
}

beforeEach(() => {
  localStorage.clear()
  vi.clearAllMocks()
})

describe('RegisterPage', () => {
  it('renders email and password fields', () => {
    renderPage()

    expect(screen.getByLabelText('Email')).toBeInTheDocument()
    expect(screen.getByLabelText('Password')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Register' })).toBeInTheDocument()
  })

  it('registers and navigates to / on success', async () => {
    authApi.register.mockResolvedValue({ token: 'jwt-token', email: 'trainer@example.com' })
    const user = userEvent.setup()

    renderPage()
    await fillAndSubmit(user)

    await waitFor(() =>
      expect(authApi.register).toHaveBeenCalledWith({
        email: 'trainer@example.com',
        password: 'Password123!',
      }),
    )
    expect(await screen.findByText('Home page')).toBeInTheDocument()
  })

  it('shows an error message and stays on the page when registration fails', async () => {
    authApi.register.mockRejectedValue({ status: 409, message: 'User already exists: trainer@example.com' })
    const user = userEvent.setup()

    renderPage()
    await fillAndSubmit(user)

    expect(await screen.findByRole('alert')).toHaveTextContent('User already exists')
    expect(screen.getByRole('button', { name: 'Register' })).toBeInTheDocument()
  })
})
