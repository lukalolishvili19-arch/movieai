import { useMemo, useState } from 'react'
import { useInfiniteQuery } from '@tanstack/react-query'
import { getTrendingPeople } from '@/api/people'
import { queryKeys } from '@/api/queryKeys'
import type { TimeWindow } from '@/types/api'
import { useInfiniteSentinel } from '@/hooks/useInfiniteSentinel'
import ActorCard from '@/components/ActorCard'
import TimeWindowToggle from '@/components/TimeWindowToggle'
import ErrorState from '@/components/ErrorState'
import { ActorSkeleton } from '@/components/Skeletons'

export default function ActorsPage() {
  const [period, setPeriod] = useState<TimeWindow>('day')

  const list = useInfiniteQuery({
    queryKey: [...queryKeys.peopleList('trending', period), 'infinite'],
    queryFn: ({ pageParam, signal }) => getTrendingPeople(period, pageParam, signal),
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

  const people = useMemo(() => {
    const seen = new Set<number>()
    return (list.data?.pages ?? []).flatMap(p => p.results).filter(a => (seen.has(a.id) ? false : (seen.add(a.id), true)))
  }, [list.data])

  return (
    <div className="pt-24 pb-16 px-8 md:px-16">
      <div className="flex items-center justify-between mb-8 gap-4 flex-wrap">
        <h1 className="text-3xl font-bold" style={{ fontFamily: "'DM Serif Display', serif", color: '#f0ede8' }}>Trending Actors</h1>
        <TimeWindowToggle value={period} onChange={setPeriod} size="md" />
      </div>
      {list.isError && people.length === 0 ? (
        <ErrorState error={list.error} onRetry={() => list.refetch()} title="Couldn't load actors" />
      ) : (
        <>
          <div className="grid gap-8 justify-items-center" style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(140px, 1fr))' }}>
            {list.isPending
              ? Array.from({ length: 18 }, (_, i) => <ActorSkeleton key={i} />)
              : people.map(a => <ActorCard key={a.id} actor={a} trending />)}
            {list.isFetchingNextPage && Array.from({ length: 6 }, (_, i) => <ActorSkeleton key={`more-${i}`} />)}
          </div>
          {list.isError && people.length > 0 && (
            <div className="mt-8">
              <ErrorState variant="inline" error={list.error} title="Couldn't load more actors" onRetry={() => list.fetchNextPage()} />
            </div>
          )}
          <div ref={sentinel} aria-hidden="true" style={{ height: 1 }} />
        </>
      )}
    </div>
  )
}
