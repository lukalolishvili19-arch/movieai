import { buildQuery, request } from './client'
import type {
  BrowseParams,
  Genre,
  MediaImages,
  MovieCategory,
  MovieDetails,
  MovieSummary,
  PagedResponse,
  TimeWindow,
  VideoAsset,
  WatchProviders,
} from '@/types/api'

type Page = PagedResponse<MovieSummary>

export const getTrendingMovies = (period: TimeWindow = 'day', page = 1, signal?: AbortSignal) =>
  request<Page>(`/movies/trending${buildQuery({ period, page })}`, { signal })

export const getPopularMovies = (page = 1, signal?: AbortSignal) =>
  request<Page>(`/movies/popular${buildQuery({ page })}`, { signal })

export const getNowPlayingMovies = (page = 1, signal?: AbortSignal) =>
  request<Page>(`/movies/now-playing${buildQuery({ page })}`, { signal })

export const getUpcomingMovies = (page = 1, signal?: AbortSignal) =>
  request<Page>(`/movies/upcoming${buildQuery({ page })}`, { signal })

export const getTopRatedMovies = (page = 1, signal?: AbortSignal) =>
  request<Page>(`/movies/top-rated${buildQuery({ page })}`, { signal })

export const browseMovies = (params: BrowseParams<MovieCategory>, signal?: AbortSignal) =>
  request<Page>(`/movies${buildQuery({ ...params })}`, { signal })

export const getMovieGenres = (signal?: AbortSignal) => request<Genre[]>('/movies/genres', { signal })

export const getMovieDetails = (id: number, region?: string, signal?: AbortSignal) =>
  request<MovieDetails>(`/movies/${id}${buildQuery({ region })}`, { signal })

export const getMovieImages = (id: number, signal?: AbortSignal) =>
  request<MediaImages>(`/movies/${id}/images`, { signal })

export const getMovieVideos = (id: number, signal?: AbortSignal) =>
  request<VideoAsset[]>(`/movies/${id}/videos`, { signal })

export const getMovieWatchProviders = (id: number, region?: string, signal?: AbortSignal) =>
  request<WatchProviders>(`/movies/${id}/watch-providers${buildQuery({ region })}`, { signal })
