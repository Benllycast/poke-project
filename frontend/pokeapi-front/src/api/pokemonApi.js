import { apiFetch } from './apiClient.js'

export function list(page = 0, size = 20) {
  return apiFetch(`/api/pokemon?page=${page}&size=${size}`)
}

export function getById(id) {
  return apiFetch(`/api/pokemon/${id}`)
}

export function sync(idOrName) {
  return apiFetch(`/api/pokemon/sync/${idOrName}`, { method: 'POST' })
}

export function update(id, payload) {
  return apiFetch(`/api/pokemon/${id}`, { method: 'PUT', body: JSON.stringify(payload) })
}

export function remove(id) {
  return apiFetch(`/api/pokemon/${id}`, { method: 'DELETE' })
}
