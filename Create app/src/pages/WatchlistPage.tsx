import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useInfiniteQuery } from '@tanstack/react-query'
import { getWatchlist } from '@/api/watchlist'
import { useAuth } from '@/auth/context'
import { useWatchlistToggle, watchlistKeys } from '@/hooks/useWatchlist'
import { useInfiniteSentinel } from '@/hooks/useInfiniteSentinel'
import { detailPath } from '@/lib/format'
import type { MediaType, WatchlistItem } from '@/types/api'
import RatingBadge from '@/components/RatingBadge'
import TabBar from '@/components/TabBar'
import PosterImage from '@/components/PosterImage'
import ErrorState from '@/components/ErrorState'
import { RowSkeleton } from '@/components/Skeletons'

const TABS = ['All', 'Movies', 'TV Shows'] as const
type Tab = (typeof TABS)[number]
const FILTER: Record<Tab, MediaType | 'all'> = { All: 'all', Movies: 'movie', 'TV Shows': 'tv' }

function WatchlistRow({ item, onRemove }: { item: WatchlistItem; onRemove: () => void }) {
  const navigate = useNavigate()
  const media = item.media
  const title = media?.title ?? (item.mediaType === 'movie' ? 'Movie' : 'TV show')
  return (
    <div className="flex items-center gap-4 p-4 rounded-xl transition-smooth" style={{ background: '#18181d', border: '1px solid rgba(255,255,255,0.06)' }}>
      <div className="poster-wrap rounded-lg overflow-hidden flex-shrink-0" style={{ width: 56, height: 80, background: '#111114' }}>
        <PosterImage src={media?.posterUrl} alt={title} />
      </div>
      <div className="flex-1 min-w-0">
        <p className="font-semibold truncate" style={{ color: '#f0ede8' }}>{title}</p>
        <div className="flex items-center gap-3 mt-0.5 flex-wrap">
          {media ? (
            <>
              {media.year !== null && <span className="text-xs" style={{ color: '#8a8590' }}>{media.year}</span>}
              <RatingBadge rating={media.rating} />
            </>
          ) : (
            <span className="text-xs" style={{ color: '#8a8590' }}>Details temporarily unavailable</span>
          )}
          <span className="text-xs font-medium px-1.5 py-0.5 rounded" style={{ background: 'rgba(255,255,255,0.06)', color: '#8a8590' }}>{item.mediaType === 'movie' ? 'Movie' : 'TV'}</span>
        </div>
      </div>
      <div className="flex items-center gap-2">
        <button
          onClick={() => navigate(detailPath(item.mediaType, item.tmdbId))}
          className="px-3 py-1.5 rounded-lg text-xs font-medium transition-smooth"
          style={{ background: 'rgba(232,132,58,0.1)', color: '#e8843a', border: '1px solid rgba(232,132,58,0.2)' }}
        >
          Details
        </button>
        <button
          onClick={onRemove}
          aria-label={`Remove ${title} from watchlist`}
          title="Remove from Watchlist"
          className="w-8 h-8 rounded-lg flex items-center justify-center text-xs transition-smooth"
          style={{ background: 'rgba(255,255,255,0.05)', color: '#8a8590' }}
        >
          ✕
        </button>
      </div>
    </div>
  )
}

function EmptyState({ emoji, title, text, action }: { emoji: string; title: string; text: string; action?: { label: string; onClick: () => void } }) {
  return (
    <div className="text-center py-16">
      <p className="text-4xl mb-4">{emoji}</p>
      <p className="text-lg font-semibold mb-1" style={{ color: '#f0ede8' }}>{title}</p>
      <p className="text-sm" style={{ color: '#8a8590' }}>{text}</p>
      {action && (
        <button
          onClick={action.onClick}
          className="mt-6 px-5 py-2.5 rounded-lg font-semibold text-sm transition-smooth"
          style={{ background: '#e8843a', color: '#fff' }}
        >
          {action.label}
        </button>
      )}
    </div>
  )
}

function WatchlistContent({ userId }: { userId: string }) {
  const navigate = useNavigate()
  const [activeTab, setActiveTab] = useState<Tab>('All')
  const [removed, setRemoved] = useState<Set<string>>(new Set())
  const { setMembership } = useWatchlistToggle()
  const filter = FILTER[activeTab]

  const list = useInfiniteQuery({
    queryKey: watchlistKeys.list(userId, filter),
    queryFn: ({ pageParam, signal }) =>
      getWatchlist({ mediaType: filter === 'all' ? undefined : filter, page: pageParam, size: 20 }, signal),
    initialPageParam: 1,
    getNextPageParam: last => (last.page < last.totalPages ? last.page + 1 : undefined),
    staleTime: 0,
  })

  const sentinel = useInfiniteSentinel<HTMLDivElement>(
    () => {
      if (list.hasNextPage && !list.isFetchingNextPage && !list.isError) void list.fetchNextPage()
    },
    list.hasNextPage === true && !list.isFetchingNextPage,
    list.data?.pages.length,
  )

  const items = useMemo(
    () => (list.data?.pages ?? []).flatMap(p => p.results).filter(i => !removed.has(`${i.mediaType}:${i.tmdbId}`)),
    [list.data, removed],
  )

  const remove = (item: WatchlistItem) => {
    const key = `${item.mediaType}:${item.tmdbId}`
    // Hide immediately and restore the row if the request fails (the hook shows the error message).
    setRemoved(prev => new Set(prev).add(key))
    setMembership(item.mediaType, item.tmdbId, false).catch(() =>
      setRemoved(prev => {
        const next = new Set(prev)
        next.delete(key)
        return next
      }),
    )
  }

  return (
    <>
      <TabBar tabs={TABS} active={activeTab} onChange={setActiveTab} className="mb-8" />
      {list.isPending ? (
        <div className="flex flex-col gap-3">{Array.from({ length: 4 }, (_, i) => <RowSkeleton key={i} />)}</div>
      ) : list.isError && items.length === 0 ? (
        <ErrorState error={list.error} onRetry={() => list.refetch()} title="Couldn't load your watchlist" />
      ) : items.length === 0 ? (
        <EmptyState
          emoji="🔖"
          title="Your watchlist is empty"
          text="Add movies and shows to track what you want to watch"
          action={{ label: 'Discover Movies', onClick: () => navigate('/movies') }}
        />
      ) : (
        <div className="flex flex-col gap-3">
          {items.map(item => <WatchlistRow key={item.id} item={item} onRemove={() => remove(item)} />)}
          {list.isFetchingNextPage && <RowSkeleton />}
        </div>
      )}
      <div ref={sentinel} aria-hidden="true" style={{ height: 1 }} />
    </>
  )
}

export default function WatchlistPage() {
  const { status, user, openAuthModal } = useAuth()

  return (
    <div className="pt-24 pb-16 px-8 md:px-16">
      <h1 className="text-3xl font-bold mb-6" style={{ fontFamily: "'DM Serif Display', serif", color: '#f0ede8' }}>My Watchlist</h1>
      {status === 'loading' ? (
        <div className="flex flex-col gap-3">{Array.from({ length: 3 }, (_, i) => <RowSkeleton key={i} />)}</div>
      ) : status === 'authenticated' && user ? (
        <WatchlistContent key={user.id} userId={user.id} />
      ) : (
        <EmptyState
          emoji="🔐"
          title="Sign in to see your watchlist"
          text="Your watchlist is saved to your account so it follows you across devices."
          action={{ label: 'Sign In', onClick: () => openAuthModal({ reason: 'Sign in to see your watchlist.' }) }}
        />
      )}
    </div>
  )
}
