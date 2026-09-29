import { useCallback, useRef } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { addToWatchlist, getWatchlistIds, removeFromWatchlist } from '@/api/watchlist'
import { ApiError } from '@/api/client'
import { useAuth } from '@/auth/context'
import type { MediaType, WatchlistKey } from '@/types/api'
import { showToast } from './toast'

// Every watchlist query key starts with ['watchlist', userId] so one user's data can never be read
// from the cache under another user's session; all of it is dropped on logout.
export const watchlistKeys = {
  all: ['watchlist'] as const,
  user: (userId: string | undefined) => ['watchlist', userId ?? 'anonymous'] as const,
  ids: (userId: string | undefined) => ['watchlist', userId ?? 'anonymous', 'ids'] as const,
  list: (userId: string | undefined, mediaType: MediaType | 'all') =>
    ['watchlist', userId ?? 'anonymous', 'list', mediaType] as const,
}

const keyOf = (k: WatchlistKey) => `${k.mediaType}:${k.tmdbId}`
const toKeySet = (keys: WatchlistKey[]) => new Set(keys.map(keyOf))

export function useWatchlistIds() {
  const { status, user } = useAuth()
  return useQuery({
    queryKey: watchlistKeys.ids(user?.id),
    queryFn: ({ signal }) => getWatchlistIds(signal),
    enabled: status === 'authenticated' && !!user,
    staleTime: 60_000,
    select: toKeySet,
  })
}

interface ToggleVars {
  key: WatchlistKey
  add: boolean
}

export function useWatchlistToggle() {
  const queryClient = useQueryClient()
  const { status, user, openAuthModal } = useAuth()
  const { data: ids } = useWatchlistIds()
  const userIdRef = useRef(user?.id)
  userIdRef.current = user?.id

  const mutation = useMutation({
    mutationFn: ({ key, add }: ToggleVars) => (add ? addToWatchlist(key).then(() => undefined) : removeFromWatchlist(key)),
    onMutate: async ({ key, add }) => {
      const idsKey = watchlistKeys.ids(userIdRef.current)
      await queryClient.cancelQueries({ queryKey: idsKey })
      const previous = queryClient.getQueryData<WatchlistKey[]>(idsKey)
      if (previous) {
        const next = add
          ? [...previous.filter(k => keyOf(k) !== keyOf(key)), key]
          : previous.filter(k => keyOf(k) !== keyOf(key))
        queryClient.setQueryData(idsKey, next)
      }
      return { previous, idsKey }
    },
    onError: (error, _vars, context) => {
      if (context?.previous) queryClient.setQueryData(context.idsKey, context.previous)
      if (error instanceof ApiError && error.isUnauthorized) return
      showToast(error instanceof ApiError ? error.message : "Couldn't update your watchlist. Please try again.")
    },
    onSettled: () => queryClient.invalidateQueries({ queryKey: watchlistKeys.user(userIdRef.current) }),
  })

  const isInWatchlist = useCallback(
    (mediaType: MediaType, tmdbId: number) => ids?.has(`${mediaType}:${tmdbId}`) ?? false,
    [ids],
  )

  const toggle = useCallback(
    (mediaType: MediaType, tmdbId: number) => {
      const key = { mediaType, tmdbId }
      if (status === 'anonymous') {
        openAuthModal({
          reason: 'Sign in to save titles to your watchlist.',
          onSuccess: () =>
            addToWatchlist(key)
              .catch((error: unknown) => {
                showToast(error instanceof ApiError ? error.message : "Couldn't update your watchlist. Please try again.")
              })
              .finally(() => queryClient.invalidateQueries({ queryKey: watchlistKeys.all })),
        })
        return
      }
      if (status !== 'authenticated') return
      mutation.mutate({ key, add: !isInWatchlist(mediaType, tmdbId) })
    },
    [status, openAuthModal, queryClient, mutation, isInWatchlist],
  )

  /** Explicit add/remove (rejects on failure after the error has been surfaced). */
  const setMembership = useCallback(
    (mediaType: MediaType, tmdbId: number, add: boolean) => mutation.mutateAsync({ key: { mediaType, tmdbId }, add }),
    [mutation],
  )

  return { isInWatchlist, toggle, setMembership }
}

/** Convenience wrapper for a single title's +/✓ button. */
export function useWatchlistEntry(mediaType: MediaType, tmdbId: number) {
  const { isInWatchlist, toggle } = useWatchlistToggle()
  return {
    inWatchlist: isInWatchlist(mediaType, tmdbId),
    toggle: () => toggle(mediaType, tmdbId),
  }
}
