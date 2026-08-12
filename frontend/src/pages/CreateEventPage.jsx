import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { eventAPI } from '../api/client'
import { useAuth } from '../context/useAuth'
import { friendlyError } from '../utils/format'

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

  if (user?.role !== 'ORGANIZER') {
    return (
      <div className="page-container">
        <p className="state-message">Você não tem permissão para acessar esta página.</p>
      </div>
    )
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      const payload = {
        tmdbMovieId: Number(tmdbMovieId),
        title,
        posterUrl: posterUrl || null,
        synopsis: synopsis || null,
        startsAt: startsAt ? new Date(startsAt).toISOString() : null,
        venue,
        address,
        price: Number(price),
      }

      const res = await eventAPI.create(payload)
      navigate(`/events/${res.data.id}/edit`)
    } catch (err) {
      setError(friendlyError(err, 'Não foi possível criar o evento no momento.'))
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
          <label htmlFor="tmdbMovieId">TMDB Movie ID</label>
          <input
            id="tmdbMovieId"
            type="number"
            value={tmdbMovieId}
            onChange={(e) => setTmdbMovieId(e.target.value)}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="title">Título</label>
          <input
            id="title"
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="posterUrl">Poster URL</label>
          <input
            id="posterUrl"
            type="url"
            value={posterUrl}
            onChange={(e) => setPosterUrl(e.target.value)}
          />
        </div>

        <div className="form-group">
          <label htmlFor="synopsis">Sinopse</label>
          <textarea
            id="synopsis"
            value={synopsis}
            onChange={(e) => setSynopsis(e.target.value)}
          />
        </div>

        <div className="form-group">
          <label htmlFor="startsAt">Data e hora</label>
          <input
            id="startsAt"
            type="datetime-local"
            value={startsAt}
            onChange={(e) => setStartsAt(e.target.value)}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="venue">Local (venue)</label>
          <input
            id="venue"
            type="text"
            value={venue}
            onChange={(e) => setVenue(e.target.value)}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="address">Endereço</label>
          <input
            id="address"
            type="text"
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="price">Preço</label>
          <input
            id="price"
            type="number"
            step="0.01"
            value={price}
            onChange={(e) => setPrice(e.target.value)}
            required
          />
        </div>

        <button type="submit" className="submit-btn action-btn" disabled={loading}>
          {loading ? 'Criando...' : 'Criar Evento'}
        </button>
      </form>
    </div>
  )
}
