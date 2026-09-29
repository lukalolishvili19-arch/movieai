export type Section = 'home' | 'movies' | 'tv' | 'actors' | 'search' | 'watchlist' | 'detail'

export function activeSection(pathname: string): Section {
  if (pathname === '/') return 'home'
  if (pathname.startsWith('/movies')) return 'movies'
  if (pathname.startsWith('/tv/')) return 'detail'
  if (pathname.startsWith('/tv')) return 'tv'
  if (pathname.startsWith('/actors')) return 'actors'
  if (pathname.startsWith('/search')) return 'search'
  if (pathname.startsWith('/watchlist')) return 'watchlist'
  return 'detail'
}
