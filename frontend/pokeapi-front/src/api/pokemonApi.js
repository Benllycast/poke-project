import { apiFetch } from './apiClient.js'

export function list(page = 0, size = 20) {
  return apiFetch(`/api/pokemon?page=${page}&size=${size}`)
}

export function getById(id) {
  return apiFetch(`/api/pokemon/${id}`)
}
