import type { BrowseParams, MovieCategory, TimeWindow, TvCategory } from '@/types/api'

// Public catalog keys. Watchlist keys live in hooks/useWatchlist.ts and are always scoped by user id.
export const queryKeys = {
  movieGenres: ['movies', 'genres'] as const,
  tvGenres: ['tv', 'genres'] as const,
  movieList: (list: string, period?: TimeWindow) => ['movies', 'list', list, period ?? null] as const,
  tvList: (list: string, period?: TimeWindow) => ['tv', 'list', list, period ?? null] as const,
  peopleList: (list: string, period?: TimeWindow) => ['people', 'list', list, period ?? null] as const,
  movieBrowse: (params: Omit<BrowseParams<MovieCategory>, 'page'>) => ['movies', 'browse', params] as const,
  tvBrowse: (params: Omit<BrowseParams<TvCategory>, 'page'>) => ['tv', 'browse', params] as const,
  movie: (id: number) => ['movies', 'details', id] as const,
  tv: (id: number) => ['tv', 'details', id] as const,
  person: (id: number) => ['people', 'details', id] as const,
  searchAll: (query: string) => ['search', 'all', query] as const,
  search: (kind: 'movies' | 'tv' | 'people', query: string) => ['search', kind, query] as const,
}
