import { beforeEach, describe, expect, it } from 'vitest'
import { ApiError, buildQuery, request, setAccessToken } from './client'
import { apiError, json, mockFetch } from '@/test/utils'

describe('buildQuery', () => {
  it('skips empty values and joins arrays', () => {
    expect(buildQuery({ page: 2, genres: [28, 12], minRating: undefined, q: '', sort: null, empty: [] })).toBe('?page=2&genres=28%2C12')
  })

  it('returns an empty string when nothing is set', () => {
    expect(buildQuery({ a: undefined })).toBe('')
  })
})

describe('request', () => {
  beforeEach(() => setAccessToken(null))

  it('calls the backend under /api/v1, never TMDB', async () => {
    const fetch = mockFetch(url => (url.pathname === '/api/v1/movies/popular' ? json({ ok: true }) : undefined))
    await request('/movies/popular')
    const calledUrl = String(fetch.mock.calls[0][0])
    expect(calledUrl).toBe('/api/v1/movies/popular')
    expect(calledUrl).not.toContain('themoviedb')
  })

  it('maps the error envelope to ApiError', async () => {
    mockFetch(() => apiError(503, 'TMDB_UNAVAILABLE', 'Movie data is temporarily unavailable.'))
    const error = await request('/movies/popular').catch((e: unknown) => e)
    expect(error).toBeInstanceOf(ApiError)
    expect(error).toMatchObject({ status: 503, code: 'TMDB_UNAVAILABLE', message: 'Movie data is temporarily unavailable.' })
  })

  it('maps network failures to a friendly error', async () => {
    mockFetch(() => {
      throw new TypeError('Failed to fetch')
    })
    await expect(request('/movies/popular')).rejects.toMatchObject({ code: 'NETWORK_ERROR' })
  })

  it('refreshes an expired access token once and retries', async () => {
    setAccessToken('expired')
    const fetch = mockFetch((url, init) => {
      const auth = (init.headers as Record<string, string>).Authorization
      if (url.pathname === '/api/v1/auth/refresh') {
        expect((init.headers as Record<string, string>)['X-MovieAI-Client']).toBe('web')
        expect(init.credentials).toBe('include')
        return json({ accessToken: 'fresh', tokenType: 'Bearer', expiresIn: 900, user: { id: 'u1', email: 'a@b.c', createdAt: '2026-01-01T00:00:00Z' } })
      }
      if (url.pathname === '/api/v1/watchlist/ids') {
        return auth === 'Bearer fresh' ? json([]) : apiError(401, 'TOKEN_EXPIRED', 'Your session has expired.')
      }
      return undefined
    })
    await expect(request('/watchlist/ids', { auth: true })).resolves.toEqual([])
    expect(fetch.mock.calls.map(c => new URL(String(c[0]), 'http://x').pathname)).toEqual([
      '/api/v1/watchlist/ids',
      '/api/v1/auth/refresh',
      '/api/v1/watchlist/ids',
    ])
  })

  it('does not attach the bearer token to public requests', async () => {
    setAccessToken('secret')
    const fetch = mockFetch(() => json({}))
    await request('/movies/popular')
    expect((fetch.mock.calls[0][1]?.headers as Record<string, string>).Authorization).toBeUndefined()
  })
})
