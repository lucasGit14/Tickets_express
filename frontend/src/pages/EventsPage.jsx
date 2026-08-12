import { useEffect, useState } from 'react'
import { eventAPI } from '../api/client'

export function EventsPage() {
  const [events, setEvents] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let mounted = true
    setLoading(true)
    setError('')

    eventAPI
      .listPublished()
      .then((res) => {
        if (!mounted) return
        setEvents(res.data || [])
      })
      .catch((err) => {
        console.error('Failed to load events', err)
        setError('Não foi possível carregar os eventos. Tente novamente mais tarde.')
      })
      .finally(() => mounted && setLoading(false))

    return () => {
      mounted = false
    }
  }, [])

  const formatDate = (iso) => {
    try {
      return new Date(iso).toLocaleString('pt-BR')
    } catch (e) {
      return iso
    }
  }

  const formatPrice = (p) => {
    try {
      const n = typeof p === 'number' ? p : Number(p)
      if (Number.isNaN(n)) return '-'
      return n.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
    } catch (e) {
      return '-'
    }
  }

  return (
    <div className="page-container">
      <h2>Eventos</h2>

      {loading && <p>Carregando eventos...</p>}
      {error && <div className="error-message">{error}</div>}

      {!loading && !error && events.length === 0 && (
        <p>Não há eventos publicados no momento.</p>
      )}

      <div className="events-grid">
        {events.map((ev) => (
          <div key={ev.id} className="event-card">
            <h3 className="event-title">{ev.title}</h3>
            <p className="event-starts">{formatDate(ev.startsAt)}</p>
            <p className="event-venue">{ev.venue} — {ev.address}</p>
            <p className="event-price">{formatPrice(ev.price)}</p>
          </div>
        ))}
      </div>
    </div>
  )
}
