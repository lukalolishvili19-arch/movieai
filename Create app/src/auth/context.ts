import { createContext, useContext } from 'react'
import type { Credentials, UserProfile } from '@/types/api'

export type AuthStatus = 'loading' | 'authenticated' | 'anonymous'
export type AuthMode = 'login' | 'register'

export interface AuthModalOptions {
  mode?: AuthMode
  /** Short explanation shown above the form, e.g. why sign-in is needed. */
  reason?: string
  /** Runs once after a successful sign-in or registration. */
  onSuccess?: () => void
}

export interface AuthContextValue {
  status: AuthStatus
  user: UserProfile | null
  login: (credentials: Credentials) => Promise<void>
  register: (credentials: Credentials) => Promise<void>
  logout: () => Promise<void>
  openAuthModal: (options?: AuthModalOptions) => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)

export function useAuth(): AuthContextValue {
  const value = useContext(AuthContext)
  if (!value) throw new Error('useAuth must be used inside <AuthProvider>')
  return value
}
