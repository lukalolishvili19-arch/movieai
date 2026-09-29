export default function GenreChip({ label, selected, onClick }: { label: string; selected?: boolean; onClick?: () => void }) {
  return (
    <button
      onClick={onClick}
      aria-pressed={selected}
      className="px-3 py-1 rounded-full text-xs font-medium transition-smooth border"
      style={{
        background: selected ? '#e8843a' : 'rgba(255,255,255,0.06)',
        borderColor: selected ? '#e8843a' : 'rgba(255,255,255,0.1)',
        color: selected ? '#fff' : '#c0bdb8',
      }}
    >
      {label}
    </button>
  )
}
