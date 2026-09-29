// TMDB dates are calendar dates ("2024-03-01"); parse them as local dates so they never shift a day.
export function parseDate(value: string | null | undefined): Date | null {
  if (!value) return null
  const match = /^(\d{4})-(\d{2})-(\d{2})/.exec(value)
  if (!match) return null
  return new Date(Number(match[1]), Number(match[2]) - 1, Number(match[3]))
}

export function formatDate(value: string | null | undefined): string | null {
  const date = parseDate(value)
  return date ? date.toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' }) : null
}

export function formatShortDate(value: string | null | undefined): string | null {
  const date = parseDate(value)
  return date ? date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }) : null
}

export function formatRuntime(minutes: number | null | undefined): string | null {
  if (!minutes || minutes <= 0) return null
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return h > 0 ? `${h}h ${m}m` : `${m}m`
}

export function initials(name: string): string {
  return name
    .split(/\s+/)
    .filter(Boolean)
    .map(n => n[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()
}

export function ageOn(birthday: string | null, until: string | null): number | null {
  const born = parseDate(birthday)
  if (!born) return null
  const end = parseDate(until) ?? new Date()
  let age = end.getFullYear() - born.getFullYear()
  const beforeBirthday =
    end.getMonth() < born.getMonth() || (end.getMonth() === born.getMonth() && end.getDate() < born.getDate())
  if (beforeBirthday) age -= 1
  return age
}

export function detailPath(mediaType: 'movie' | 'tv', id: number): string {
  return mediaType === 'movie' ? `/movie/${id}` : `/tv/${id}`
}
