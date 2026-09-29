interface Props<T extends string> {
  tabs: readonly T[]
  active: T
  onChange: (tab: T) => void
  className?: string
  labels?: Partial<Record<T, string>>
}

export default function TabBar<T extends string>({ tabs, active, onChange, className = 'mb-6', labels }: Props<T>) {
  return (
    <div
      role="tablist"
      className={`flex gap-1 border-b overflow-x-auto scrollbar-hide ${className}`}
      style={{ borderColor: 'rgba(255,255,255,0.08)' }}
    >
      {tabs.map(t => (
        <button
          key={t}
          role="tab"
          aria-selected={active === t}
          onClick={() => onChange(t)}
          className="px-4 py-2.5 text-sm font-medium transition-smooth relative whitespace-nowrap flex-shrink-0"
          style={{ color: active === t ? '#e8843a' : '#8a8590' }}
        >
          {labels?.[t] ?? t}
          {active === t && (
            <span className="absolute bottom-0 left-0 right-0 h-0.5 rounded-t" style={{ background: '#e8843a' }} />
          )}
        </button>
      ))}
    </div>
  )
}
