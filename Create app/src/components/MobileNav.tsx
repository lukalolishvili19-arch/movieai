import { useLocation, useNavigate } from 'react-router-dom'
import { activeSection, type Section } from './sections'

export default function MobileNav() {
  const navigate = useNavigate()
  const page = activeSection(useLocation().pathname)
  const items: { id: Section; to: string; label: string; icon: string }[] = [
    { id: 'home', to: '/', label: 'Home', icon: '🏠' },
    { id: 'movies', to: '/movies', label: 'Movies', icon: '🎬' },
    { id: 'search', to: '/search', label: 'Search', icon: '🔍' },
    { id: 'watchlist', to: '/watchlist', label: 'Watchlist', icon: '🔖' },
    { id: 'actors', to: '/actors', label: 'Actors', icon: '⭐' },
  ]
  return (
    <nav
      className="md:hidden fixed bottom-0 left-0 right-0 z-50 flex"
      style={{ background: 'rgba(10,10,11,0.95)', backdropFilter: 'blur(20px)', borderTop: '1px solid rgba(255,255,255,0.08)', paddingBottom: 'env(safe-area-inset-bottom)' }}
    >
      {items.map(item => (
        <button
          key={item.id}
          onClick={() => navigate(item.to)}
          aria-current={page === item.id ? 'page' : undefined}
          className="flex-1 flex flex-col items-center py-3 gap-0.5 transition-smooth"
          style={{ color: page === item.id ? '#e8843a' : '#8a8590' }}
        >
          <span style={{ fontSize: 18 }}>{item.icon}</span>
          <span style={{ fontSize: 10, fontWeight: 500 }}>{item.label}</span>
        </button>
      ))}
    </nav>
  )
}
