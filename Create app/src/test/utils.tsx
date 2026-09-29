import type { ReactElement } from 'react'
import { render } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { vi } from 'vitest'
import AuthProvider from '@/auth/AuthProvider'
import type { MovieSummary, PagedResponse } from '@/types/api'

type Handler = (url: URL, init: RequestInit) => Response | undefined

export function json(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } })
}

export function apiError(status: number, code: string, message: string): Response {
  return json({ success: false, error: { code, message } }, status)
}

/** Stubs fetch with a router over the request URL; unmatched requests fail loudly. */
export function mockFetch(handler: Handler) {
  const fn = vi.fn(async (input: RequestInfo | URL, init: RequestInit = {}) => {
    const url = new URL(typeof input === 'string' ? input : input.toString(), 'http://localhost')
    const response = handler(url, init)
    if (!response) throw new Error(`Unexpected request: ${init.method ?? 'GET'} ${url.pathname}${url.search}`)
    return response
  })
  vi.stubGlobal('fetch', fn)
  return fn
}

export function renderWithProviders(ui: ReactElement, { route = '/', path = '*' }: { route?: string; path?: string } = {}) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false, gcTime: 0 }, mutations: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={[route]}>
        <AuthProvider>
          <Routes>
            <Route path={path} element={ui} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

export function movie(overrides: Partial<MovieSummary> = {}): MovieSummary {
  return {
    id: 693134,
    mediaType: 'movie',
    title: 'Dune: Part Two',
    originalTitle: 'Dune: Part Two',
    overview: 'Paul Atreides unites with the Fremen.',
    releaseDate: '2024-02-27',
    year: 2024,
    rating: 8.2,
    voteCount: 5000,
    popularity: 300,
    genreIds: [878],
    genres: ['Science Fiction'],
    posterPath: '/poster.jpg',
    posterUrl: 'https://image.tmdb.org/t/p/w342/poster.jpg',
    backdropPath: '/backdrop.jpg',
    backdropUrl: 'https://image.tmdb.org/t/p/w1280/backdrop.jpg',
    ...overrides,
  }
}

export function page<T>(results: T[], totalPages = 1): PagedResponse<T> {
  return { results, page: 1, totalPages, totalResults: results.length }
}
