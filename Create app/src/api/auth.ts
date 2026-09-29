import { refreshSession, request, setAccessToken } from './client'
import type { AuthResponse, Credentials, UserProfile } from '@/types/api'

async function startSession(path: '/auth/login' | '/auth/register', credentials: Credentials) {
  const session = await request<AuthResponse>(path, { method: 'POST', body: credentials, session: true })
  setAccessToken(session.accessToken)
  return session
}

export const login = (credentials: Credentials) => startSession('/auth/login', credentials)

export const register = (credentials: Credentials) => startSession('/auth/register', credentials)

export const refresh = () => refreshSession()

export async function logout() {
  try {
    await request<void>('/auth/logout', { method: 'POST', session: true })
  } finally {
    setAccessToken(null)
  }
}

export const getCurrentUser = () => request<UserProfile>('/users/me', { auth: true })
