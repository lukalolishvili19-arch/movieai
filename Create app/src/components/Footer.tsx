export default function Footer() {
  return (
    <footer
      className="px-8 md:px-16 pt-10 pb-28 md:pb-10 mt-8"
      style={{ borderTop: '1px solid rgba(255,255,255,0.06)' }}
    >
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div className="flex items-center gap-2 font-bold text-lg tracking-tight">
          <span
            className="w-7 h-7 rounded-lg flex items-center justify-center text-xs font-black"
            style={{ background: 'linear-gradient(135deg, #e8843a, #c0392b)', color: '#fff' }}
          >M</span>
          <span style={{ color: '#f0ede8' }}>Movie<span style={{ color: '#e8843a' }}>AI</span></span>
        </div>
        <div className="flex items-center gap-4 max-w-xl">
          <a
            href="https://www.themoviedb.org"
            target="_blank"
            rel="noopener noreferrer"
            aria-label="The Movie Database (TMDB)"
            className="flex-shrink-0 px-2.5 py-1 rounded-md text-xs font-black tracking-wider"
            style={{ background: 'linear-gradient(90deg, #90cea1, #01b4e4)', color: '#0d253f' }}
          >
            TMDB
          </a>
          <p className="text-xs leading-relaxed" style={{ color: '#8a8590' }}>
            This product uses the TMDB API but is not endorsed or certified by TMDB. Streaming availability data provided by{' '}
            <a href="https://www.justwatch.com" target="_blank" rel="noopener noreferrer" style={{ color: '#c0bdb8' }}>JustWatch</a>.
          </p>
        </div>
      </div>
    </footer>
  )
}
