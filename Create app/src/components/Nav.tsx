import { useEffect, useRef, useState } from 'react'
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '@/auth/context'
import { activeSection, type Section } from './sections'

function UserMenu() {
  const { status, user, openAuthModal, logout } = useAuth()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!open) return
    const onDown = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false)
    }
    const onKey = (e: KeyboardEvent) => e.key === 'Escape' && setOpen(false)
    document.addEventListener('mousedown', onDown)
    document.addEventListener('keydown', onKey)
    return () => {
      document.removeEventListener('mousedown', onDown)
      document.removeEventListener('keydown', onKey)
    }
  }, [open])

  if (status !== 'authenticated' || !user) {
    return (
      <button
        onClick={() => status === 'anonymous' && openAuthModal()}
        aria-label="Sign in"
        title="Sign in"
        className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold transition-smooth ${status === 'loading' ? 'animate-pulse' : ''}`}
        style={{ background: 'rgba(255,255,255,0.08)', color: '#c0bdb8', border: '1px solid rgba(255,255,255,0.12)' }}
      >
        👤
      </button>
    )
  }

  const letters = user.email.split('@')[0].replace(/[^a-zA-Z0-9]/g, '').slice(0, 2).toUpperCase() || 'ME'

  return (
    <div className="relative" ref={ref}>
      <button
        onClick={() => setOpen(v => !v)}
        aria-haspopup="menu"
        aria-expanded={open}
        aria-label="Account menu"
        className="w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold"
        style={{ background: 'linear-gradient(135deg, #e8843a, #c0392b)', color: '#fff' }}
      >
        {letters}
      </button>
      {open && (
        <div
          role="menu"
          className="absolute right-0 mt-2 w-56 rounded-xl p-2"
          style={{ background: 'rgba(24,24,29,0.98)', border: '1px solid rgba(255,255,255,0.08)', backdropFilter: 'blur(20px)' }}
        >
          <p className="px-3 py-2 text-xs truncate" style={{ color: '#8a8590' }}>{user.email}</p>
          <button
            role="menuitem"
            onClick={() => { setOpen(false); navigate('/watchlist') }}
            className="w-full text-left px-3 py-2 rounded-lg text-sm transition-smooth hover:bg-white/5"
            style={{ color: '#f0ede8' }}
          >
            🔖 My Watchlist
          </button>
          <button
            role="menuitem"
            onClick={() => { setOpen(false); void logout() }}
            className="w-full text-left px-3 py-2 rounded-lg text-sm transition-smooth hover:bg-white/5"
            style={{ color: '#f0ede8' }}
          >
            Sign Out
          </button>
        </div>
      )}
    </div>
  )
}

export default function Nav() {
  const navigate = useNavigate()
  const location = useLocation()
  const [params] = useSearchParams()
  const page = activeSection(location.pathname)
  const onSearchPage = page === 'search'
  const urlQuery = onSearchPage ? params.get('q') ?? '' : ''

  const [scrolled, setScrolled] = useState(false)
  const [searchOpen, setSearchOpen] = useState(onSearchPage)
  const [searchQuery, setSearchQuery] = useState(urlQuery)

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 40)
    onScroll()
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [])

  // Keep the header field in sync when the query changes elsewhere (back/forward, mobile search page).
  useEffect(() => {
    setSearchQuery(urlQuery)
    setSearchOpen(onSearchPage)
  }, [urlQuery, onSearchPage])

  const links: { label: string; id: Section; to: string }[] = [
    { label: 'Home', id: 'home', to: '/' },
    { label: 'Movies', id: 'movies', to: '/movies' },
    { label: 'TV Shows', id: 'tv', to: '/tv' },
    { label: 'Actors', id: 'actors', to: '/actors' },
  ]

  const onQueryChange = (value: string) => {
    setSearchQuery(value)
    const target = value ? `/search?q=${encodeURIComponent(value)}` : '/search'
    navigate(target, { replace: onSearchPage })
  }

  return (
    <nav
      className="fixed top-0 left-0 right-0 z-50 transition-smooth"
      style={{
        background: scrolled ? 'rgba(10,10,11,0.95)' : 'transparent',
        backdropFilter: scrolled ? 'blur(20px)' : 'none',
        borderBottom: scrolled ? '1px solid rgba(255,255,255,0.06)' : 'none',
      }}
    >
      <div className="flex items-center justify-between px-8 md:px-16 h-16 gap-3">
        {/* Logo */}
        <button
          className="flex items-center gap-2 font-bold text-lg tracking-tight flex-shrink-0"
          onClick={() => navigate('/')}
        >
          <span
            className="w-7 h-7 rounded-lg flex items-center justify-center text-xs font-black"
            style={{ background: 'linear-gradient(135deg, #e8843a, #c0392b)', color: '#fff' }}
          >M</span>
          <span className={searchOpen ? 'hidden sm:inline' : ''} style={{ color: '#f0ede8' }}>Movie<span style={{ color: '#e8843a' }}>AI</span></span>
        </button>

        {/* Links */}
        <div className="hidden md:flex items-center gap-6">
          {links.map(l => (
            <button
              key={l.id}
              onClick={() => navigate(l.to)}
              aria-current={page === l.id ? 'page' : undefined}
              className="text-sm font-medium transition-smooth"
              style={{ color: page === l.id ? '#e8843a' : '#c0bdb8' }}
            >
              {l.label}
            </button>
          ))}
        </div>

        {/* Right */}
        <div className="flex items-center gap-3 min-w-0">
          {searchOpen ? (
            <input
              autoFocus
              type="search"
              aria-label="Search movies, TV shows and actors"
              value={searchQuery}
              onChange={e => onQueryChange(e.target.value)}
              onBlur={() => !searchQuery && !onSearchPage && setSearchOpen(false)}
              placeholder="Search movies, TV shows, actors…"
              className="text-sm px-3 py-1.5 rounded-lg outline-none w-full max-w-56 sm:w-56"
              style={{ background: 'rgba(255,255,255,0.08)', color: '#f0ede8', border: '1px solid rgba(255,255,255,0.12)' }}
            />
          ) : (
            <button onClick={() => setSearchOpen(true)} aria-label="Search" className="w-8 h-8 flex items-center justify-center rounded-full transition-smooth" style={{ color: '#c0bdb8' }}>🔍</button>
          )}
          <button
            onClick={() => navigate('/watchlist')}
            aria-label="Watchlist"
            className="w-8 h-8 flex items-center justify-center rounded-full transition-smooth flex-shrink-0"
            style={{ color: page === 'watchlist' ? '#e8843a' : '#c0bdb8' }}
          >🔖</button>
          <div className="flex-shrink-0">
            <UserMenu />
          </div>
        </div>
      </div>
    </nav>
  )
}
