import { buildQuery, request } from './client'
import type { RecommendationParams, RecommendationResponse } from '@/types/api'

export const getMovieRecommendations = (params: RecommendationParams, signal?: AbortSignal) =>
  request<RecommendationResponse>(`/recommendations/movies${buildQuery({ ...params })}`, { signal })
