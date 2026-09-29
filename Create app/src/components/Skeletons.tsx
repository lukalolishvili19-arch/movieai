const block = { background: 'rgba(255,255,255,0.06)' }

export function CardSkeleton({ fluid }: { fluid?: boolean }) {
  return (
    <div className="flex-shrink-0 animate-pulse" style={{ width: fluid ? '100%' : 160 }} aria-hidden="true">
      <div className="rounded-xl" style={fluid ? { ...block, aspectRatio: '2 / 3' } : { ...block, height: 240 }} />
      <div className="mt-2 h-3.5 rounded" style={{ ...block, width: '80%' }} />
      <div className="mt-2 h-3 rounded" style={{ ...block, width: '50%' }} />
    </div>
  )
}

export function ActorSkeleton() {
  return (
    <div className="flex-shrink-0 flex flex-col items-center animate-pulse" style={{ width: 140 }} aria-hidden="true">
      <div className="rounded-full mb-3" style={{ ...block, width: 110, height: 110 }} />
      <div className="h-3.5 rounded" style={{ ...block, width: 90 }} />
      <div className="mt-2 h-3 rounded" style={{ ...block, width: 70 }} />
    </div>
  )
}

export function CardRowSkeleton({ count = 8 }: { count?: number }) {
  return (
    <>
      {Array.from({ length: count }, (_, i) => <CardSkeleton key={i} />)}
    </>
  )
}

export function ActorRowSkeleton({ count = 8 }: { count?: number }) {
  return (
    <>
      {Array.from({ length: count }, (_, i) => <ActorSkeleton key={i} />)}
    </>
  )
}

export function GridSkeleton({ count = 12 }: { count?: number }) {
  return (
    <>
      {Array.from({ length: count }, (_, i) => <CardSkeleton key={i} fluid />)}
    </>
  )
}

export function HeroSkeleton({ height = '88vh', minHeight = 560 }: { height?: string; minHeight?: number }) {
  return (
    <div className="relative w-full animate-pulse" style={{ height, minHeight, background: '#111114' }} aria-busy="true" aria-label="Loading">
      <div className="absolute inset-0" style={{ background: 'linear-gradient(to top, rgba(10,10,11,1) 0%, transparent 40%)' }} />
      <div className="absolute inset-0 flex items-center px-8 md:px-16 pt-16">
        <div className="max-w-xl w-full">
          <div className="flex gap-2 mb-4">
            <div className="h-5 w-20 rounded" style={block} />
            <div className="h-5 w-16 rounded" style={block} />
          </div>
          <div className="h-14 rounded mb-4" style={{ ...block, width: '80%' }} />
          <div className="h-4 rounded mb-2" style={{ ...block, width: '40%' }} />
          <div className="h-3 rounded mb-2 mt-6" style={block} />
          <div className="h-3 rounded mb-2" style={block} />
          <div className="h-3 rounded mb-6" style={{ ...block, width: '60%' }} />
          <div className="flex gap-3">
            <div className="h-10 w-36 rounded-lg" style={block} />
            <div className="h-10 w-32 rounded-lg" style={block} />
          </div>
        </div>
      </div>
    </div>
  )
}

export function RowSkeleton() {
  return (
    <div className="flex items-center gap-4 p-4 rounded-xl animate-pulse" style={{ background: '#18181d', border: '1px solid rgba(255,255,255,0.06)' }} aria-hidden="true">
      <div className="rounded-lg flex-shrink-0" style={{ ...block, width: 56, height: 80 }} />
      <div className="flex-1">
        <div className="h-4 rounded mb-2" style={{ ...block, width: '40%' }} />
        <div className="h-3 rounded" style={{ ...block, width: '25%' }} />
      </div>
    </div>
  )
}

export function TextSkeleton({ lines = 3 }: { lines?: number }) {
  return (
    <div className="animate-pulse max-w-2xl" aria-hidden="true">
      {Array.from({ length: lines }, (_, i) => (
        <div key={i} className="h-3 rounded mb-2.5" style={{ ...block, width: i === lines - 1 ? '60%' : '100%' }} />
      ))}
    </div>
  )
}
