import { useState, type FormEvent } from 'react'
import { ApiError } from '@/api/client'
import type { Credentials } from '@/types/api'
import type { AuthMode } from '@/auth/context'
import Modal from './Modal'

interface Props {
  initialMode: AuthMode
  reason?: string
  onClose: () => void
  onLogin: (credentials: Credentials) => Promise<void>
  onRegister: (credentials: Credentials) => Promise<void>
}

const inputStyle = { background: 'rgba(255,255,255,0.06)', color: '#f0ede8', border: '1px solid rgba(255,255,255,0.1)' }

export default function AuthModal({ initialMode, reason, onClose, onLogin, onRegister }: Props) {
  const [mode, setMode] = useState<AuthMode>(initialMode)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})

  const switchMode = (next: AuthMode) => {
    setMode(next)
    setError(null)
    setFieldErrors({})
  }

  const submit = async (e: FormEvent) => {
    e.preventDefault()
    if (submitting) return
    setError(null)
    setFieldErrors({})
    if (mode === 'register' && password.length < 8) {
      setFieldErrors({ password: 'Use at least 8 characters.' })
      return
    }
    setSubmitting(true)
    try {
      const credentials = { email: email.trim(), password }
      await (mode === 'login' ? onLogin(credentials) : onRegister(credentials))
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.details.length > 0) {
          setFieldErrors(Object.fromEntries(err.details.map(d => [d.field, d.message])))
        }
        setError(err.code === 'VALIDATION_ERROR' && err.details.length > 0 ? null : err.message)
      } else {
        setError('Something went wrong. Please try again.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Modal onClose={onClose} labelledBy="auth-title" maxWidth={400}>
      <div className="p-8 rounded-2xl" style={{ background: '#18181d', border: '1px solid rgba(255,255,255,0.08)' }}>
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-2 font-bold text-lg tracking-tight">
            <span
              className="w-7 h-7 rounded-lg flex items-center justify-center text-xs font-black"
              style={{ background: 'linear-gradient(135deg, #e8843a, #c0392b)', color: '#fff' }}
            >M</span>
            <span style={{ color: '#f0ede8' }}>Movie<span style={{ color: '#e8843a' }}>AI</span></span>
          </div>
          <button
            onClick={onClose}
            aria-label="Close"
            className="w-8 h-8 rounded-lg flex items-center justify-center text-xs transition-smooth"
            style={{ background: 'rgba(255,255,255,0.05)', color: '#8a8590' }}
          >✕</button>
        </div>

        <h2 id="auth-title" className="text-2xl font-bold mb-1" style={{ fontFamily: "'DM Serif Display', serif", color: '#f0ede8' }}>
          {mode === 'login' ? 'Welcome back' : 'Create your account'}
        </h2>
        <p className="text-sm mb-6" style={{ color: '#8a8590' }}>
          {reason ?? (mode === 'login' ? 'Sign in to sync your watchlist.' : 'Save movies and shows to your personal watchlist.')}
        </p>

        <div className="flex gap-1 border-b mb-6" style={{ borderColor: 'rgba(255,255,255,0.08)' }}>
          {(['login', 'register'] as const).map(m => (
            <button
              key={m}
              type="button"
              onClick={() => switchMode(m)}
              className="px-4 py-2.5 text-sm font-medium transition-smooth relative"
              style={{ color: mode === m ? '#e8843a' : '#8a8590' }}
            >
              {m === 'login' ? 'Sign In' : 'Sign Up'}
              {mode === m && <span className="absolute bottom-0 left-0 right-0 h-0.5 rounded-t" style={{ background: '#e8843a' }} />}
            </button>
          ))}
        </div>

        <form onSubmit={submit} noValidate className="flex flex-col gap-4">
          <div>
            <label htmlFor="auth-email" className="text-xs font-medium block mb-1.5" style={{ color: '#8a8590' }}>Email</label>
            <input
              id="auth-email"
              type="email"
              autoComplete="email"
              required
              value={email}
              onChange={e => setEmail(e.target.value)}
              className="w-full text-sm rounded-lg px-3 py-2 outline-none"
              style={inputStyle}
            />
            {fieldErrors.email && <p className="text-xs mt-1" style={{ color: '#f87171' }}>{fieldErrors.email}</p>}
          </div>
          <div>
            <label htmlFor="auth-password" className="text-xs font-medium block mb-1.5" style={{ color: '#8a8590' }}>Password</label>
            <input
              id="auth-password"
              type="password"
              autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
              required
              minLength={mode === 'register' ? 8 : undefined}
              maxLength={72}
              value={password}
              onChange={e => setPassword(e.target.value)}
              className="w-full text-sm rounded-lg px-3 py-2 outline-none"
              style={inputStyle}
            />
            {fieldErrors.password ? (
              <p className="text-xs mt-1" style={{ color: '#f87171' }}>{fieldErrors.password}</p>
            ) : (
              mode === 'register' && <p className="text-xs mt-1" style={{ color: '#8a8590' }}>8 to 72 characters.</p>
            )}
          </div>

          {error && (
            <div
              role="alert"
              className="text-sm rounded-lg px-3 py-2"
              style={{ background: 'rgba(192,57,43,0.12)', border: '1px solid rgba(192,57,43,0.3)', color: '#f0ede8' }}
            >
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={submitting || !email || !password}
            className="mt-2 px-6 py-3 rounded-xl font-semibold text-sm transition-smooth"
            style={{
              background: 'linear-gradient(135deg, #e8843a, #c0392b)',
              color: '#fff',
              opacity: submitting || !email || !password ? 0.6 : 1,
            }}
          >
            {submitting ? 'Please wait…' : mode === 'login' ? 'Sign In' : 'Create Account'}
          </button>
        </form>
      </div>
    </Modal>
  )
}
