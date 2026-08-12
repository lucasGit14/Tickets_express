import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { eventAPI, reservationAPI } from '../api/client'
import { useAuth } from '../context/useAuth'
import { formatDate, formatPrice, friendlyError } from '../utils/format'

export function EventDetailsPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { isAuthenticated } = useAuth()

  const [event, setEvent] = useState(null)
  const [seats, setSeats] = useState([])
  const [selected, setSelected] = useState(() => new Set())
  const [loading, setLoading] = useState(true)
  const [reserving, setReserving] = useState(false)
  const [loadError, setLoadError] = useState('')
  const [actionError, setActionError] = useState('')

  useEffect(() => {
    let mounted = true

    Promise.all([eventAPI.getById(id), eventAPI.listSeats(id)])
      .then(([evRes, seatsRes]) => {
        if (!mounted) return
        setEvent(evRes.data)
        setSeats(seatsRes.data || [])
      })
      .catch(() => {
        if (!mounted) return
        setLoadError(
          'Não foi possível carregar os detalhes do evento. Tente novamente mais tarde.'
        )
      })
      .finally(() => {
        if (mounted) setLoading(false)
      })

    return () => {
      mounted = false
    }
  }, [id])

  const toggleSeat = (seatId, available) => {
    if (!available) return
    setSelected((prev) => {
      const copy = new Set(prev)
      if (copy.has(seatId)) copy.delete(seatId)
      else copy.add(seatId)
      return copy
    })
  }

  const handleReserve = async () => {
    if (!isAuthenticated) {
      navigate('/login')
      return
    }

    if (selected.size === 0) {
      setActionError('Selecione ao menos 1 assento para reservar.')
      return
    }

    setActionError('')
    setReserving(true)

    try {
      const seatIds = Array.from(selected)
      const res = await reservationAPI.reserve(id, seatIds)
      navigate(`/payment/${res.data.id}`)
    } catch (err) {
      setActionError(
        friendlyError(err, 'Erro ao reservar. Tente novamente mais tarde.')
      )
    } finally {
      setReserving(false)
    }
  }

  if (loading) {
    return (
      <div className="page-container">
        <p className="state-message">Carregando...</p>
      </div>
    )
  }

  if (loadError) {
    return (
      <div className="page-container">
        <div className="error-message">{loadError}</div>
      </div>
    )
  }

  if (!event) {
    return (
      <div className="page-container">
        <p className="state-message">Evento não encontrado.</p>
      </div>
    )
  }

  const availableSeats = seats.filter((s) => s.available)

  return (
    <div className="page-container">
      <div className="event-details">
        {event.posterUrl && (
          <img src={event.posterUrl} alt="" className="event-details-poster" />
        )}
        <div>
          <h2>{event.title}</h2>
          <p>{formatDate(event.startsAt)}</p>
          <p>
            {event.venue} — {event.address}
          </p>
          <p className="event-price">{formatPrice(event.price)}</p>
          {event.synopsis && <p className="event-synopsis">{event.synopsis}</p>}
        </div>
      </div>

      <h3>Assentos</h3>
      {seats.length === 0 && (
        <p className="state-message">Não há assentos cadastrados para este evento.</p>
      )}
      {seats.length > 0 && availableSeats.length === 0 && (
        <p className="state-message">Não há assentos disponíveis no momento.</p>
      )}

      {actionError && <div className="error-message">{actionError}</div>}

      <div className="seats-list">
        {seats.map((s) => {
          const seatId = s.id
          const isSelected = selected.has(seatId)
          const available = s.available !== false
          return (
            <label
              key={seatId}
              className={`seat-item ${isSelected ? 'selected' : ''} ${!available ? 'unavailable' : ''}`}
            >
              <input
                type="checkbox"
                checked={isSelected}
                disabled={!available}
                onChange={() => toggleSeat(seatId, available)}
              />
              <span>
                {s.rowLabel}
                {s.seatNumber}
                {s.category ? ` — ${s.category}` : ''}
                {!available ? ' (indisponível)' : ''}
              </span>
            </label>
          )
        })}
      </div>

      <button
        type="button"
        onClick={handleReserve}
        disabled={reserving || selected.size === 0}
        className="submit-btn action-btn"
      >
        {reserving ? 'Reservando...' : 'Reservar e ir para pagamento'}
      </button>
    </div>
  )
}
