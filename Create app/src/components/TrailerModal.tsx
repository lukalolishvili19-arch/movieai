import type { VideoAsset } from '@/types/api'
import Modal from './Modal'

export default function TrailerModal({ video, title, onClose }: { video: VideoAsset; title: string; onClose: () => void }) {
  const src = video.embedUrl ? `${video.embedUrl}${video.embedUrl.includes('?') ? '&' : '?'}autoplay=1&rel=0` : null

  return (
    <Modal onClose={onClose} label={`${title} — ${video.name}`}>
      <div className="flex items-center justify-between gap-4 mb-3">
        <div className="min-w-0">
          <p className="text-xs font-medium uppercase tracking-wider" style={{ color: '#e8843a' }}>{video.type}</p>
          <p className="text-sm font-semibold truncate" style={{ color: '#f0ede8' }}>{video.name}</p>
        </div>
        <button
          onClick={onClose}
          aria-label="Close trailer"
          className="w-8 h-8 rounded-lg flex items-center justify-center text-xs transition-smooth flex-shrink-0"
          style={{ background: 'rgba(255,255,255,0.08)', color: '#f0ede8' }}
        >✕</button>
      </div>
      <div className="relative w-full rounded-2xl overflow-hidden" style={{ aspectRatio: '16 / 9', background: '#000', border: '1px solid rgba(255,255,255,0.08)' }}>
        {src ? (
          <iframe
            src={src}
            title={video.name}
            className="absolute inset-0 w-full h-full"
            allow="autoplay; encrypted-media; picture-in-picture; fullscreen"
            allowFullScreen
            referrerPolicy="strict-origin-when-cross-origin"
          />
        ) : (
          <div className="absolute inset-0 flex flex-col items-center justify-center gap-3 text-center p-6">
            <p className="text-sm" style={{ color: '#c0bdb8' }}>This video can't be played here.</p>
            {video.watchUrl && (
              <a
                href={video.watchUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="px-5 py-2.5 rounded-lg font-semibold text-sm"
                style={{ background: '#e8843a', color: '#fff' }}
              >
                Watch on {video.site}
              </a>
            )}
          </div>
        )}
      </div>
    </Modal>
  )
}
