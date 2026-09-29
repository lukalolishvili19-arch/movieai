import { lazy, Suspense, useEffect } from 'react'
import { Route, Routes, useLocation } from 'react-router-dom'
import Nav from './components/Nav'
import MobileNav from './components/MobileNav'
import Footer from './components/Footer'
import Toaster from './components/Toaster'
import NotFoundState from './components/NotFoundState'
import HomePage from './pages/HomePage'

const BrowsePage = lazy(() => import('./pages/BrowsePage'))
const ActorsPage = lazy(() => import('./pages/ActorsPage'))
const SearchPage = lazy(() => import('./pages/SearchPage'))
const WatchlistPage = lazy(() => import('./pages/WatchlistPage'))
const MovieDetailPage = lazy(() => import('./pages/MovieDetailPage'))
const TVDetailPage = lazy(() => import('./pages/TVDetailPage'))
const ActorDetailPage = lazy(() => import('./pages/ActorDetailPage'))

function PageFallback() {
  return <div style={{ minHeight: '100vh' }} aria-busy="true" />
}

function ScrollToTop() {
  const { pathname } = useLocation()
  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }, [pathname])
  return null
}

export default function App() {
  return (
    <div style={{ background: '#0a0a0b', minHeight: '100vh' }}>
      <ScrollToTop />
      <Nav />

      <Suspense fallback={<PageFallback />}>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/movies" element={<BrowsePage key="movie" type="movie" />} />
        <Route path="/tv" element={<BrowsePage key="tv" type="tv" />} />
        <Route path="/actors" element={<ActorsPage />} />
        <Route path="/search" element={<SearchPage />} />
        <Route path="/watchlist" element={<WatchlistPage />} />
        <Route path="/movie/:id" element={<MovieDetailPage />} />
        <Route path="/tv/:id" element={<TVDetailPage />} />
        <Route path="/person/:id" element={<ActorDetailPage />} />
        <Route path="*" element={<NotFoundState title="Page not found" message="The page you're looking for doesn't exist." />} />
      </Routes>
      </Suspense>

      <Footer />
      <MobileNav />
      <Toaster />
    </div>
  )
}
