import type { ApiErrorBody, AuthResponse, FieldViolation } from '@/types/api'

const API_BASE = (import.meta.env.VITE_API_URL ?? '').replace(/\/+$/, '')
const API_PREFIX = `${API_BASE}/api/v1`
const CLIENT_HEADER = { 'X-MovieAI-Client': 'web' }

export class ApiError extends Error {
  readonly status: number
  readonly code: string
  readonly details: FieldViolation[]

  constructor(status: number, code: string, message: string, details: FieldViolation[] = []) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
    this.details = details
  }

  get isNotFound() {
    return this.status === 404
  }

  get isUnauthorized() {
    return this.status === 401
  }
}

// The access token lives only in memory; the refresh token is an HttpOnly cookie the browser manages.
let accessToken: string | null = null
let refreshInFlight: Promise<AuthResponse | null> | null = null
const sessionListeners = new Set<(session: AuthResponse | null) => void>()

export function setAccessToken(token: string | null) {
  accessToken = token
}

export function hasAccessToken() {
  return accessToken !== null
}

/** Notified whenever a refresh attempt succeeds (new session) or fails (session ended). */
export function onSessionChange(listener: (session: AuthResponse | null) => void) {
  sessionListeners.add(listener)
  return () => {
    sessionListeners.delete(listener)
  }
}

type QueryValue = string | number | boolean | null | undefined | Array<string | number>

export function buildQuery(params: Record<string, QueryValue>) {
  const search = new URLSearchParams()
  for (const [key, value] of Object.entries(params)) {
    if (value === undefined || value === null || value === '') continue
    if (Array.isArray(value)) {
      if (value.length > 0) search.set(key, value.join(','))
    } else {
      search.set(key, String(value))
    }
  }
  const qs = search.toString()
  return qs ? `?${qs}` : ''
}

interface RequestOptions {
  method?: 'GET' | 'POST' | 'DELETE'
  body?: unknown
  /** Sends the bearer token and transparently refreshes it once on 401. */
  auth?: boolean
  /** Auth endpoints need the refresh cookie and the anti-CSRF client header. */
  session?: boolean
  signal?: AbortSignal
}

async function parseError(response: Response): Promise<ApiError> {
  try {
    const body = (await response.json()) as Partial<ApiErrorBody>
    if (body && body.error && typeof body.error.code === 'string') {
      return new ApiError(response.status, body.error.code, body.error.message, body.error.details ?? [])
    }
  } catch {
    // Non-JSON error bodies (e.g. a proxy page) fall through to the generic error.
  }
  if (response.status === 429) {
    return new ApiError(429, 'RATE_LIMITED', 'Too many requests. Please wait a moment and try again.')
  }
  if (response.status >= 500) {
    return new ApiError(response.status, 'SERVICE_UNAVAILABLE', 'MovieAI is temporarily unavailable. Please try again shortly.')
  }
  return new ApiError(response.status, 'REQUEST_FAILED', 'Something went wrong. Please try again.')
}

async function send(path: string, options: RequestOptions): Promise<Response> {
  const headers: Record<string, string> = { Accept: 'application/json' }
  if (options.body !== undefined) headers['Content-Type'] = 'application/json'
  if (options.session) Object.assign(headers, CLIENT_HEADER)
  if (options.auth && accessToken) headers.Authorization = `Bearer ${accessToken}`

  try {
    return await fetch(`${API_PREFIX}${path}`, {
      method: options.method ?? 'GET',
      headers,
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
      credentials: options.session ? 'include' : 'same-origin',
      signal: options.signal,
    })
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') throw error
    throw new ApiError(0, 'NETWORK_ERROR', "Can't reach MovieAI right now. Check your connection and try again.")
  }
}

/**
 * Exchanges the refresh cookie for a new access token. Concurrent callers share one request so the
 * rotated refresh token is never presented twice (which the server treats as token reuse).
 */
export function refreshSession(): Promise<AuthResponse | null> {
  if (!refreshInFlight) {
    refreshInFlight = (async () => {
      try {
        const response = await send('/auth/refresh', { method: 'POST', session: true })
        if (!response.ok) {
          setAccessToken(null)
          sessionListeners.forEach(l => l(null))
          return null
        }
        const session = (await response.json()) as AuthResponse
        setAccessToken(session.accessToken)
        sessionListeners.forEach(l => l(session))
        return session
      } catch {
        // Network failure: keep the current state; the caller surfaces the error.
        return null
      } finally {
        refreshInFlight = null
      }
    })()
  }
  return refreshInFlight
}

export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  let response = await send(path, options)

  if (response.status === 401 && options.auth) {
    const session = await refreshSession()
    if (session) response = await send(path, options)
  }

  if (!response.ok) throw await parseError(response)
  if (response.status === 204) return undefined as T
  return (await response.json()) as T
}
