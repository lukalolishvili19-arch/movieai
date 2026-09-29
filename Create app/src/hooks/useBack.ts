import { useNavigate } from 'react-router-dom'

/** Goes back within the app when there is history, otherwise to a sensible fallback page. */
export function useBack(fallback: string) {
  const navigate = useNavigate()
  return () => {
    const state = window.history.state as { idx?: number } | null
    if (state && typeof state.idx === 'number' && state.idx > 0) navigate(-1)
    else navigate(fallback)
  }
}
