import { QueryClient } from '@tanstack/react-query'
import { ApiError } from '@/api/client'

export function createQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: {
        staleTime: 5 * 60_000,
        gcTime: 30 * 60_000,
        refetchOnWindowFocus: false,
        // Client errors (404, validation, auth) won't succeed on retry; transient failures get one more try.
        retry: (failureCount, error) => {
          if (error instanceof ApiError && error.status >= 400 && error.status < 500 && error.status !== 429) return false
          return failureCount < 1
        },
      },
      mutations: { retry: false },
    },
  })
}
