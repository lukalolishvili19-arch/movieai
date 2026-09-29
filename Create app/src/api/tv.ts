import { buildQuery, request } from './client'
import type {
  BrowseParams,
  Genre,
  MediaImages,
  PagedResponse,
  TimeWindow,
  TvCategory,
  TvDetails,
  TvSummary,
  VideoAsset,
  WatchProviders,
} from '@/types/api'

type Page = PagedResponse<TvSummary>

export const getTrendingTv = (period: TimeWindow = 'day', page = 1, signal?: AbortSignal) =>
  request<Page>(`/tv/trending${buildQuery({ period, page })}`, { signal })

export const getPopularTv = (page = 1, signal?: AbortSignal) =>
  request<Page>(`/tv/popular${buildQuery({ page })}`, { signal })

export const getAiringTodayTv = (page = 1, signal?: AbortSignal) =>
  request<Page>(`/tv/airing-today${buildQuery({ page })}`, { signal })

export const getOnTheAirTv = (page = 1, signal?: AbortSignal) =>
  request<Page>(`/tv/on-the-air${buildQuery({ page })}`, { signal })

export const getTopRatedTv = (page = 1, signal?: AbortSignal) =>
  request<Page>(`/tv/top-rated${buildQuery({ page })}`, { signal })

export const browseTv = (params: BrowseParams<TvCategory>, signal?: AbortSignal) =>
  request<Page>(`/tv${buildQuery({ ...params })}`, { signal })

export const getTvGenres = (signal?: AbortSignal) => request<Genre[]>('/tv/genres', { signal })

export const getTvDetails = (id: number, region?: string, signal?: AbortSignal) =>
  request<TvDetails>(`/tv/${id}${buildQuery({ region })}`, { signal })

export const getTvImages = (id: number, signal?: AbortSignal) => request<MediaImages>(`/tv/${id}/images`, { signal })

export const getTvVideos = (id: number, signal?: AbortSignal) => request<VideoAsset[]>(`/tv/${id}/videos`, { signal })

export const getTvWatchProviders = (id: number, region?: string, signal?: AbortSignal) =>
  request<WatchProviders>(`/tv/${id}/watch-providers${buildQuery({ region })}`, { signal })
