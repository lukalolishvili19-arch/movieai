import { useNavigate } from 'react-router-dom'
import type { CastMember, ImageAsset, VideoAsset } from '@/types/api'
import { initials } from '@/lib/format'

export function CastList({ cast, variant = 'photo' }: { cast: CastMember[]; variant?: 'photo' | 'initials' }) {
  const navigate = useNavigate()
  if (cast.length === 0) return <p className="text-sm" style={{ color: '#8a8590' }}>Cast information is not available.</p>
  return (
    <div className="flex flex-wrap gap-4">
      {cast.map(a => (
        <button
          key={`${a.id}-${a.order}`}
          onClick={() => navigate(`/person/${a.id}`)}
          className="flex items-center gap-3 p-3 rounded-xl text-left transition-smooth hover:border-white/20"
          style={{ background: '#18181d', border: '1px solid rgba(255,255,255,0.06)' }}
        >
          {a.profileUrl ? (
            <div className={`${variant === 'photo' ? 'w-10 h-10' : 'w-9 h-9'} rounded-full overflow-hidden flex-shrink-0`} style={{ background: '#111114' }}>
              <img src={a.profileUrl} alt={a.name} className="w-full h-full object-cover" loading="lazy" />
            </div>
          ) : (
            <div
              className={`${variant === 'photo' ? 'w-10 h-10' : 'w-9 h-9'} rounded-full flex items-center justify-center text-xs font-bold flex-shrink-0`}
              style={{ background: 'linear-gradient(135deg, #e8843a, #c0392b)', color: '#fff' }}
            >
              {initials(a.name)}
            </div>
          )}
          <div>
            <p className="text-sm font-medium" style={{ color: '#f0ede8' }}>{a.name}</p>
            {a.character && <p className="text-xs" style={{ color: '#8a8590' }}>{a.character}</p>}
          </div>
        </button>
      ))}
    </div>
  )
}

export function VideoList({ videos, onPlay }: { videos: VideoAsset[]; onPlay: (video: VideoAsset) => void }) {
  if (videos.length === 0) return <p className="text-sm" style={{ color: '#8a8590' }}>No videos are available.</p>
  return (
    <div className="flex gap-4 flex-wrap">
      {videos.map(v => (
        <button
          key={v.id}
          onClick={() => onPlay(v)}
          className="w-48 flex-shrink-0 text-left group"
          aria-label={`Play ${v.name}`}
        >
          <div
            className="relative w-48 h-28 rounded-xl overflow-hidden flex items-center justify-center cursor-pointer transition-smooth"
            style={{ background: '#18181d', border: '1px solid rgba(255,255,255,0.08)' }}
          >
            {v.thumbnailUrl && <img src={v.thumbnailUrl} alt="" className="absolute inset-0 w-full h-full object-cover opacity-70 group-hover:opacity-90 transition-smooth" loading="lazy" />}
            <div className="relative w-10 h-10 rounded-full flex items-center justify-center" style={{ background: 'rgba(232,132,58,0.85)' }}>
              <span style={{ color: '#fff' }}>▶</span>
            </div>
          </div>
          <p className="text-xs mt-2 line-clamp-2" style={{ color: '#c0bdb8' }}>{v.name}</p>
          <p className="text-xs" style={{ color: '#8a8590' }}>{v.type}</p>
        </button>
      ))}
    </div>
  )
}

export function PhotoGrid({ images, emptyText = 'No photos are available.', portrait }: { images: ImageAsset[]; emptyText?: string; portrait?: boolean }) {
  if (images.length === 0) return <p className="text-sm" style={{ color: '#8a8590' }}>{emptyText}</p>
  return (
    <div className={portrait ? 'grid grid-cols-3 md:grid-cols-5 gap-3' : 'grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3'}>
      {images.map(img => (
        <a
          key={img.filePath}
          href={img.url}
          target="_blank"
          rel="noopener noreferrer"
          className="rounded-xl overflow-hidden block poster-wrap"
          style={{ aspectRatio: portrait ? '2 / 3' : '16 / 9', background: '#18181d' }}
        >
          <img src={img.thumbnailUrl} alt="" className="w-full h-full object-cover" loading="lazy" />
        </a>
      ))}
    </div>
  )
}
