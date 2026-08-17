import { apiFetch } from './apiClient.js'

export function register(credentials) {
  return apiFetch('/api/auth/register', { method: 'POST', body: JSON.stringify(credentials) })
}

export function login(credentials) {
  return apiFetch('/api/auth/login', { method: 'POST', body: JSON.stringify(credentials) })
}
