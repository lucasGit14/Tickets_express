import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { eventAPI } from '../api/client'
import { formatDate, formatPrice } from '../utils/format'

export function MyEventsPage() {
  const [events, setEvents] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let mounted = true

    eventAPI
      .listMine()
      .then((res) => {
        if (!mounted) return
        setEvents(res.data || [])
      })
      .catch(() => {
        if (!mounted) return
        setError('Não foi possível carregar seus eventos.')
      })
      .finally(() => {
        if (mounted) setLoading(false)
      })

    return () => {
      mounted = false
    }
  }, [])

  return (
    <div className="page-container">
      <div className="page-header-row">
        <h2>Meus Eventos</h2>
        <Link to="/events/new" className="submit-btn action-btn">
          Criar evento
        </Link>
      </div>

      {loading && <p className="state-message">Carregando eventos...</p>}
      {error && <div className="error-message">{error}</div>}
      {!loading && !error && events.length === 0 && (
        <p className="state-message">Você ainda não criou nenhum evento.</p>
      )}

      <div className="events-grid">
        {events.map((ev) => (
          <Link key={ev.id} to={`/events/${ev.id}/edit`} className="event-card">
            <h3 className="event-title">{ev.title}</h3>
            <p className="event-starts">{formatDate(ev.startsAt)}</p>
            <p className="event-venue">
              {ev.venue} — {ev.address}
            </p>
            <p className="event-price">{formatPrice(ev.price)}</p>
            <p className="status-badge">Status: {ev.status}</p>
          </Link>
        ))}
      </div>
    </div>
  )
}
