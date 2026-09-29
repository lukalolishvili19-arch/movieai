import { useMemo, useState } from 'react'
import { useMutation, useQuery } from '@tanstack/react-query'
import { getMovieGenres } from '@/api/movies'
import { getMovieRecommendations } from '@/api/recommendations'
import { queryKeys } from '@/api/queryKeys'
import type { RecommendationParams, RecommendationResponse } from '@/types/api'
import GenreChip from './GenreChip'
import MovieCard from './MovieCard'
import ErrorState from './ErrorState'
import { CardSkeleton } from './Skeletons'

const PICKS = 10

const RATING_OPTIONS = [
  { label: '6+', value: 6 },
  { label: '7+', value: 7 },
  { label: '8+', value: 8 },
  { label: '9+', value: 9 },
]

const RUNTIME_OPTIONS = [
  { label: 'Any', value: 'any' },
  { label: 'Under 90 min', value: 'under-90' },
  { label: '90–120 min', value: '90-120' },
  { label: '120+ min', value: 'over-120' },
]

function releasePeriodOptions(currentYear: number) {
  return [
    { label: 'Any Year', value: 'any' },
    { label: String(currentYear), value: String(currentYear) },
    { label: `${currentYear - 6}–${currentYear}`, value: `${currentYear - 6}-${currentYear}` },
    { label: `${currentYear - 16}–${currentYear}`, value: `${currentYear - 16}-${currentYear}` },
  ]
}

interface Batch {
  filters: Omit<RecommendationParams, 'page' | 'seed'>
  response: RecommendationResponse
}

export default function WhatShouldIWatch() {
  const periods = useMemo(() => releasePeriodOptions(new Date().getFullYear()), [])
  const genresQuery = useQuery({ queryKey: queryKeys.movieGenres, queryFn: ({ signal }) => getMovieGenres(signal), staleTime: 24 * 60 * 60_000 })

  const [selectedGenres, setSelectedGenres] = useState<number[]>([])
  const [minRating, setMinRating] = useState('6')
  const [period, setPeriod] = useState('any')
  const [runtime, setRuntime] = useState('any')
  const [batch, setBatch] = useState<Batch | null>(null)

  const toggleGenre = (id: number) =>
    setSelectedGenres(v => (v.includes(id) ? v.filter(x => x !== id) : [...v, id]))

  const picks = useMutation({
    mutationFn: ({ filters, page, seed }: { filters: Batch['filters']; page: number; seed?: number }) =>
      getMovieRecommendations({ ...filters, page, seed, limit: PICKS }).then(response => ({ filters, response })),
    onSuccess: setBatch,
  })

  const currentFilters = (): Batch['filters'] => ({
    genres: selectedGenres,
    minRating: Number(minRating),
    releasePeriod: period,
    runtime,
  })

  // A fresh request without a seed makes the server pick a new random order.
  const showPicks = () => picks.mutate({ filters: currentFilters(), page: 1 })

  const showMore = () => {
    if (!batch) return showPicks()
    const { filters, response } = batch
    if (response.hasMore) picks.mutate({ filters, page: response.page + 1, seed: response.seed })
    else picks.mutate({ filters, page: 1 })
  }

  const shown = batch !== null || picks.isPending || picks.isError
  const results = batch?.response.results ?? []

  const selects = [
    { label: 'Min Rating', value: minRating, options: RATING_OPTIONS.map(o => ({ label: o.label, value: String(o.value) })), set: setMinRating },
    { label: 'Release Period', value: period, options: periods, set: setPeriod },
    { label: 'Runtime', value: runtime, options: RUNTIME_OPTIONS, set: setRuntime },
  ]

  return (
    <section
      className="mx-4 sm:mx-8 md:mx-16 my-8 rounded-2xl p-6 sm:p-8 md:p-12"
      style={{ background: 'linear-gradient(135deg, rgba(232,132,58,0.08) 0%, rgba(192,57,43,0.06) 100%)', border: '1px solid rgba(232,132,58,0.15)' }}
    >
      <div className="max-w-3xl mx-auto">
        <h2 className="text-2xl font-bold mb-1" style={{ fontFamily: "'DM Serif Display', serif", color: '#f0ede8' }}>
          What Should I Watch?
        </h2>
        <p className="text-sm mb-8" style={{ color: '#8a8590' }}>Can't decide? Pick what you're in the mood for.</p>

        {/* Genres */}
        <div className="flex flex-wrap gap-2 mb-8">
          {genresQuery.isPending &&
            Array.from({ length: 10 }, (_, i) => (
              <span key={i} className="h-6 rounded-full animate-pulse" style={{ width: 70 + (i % 3) * 16, background: 'rgba(255,255,255,0.06)' }} />
            ))}
          {genresQuery.isError && (
            <p className="text-xs" style={{ color: '#8a8590' }}>
              Genres are unavailable right now.{' '}
              <button onClick={() => genresQuery.refetch()} style={{ color: '#e8843a' }}>Retry</button>
            </p>
          )}
          {genresQuery.data?.map(g => (
            <GenreChip key={g.id} label={g.name} selected={selectedGenres.includes(g.id)} onClick={() => toggleGenre(g.id)} />
          ))}
        </div>

        {/* Filters */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
          {selects.map(f => (
            <div key={f.label}>
              <label className="text-xs font-medium block mb-1.5" style={{ color: '#8a8590' }}>{f.label}</label>
              <select
                value={f.value}
                onChange={e => f.set(e.target.value)}
                className="w-full text-sm rounded-lg px-3 py-2 outline-none"
                style={{ background: 'rgba(255,255,255,0.06)', color: '#f0ede8', border: '1px solid rgba(255,255,255,0.1)' }}
              >
                {f.options.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
              </select>
            </div>
          ))}
        </div>

        <button
          onClick={showPicks}
          disabled={picks.isPending}
          className="px-6 py-3 rounded-xl font-semibold text-sm transition-smooth"
          style={{ background: 'linear-gradient(135deg, #e8843a, #c0392b)', color: '#fff', opacity: picks.isPending ? 0.7 : 1 }}
        >
          🎲 Show Me 10 Movies
        </button>

        {/* Results */}
        {shown && (
          <div className="mt-10">
            <div className="flex items-center justify-between mb-4 gap-4">
              <h3 className="font-semibold" style={{ color: '#f0ede8' }}>Your 10 Picks</h3>
              {batch && results.length > 0 && (
                <button
                  onClick={showMore}
                  disabled={picks.isPending}
                  className="text-sm font-medium transition-smooth"
                  style={{ color: '#e8843a', opacity: picks.isPending ? 0.6 : 1 }}
                >
                  Give Me 10 More →
                </button>
              )}
            </div>
            {picks.isError ? (
              <ErrorState variant="inline" error={picks.error} title="Couldn't find picks" onRetry={showMore} />
            ) : picks.isPending ? (
              <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
                {Array.from({ length: PICKS }, (_, i) => <CardSkeleton key={i} fluid />)}
              </div>
            ) : (
              <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
                {results.length > 0 ? results.map(m => (
                  <MovieCard key={m.id} item={m} fluid />
                )) : (
                  <p className="col-span-2 md:col-span-5 text-center text-sm py-8" style={{ color: '#8a8590' }}>No matches found. Try relaxing your filters.</p>
                )}
              </div>
            )}
          </div>
        )}
      </div>
    </section>
  )
}
