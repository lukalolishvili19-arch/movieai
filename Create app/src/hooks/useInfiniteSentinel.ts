import { useEffect, useRef } from 'react'

/**
 * Calls onVisible when the returned element scrolls near the viewport. Pass a changing resetKey
 * (e.g. the number of loaded pages) so the observer re-checks after each load: an element that is
 * still in view after new content renders would otherwise never fire again.
 */
export function useInfiniteSentinel<T extends Element>(onVisible: () => void, enabled: boolean, resetKey: unknown = null) {
  const ref = useRef<T>(null)
  const callback = useRef(onVisible)
  callback.current = onVisible

  useEffect(() => {
    const node = ref.current
    if (!node || !enabled || typeof IntersectionObserver === 'undefined') return
    const observer = new IntersectionObserver(
      entries => {
        if (entries.some(e => e.isIntersecting)) callback.current()
      },
      { rootMargin: '600px 0px' },
    )
    observer.observe(node)
    return () => observer.disconnect()
  }, [enabled, resetKey])

  return ref
}
