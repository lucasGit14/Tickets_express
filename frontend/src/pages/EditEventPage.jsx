import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { eventAPI } from '../api/client'
import { formatDate, formatPrice, friendlyError } from '../utils/format'

function toLocalInputValue(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return ''
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

export function EditEventPage() {
  const { id } = useParams()

  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  const [tmdbMovieId, setTmdbMovieId] = useState('')
  const [title, setTitle] = useState('')
  const [posterUrl, setPosterUrl] = useState('')
  const [synopsis, setSynopsis] = useState('')
  const [startsAt, setStartsAt] = useState('')
  const [venue, setVenue] = useState('')
  const [address, setAddress] = useState('')
  const [price, setPrice] = useState('')
  const [status, setStatus] = useState('')

  const [seats, setSeats] = useState([])
  const [reservations, setReservations] = useState([])

  const [rowLabel, setRowLabel] = useState('A')
  const [seatNumber, setSeatNumber] = useState('1')
  const [category, setCategory] = useState('STANDARD')
  const [seatCount, setSeatCount] = useState('1')
  const [addingSeats, setAddingSeats] = useState(false)

  useEffect(() => {
    let mounted = true

    Promise.all([
      eventAPI.getById(id),
      eventAPI.listSeats(id),
      eventAPI.listReservations(id),
    ])
      .then(([evRes, seatsRes, resRes]) => {
        if (!mounted) return
        const ev = evRes.data
        setTmdbMovieId(ev.tmdbMovieId ?? '')
        setTitle(ev.title || '')
        setPosterUrl(ev.posterUrl || '')
        setSynopsis(ev.synopsis || '')
        setStartsAt(toLocalInputValue(ev.startsAt))
        setVenue(ev.venue || '')
        setAddress(ev.address || '')
        setPrice(ev.price ?? '')
        setStatus(ev.status || '')
        setSeats(seatsRes.data || [])
        setReservations(resRes.data || [])
      })
      .catch(() => {
        if (!mounted) return
        setError('Não foi possível carregar o evento.')
      })
      .finally(() => {
        if (mounted) setLoading(false)
      })

    return () => {
      mounted = false
    }
  }, [id])

  const refreshSeatsAndReservations = async () => {
    const [seatsRes, resRes] = await Promise.all([
      eventAPI.listSeats(id),
      eventAPI.listReservations(id),
    ])
    setSeats(seatsRes.data || [])
    setReservations(resRes.data || [])
  }

  const handleSave = async (e) => {
    e.preventDefault()
    setError('')
    setMessage('')
    setSaving(true)
    try {
      const payload = {
        tmdbMovieId: tmdbMovieId === '' ? null : Number(tmdbMovieId),
        title,
        posterUrl: posterUrl || null,
        synopsis: synopsis || null,
        startsAt: startsAt ? new Date(startsAt).toISOString() : null,
        venue,
        address,
        price: price === '' ? null : Number(price),
      }
      const res = await eventAPI.update(id, payload)
      setStatus(res.data.status)
      setMessage('Evento atualizado com sucesso.')
    } catch (err) {
      setError(friendlyError(err, 'Não foi possível atualizar o evento.'))
    } finally {
      setSaving(false)
    }
  }

  const handlePublish = async () => {
    setError('')
    setMessage('')
    setSaving(true)
    try {
      const res = await eventAPI.publish(id)
      setStatus(res.data.status)
      setMessage('Evento publicado.')
    } catch (err) {
      setError(friendlyError(err, 'Não foi possível publicar o evento.'))
    } finally {
      setSaving(false)
    }
  }

  const handleCancel = async () => {
    setError('')
    setMessage('')
    setSaving(true)
    try {
      const res = await eventAPI.cancel(id)
      setStatus(res.data.status)
      setMessage('Evento cancelado.')
    } catch (err) {
      setError(friendlyError(err, 'Não foi possível cancelar o evento.'))
    } finally {
      setSaving(false)
    }
  }

  const handleAddSeats = async (e) => {
    e.preventDefault()
    setError('')
    setMessage('')
    setAddingSeats(true)
    try {
      const start = Number(seatNumber)
      const count = Math.max(1, Number(seatCount) || 1)
      const seatsPayload = []
      for (let i = 0; i < count; i += 1) {
        seatsPayload.push({
          rowLabel,
          seatNumber: start + i,
          category: category || null,
        })
      }
      await eventAPI.createSeats(id, seatsPayload)
      await refreshSeatsAndReservations()
      setMessage(`${count} assento(s) adicionado(s).`)
    } catch (err) {
      setError(friendlyError(err, 'Não foi possível adicionar assentos.'))
    } finally {
      setAddingSeats(false)
    }
  }

  if (loading) {
    return (
      <div className="page-container">
        <p className="state-message">Carregando evento...</p>
      </div>
    )
  }

  if (error && !title) {
    return (
      <div className="page-container">
        <div className="error-message">{error}</div>
        <Link to="/my-events">Voltar</Link>
      </div>
    )
  }

  return (
    <div className="page-container">
      <div className="page-header-row">
        <h2>Gerenciar evento</h2>
        <Link to="/my-events">Voltar para Meus Eventos</Link>
      </div>

      <p className="status-badge">Status: {status}</p>

      {message && <div className="success-message">{message}</div>}
      {error && <div className="error-message">{error}</div>}

      <form onSubmit={handleSave} className="event-form">
        <div className="form-group">
          <label htmlFor="tmdbMovieId">TMDB Movie ID</label>
          <input
            id="tmdbMovieId"
            type="number"
            value={tmdbMovieId}
            onChange={(e) => setTmdbMovieId(e.target.value)}
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

        <div className="btn-row">
          <button type="submit" className="submit-btn action-btn" disabled={saving}>
            {saving ? 'Salvando...' : 'Salvar alterações'}
          </button>
          {status === 'DRAFT' && (
            <button
              type="button"
              className="secondary-btn"
              onClick={handlePublish}
              disabled={saving}
            >
              Publicar
            </button>
          )}
          {status !== 'CANCELLED' && (
            <button
              type="button"
              className="danger-btn"
              onClick={handleCancel}
              disabled={saving}
            >
              Cancelar evento
            </button>
          )}
        </div>
      </form>

      <section className="manage-section">
        <h3>Assentos ({seats.length})</h3>
        {seats.length === 0 ? (
          <p className="state-message">Nenhum assento cadastrado.</p>
        ) : (
          <ul className="simple-list">
            {seats.map((s) => (
              <li key={s.id}>
                {s.rowLabel}
                {s.seatNumber}
                {s.category ? ` — ${s.category}` : ''}
                {s.available ? ' (disponível)' : ' (indisponível)'}
              </li>
            ))}
          </ul>
        )}

        <form onSubmit={handleAddSeats} className="seat-form">
          <h4>Adicionar assentos</h4>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="rowLabel">Fileira</label>
              <input
                id="rowLabel"
                type="text"
                value={rowLabel}
                onChange={(e) => setRowLabel(e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="seatNumber">Número inicial</label>
              <input
                id="seatNumber"
                type="number"
                value={seatNumber}
                onChange={(e) => setSeatNumber(e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="seatCount">Quantidade</label>
              <input
                id="seatCount"
                type="number"
                min="1"
                value={seatCount}
                onChange={(e) => setSeatCount(e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="category">Categoria</label>
              <select
                id="category"
                value={category}
                onChange={(e) => setCategory(e.target.value)}
              >
                <option value="STANDARD">STANDARD</option>
                <option value="PREMIUM">PREMIUM</option>
                <option value="VIP">VIP</option>
              </select>
            </div>
          </div>
          <button type="submit" className="submit-btn action-btn" disabled={addingSeats}>
            {addingSeats ? 'Adicionando...' : 'Adicionar assentos'}
          </button>
        </form>
      </section>

      <section className="manage-section">
        <h3>Reservas ({reservations.length})</h3>
        {reservations.length === 0 ? (
          <p className="state-message">Nenhuma reserva para este evento.</p>
        ) : (
          <ul className="simple-list">
            {reservations.map((r) => (
              <li key={r.id}>
                {r.customerName} ({r.customerEmail}) — {r.status} —{' '}
                {formatPrice(r.totalAmount)} — {formatDate(r.createdAt)}
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  )
}
