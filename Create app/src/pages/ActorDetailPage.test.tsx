import { describe, expect, it } from 'vitest'
import { screen } from '@testing-library/react'
import ActorDetailPage from './ActorDetailPage'
import { apiError, json, mockFetch, renderWithProviders } from '@/test/utils'
import type { PersonDetails } from '@/types/api'

const person: PersonDetails = {
  id: 1190668,
  name: 'Timothée Chalamet',
  biography: null,
  birthday: null,
  deathday: null,
  placeOfBirth: null,
  knownForDepartment: 'Acting',
  alsoKnownAs: [],
  popularity: 88.4,
  profileUrl: null,
  homepage: null,
  imdbId: null,
  credits: {
    movies: [
      { id: 693134, mediaType: 'movie', title: 'Dune: Part Two', character: 'Paul Atreides', job: null, creditType: 'cast', releaseDate: '2024-02-27', year: 2024, rating: 8.2, voteCount: 10, popularity: 100, genres: ['Science Fiction'], posterUrl: null, episodeCount: null },
    ],
    tv: [],
  },
  images: [],
}

describe('ActorDetailPage', () => {
  it('shows real TMDB fields and marks missing ones unavailable', async () => {
    mockFetch(url => (url.pathname === '/api/v1/people/1190668' ? json(person) : undefined))
    renderWithProviders(<ActorDetailPage />, { route: '/person/1190668', path: '/person/:id' })

    expect(await screen.findByRole('heading', { name: 'Timothée Chalamet' })).toBeInTheDocument()
    expect(screen.getByText('No biography is available.')).toBeInTheDocument()
    expect(screen.getAllByText('Unavailable')).toHaveLength(2)
    expect(screen.getByText('Paul Atreides')).toBeInTheDocument()
    expect(screen.queryByText('March 15, 1990')).not.toBeInTheDocument()
    expect(screen.queryByText('Los Angeles, CA')).not.toBeInTheDocument()
  })

  it('renders a not-found state for unknown people', async () => {
    mockFetch(() => apiError(404, 'PERSON_NOT_FOUND', 'Person not found.'))
    renderWithProviders(<ActorDetailPage />, { route: '/person/999', path: '/person/:id' })
    expect(await screen.findByText('Person not found')).toBeInTheDocument()
  })
})
