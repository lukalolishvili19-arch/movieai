import { useRef, type ReactNode } from 'react'

export default function CarouselSection({
  title,
  subtitle,
  seeAll,
  children,
}: {
  title: string
  subtitle?: ReactNode
  seeAll?: () => void
  children: ReactNode
}) {
  const ref = useRef<HTMLDivElement>(null)
  const scroll = (dir: 'l' | 'r') => {
    if (ref.current) ref.current.scrollBy({ left: dir === 'l' ? -360 : 360, behavior: 'smooth' })
  }
  return (
    <section className="py-8">
      <div className="flex items-center justify-between mb-5 px-8 md:px-16 gap-3">
        <div className="flex items-center gap-4 flex-wrap min-w-0">
          <h2 className="text-xl font-bold" style={{ color: '#f0ede8' }}>{title}</h2>
          {subtitle}
        </div>
        <div className="flex items-center gap-3 flex-shrink-0">
          {seeAll && (
            <button
              className="text-xs font-semibold uppercase tracking-wider transition-smooth"
              style={{ color: '#e8843a' }}
              onClick={seeAll}
            >
              See All →
            </button>
          )}
          <button
            onClick={() => scroll('l')}
            aria-label={`Scroll ${title} left`}
            className="w-8 h-8 rounded-full flex items-center justify-center transition-smooth"
            style={{ background: 'rgba(255,255,255,0.08)', color: '#f0ede8' }}
          >‹</button>
          <button
            onClick={() => scroll('r')}
            aria-label={`Scroll ${title} right`}
            className="w-8 h-8 rounded-full flex items-center justify-center transition-smooth"
            style={{ background: 'rgba(255,255,255,0.08)', color: '#f0ede8' }}
          >›</button>
        </div>
      </div>
      <div
        ref={ref}
        className="flex gap-4 px-8 md:px-16 pb-2 scrollbar-hide carousel-scroll"
        style={{ overflowX: 'auto' }}
      >
        {children}
      </div>
    </section>
  )
}
