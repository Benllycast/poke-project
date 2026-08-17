import { createContext, useCallback, useContext, useState } from 'react'
import * as authApi from '../api/authApi.js'
import { getToken, setToken } from '../api/apiClient.js'

const EMAIL_KEY = 'pokeapp_email'
const AuthContext = createContext(null)

function getStoredEmail() {
  return localStorage.getItem(EMAIL_KEY)
}

function setStoredEmail(email) {
  if (email) {
    localStorage.setItem(EMAIL_KEY, email)
  } else {
    localStorage.removeItem(EMAIL_KEY)
  }
}

export function AuthProvider({ children }) {
  const [token, setTokenState] = useState(getToken)
  const [email, setEmailState] = useState(getStoredEmail)

  const applyAuth = useCallback((auth) => {
    setToken(auth.token)
    setStoredEmail(auth.email)
    setTokenState(auth.token)
    setEmailState(auth.email)
  }, [])

  const login = useCallback(
    async (credentials) => {
      const auth = await authApi.login(credentials)
      applyAuth(auth)
    },
    [applyAuth],
  )

  const register = useCallback(
    async (credentials) => {
      const auth = await authApi.register(credentials)
      applyAuth(auth)
    },
    [applyAuth],
  )

  const logout = useCallback(() => {
    setToken(null)
    setStoredEmail(null)
    setTokenState(null)
    setEmailState(null)
  }, [])

  const value = { token, email, isAuthenticated: token != null, login, register, logout }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components -- hook colocated with its provider
export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
