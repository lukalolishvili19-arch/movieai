import { describe, expect, it } from 'vitest'
import { fireEvent, screen } from '@testing-library/react'
import MovieCard from './MovieCard'
import WatchProvidersSection from './WatchProvidersSection'
import { mockFetch, movie, renderWithProviders } from '@/test/utils'

describe('MovieCard', () => {
  it('renders real summary data and marks unrated titles as NR', () => {
    mockFetch(() => undefined)
    renderWithProviders(<MovieCard item={movie({ rating: null, year: null, title: 'Unreleased' })} />)
    expect(screen.getByText('Unreleased')).toBeInTheDocument()
    expect(screen.getByText('NR')).toBeInTheDocument()
    expect(screen.getByText('Science Fiction')).toBeInTheDocument()
  })

  it('asks anonymous users to sign in when adding to the watchlist', async () => {
    const fetch = mockFetch(() => undefined)
    renderWithProviders(<MovieCard item={movie()} />)
    fireEvent.mouseEnter(screen.getByRole('link', { name: 'Dune: Part Two' }))
    fireEvent.click(screen.getByRole('button', { name: 'Add Dune: Part Two to Watchlist' }))
    expect(await screen.findByRole('dialog')).toBeInTheDocument()
    expect(screen.getByText('Sign in to save titles to your watchlist.')).toBeInTheDocument()
    expect(fetch).not.toHaveBeenCalled()
  })
})

describe('WatchProvidersSection', () => {
  it('groups providers and credits JustWatch', () => {
    renderWithProviders(
      <WatchProvidersSection
        providers={{
          region: 'US',
          link: 'https://www.themoviedb.org/movie/693134/watch?locale=US',
          flatrate: [{ id: 8, name: 'Netflix', logoUrl: null, displayPriority: 1 }],
          free: [],
          ads: [],
          rent: [{ id: 2, name: 'Apple TV', logoUrl: null, displayPriority: 3 }],
          buy: [],
          attribution: 'JustWatch',
        }}
      />,
    )
    expect(screen.getByText('Netflix')).toBeInTheDocument()
    expect(screen.getByText('Apple TV')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'JustWatch' })).toHaveAttribute('href', 'https://www.justwatch.com')
  })

  it('says so when nothing is available instead of inventing providers', () => {
    renderWithProviders(<WatchProvidersSection providers={null} />)
    expect(screen.getByText(/No streaming, rental or purchase options/)).toBeInTheDocument()
  })
})
