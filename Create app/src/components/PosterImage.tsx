import { useState } from 'react'

interface Props {
  src: string | null | undefined
  alt: string
  className?: string
  loading?: 'lazy' | 'eager'
  /** Glyph shown when there is no image (or it fails to load). */
  fallback?: string
}

/** An <img> that degrades to a neutral placeholder when TMDB has no artwork. */
export default function PosterImage({ src, alt, className = 'w-full h-full object-cover', loading = 'lazy', fallback = '🎬' }: Props) {
  const [failed, setFailed] = useState(false)
  if (!src || failed) {
    return (
      <div
        role="img"
        aria-label={alt}
        className="w-full h-full flex items-center justify-center"
        style={{ background: 'linear-gradient(160deg, #18181d 0%, #111114 100%)', color: '#8a8590' }}
      >
        <span style={{ fontSize: 28, opacity: 0.5 }}>{fallback}</span>
      </div>
    )
  }
  return <img src={src} alt={alt} className={className} loading={loading} decoding="async" onError={() => setFailed(true)} />
}
