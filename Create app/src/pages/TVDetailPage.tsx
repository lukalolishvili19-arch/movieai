import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getTvDetails } from '@/api/tv'
import { ApiError } from '@/api/client'
import { queryKeys } from '@/api/queryKeys'
import { useWatchlistEntry } from '@/hooks/useWatchlist'
import { useBack } from '@/hooks/useBack'
import { formatDate, parseDate } from '@/lib/format'
import type { TvDetails, VideoAsset } from '@/types/api'
import RatingBadge from '@/components/RatingBadge'
import MovieCard from '@/components/MovieCard'
import PosterImage from '@/components/PosterImage'
import TrailerModal from '@/components/TrailerModal'
import WatchProvidersSection from '@/components/WatchProvidersSection'
import ErrorState from '@/components/ErrorState'
import NotFoundState from '@/components/NotFoundState'
import { CastList, PhotoGrid, VideoList } from '@/components/MediaExtras'
import { HeroSkeleton, TextSkeleton } from '@/components/Skeletons'

const ACTIVE_STATUSES = new Set(['Returning Series', 'In Production', 'Planned', 'Pilot'])

function TVDetailView({ show }: { show: TvDetails }) {
  const back = useBack('/')
  const { inWatchlist, toggle } = useWatchlistEntry('tv', show.id)
  const [playing, setPlaying] = useState<VideoAsset | null>(null)
  const seasonCount = show.numberOfSeasons ?? show.seasons.filter(s => s.seasonNumber > 0).length
  const active = show.status !== null && ACTIVE_STATUSES.has(show.status)
  const statusLabel = show.status === 'Returning Series' ? 'Returning' : show.status

  return (
    <div>
      <div className="relative" style={{ height: '60vh', minHeight: 400 }}>
        {show.backdropUrl ? (
          <img src={show.backdropUrl} alt={show.title} className="absolute inset-0 w-full h-full object-cover" />
        ) : (
          <div className="absolute inset-0" style={{ background: '#111114' }} />
        )}
        <div className="absolute inset-0" style={{ background: 'linear-gradient(to top, rgba(10,10,11,1) 0%, rgba(10,10,11,0.5) 60%, transparent 100%)' }} />
        <button onClick={back} className="absolute top-20 left-8 md:left-16 text-sm font-medium z-10" style={{ color: '#8a8590' }}>← Back</button>
      </div>
      <div className="px-8 md:px-16 py-8">
        <div className="flex gap-6 items-start -mt-20 relative z-10">
          <div className="poster-wrap rounded-xl overflow-hidden flex-shrink-0 hidden sm:block" style={{ width: 120, height: 180, background: '#18181d' }}>
            <PosterImage src={show.posterUrl} alt={show.title} loading="eager" />
          </div>
          <div className="sm:pt-16">
            <div className="flex flex-wrap gap-2 mb-2">
              {show.genres.map(g => <span key={g.id} className="px-2 py-0.5 rounded text-xs font-medium" style={{ background: 'rgba(232,132,58,0.2)', color: '#e8843a' }}>{g.name}</span>)}
            </div>
            <h1 className="text-3xl font-bold mb-3" style={{ fontFamily: "'DM Serif Display', serif", color: '#f0ede8' }}>{show.title}</h1>
            {show.originalTitle && show.originalTitle !== show.title && (
              <p className="text-sm -mt-2 mb-3" style={{ color: '#8a8590' }}>{show.originalTitle}</p>
            )}
            <div className="flex items-center gap-4 mb-4 flex-wrap">
              <RatingBadge rating={show.rating} size="md" />
              {show.year !== null && <span className="text-sm" style={{ color: '#8a8590' }}>{show.year}</span>}
              {seasonCount > 0 && <span className="text-sm" style={{ color: '#8a8590' }}>{seasonCount} Season{seasonCount > 1 ? 's' : ''}</span>}
              {show.numberOfEpisodes ? <span className="text-sm" style={{ color: '#8a8590' }}>{show.numberOfEpisodes} Episodes</span> : null}
              {show.networks.length > 0 && <span className="text-sm" style={{ color: '#8a8590' }}>{show.networks.map(n => n.name).join(', ')}</span>}
              {statusLabel && <span className="px-2 py-0.5 rounded text-xs font-medium" style={{ background: active ? 'rgba(34,197,94,0.15)' : 'rgba(138,133,144,0.15)', color: active ? '#22c55e' : '#8a8590' }}>{statusLabel}</span>}
            </div>
            <div className="flex gap-3 flex-wrap">
              <button
                onClick={() => show.trailer && setPlaying(show.trailer)}
                disabled={!show.trailer}
                title={show.trailer ? undefined : 'No trailer available'}
                className="px-5 py-2.5 rounded-lg font-semibold text-sm"
                style={{ background: '#e8843a', color: '#fff', opacity: show.trailer ? 1 : 0.55, cursor: show.trailer ? 'pointer' : 'not-allowed' }}
              >▶ Watch Trailer</button>
              <button onClick={toggle} className="px-5 py-2.5 rounded-lg font-semibold text-sm transition-smooth" style={{ background: inWatchlist ? 'rgba(232,132,58,0.2)' : 'rgba(255,255,255,0.1)', color: inWatchlist ? '#e8843a' : '#f0ede8', border: '1px solid rgba(255,255,255,0.15)' }}>
                {inWatchlist ? '✓ In Watchlist' : '+ Watchlist'}
              </button>
            </div>
          </div>
        </div>
        {show.tagline && <p className="text-sm italic mt-6" style={{ color: '#e8843a' }}>{show.tagline}</p>}
        <p className="text-sm leading-relaxed mt-6 max-w-2xl" style={{ color: show.overview ? '#c0bdb8' : '#8a8590' }}>
          {show.overview || 'No overview is available for this show.'}
        </p>
        <div className="mt-4">
          {show.createdBy.length > 0 && (
            <p className="text-sm mb-2"><span style={{ color: '#8a8590' }}>Created by: </span><span style={{ color: '#f0ede8' }}>{show.createdBy.map(c => c.name).join(', ')}</span></p>
          )}
          <p className="text-sm mb-2"><span style={{ color: '#8a8590' }}>First Aired: </span><span style={{ color: '#f0ede8' }}>{formatDate(show.releaseDate) ?? 'Unavailable'}</span></p>
          {show.lastAirDate && (
            <p className="text-sm mb-2"><span style={{ color: '#8a8590' }}>Last Aired: </span><span style={{ color: '#f0ede8' }}>{formatDate(show.lastAirDate)}</span></p>
          )}
        </div>

        <WatchProvidersSection providers={show.watchProviders} />

        {/* Seasons */}
        <h3 className="text-lg font-semibold mt-8 mb-4" style={{ color: '#f0ede8' }}>Seasons</h3>
        {show.seasons.length > 0 ? (
          <div className="flex flex-wrap gap-3">
            {show.seasons.map(s => {
              const year = parseDate(s.airDate)?.getFullYear()
              return (
                <div key={s.id} className="px-4 py-3 rounded-xl" style={{ background: '#18181d', border: '1px solid rgba(255,255,255,0.08)' }}>
                  <p className="text-sm font-semibold" style={{ color: '#f0ede8' }}>{s.name}</p>
                  <p className="text-xs mt-0.5" style={{ color: '#8a8590' }}>
                    {s.episodeCount} Episode{s.episodeCount === 1 ? '' : 's'}{year ? ` · ${year}` : ''}
                  </p>
                </div>
              )
            })}
          </div>
        ) : (
          <p className="text-sm" style={{ color: '#8a8590' }}>Season information is not available.</p>
        )}

        {/* Cast */}
        <h3 className="text-lg font-semibold mt-8 mb-4" style={{ color: '#f0ede8' }}>Cast</h3>
        <CastList cast={show.cast} variant="initials" />

        {show.videos.length > 0 && (
          <>
            <h3 className="text-lg font-semibold mt-8 mb-4" style={{ color: '#f0ede8' }}>Videos</h3>
            <VideoList videos={show.videos} onPlay={setPlaying} />
          </>
        )}

        {show.images.backdrops.length > 0 && (
          <>
            <h3 className="text-lg font-semibold mt-8 mb-4" style={{ color: '#f0ede8' }}>Photos</h3>
            <PhotoGrid images={show.images.backdrops.slice(0, 12)} />
          </>
        )}

        {show.similar.length > 0 && (
          <>
            <h3 className="text-lg font-semibold mt-8 mb-4" style={{ color: '#f0ede8' }}>Similar Shows</h3>
            <div className="media-grid">
              {show.similar.map(t => <MovieCard key={t.id} item={t} fluid />)}
            </div>
          </>
        )}
      </div>
      {playing && <TrailerModal video={playing} title={show.title} onClose={() => setPlaying(null)} />}
    </div>
  )
}

export default function TVDetailPage() {
  const id = Number(useParams().id)
  const valid = Number.isInteger(id) && id > 0
  const query = useQuery({
    queryKey: queryKeys.tv(id),
    queryFn: ({ signal }) => getTvDetails(id, undefined, signal),
    enabled: valid,
  })

  if (!valid || (query.error instanceof ApiError && query.error.isNotFound)) {
    return <NotFoundState title="TV show not found" message="We couldn't find this show. It may have been removed from TMDB." />
  }
  if (query.isError) {
    return (
      <div className="pt-24 px-8 md:px-16">
        <ErrorState error={query.error} onRetry={() => query.refetch()} title="Couldn't load this show" />
      </div>
    )
  }
  if (!query.data) {
    return (
      <div>
        <HeroSkeleton height="60vh" minHeight={400} />
        <div className="px-8 md:px-16 py-8"><TextSkeleton lines={4} /></div>
      </div>
    )
  }
  return <TVDetailView key={id} show={query.data} />
}
