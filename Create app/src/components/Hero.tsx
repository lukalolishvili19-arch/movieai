import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { getMovieDetails } from '@/api/movies'
import { queryKeys } from '@/api/queryKeys'
import { useWatchlistEntry } from '@/hooks/useWatchlist'
import { formatRuntime } from '@/lib/format'
import type { MovieSummary } from '@/types/api'
import RatingBadge from './RatingBadge'
import TrailerModal from './TrailerModal'

export default function Hero({ movie, onDetails }: { movie: MovieSummary; onDetails: () => void }) {
  const { inWatchlist, toggle } = useWatchlistEntry('movie', movie.id)
  const [trailerOpen, setTrailerOpen] = useState(false)
  // Shares the cache entry with the detail page, so "More Details" opens instantly.
  const details = useQuery({
    queryKey: queryKeys.movie(movie.id),
    queryFn: ({ signal }) => getMovieDetails(movie.id, undefined, signal),
  })
  const trailer = details.data?.trailer ?? null
  const runtime = formatRuntime(details.data?.runtime)

  return (
    <div className="relative w-full" style={{ height: '88vh', minHeight: 560 }}>
      {movie.backdropUrl ? (
        <img
          src={movie.backdropUrl}
          alt={movie.title}
          className="absolute inset-0 w-full h-full object-cover"
          fetchPriority="high"
        />
      ) : (
        <div className="absolute inset-0" style={{ background: '#111114' }} />
      )}
      {/* Gradient overlays */}
      <div className="absolute inset-0" style={{ background: 'linear-gradient(to right, rgba(10,10,11,0.92) 0%, rgba(10,10,11,0.6) 50%, rgba(10,10,11,0.1) 100%)' }} />
      <div className="absolute inset-0" style={{ background: 'linear-gradient(to top, rgba(10,10,11,1) 0%, transparent 40%)' }} />

      <div className="absolute inset-0 flex items-center px-8 md:px-16 pt-16">
        <div className="max-w-xl">
          {/* Badges */}
          <div className="flex items-center gap-2 mb-4 flex-wrap">
            <span className="px-2 py-0.5 rounded text-xs font-semibold uppercase tracking-wider" style={{ background: '#e8843a', color: '#fff' }}>Featured</span>
            {movie.genres.slice(0, 2).map(g => (
              <span key={g} className="px-2 py-0.5 rounded text-xs font-medium" style={{ background: 'rgba(255,255,255,0.1)', color: '#c0bdb8' }}>{g}</span>
            ))}
          </div>

          <h1
            className="font-bold leading-none mb-4"
            style={{ fontFamily: "'DM Serif Display', serif", fontSize: 'clamp(2.5rem, 6vw, 4.5rem)', color: '#f0ede8' }}
          >
            {movie.title}
          </h1>

          {/* Meta */}
          <div className="flex items-center gap-4 mb-4">
            <RatingBadge rating={movie.rating} size="md" />
            {movie.year !== null && <span className="text-sm" style={{ color: '#8a8590' }}>{movie.year}</span>}
            {runtime && <span className="text-sm" style={{ color: '#8a8590' }}>{runtime}</span>}
          </div>

          {movie.overview && (
            <p className="text-sm leading-relaxed mb-6 line-clamp-3" style={{ color: '#c0bdb8', maxWidth: 480 }}>
              {movie.overview}
            </p>
          )}

          <div className="flex items-center gap-3 flex-wrap">
            <button
              onClick={() => trailer && setTrailerOpen(true)}
              disabled={!trailer}
              title={trailer ? undefined : details.isLoading ? 'Loading trailer…' : 'No trailer available'}
              className="flex items-center gap-2 px-5 py-2.5 rounded-lg font-semibold text-sm transition-smooth"
              style={{ background: '#e8843a', color: '#fff', opacity: trailer ? 1 : 0.55, cursor: trailer ? 'pointer' : 'not-allowed' }}
            >
              ▶ Watch Trailer
            </button>
            <button
              onClick={onDetails}
              className="flex items-center gap-2 px-5 py-2.5 rounded-lg font-semibold text-sm transition-smooth"
              style={{ background: 'rgba(255,255,255,0.1)', color: '#f0ede8', border: '1px solid rgba(255,255,255,0.15)' }}
            >
              More Details
            </button>
            <button
              onClick={toggle}
              className="w-10 h-10 rounded-lg flex items-center justify-center transition-smooth"
              style={{ background: inWatchlist ? '#e8843a' : 'rgba(255,255,255,0.1)', color: '#fff', border: '1px solid rgba(255,255,255,0.15)' }}
              title={inWatchlist ? 'Remove from Watchlist' : 'Add to Watchlist'}
              aria-label={inWatchlist ? 'Remove from Watchlist' : 'Add to Watchlist'}
            >
              {inWatchlist ? '✓' : '🔖'}
            </button>
          </div>
        </div>
      </div>
      {trailerOpen && trailer && <TrailerModal video={trailer} title={movie.title} onClose={() => setTrailerOpen(false)} />}
    </div>
  )
}
