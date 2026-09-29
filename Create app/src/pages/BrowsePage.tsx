import { useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useInfiniteQuery, useQuery } from '@tanstack/react-query'
import { browseMovies, getMovieGenres } from '@/api/movies'
import { browseTv, getTvGenres } from '@/api/tv'
import { queryKeys } from '@/api/queryKeys'
import type { MediaSummary, MovieCategory, PagedResponse, SortOption, TimeWindow, TvCategory } from '@/types/api'
import { useInfiniteSentinel } from '@/hooks/useInfiniteSentinel'
import GenreChip from '@/components/GenreChip'
import MovieCard from '@/components/MovieCard'
import TabBar from '@/components/TabBar'
import TimeWindowToggle from '@/components/TimeWindowToggle'
import ErrorState from '@/components/ErrorState'
import { GridSkeleton } from '@/components/Skeletons'

const MOVIE_TABS = ['Trending', 'Popular', 'Top Rated', 'Now Playing', 'Upcoming'] as const
const TV_TABS = ['Trending', 'Popular', 'Top Rated', 'Airing Today', 'On The Air', 'Upcoming'] as const

const toCategory = (tab: string) => tab.toLowerCase().replace(/ /g, '-')

function yearOptions(currentYear: number) {
  return [
    { label: 'Any Year', value: '' },
    { label: String(currentYear), value: `${currentYear}-${currentYear}` },
    { label: `${currentYear - 6}–${currentYear}`, value: `${currentYear - 6}-${currentYear}` },
    { label: '2010s', value: '2010-2019' },
    { label: '2000s', value: '2000-2009' },
    { label: '1990s', value: '1990-1999' },
    { label: 'Before 1990', value: '1900-1989' },
  ]
}

const RATING_OPTIONS = [
  { label: 'Any', value: '' },
  { label: '6+', value: '6' },
  { label: '7+', value: '7' },
  { label: '8+', value: '8' },
  { label: '9+', value: '9' },
]

const MOVIE_RUNTIMES = [
  { label: 'Any', value: '' },
  { label: 'Under 90 min', value: 'under-90' },
  { label: '90–120 min', value: '90-120' },
  { label: '120+ min', value: 'over-120' },
]

const TV_RUNTIMES = [
  { label: 'Any', value: '' },
  { label: 'Under 30 min', value: 'under-30' },
  { label: '30–60 min', value: '30-60' },
  { label: '60+ min', value: 'over-60' },
]

const SORT_OPTIONS: { label: string; value: SortOption | '' }[] = [
  { label: 'Default', value: '' },
  { label: 'Most Popular', value: 'popularity' },
  { label: 'Highest Rated', value: 'rating' },
  { label: 'Newest', value: 'release_date' },
  { label: 'Title (A–Z)', value: 'title' },
]

const selectStyle = { background: 'rgba(255,255,255,0.06)', color: '#f0ede8', border: '1px solid rgba(255,255,255,0.1)' }

export default function BrowsePage({ type }: { type: 'movie' | 'tv' }) {
  const tabs: readonly string[] = type === 'movie' ? MOVIE_TABS : TV_TABS
  const [searchParams, setSearchParams] = useSearchParams()
  const activeTab = tabs.find(t => toCategory(t) === searchParams.get('category')) ?? 'Popular'
  const category = toCategory(activeTab)

  const [period, setPeriod] = useState<TimeWindow>('day')
  const [selectedGenres, setSelectedGenres] = useState<number[]>([])
  const [minRating, setMinRating] = useState('')
  const [years, setYears] = useState('')
  const [runtime, setRuntime] = useState('')
  const [sort, setSort] = useState<SortOption | ''>('')
  const yearChoices = useMemo(() => yearOptions(new Date().getFullYear()), [])

  const genres = useQuery({
    queryKey: type === 'movie' ? queryKeys.movieGenres : queryKeys.tvGenres,
    queryFn: ({ signal }) => (type === 'movie' ? getMovieGenres(signal) : getTvGenres(signal)),
    staleTime: 24 * 60 * 60_000,
  })

  const [yearFrom, yearTo] = years ? years.split('-').map(Number) : [undefined, undefined]
  const filters = {
    period: category === 'trending' ? period : undefined,
    genres: selectedGenres.length > 0 ? [...selectedGenres].sort((a, b) => a - b) : undefined,
    minRating: minRating ? Number(minRating) : undefined,
    yearFrom,
    yearTo,
    runtime: runtime || undefined,
    sort: sort || undefined,
  }

  const list = useInfiniteQuery<PagedResponse<MediaSummary>>({
    queryKey:
      type === 'movie'
        ? queryKeys.movieBrowse({ category: category as MovieCategory, ...filters })
        : queryKeys.tvBrowse({ category: category as TvCategory, ...filters }),
    queryFn: ({ pageParam, signal }) => {
      const page = pageParam as number
      return type === 'movie'
        ? browseMovies({ category: category as MovieCategory, ...filters, page }, signal)
        : browseTv({ category: category as TvCategory, ...filters, page }, signal)
    },
    initialPageParam: 1,
    getNextPageParam: last => (last.page < last.totalPages ? last.page + 1 : undefined),
  })

  const sentinel = useInfiniteSentinel<HTMLDivElement>(
    () => {
      if (list.hasNextPage && !list.isFetchingNextPage && !list.isError) void list.fetchNextPage()
    },
    list.hasNextPage === true && !list.isFetchingNextPage,
    list.data?.pages.length,
  )

  // TMDB pages can overlap while popularity shifts; keep the first occurrence of each title.
  const items = useMemo(() => {
    const seen = new Set<number>()
    return (list.data?.pages ?? []).flatMap(p => p.results).filter(i => (seen.has(i.id) ? false : (seen.add(i.id), true)))
  }, [list.data])

  const hasFilters = !!(filters.genres || filters.minRating || years || runtime || sort)
  const clearFilters = () => {
    setSelectedGenres([])
    setMinRating('')
    setYears('')
    setRuntime('')
    setSort('')
  }

  const selects = [
    { label: 'Min Rating', value: minRating, options: RATING_OPTIONS, set: setMinRating },
    { label: type === 'movie' ? 'Release Year' : 'First Aired', value: years, options: yearChoices, set: setYears },
    { label: 'Runtime', value: runtime, options: type === 'movie' ? MOVIE_RUNTIMES : TV_RUNTIMES, set: setRuntime },
    { label: 'Sort By', value: sort, options: SORT_OPTIONS, set: (v: string) => setSort(v as SortOption | '') },
  ]

  return (
    <div className="pt-24 pb-16 px-8 md:px-16">
      <div className="flex items-center justify-between gap-4 mb-2 flex-wrap">
        <h1 className="text-3xl font-bold" style={{ fontFamily: "'DM Serif Display', serif", color: '#f0ede8' }}>
          {type === 'movie' ? 'Movies' : 'TV Shows'}
        </h1>
        {category === 'trending' && <TimeWindowToggle value={period} onChange={setPeriod} size="md" />}
      </div>

      {/* Tabs */}
      <TabBar
        tabs={tabs}
        active={activeTab}
        onChange={t => setSearchParams(toCategory(t) === 'popular' ? {} : { category: toCategory(t) }, { replace: true })}
      />

      {/* Genre filter */}
      <div className="flex flex-wrap gap-2 mb-6">
        {genres.isPending &&
          Array.from({ length: 12 }, (_, i) => (
            <span key={i} className="h-6 rounded-full animate-pulse" style={{ width: 70 + (i % 3) * 16, background: 'rgba(255,255,255,0.06)' }} />
          ))}
        {genres.data?.map(g => (
          <GenreChip
            key={g.id}
            label={g.name}
            selected={selectedGenres.includes(g.id)}
            onClick={() => setSelectedGenres(v => (v.includes(g.id) ? v.filter(x => x !== g.id) : [...v, g.id]))}
          />
        ))}
      </div>

      {/* Filters */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8 max-w-3xl">
        {selects.map(f => (
          <div key={f.label}>
            <label className="text-xs font-medium block mb-1.5" style={{ color: '#8a8590' }}>{f.label}</label>
            <select
              value={f.value}
              onChange={e => f.set(e.target.value)}
              className="w-full text-sm rounded-lg px-3 py-2 outline-none"
              style={selectStyle}
            >
              {f.options.map(o => <option key={o.label} value={o.value}>{o.label}</option>)}
            </select>
          </div>
        ))}
      </div>

      {/* Grid */}
      {list.isError && items.length === 0 ? (
        <ErrorState error={list.error} onRetry={() => list.refetch()} title="Couldn't load titles" />
      ) : (
        <>
          <div className="media-grid">
            {list.isPending ? <GridSkeleton /> : items.map(item => <MovieCard key={item.id} item={item} fluid />)}
            {list.isFetchingNextPage && <GridSkeleton count={6} />}
          </div>

          {!list.isPending && items.length === 0 && (
            <div className="text-center py-16">
              <p className="text-4xl mb-4">🎬</p>
              <p className="text-lg font-semibold mb-1" style={{ color: '#f0ede8' }}>No titles match these filters</p>
              <p className="text-sm" style={{ color: '#8a8590' }}>Try removing a genre or widening the year range.</p>
              {hasFilters && (
                <button onClick={clearFilters} className="mt-4 text-sm font-medium" style={{ color: '#e8843a' }}>Clear filters</button>
              )}
            </div>
          )}

          {list.isError && items.length > 0 && (
            <div className="mt-8">
              <ErrorState variant="inline" error={list.error} title="Couldn't load more titles" onRetry={() => list.fetchNextPage()} />
            </div>
          )}
          <div ref={sentinel} aria-hidden="true" style={{ height: 1 }} />
        </>
      )}
    </div>
  )
}
