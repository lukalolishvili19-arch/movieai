import { useMemo, type ReactNode } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useInfiniteQuery, useQuery } from '@tanstack/react-query'
import { searchAll, searchMovies, searchPeople, searchTv } from '@/api/search'
import { queryKeys } from '@/api/queryKeys'
import type { MediaSummary, PagedResponse, PersonSummary } from '@/types/api'
import { useDebounce } from '@/hooks/useDebounce'
import { useInfiniteSentinel } from '@/hooks/useInfiniteSentinel'
import MovieCard from '@/components/MovieCard'
import ActorCard from '@/components/ActorCard'
import TabBar from '@/components/TabBar'
import ErrorState from '@/components/ErrorState'
import { ActorSkeleton, GridSkeleton } from '@/components/Skeletons'

const TABS = ['All', 'Movies', 'TV Shows', 'Actors'] as const
type Tab = (typeof TABS)[number]
type Kind = 'movies' | 'tv' | 'people'
const KIND: Record<Exclude<Tab, 'All'>, Kind> = { Movies: 'movies', 'TV Shows': 'tv', Actors: 'people' }

function EmptyResults() {
  return (
    <div className="text-center py-16">
      <p className="text-4xl mb-4">🎬</p>
      <p className="text-lg font-semibold mb-1" style={{ color: '#f0ede8' }}>No results found</p>
      <p className="text-sm" style={{ color: '#8a8590' }}>Try searching for a different title or actor</p>
    </div>
  )
}

function Section({ title, count, children }: { title: string; count?: number; children: ReactNode }) {
  return (
    <div className="mb-8">
      <h2 className="text-sm font-semibold uppercase tracking-wider mb-4" style={{ color: '#8a8590' }}>
        {title}
        {count ? <span className="ml-2 normal-case tracking-normal font-medium">({count.toLocaleString('en-US')})</span> : null}
      </h2>
      {children}
    </div>
  )
}

function AllResults({ query }: { query: string }) {
  const result = useQuery({ queryKey: queryKeys.searchAll(query), queryFn: ({ signal }) => searchAll(query, signal) })

  if (result.isPending) {
    return (
      <>
        <Section title="Movies"><div className="media-grid"><GridSkeleton count={6} /></div></Section>
        <Section title="TV Shows"><div className="media-grid"><GridSkeleton count={6} /></div></Section>
      </>
    )
  }
  if (result.isError) return <ErrorState error={result.error} onRetry={() => result.refetch()} title="Search is unavailable" />

  const { movies, tv, people } = result.data
  if (movies.results.length + tv.results.length + people.results.length === 0) return <EmptyResults />

  return (
    <>
      {movies.results.length > 0 && (
        <Section title="Movies" count={movies.totalResults}>
          <div className="media-grid">{movies.results.map(m => <MovieCard key={m.id} item={m} showType fluid />)}</div>
        </Section>
      )}
      {tv.results.length > 0 && (
        <Section title="TV Shows" count={tv.totalResults}>
          <div className="media-grid">{tv.results.map(t => <MovieCard key={t.id} item={t} showType fluid />)}</div>
        </Section>
      )}
      {people.results.length > 0 && (
        <Section title="Actors" count={people.totalResults}>
          <div className="flex flex-wrap gap-6">{people.results.map(a => <ActorCard key={a.id} actor={a} />)}</div>
        </Section>
      )}
    </>
  )
}

function KindResults({ query, kind }: { query: string; kind: Kind }) {
  const list = useInfiniteQuery<PagedResponse<MediaSummary | PersonSummary>>({
    queryKey: queryKeys.search(kind, query),
    queryFn: ({ pageParam, signal }) => {
      const page = pageParam as number
      if (kind === 'movies') return searchMovies(query, page, signal)
      if (kind === 'tv') return searchTv(query, page, signal)
      return searchPeople(query, page, signal)
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

  const items = useMemo(() => {
    const seen = new Set<number>()
    return (list.data?.pages ?? []).flatMap(p => p.results).filter(i => (seen.has(i.id) ? false : (seen.add(i.id), true)))
  }, [list.data])

  const title = kind === 'movies' ? 'Movies' : kind === 'tv' ? 'TV Shows' : 'Actors'
  const total = list.data?.pages[0]?.totalResults

  if (list.isError && items.length === 0) {
    return <ErrorState error={list.error} onRetry={() => list.refetch()} title="Search is unavailable" />
  }
  if (!list.isPending && items.length === 0) return <EmptyResults />

  return (
    <Section title={title} count={total}>
      {kind === 'people' ? (
        <div className="flex flex-wrap gap-6">
          {list.isPending
            ? Array.from({ length: 10 }, (_, i) => <ActorSkeleton key={i} />)
            : (items as PersonSummary[]).map(a => <ActorCard key={a.id} actor={a} />)}
          {list.isFetchingNextPage && Array.from({ length: 5 }, (_, i) => <ActorSkeleton key={`more-${i}`} />)}
        </div>
      ) : (
        <div className="media-grid">
          {list.isPending ? <GridSkeleton /> : (items as MediaSummary[]).map(m => <MovieCard key={m.id} item={m} showType fluid />)}
          {list.isFetchingNextPage && <GridSkeleton count={6} />}
        </div>
      )}
      {list.isError && items.length > 0 && (
        <div className="mt-8">
          <ErrorState variant="inline" error={list.error} title="Couldn't load more results" onRetry={() => list.fetchNextPage()} />
        </div>
      )}
      <div ref={sentinel} aria-hidden="true" style={{ height: 1 }} />
    </Section>
  )
}

export default function SearchPage() {
  const [params, setParams] = useSearchParams()
  const raw = params.get('q') ?? ''
  const activeTab = TABS.find(t => t.toLowerCase().replace(' ', '-') === params.get('tab')) ?? 'All'
  const query = useDebounce(raw.trim(), 350).slice(0, 100)

  const setTab = (tab: Tab) => {
    const next = new URLSearchParams(params)
    if (tab === 'All') next.delete('tab')
    else next.set('tab', tab.toLowerCase().replace(' ', '-'))
    setParams(next, { replace: true })
  }

  return (
    <div className="pt-24 pb-16 px-8 md:px-16">
      <h1 className="text-2xl font-bold mb-1" style={{ color: '#f0ede8' }}>Search Results</h1>
      <p className="text-sm mb-6" style={{ color: '#8a8590' }}>
        {raw.trim() ? <>Results for "{raw.trim()}"</> : 'Search for movies, TV shows and actors'}
      </p>

      <TabBar tabs={TABS} active={activeTab} onChange={setTab} className="mb-8" />

      {!query ? (
        <div className="text-center py-16">
          <p className="text-4xl mb-4">🔍</p>
          <p className="text-lg font-semibold mb-1" style={{ color: '#f0ede8' }}>Start typing to search</p>
          <p className="text-sm" style={{ color: '#8a8590' }}>Use the search field at the top to find any title or actor</p>
        </div>
      ) : activeTab === 'All' ? (
        <AllResults query={query} />
      ) : (
        <KindResults key={`${activeTab}-${query}`} query={query} kind={KIND[activeTab]} />
      )}
    </div>
  )
}
