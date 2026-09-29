import { buildQuery, request } from './client'
import type { MovieSummary, PagedResponse, PersonSummary, SearchResults, TvSummary } from '@/types/api'

export const searchAll = (query: string, signal?: AbortSignal) =>
  request<SearchResults>(`/search${buildQuery({ query })}`, { signal })

export const searchMovies = (query: string, page = 1, signal?: AbortSignal) =>
  request<PagedResponse<MovieSummary>>(`/search/movies${buildQuery({ query, page })}`, { signal })

export const searchTv = (query: string, page = 1, signal?: AbortSignal) =>
  request<PagedResponse<TvSummary>>(`/search/tv${buildQuery({ query, page })}`, { signal })

export const searchPeople = (query: string, page = 1, signal?: AbortSignal) =>
  request<PagedResponse<PersonSummary>>(`/search/people${buildQuery({ query, page })}`, { signal })
