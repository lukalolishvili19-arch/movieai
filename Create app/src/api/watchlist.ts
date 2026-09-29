import { buildQuery, request } from './client'
import type { MediaType, PagedResponse, WatchlistItem, WatchlistKey, WatchlistStatus } from '@/types/api'

export const getWatchlist = (params: { mediaType?: MediaType; page?: number; size?: number }, signal?: AbortSignal) =>
  request<PagedResponse<WatchlistItem>>(`/watchlist${buildQuery({ ...params })}`, { auth: true, signal })

export const getWatchlistIds = (signal?: AbortSignal) => request<WatchlistKey[]>('/watchlist/ids', { auth: true, signal })

export const checkWatchlist = (mediaType: MediaType, tmdbId: number, signal?: AbortSignal) =>
  request<WatchlistStatus>(`/watchlist/check/${mediaType}/${tmdbId}`, { auth: true, signal })

export const addToWatchlist = (key: WatchlistKey) =>
  request<WatchlistItem>('/watchlist', { method: 'POST', body: key, auth: true })

export const removeFromWatchlist = ({ mediaType, tmdbId }: WatchlistKey) =>
  request<void>(`/watchlist/${mediaType}/${tmdbId}`, { method: 'DELETE', auth: true })
