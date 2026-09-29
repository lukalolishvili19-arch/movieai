import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import type { MediaType } from '@/types/api'
import { useWatchlistEntry } from '@/hooks/useWatchlist'
import { detailPath } from '@/lib/format'
import RatingBadge from './RatingBadge'
import PosterImage from './PosterImage'

/** The fields a poster card needs; satisfied by movie/TV summaries and person credits. */
export interface CardMedia {
  id: number
  mediaType: MediaType
  title: string
  year: number | null
  rating: number | null
  posterUrl: string | null
  genres: string[]
}

interface Props {
  item: CardMedia
  showType?: boolean
  /** Fills the grid cell (2:3 poster) instead of the fixed 160px carousel width. */
  fluid?: boolean
  /** Replaces the genre line, e.g. with the character an actor played. */
  subtitle?: string | null
  seasons?: number | null
}

export default function MovieCard({ item, showType, fluid, subtitle, seasons }: Props) {
  const navigate = useNavigate()
  const [hovering, setHovering] = useState(false)
  const { inWatchlist, toggle } = useWatchlistEntry(item.mediaType, item.id)
  const href = detailPath(item.mediaType, item.id)
  const secondary = subtitle !== undefined ? subtitle : item.genres[0]

  return (
    <div
      className="relative flex-shrink-0 cursor-pointer group"
      style={{ width: fluid ? '100%' : 160 }}
      role="link"
      tabIndex={0}
      aria-label={item.title}
      onClick={() => navigate(href)}
      onKeyDown={e => {
        if (e.key === 'Enter') navigate(href)
      }}
      onMouseEnter={() => setHovering(true)}
      onMouseLeave={() => setHovering(false)}
      onFocus={() => setHovering(true)}
      onBlur={e => {
        if (!e.currentTarget.contains(e.relatedTarget as Node | null)) setHovering(false)
      }}
    >
      <div
        className="poster-wrap rounded-xl overflow-hidden"
        style={fluid ? { aspectRatio: '2 / 3', background: '#18181d' } : { height: 240, background: '#18181d' }}
      >
        <PosterImage src={item.posterUrl} alt={item.title} />
        {/* Overlay on hover */}
        <div
          className="absolute inset-0 rounded-xl transition-smooth flex items-end p-3"
          style={{ background: hovering ? 'linear-gradient(to top, rgba(0,0,0,0.85) 0%, transparent 60%)' : 'transparent' }}
        />
        {hovering && (
          <button
            className="absolute top-2 right-2 w-7 h-7 rounded-full flex items-center justify-center transition-smooth"
            style={{ background: inWatchlist ? '#e8843a' : 'rgba(0,0,0,0.7)' }}
            onClick={e => { e.stopPropagation(); toggle() }}
            onKeyDown={e => e.stopPropagation()}
            title={inWatchlist ? 'Remove from Watchlist' : 'Add to Watchlist'}
            aria-label={inWatchlist ? `Remove ${item.title} from Watchlist` : `Add ${item.title} to Watchlist`}
          >
            <span style={{ fontSize: 12, color: '#fff' }}>{inWatchlist ? '✓' : '+'}</span>
          </button>
        )}
      </div>
      <div className="mt-2 px-0.5">
        {showType && (
          <span className="text-xs font-medium uppercase tracking-wider" style={{ color: '#e8843a' }}>
            {item.mediaType === 'movie' ? 'Movie' : 'TV'}
          </span>
        )}
        <p className="text-sm font-semibold leading-snug mt-0.5 line-clamp-2" style={{ color: '#f0ede8' }}>{item.title}</p>
        <div className="flex items-center gap-2 mt-1">
          {item.year !== null && <span className="text-xs" style={{ color: '#8a8590' }}>{item.year}</span>}
          {seasons ? <span className="text-xs" style={{ color: '#8a8590' }}>{seasons}S</span> : null}
          <RatingBadge rating={item.rating} />
        </div>
        {secondary && <span className="text-xs line-clamp-1" style={{ color: '#8a8590' }}>{secondary}</span>}
      </div>
    </div>
  )
}
