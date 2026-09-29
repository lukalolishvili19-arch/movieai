import { dismissToast, useToasts } from '@/hooks/toast'

export default function Toaster() {
  const toasts = useToasts()
  if (toasts.length === 0) return null
  return (
    <div className="fixed left-1/2 -translate-x-1/2 bottom-24 md:bottom-8 z-[110] flex flex-col gap-2 w-[calc(100%-2rem)] max-w-sm" aria-live="polite">
      {toasts.map(t => (
        <div
          key={t.id}
          role="status"
          className="flex items-center gap-3 px-4 py-3 rounded-xl text-sm"
          style={{ background: 'rgba(24,24,29,0.97)', border: '1px solid rgba(232,132,58,0.25)', color: '#f0ede8', backdropFilter: 'blur(20px)' }}
        >
          <span className="flex-1">{t.message}</span>
          <button onClick={() => dismissToast(t.id)} aria-label="Dismiss" className="text-xs" style={{ color: '#8a8590' }}>✕</button>
        </div>
      ))}
    </div>
  )
}
