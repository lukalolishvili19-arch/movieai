import { describe, expect, it } from 'vitest'
import { screen } from '@testing-library/react'
import SearchPage from './SearchPage'
import { apiError, json, mockFetch, movie, page, renderWithProviders } from '@/test/utils'

describe('SearchPage', () => {
  it('searches through the backend and renders grouped results', async () => {
    const fetch = mockFetch(url => {
      if (url.pathname === '/api/v1/search') {
        expect(url.searchParams.get('query')).toBe('dune')
        return json({
          query: 'dune',
          movies: page([movie()]),
          tv: page([]),
          people: page([{ id: 1, name: 'Zendaya', knownForDepartment: 'Acting', knownFor: ['Euphoria'], popularity: 50, profilePath: null, profileUrl: null }]),
        })
      }
      return undefined
    })
    renderWithProviders(<SearchPage />, { route: '/search?q=dune' })

    expect(await screen.findByText('Dune: Part Two')).toBeInTheDocument()
    expect(screen.getByText('Zendaya')).toBeInTheDocument()
    expect(fetch).toHaveBeenCalledTimes(1)
  })

  it('shows the empty state when nothing matches', async () => {
    mockFetch(() => json({ query: 'zzz', movies: page([]), tv: page([]), people: page([]) }))
    renderWithProviders(<SearchPage />, { route: '/search?q=zzz' })
    expect(await screen.findByText('No results found')).toBeInTheDocument()
  })

  it('shows a designed error state when the catalog is unavailable', async () => {
    mockFetch(() => apiError(503, 'TMDB_UNAVAILABLE', 'Movie data is temporarily unavailable.'))
    renderWithProviders(<SearchPage />, { route: '/search?q=dune' })
    expect(await screen.findByText('Movie data is temporarily unavailable.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Try Again' })).toBeInTheDocument()
  })

  it('prompts for input without calling the API when the query is empty', () => {
    const fetch = mockFetch(() => undefined)
    renderWithProviders(<SearchPage />, { route: '/search' })
    expect(screen.getByText('Start typing to search')).toBeInTheDocument()
    expect(fetch).not.toHaveBeenCalled()
  })
})
