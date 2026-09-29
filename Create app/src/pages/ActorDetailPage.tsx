import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getPersonDetails } from '@/api/people'
import { ApiError } from '@/api/client'
import { queryKeys } from '@/api/queryKeys'
import { useBack } from '@/hooks/useBack'
import { ageOn, formatDate } from '@/lib/format'
import type { PersonCredit, PersonDetails } from '@/types/api'
import MovieCard from '@/components/MovieCard'
import TabBar from '@/components/TabBar'
import PosterImage from '@/components/PosterImage'
import ErrorState from '@/components/ErrorState'
import NotFoundState from '@/components/NotFoundState'
import { PhotoGrid } from '@/components/MediaExtras'
import { TextSkeleton } from '@/components/Skeletons'

const TABS = ['Movies', 'TV Shows', 'Photos'] as const
type Tab = (typeof TABS)[number]
const PAGE = 30

function creditSubtitle(credit: PersonCredit): string | null {
  if (credit.character) return credit.character
  if (credit.job) return credit.job
  return null
}

function CreditGrid({ credits, empty }: { credits: PersonCredit[]; empty: string }) {
  const [visible, setVisible] = useState(PAGE)
  if (credits.length === 0) return <p className="text-sm" style={{ color: '#8a8590' }}>{empty}</p>
  return (
    <>
      <div className="media-grid">
        {credits.slice(0, visible).map(c => (
          <MovieCard key={`${c.mediaType}-${c.id}`} item={c} fluid subtitle={creditSubtitle(c)} />
        ))}
      </div>
      {visible < credits.length && (
        <div className="text-center mt-8">
          <button
            onClick={() => setVisible(v => v + PAGE)}
            className="px-5 py-2.5 rounded-lg font-semibold text-sm transition-smooth"
            style={{ background: 'rgba(255,255,255,0.1)', color: '#f0ede8', border: '1px solid rgba(255,255,255,0.15)' }}
          >
            Show More ({credits.length - visible} more)
          </button>
        </div>
      )}
    </>
  )
}

function Biography({ text }: { text: string | null }) {
  const [expanded, setExpanded] = useState(false)
  if (!text) {
    return <p className="text-sm leading-relaxed max-w-lg mb-4" style={{ color: '#8a8590' }}>No biography is available.</p>
  }
  const long = text.length > 600
  return (
    <div className="max-w-2xl mb-4">
      <p className={`text-sm leading-relaxed whitespace-pre-line ${long && !expanded ? 'line-clamp-6' : ''}`} style={{ color: '#c0bdb8' }}>
        {text}
      </p>
      {long && (
        <button onClick={() => setExpanded(v => !v)} className="text-sm font-medium mt-2" style={{ color: '#e8843a' }}>
          {expanded ? 'Show less' : 'Read more'}
        </button>
      )}
    </div>
  )
}

function ActorDetailView({ person }: { person: PersonDetails }) {
  const back = useBack('/actors')
  const [activeTab, setActiveTab] = useState<Tab>('Movies')
  const age = ageOn(person.birthday, person.deathday)
  const birthday = formatDate(person.birthday)

  const facts: [string, string][] = [
    ['Birthday', birthday ? (age !== null && !person.deathday ? `${birthday} (age ${age})` : birthday) : 'Unavailable'],
    ...(person.deathday ? ([['Died', `${formatDate(person.deathday)}${age !== null ? ` (aged ${age})` : ''}`]] as [string, string][]) : []),
    ['Birthplace', person.placeOfBirth ?? 'Unavailable'],
    ['Known For', person.knownForDepartment ?? 'Unavailable'],
  ]

  return (
    <div className="pt-16">
      <div className="px-8 md:px-16 py-12">
        <button onClick={back} className="text-sm font-medium mb-8 block" style={{ color: '#8a8590' }}>← All Actors</button>
        <div className="flex flex-col md:flex-row gap-8 mb-10">
          <div className="poster-wrap rounded-2xl overflow-hidden flex-shrink-0" style={{ width: 200, height: 280, background: '#18181d' }}>
            <PosterImage src={person.profileUrl} alt={person.name} loading="eager" fallback="👤" />
          </div>
          <div>
            <h1 className="text-4xl font-bold mb-2" style={{ fontFamily: "'DM Serif Display', serif", color: '#f0ede8' }}>{person.name}</h1>
            <div className="flex items-center gap-3 mb-4">
              <div className="flex items-center gap-1.5">
                <div className="w-2 h-2 rounded-full" style={{ background: '#e8843a' }} />
                <span className="text-sm font-medium" style={{ color: '#e8843a' }}>Popularity {Math.round(person.popularity)}</span>
              </div>
            </div>
            <Biography text={person.biography} />
            <div className="grid grid-cols-2 gap-4 max-w-md">
              {facts.map(([k, v]) => (
                <div key={k}>
                  <p className="text-xs font-medium mb-0.5" style={{ color: '#8a8590' }}>{k}</p>
                  <p className="text-sm" style={{ color: v === 'Unavailable' ? '#8a8590' : '#f0ede8' }}>{v}</p>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Tabs */}
        <TabBar
          tabs={TABS}
          active={activeTab}
          onChange={setActiveTab}
          labels={{
            Movies: `Movies${person.credits.movies.length ? ` (${person.credits.movies.length})` : ''}`,
            'TV Shows': `TV Shows${person.credits.tv.length ? ` (${person.credits.tv.length})` : ''}`,
          }}
        />

        {activeTab === 'Movies' && <CreditGrid credits={person.credits.movies} empty="No movie credits are listed." />}
        {activeTab === 'TV Shows' && <CreditGrid credits={person.credits.tv} empty="No TV credits are listed." />}
        {activeTab === 'Photos' && <PhotoGrid images={person.images} portrait />}
      </div>
    </div>
  )
}

export default function ActorDetailPage() {
  const id = Number(useParams().id)
  const valid = Number.isInteger(id) && id > 0
  const query = useQuery({
    queryKey: queryKeys.person(id),
    queryFn: ({ signal }) => getPersonDetails(id, signal),
    enabled: valid,
  })

  if (!valid || (query.error instanceof ApiError && query.error.isNotFound)) {
    return <NotFoundState title="Person not found" message="We couldn't find this person on TMDB." />
  }
  if (query.isError) {
    return (
      <div className="pt-24 px-8 md:px-16">
        <ErrorState error={query.error} onRetry={() => query.refetch()} title="Couldn't load this profile" />
      </div>
    )
  }
  if (!query.data) {
    return (
      <div className="pt-16">
        <div className="px-8 md:px-16 py-12">
          <div className="flex flex-col md:flex-row gap-8 mb-10 animate-pulse">
            <div className="rounded-2xl flex-shrink-0" style={{ width: 200, height: 280, background: 'rgba(255,255,255,0.06)' }} />
            <div className="flex-1">
              <div className="h-10 rounded mb-4" style={{ width: '50%', background: 'rgba(255,255,255,0.06)' }} />
              <TextSkeleton lines={5} />
            </div>
          </div>
        </div>
      </div>
    )
  }
  return <ActorDetailView key={id} person={query.data} />
}
