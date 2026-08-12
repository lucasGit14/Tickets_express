import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { eventAPI } from '../api/client'
import { useAuth } from '../context/useAuth'

export function CreateEventPage() {
  const { user } = useAuth()
  const navigate = useNavigate()

  const [tmdbMovieId, setTmdbMovieId] = useState('')
  const [title, setTitle] = useState('')
  const [posterUrl, setPosterUrl] = useState('')
  const [synopsis, setSynopsis] = useState('')
  const [startsAt, setStartsAt] = useState('')
  const [venue, setVenue] = useState('')
  const [address, setAddress] = useState('')
  const [price, setPrice] = useState('')

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  // simple guard: ensure only organizers can access (route is protected by authentication)
  if (user?.role !== 'ORGANIZER') {
    return (
      <div className="page-container">
        <p>Você não tem permissão para acessar esta página.</p>
      </div>
    )
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      const payload = {
        tmdbMovieId: tmdbMovieId ? Number(tmdbMovieId) : null,
        title,
        posterUrl: posterUrl || null,
        synopsis: synopsis || null,
        startsAt: startsAt ? new Date(startsAt).toISOString() : null,
        venue,
        address,
        price: price ? Number(price) : null,
      }

      await eventAPI.create(payload)
      navigate('/events')
    } catch (err) {
      console.error('Create event failed', err)
      const status = err?.response?.status
      if (status === 400) setError('Dados inválidos. Verifique os campos e tente novamente.')
      else if (status === 401) setError('Você precisa estar autenticado para criar eventos.')
      else setError('Não foi possível criar o evento no momento. Tente novamente mais tarde.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page-container">
      <h2>Criar Evento</h2>

      {error && <div className="error-message">{error}</div>}

      <form onSubmit={handleSubmit} className="event-form">
        <div className="form-group">
          <label>TMDB Movie ID</label>
          <input type="number" value={tmdbMovieId} onChange={(e) => setTmdbMovieId(e.target.value)} />
        </div>

        <div className="form-group">
          <label>Título</label>
          <input type="text" value={title} onChange={(e) => setTitle(e.target.value)} required />
        </div>

        <div className="form-group">
          <label>Poster URL</label>
          <input type="url" value={posterUrl} onChange={(e) => setPosterUrl(e.target.value)} />
        </div>

        <div className="form-group">
          <label>Sinopse</label>
          <textarea value={synopsis} onChange={(e) => setSynopsis(e.target.value)} />
        </div>

        <div className="form-group">
          <label>Data e hora</label>
          <input type="datetime-local" value={startsAt} onChange={(e) => setStartsAt(e.target.value)} required />
        </div>

        <div className="form-group">
          <label>Local (venue)</label>
          <input type="text" value={venue} onChange={(e) => setVenue(e.target.value)} required />
        </div>

        <div className="form-group">
          <label>Endereço</label>
          <input type="text" value={address} onChange={(e) => setAddress(e.target.value)} required />
        </div>

        <div className="form-group">
          <label>Preço</label>
          <input type="number" step="0.01" value={price} onChange={(e) => setPrice(e.target.value)} required />
        </div>

        <button type="submit" className="submit-btn" disabled={loading}>
          {loading ? 'Criando...' : 'Criar Evento'}
        </button>
      </form>
    </div>
  )
}
