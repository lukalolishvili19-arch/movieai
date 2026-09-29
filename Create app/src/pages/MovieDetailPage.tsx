import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getMovieDetails } from '@/api/movies'
import { ApiError } from '@/api/client'
import { queryKeys } from '@/api/queryKeys'
import { useWatchlistEntry } from '@/hooks/useWatchlist'
import { useBack } from '@/hooks/useBack'
import { formatDate, formatRuntime } from '@/lib/format'
import type { MovieDetails, VideoAsset } from '@/types/api'
import RatingBadge from '@/components/RatingBadge'
import MovieCard from '@/components/MovieCard'
import TabBar from '@/components/TabBar'
import PosterImage from '@/components/PosterImage'
import TrailerModal from '@/components/TrailerModal'
import WatchProvidersSection from '@/components/WatchProvidersSection'
import ErrorState from '@/components/ErrorState'
import NotFoundState from '@/components/NotFoundState'
import { CastList, PhotoGrid, VideoList } from '@/components/MediaExtras'
import { HeroSkeleton, TextSkeleton } from '@/components/Skeletons'

const TABS = ['Cast', 'Videos', 'Photos', 'Similar'] as const
type Tab = (typeof TABS)[number]

function Fact({ label, value }: { label: string; value: string | null | undefined }) {
  if (!value) return null
  return (
    <p className="text-sm mb-2">
      <span style={{ color: '#8a8590' }}>{label}: </span>
      <span style={{ color: '#f0ede8' }}>{value}</span>
    </p>
  )
}

function MovieDetailView({ movie }: { movie: MovieDetails }) {
  const back = useBack('/')
  const { inWatchlist, toggle } = useWatchlistEntry('movie', movie.id)
  const [activeTab, setActiveTab] = useState<Tab>('Cast')
  const [playing, setPlaying] = useState<VideoAsset | null>(null)
  const runtime = formatRuntime(movie.runtime)
  const writers = movie.crew.filter(c => c.department === 'Writing').map(c => c.name)

  return (
    <div>
      {/* Hero */}
      <div className="relative" style={{ height: '70vh', minHeight: 480 }}>
        {movie.backdropUrl ? (
          <img src={movie.backdropUrl} alt={movie.title} className="absolute inset-0 w-full h-full object-cover" />
        ) : (
          <div className="absolute inset-0" style={{ background: '#111114' }} />
        )}
        <div className="absolute inset-0" style={{ background: 'linear-gradient(to right, rgba(10,10,11,0.95) 0%, rgba(10,10,11,0.6) 60%, rgba(10,10,11,0.2) 100%)' }} />
        <div className="absolute inset-0" style={{ background: 'linear-gradient(to top, rgba(10,10,11,1) 0%, transparent 50%)' }} />
        <button onClick={back} className="absolute top-20 left-8 md:left-16 flex items-center gap-2 text-sm font-medium transition-smooth z-10" style={{ color: '#8a8590' }}>
          ← Back
        </button>
        <div className="absolute inset-0 flex items-end pb-10 px-8 md:px-16">
          <div className="flex gap-8 items-end">
            <div className="poster-wrap rounded-xl overflow-hidden flex-shrink-0 hidden sm:block" style={{ width: 140, height: 210, background: '#18181d' }}>
              <PosterImage src={movie.posterUrl} alt={movie.title} loading="eager" />
            </div>
            <div className="pb-2">
              <div className="flex flex-wrap gap-2 mb-3">
                {movie.genres.map(g => (
                  <span key={g.id} className="px-2 py-0.5 rounded text-xs font-medium" style={{ background: 'rgba(232,132,58,0.2)', color: '#e8843a' }}>{g.name}</span>
                ))}
              </div>
              <h1 className="font-bold leading-none mb-3" style={{ fontFamily: "'DM Serif Display', serif", fontSize: 'clamp(1.8rem, 4vw, 3.5rem)', color: '#f0ede8' }}>{movie.title}</h1>
              {movie.originalTitle && movie.originalTitle !== movie.title && (
                <p className="text-sm mb-3" style={{ color: '#8a8590' }}>{movie.originalTitle}</p>
              )}
              <div className="flex items-center gap-4 mb-4 flex-wrap">
                <RatingBadge rating={movie.rating} size="md" />
                {movie.year !== null && <span className="text-sm" style={{ color: '#8a8590' }}>{movie.year}</span>}
                {runtime && <span className="text-sm" style={{ color: '#8a8590' }}>{runtime}</span>}
              </div>
              <div className="flex gap-3 flex-wrap">
                <button
                  onClick={() => movie.trailer && setPlaying(movie.trailer)}
                  disabled={!movie.trailer}
                  title={movie.trailer ? undefined : 'No trailer available'}
                  className="flex items-center gap-2 px-5 py-2.5 rounded-lg font-semibold text-sm"
                  style={{ background: '#e8843a', color: '#fff', opacity: movie.trailer ? 1 : 0.55, cursor: movie.trailer ? 'pointer' : 'not-allowed' }}
                >▶ Watch Trailer</button>
                <button onClick={toggle} className="px-5 py-2.5 rounded-lg font-semibold text-sm transition-smooth" style={{ background: inWatchlist ? 'rgba(232,132,58,0.2)' : 'rgba(255,255,255,0.1)', color: inWatchlist ? '#e8843a' : '#f0ede8', border: '1px solid rgba(255,255,255,0.15)' }}>
                  {inWatchlist ? '✓ In Watchlist' : '+ Watchlist'}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="px-8 md:px-16 py-8">
        {movie.tagline && <p className="text-sm italic mb-3" style={{ color: '#e8843a' }}>{movie.tagline}</p>}
        <p className="text-sm leading-relaxed mb-8 max-w-2xl" style={{ color: movie.overview ? '#c0bdb8' : '#8a8590' }}>
          {movie.overview || 'No overview is available for this title.'}
        </p>
        <Fact label={movie.directors.length > 1 ? 'Directors' : 'Director'} value={movie.directors.map(d => d.name).join(', ')} />
        <Fact label="Writers" value={writers.slice(0, 4).join(', ')} />
        <Fact label="Release Date" value={formatDate(movie.releaseDate) ?? 'Unavailable'} />
        <Fact label="Status" value={movie.status} />

        <WatchProvidersSection providers={movie.watchProviders} />

        {/* Tabs */}
        <TabBar tabs={TABS} active={activeTab} onChange={setActiveTab} className="mt-8 mb-6" />

        {activeTab === 'Cast' && <CastList cast={movie.cast} />}

        {activeTab === 'Similar' && (
          movie.similar.length > 0 ? (
            <div className="media-grid">
              {movie.similar.map(m => <MovieCard key={m.id} item={m} fluid />)}
            </div>
          ) : (
            <p className="text-sm" style={{ color: '#8a8590' }}>No similar titles found.</p>
          )
        )}

        {activeTab === 'Videos' && <VideoList videos={movie.videos} onPlay={setPlaying} />}

        {activeTab === 'Photos' && <PhotoGrid images={movie.images.backdrops} />}
      </div>
      {playing && <TrailerModal video={playing} title={movie.title} onClose={() => setPlaying(null)} />}
    </div>
  )
}

export default function MovieDetailPage() {
  const id = Number(useParams().id)
  const valid = Number.isInteger(id) && id > 0
  const query = useQuery({
    queryKey: queryKeys.movie(id),
    queryFn: ({ signal }) => getMovieDetails(id, undefined, signal),
    enabled: valid,
  })

  if (!valid || (query.error instanceof ApiError && query.error.isNotFound)) {
    return <NotFoundState title="Movie not found" message="We couldn't find this movie. It may have been removed from TMDB." />
  }
  if (query.isError) {
    return (
      <div className="pt-24 px-8 md:px-16">
        <ErrorState error={query.error} onRetry={() => query.refetch()} title="Couldn't load this movie" />
      </div>
    )
  }
  if (!query.data) {
    return (
      <div>
        <HeroSkeleton height="70vh" minHeight={480} />
        <div className="px-8 md:px-16 py-8"><TextSkeleton lines={4} /></div>
      </div>
    )
  }
  return <MovieDetailView key={id} movie={query.data} />
}
