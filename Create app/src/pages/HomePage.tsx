import { useState, type ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery, type UseQueryResult } from '@tanstack/react-query'
import { getNowPlayingMovies, getTopRatedMovies, getTrendingMovies, getUpcomingMovies } from '@/api/movies'
import { getAiringTodayTv, getTopRatedTv, getTrendingTv } from '@/api/tv'
import { getTrendingPeople } from '@/api/people'
import { queryKeys } from '@/api/queryKeys'
import { formatShortDate } from '@/lib/format'
import type { MediaSummary, PagedResponse, PersonSummary, TimeWindow } from '@/types/api'
import CarouselSection from '@/components/CarouselSection'
import MovieCard from '@/components/MovieCard'
import ActorCard from '@/components/ActorCard'
import Hero from '@/components/Hero'
import WhatShouldIWatch from '@/components/WhatShouldIWatch'
import TimeWindowToggle from '@/components/TimeWindowToggle'
import ErrorState from '@/components/ErrorState'
import { ActorRowSkeleton, CardRowSkeleton, HeroSkeleton } from '@/components/Skeletons'

function RowContent<T>({
  query,
  skeleton,
  render,
  empty = 'Nothing to show right now.',
}: {
  query: UseQueryResult<PagedResponse<T>>
  skeleton: ReactNode
  render: (items: T[]) => ReactNode
  empty?: string
}) {
  if (query.isPending) return <>{skeleton}</>
  if (query.isError) return <ErrorState variant="inline" error={query.error} onRetry={() => query.refetch()} />
  if (query.data.results.length === 0) {
    return <p className="text-sm py-8" style={{ color: '#8a8590' }}>{empty}</p>
  }
  return <>{render(query.data.results)}</>
}

function RankBadge({ rank }: { rank: number }) {
  return (
    <div
      className="absolute top-2 left-2 w-7 h-7 rounded-full flex items-center justify-center text-xs font-black pointer-events-none"
      style={{ background: 'rgba(0,0,0,0.75)', color: '#e8843a', border: '1.5px solid #e8843a' }}
    >
      {rank}
    </div>
  )
}

function RankedCards({ items }: { items: MediaSummary[] }) {
  return (
    <>
      {items.map((m, i) => (
        <div key={m.id} className="relative flex-shrink-0" style={{ width: 160 }}>
          <MovieCard item={m} />
          <RankBadge rank={i + 1} />
        </div>
      ))}
    </>
  )
}

export default function HomePage() {
  const navigate = useNavigate()
  const [movieWindow, setMovieWindow] = useState<TimeWindow>('day')
  const [tvWindow, setTvWindow] = useState<TimeWindow>('day')

  const trendingMovies = useQuery({
    queryKey: queryKeys.movieList('trending', movieWindow),
    queryFn: ({ signal }) => getTrendingMovies(movieWindow, 1, signal),
    placeholderData: prev => prev,
  })
  // The hero always features today's top trending title, independent of the toggle below.
  const heroSource = useQuery({
    queryKey: queryKeys.movieList('trending', 'day'),
    queryFn: ({ signal }) => getTrendingMovies('day', 1, signal),
  })
  const trendingTv = useQuery({
    queryKey: queryKeys.tvList('trending', tvWindow),
    queryFn: ({ signal }) => getTrendingTv(tvWindow, 1, signal),
    placeholderData: prev => prev,
  })
  const trendingPeople = useQuery({
    queryKey: queryKeys.peopleList('trending', 'day'),
    queryFn: ({ signal }) => getTrendingPeople('day', 1, signal),
  })
  const nowPlaying = useQuery({ queryKey: queryKeys.movieList('now-playing'), queryFn: ({ signal }) => getNowPlayingMovies(1, signal) })
  const airingToday = useQuery({ queryKey: queryKeys.tvList('airing-today'), queryFn: ({ signal }) => getAiringTodayTv(1, signal) })
  const upcoming = useQuery({ queryKey: queryKeys.movieList('upcoming'), queryFn: ({ signal }) => getUpcomingMovies(1, signal) })
  const topMovies = useQuery({ queryKey: queryKeys.movieList('top-rated'), queryFn: ({ signal }) => getTopRatedMovies(1, signal) })
  const topTv = useQuery({ queryKey: queryKeys.tvList('top-rated'), queryFn: ({ signal }) => getTopRatedTv(1, signal) })

  const heroMovie = heroSource.data?.results.find(m => m.backdropUrl) ?? heroSource.data?.results[0]

  const cards = (items: MediaSummary[]) => items.map(m => <MovieCard key={m.id} item={m} />)
  const actors = (items: PersonSummary[]) => items.map(a => <ActorCard key={a.id} actor={a} trending />)

  return (
    <div>
      {heroSource.isPending ? (
        <HeroSkeleton />
      ) : heroMovie ? (
        <Hero movie={heroMovie} onDetails={() => navigate(`/movie/${heroMovie.id}`)} />
      ) : (
        <div className="pt-24 px-8 md:px-16" style={{ minHeight: 320 }}>
          <ErrorState error={heroSource.error} onRetry={() => heroSource.refetch()} title="Movie data is temporarily unavailable" />
        </div>
      )}

      {/* Trending Movies */}
      <CarouselSection
        title="Trending Movies"
        subtitle={<TimeWindowToggle value={movieWindow} onChange={setMovieWindow} />}
        seeAll={() => navigate('/movies')}
      >
        <RowContent query={trendingMovies} skeleton={<CardRowSkeleton />} render={cards} />
      </CarouselSection>

      {/* Trending TV Shows */}
      <CarouselSection
        title="Trending TV Shows"
        subtitle={<TimeWindowToggle value={tvWindow} onChange={setTvWindow} />}
        seeAll={() => navigate('/tv')}
      >
        <RowContent query={trendingTv} skeleton={<CardRowSkeleton />} render={cards} />
      </CarouselSection>

      {/* Trending Actors */}
      <CarouselSection title="Trending Actors" seeAll={() => navigate('/actors')}>
        <RowContent query={trendingPeople} skeleton={<ActorRowSkeleton />} render={actors} />
      </CarouselSection>

      {/* Now Playing */}
      <CarouselSection title="Now Playing">
        <RowContent query={nowPlaying} skeleton={<CardRowSkeleton />} render={cards} />
      </CarouselSection>

      {/* Airing Today */}
      <CarouselSection title="Airing Today">
        <RowContent query={airingToday} skeleton={<CardRowSkeleton />} render={cards} empty="Nothing is airing today." />
      </CarouselSection>

      {/* Coming Soon */}
      <CarouselSection title="Coming Soon" seeAll={() => navigate('/movies?category=upcoming')}>
        <RowContent
          query={upcoming}
          skeleton={<CardRowSkeleton />}
          render={items =>
            items.map(m => (
              <div key={m.id} className="relative flex-shrink-0" style={{ width: 160 }}>
                <MovieCard item={m} />
                <div
                  className="absolute top-2 left-2 px-2 py-0.5 rounded text-xs font-bold pointer-events-none"
                  style={{ background: 'linear-gradient(135deg, #e8843a, #c0392b)', color: '#fff' }}
                >
                  {formatShortDate(m.releaseDate) ?? 'TBA'}
                </div>
              </div>
            ))
          }
        />
      </CarouselSection>

      {/* Top Rated Movies */}
      <CarouselSection title="Top Rated Movies">
        <RowContent query={topMovies} skeleton={<CardRowSkeleton />} render={items => <RankedCards items={items} />} />
      </CarouselSection>

      {/* Top Rated TV Shows */}
      <CarouselSection title="Top Rated TV Shows">
        <RowContent query={topTv} skeleton={<CardRowSkeleton />} render={items => <RankedCards items={items} />} />
      </CarouselSection>

      {/* What Should I Watch */}
      <WhatShouldIWatch />

      <div style={{ height: 80 }} />
    </div>
  )
}
