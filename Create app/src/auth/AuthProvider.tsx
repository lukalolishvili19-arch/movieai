import { useCallback, useEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import * as authApi from '@/api/auth'
import { onSessionChange } from '@/api/client'
import type { AuthResponse, Credentials, UserProfile } from '@/types/api'
import AuthModal from '@/components/AuthModal'
import { AuthContext, type AuthContextValue, type AuthModalOptions, type AuthMode, type AuthStatus } from './context'

// Only a hint that a refresh cookie may exist, so anonymous visitors skip the refresh call. Holds no secret.
const SESSION_HINT_KEY = 'movieai.session'
const REFRESH_LEEWAY_SECONDS = 60

function readHint() {
  try {
    return window.localStorage.getItem(SESSION_HINT_KEY) === '1'
  } catch {
    return false
  }
}

function writeHint(active: boolean) {
  try {
    if (active) window.localStorage.setItem(SESSION_HINT_KEY, '1')
    else window.localStorage.removeItem(SESSION_HINT_KEY)
  } catch {
    // Storage may be unavailable (private mode); the session still works for this tab.
  }
}

interface ModalState {
  open: boolean
  mode: AuthMode
  reason?: string
}

export default function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient()
  const [status, setStatus] = useState<AuthStatus>(() => (readHint() ? 'loading' : 'anonymous'))
  const [user, setUser] = useState<UserProfile | null>(null)
  const [modal, setModal] = useState<ModalState>({ open: false, mode: 'login' })
  const pendingAction = useRef<(() => void) | undefined>(undefined)
  const statusRef = useRef(status)
  const refreshTimer = useRef<number | undefined>(undefined)
  statusRef.current = status

  const scheduleRefresh = useCallback((expiresIn: number) => {
    window.clearTimeout(refreshTimer.current)
    const delay = Math.max(expiresIn - REFRESH_LEEWAY_SECONDS, 10) * 1000
    refreshTimer.current = window.setTimeout(() => {
      void authApi.refresh()
    }, delay)
  }, [])

  const applySession = useCallback(
    (session: AuthResponse) => {
      setUser(prev => {
        if (prev && prev.id !== session.user.id) queryClient.removeQueries({ queryKey: ['watchlist'] })
        return session.user
      })
      setStatus('authenticated')
      writeHint(true)
      scheduleRefresh(session.expiresIn)
    },
    [queryClient, scheduleRefresh],
  )

  const clearSession = useCallback(() => {
    window.clearTimeout(refreshTimer.current)
    setUser(null)
    setStatus('anonymous')
    writeHint(false)
    queryClient.removeQueries({ queryKey: ['watchlist'] })
  }, [queryClient])

  useEffect(() => {
    const unsubscribe = onSessionChange(session => {
      if (session) {
        applySession(session)
        return
      }
      const wasAuthenticated = statusRef.current === 'authenticated'
      clearSession()
      if (wasAuthenticated) {
        setModal({ open: true, mode: 'login', reason: 'Your session has expired. Sign in again to keep using your watchlist.' })
      }
    })
    if (readHint()) {
      void authApi.refresh().then(session => {
        // A network failure resolves to null without notifying listeners; don't stay stuck loading.
        if (!session && statusRef.current === 'loading') setStatus('anonymous')
      })
    }
    return () => {
      unsubscribe()
      window.clearTimeout(refreshTimer.current)
    }
  }, [applySession, clearSession])

  const finishAuth = useCallback(
    (session: AuthResponse) => {
      applySession(session)
      setModal(m => ({ ...m, open: false, reason: undefined }))
      const action = pendingAction.current
      pendingAction.current = undefined
      action?.()
    },
    [applySession],
  )

  const login = useCallback(async (credentials: Credentials) => finishAuth(await authApi.login(credentials)), [finishAuth])
  const register = useCallback(
    async (credentials: Credentials) => finishAuth(await authApi.register(credentials)),
    [finishAuth],
  )

  const logout = useCallback(async () => {
    try {
      await authApi.logout()
    } finally {
      clearSession()
    }
  }, [clearSession])

  const openAuthModal = useCallback((options: AuthModalOptions = {}) => {
    pendingAction.current = options.onSuccess
    setModal({ open: true, mode: options.mode ?? 'login', reason: options.reason })
  }, [])

  const closeAuthModal = useCallback(() => {
    pendingAction.current = undefined
    setModal(m => ({ ...m, open: false, reason: undefined }))
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({ status, user, login, register, logout, openAuthModal }),
    [status, user, login, register, logout, openAuthModal],
  )

  return (
    <AuthContext.Provider value={value}>
      {children}
      {modal.open && (
        <AuthModal
          initialMode={modal.mode}
          reason={modal.reason}
          onClose={closeAuthModal}
          onLogin={login}
          onRegister={register}
        />
      )}
    </AuthContext.Provider>
  )
}
