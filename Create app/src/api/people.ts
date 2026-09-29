import { buildQuery, request } from './client'
import type { ImageAsset, PagedResponse, PersonCredits, PersonDetails, PersonSummary, TimeWindow } from '@/types/api'

type Page = PagedResponse<PersonSummary>

export const getPopularPeople = (page = 1, signal?: AbortSignal) =>
  request<Page>(`/people${buildQuery({ page })}`, { signal })

export const getTrendingPeople = (period: TimeWindow = 'day', page = 1, signal?: AbortSignal) =>
  request<Page>(`/people/trending${buildQuery({ period, page })}`, { signal })

export const getPersonDetails = (id: number, signal?: AbortSignal) =>
  request<PersonDetails>(`/people/${id}`, { signal })

export const getPersonCredits = (id: number, signal?: AbortSignal) =>
  request<PersonCredits>(`/people/${id}/credits`, { signal })

export const getPersonImages = (id: number, signal?: AbortSignal) =>
  request<ImageAsset[]>(`/people/${id}/images`, { signal })
