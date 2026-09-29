import { useNavigate } from 'react-router-dom'

export default function NotFoundState({ title, message }: { title: string; message: string }) {
  const navigate = useNavigate()
  return (
    <div className="pt-32 pb-16 px-8 md:px-16 text-center">
      <p className="text-4xl mb-4">🎞️</p>
      <p className="text-lg font-semibold mb-1" style={{ color: '#f0ede8' }}>{title}</p>
      <p className="text-sm" style={{ color: '#8a8590' }}>{message}</p>
      <button
        onClick={() => navigate('/')}
        className="mt-6 px-5 py-2.5 rounded-lg font-semibold text-sm transition-smooth"
        style={{ background: '#e8843a', color: '#fff' }}
      >
        Back to Home
      </button>
    </div>
  )
}
