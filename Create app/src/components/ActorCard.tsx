import { useNavigate } from 'react-router-dom'
import type { PersonSummary } from '@/types/api'
import PosterImage from './PosterImage'

interface Props {
  actor: PersonSummary
  /** Shows the orange "Trending" marker used in trending lists. */
  trending?: boolean
}

export default function ActorCard({ actor, trending }: Props) {
  const navigate = useNavigate()
  const href = `/person/${actor.id}`
  const knownFor = actor.knownFor.length > 0 ? actor.knownFor.join(', ') : actor.knownForDepartment

  return (
    <div
      className="flex-shrink-0 cursor-pointer group flex flex-col items-center"
      style={{ width: 140 }}
      role="link"
      tabIndex={0}
      aria-label={actor.name}
      onClick={() => navigate(href)}
      onKeyDown={e => {
        if (e.key === 'Enter') navigate(href)
      }}
    >
      <div
        className="poster-wrap rounded-full overflow-hidden mb-3"
        style={{ width: 110, height: 110, background: '#18181d' }}
      >
        <PosterImage src={actor.profileUrl} alt={actor.name} fallback="👤" />
      </div>
      <p className="text-sm font-semibold text-center leading-snug" style={{ color: '#f0ede8' }}>{actor.name}</p>
      {knownFor && <p className="text-xs text-center mt-0.5 line-clamp-1" style={{ color: '#8a8590' }}>{knownFor}</p>}
      {trending ? (
        <div className="mt-1.5 flex items-center gap-1">
          <div className="w-1.5 h-1.5 rounded-full" style={{ background: '#e8843a' }} />
          <span className="text-xs font-medium" style={{ color: '#e8843a' }}>Trending</span>
        </div>
      ) : (
        actor.knownForDepartment && actor.knownFor.length > 0 && (
          <span className="mt-1.5 text-xs font-medium" style={{ color: '#e8843a' }}>{actor.knownForDepartment}</span>
        )
      )}
    </div>
  )
}
