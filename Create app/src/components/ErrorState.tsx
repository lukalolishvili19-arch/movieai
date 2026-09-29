import { ApiError } from '@/api/client'

export function errorMessage(error: unknown, fallback = 'Something went wrong. Please try again.'): string {
  if (error instanceof ApiError) {
    if (error.code === 'RATE_LIMITED') return "You're going a bit fast. Please wait a moment and try again."
    return error.message || fallback
  }
  return fallback
}

interface Props {
  error: unknown
  onRetry?: () => void
  /** "inline" fits inside a carousel row; "page" is a centered full-section state. */
  variant?: 'inline' | 'page'
  title?: string
}

export default function ErrorState({ error, onRetry, variant = 'page', title }: Props) {
  const message = errorMessage(error)

  if (variant === 'inline') {
    return (
      <div
        role="alert"
        className="flex items-center gap-4 px-5 py-4 rounded-xl w-full"
        style={{ background: '#18181d', border: '1px solid rgba(255,255,255,0.06)', minHeight: 120 }}
      >
        <span style={{ fontSize: 22 }}>⚠️</span>
        <div className="flex-1 min-w-0">
          <p className="text-sm font-semibold" style={{ color: '#f0ede8' }}>{title ?? "Couldn't load this section"}</p>
          <p className="text-xs mt-0.5" style={{ color: '#8a8590' }}>{message}</p>
        </div>
        {onRetry && (
          <button
            onClick={onRetry}
            className="px-3 py-1.5 rounded-lg text-xs font-medium transition-smooth flex-shrink-0"
            style={{ background: 'rgba(232,132,58,0.1)', color: '#e8843a', border: '1px solid rgba(232,132,58,0.2)' }}
          >
            Try Again
          </button>
        )}
      </div>
    )
  }

  return (
    <div role="alert" className="text-center py-16">
      <p className="text-4xl mb-4">⚠️</p>
      <p className="text-lg font-semibold mb-1" style={{ color: '#f0ede8' }}>{title ?? 'Something went wrong'}</p>
      <p className="text-sm max-w-md mx-auto" style={{ color: '#8a8590' }}>{message}</p>
      {onRetry && (
        <button
          onClick={onRetry}
          className="mt-6 px-5 py-2.5 rounded-lg font-semibold text-sm transition-smooth"
          style={{ background: '#e8843a', color: '#fff' }}
        >
          Try Again
        </button>
      )}
    </div>
  )
}
