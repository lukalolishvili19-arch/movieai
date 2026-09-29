import type { WatchProvider, WatchProviders } from '@/types/api'

function ProviderRow({ label, providers }: { label: string; providers: WatchProvider[] }) {
  if (providers.length === 0) return null
  return (
    <div className="flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-4">
      <span className="text-xs font-medium uppercase tracking-wider w-16 flex-shrink-0" style={{ color: '#8a8590' }}>{label}</span>
      <div className="flex flex-wrap gap-2">
        {providers.map(p => (
          <div
            key={p.id}
            title={p.name}
            className="flex items-center gap-2 pr-3 rounded-lg"
            style={{ background: '#18181d', border: '1px solid rgba(255,255,255,0.06)' }}
          >
            <div className="w-9 h-9 rounded-lg overflow-hidden flex-shrink-0" style={{ background: '#111114' }}>
              {p.logoUrl ? (
                <img src={p.logoUrl} alt="" className="w-full h-full object-cover" loading="lazy" />
              ) : (
                <span className="w-full h-full flex items-center justify-center text-xs font-bold" style={{ color: '#8a8590' }}>{p.name[0]}</span>
              )}
            </div>
            <span className="text-xs font-medium" style={{ color: '#f0ede8' }}>{p.name}</span>
          </div>
        ))}
      </div>
    </div>
  )
}

export default function WatchProvidersSection({ providers }: { providers: WatchProviders | null }) {
  const region = providers?.region ?? 'your region'
  const stream = providers ? dedupe([...providers.flatrate, ...providers.free, ...providers.ads]) : []
  const rent = providers?.rent ?? []
  const buy = providers?.buy ?? []
  const hasAny = stream.length + rent.length + buy.length > 0

  return (
    <section>
      <h3 className="text-lg font-semibold mt-8 mb-4" style={{ color: '#f0ede8' }}>Where to Watch</h3>
      {hasAny ? (
        <div className="flex flex-col gap-3">
          <ProviderRow label="Stream" providers={stream} />
          <ProviderRow label="Rent" providers={rent} />
          <ProviderRow label="Buy" providers={buy} />
        </div>
      ) : (
        <p className="text-sm" style={{ color: '#8a8590' }}>
          No streaming, rental or purchase options are listed for {region} right now.
        </p>
      )}
      <p className="text-xs mt-3" style={{ color: '#8a8590' }}>
        Availability in {region}. Streaming data by{' '}
        <a href="https://www.justwatch.com" target="_blank" rel="noopener noreferrer" style={{ color: '#e8843a' }}>JustWatch</a>
        {providers?.link && (
          <>
            {' · '}
            <a href={providers.link} target="_blank" rel="noopener noreferrer" style={{ color: '#c0bdb8' }}>All options →</a>
          </>
        )}
      </p>
    </section>
  )
}

function dedupe(list: WatchProvider[]): WatchProvider[] {
  const seen = new Set<number>()
  return list
    .filter(p => (seen.has(p.id) ? false : (seen.add(p.id), true)))
    .sort((a, b) => a.displayPriority - b.displayPriority)
}
