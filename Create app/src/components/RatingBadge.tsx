export default function RatingBadge({ rating, size = 'sm' }: { rating: number | null; size?: 'sm' | 'md' | 'lg' }) {
  const cls = size === 'lg' ? 'text-2xl font-bold' : size === 'md' ? 'text-base font-semibold' : 'text-xs font-semibold'
  if (rating === null) {
    return (
      <span className={`${cls} flex items-center gap-1`} style={{ color: '#8a8590' }} title="Not yet rated">
        <span style={{ color: '#8a8590' }}>★</span> NR
      </span>
    )
  }
  const color = rating >= 8 ? '#22c55e' : rating >= 7 ? '#e8843a' : '#8a8590'
  return (
    <span className={`${cls} flex items-center gap-1`} style={{ color }}>
      <span style={{ color: '#f59e0b' }}>★</span> {rating.toFixed(1)}
    </span>
  )
}
