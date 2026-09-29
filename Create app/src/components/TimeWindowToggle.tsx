import type { TimeWindow } from '@/types/api'

export default function TimeWindowToggle({
  value,
  onChange,
  size = 'sm',
}: {
  value: TimeWindow
  onChange: (value: TimeWindow) => void
  size?: 'sm' | 'md'
}) {
  const pad = size === 'md' ? 'px-3 py-1.5' : 'px-2.5 py-0.5'
  return (
    <div className="flex gap-2" role="group" aria-label="Time window">
      {(['day', 'week'] as const).map(w => (
        <button
          key={w}
          onClick={() => onChange(w)}
          aria-pressed={value === w}
          className={`${pad} rounded-full text-xs font-medium transition-smooth whitespace-nowrap`}
          style={value === w ? { background: '#e8843a', color: '#fff' } : { background: 'rgba(255,255,255,0.08)', color: '#8a8590' }}
        >
          {w === 'day' ? 'Today' : 'This Week'}
        </button>
      ))}
    </div>
  )
}
