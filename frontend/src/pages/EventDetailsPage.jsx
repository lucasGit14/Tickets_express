import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { eventAPI, reservationAPI } from '../api/client'
import { useAuth } from '../context/useAuth'

export function EventDetailsPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { isAuthenticated, user } = useAuth()

  const [event, setEvent] = useState(null)
  const [seats, setSeats] = useState([])
  const [selected, setSelected] = useState(new Set())
  const [loading, setLoading] = useState(true)
  const [reserving, setReserving] = useState(false)
  const [error, setError] = useState('')
  const [confirmation, setConfirmation] = useState('')

  useEffect(() => {
    let mounted = true
    setLoading(true)
    setError('')

    Promise.all([eventAPI.getById(id), eventAPI.listSeats(id)])
      .then(([evRes, seatsRes]) => {
        if (!mounted) return
        setEvent(evRes.data)
        setSeats(seatsRes.data || [])
      })
      .catch((err) => {
        console.error('Failed to load event details', err)
        setError('Não foi possível carregar os detalhes do evento. Tente novamente mais tarde.')
      })
      .finally(() => mounted && setLoading(false))

    return () => {
      mounted = false
    }
  }, [id])

  const toggleSeat = (seatId) => {
    setSelected((prev) => {
      const copy = new Set(prev)
      if (copy.has(seatId)) copy.delete(seatId)
      else copy.add(seatId)
      return copy
    })
  }

  const handleReserve = async () => {
    if (!isAuthenticated) {
      // require login
      navigate('/login')
      return
    }

    if (selected.size === 0) {
      setError('Selecione ao menos 1 assento para reservar.')
      return
    }

    setError('')
    setReserving(true)
    setConfirmation('')

    try {
      const seatIds = Array.from(selected)
      await reservationAPI.reserve(id, seatIds)
      setConfirmation('Reserva realizada com sucesso!')
      setSelected(new Set())
    } catch (err) {
      console.error('Reservation failed', err)
      const status = err?.response?.status
      if (status === 401) setError('Você precisa estar autenticado para reservar.')
      else if (status === 400) setError('Não foi possível reservar os assentos selecionados.')
      else setError('Erro ao reservar. Tente novamente mais tarde.')
    } finally {
      setReserving(false)
    }
  }

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

  if (loading) return <div className="page-container"><p>Carregando...</p></div>

  if (error) return <div className="page-container"><div className="error-message">{error}</div></div>

  if (!event) return <div className="page-container"><p>Evento não encontrado.</p></div>

  return (
    <div className="page-container">
      <h2>{event.title}</h2>
      <p>{formatDate(event.startsAt)}</p>
      <p>{event.venue} — {event.address}</p>
      <p>{formatPrice(event.price)}</p>

      <h3>Assentos</h3>
      {seats.length === 0 && <p>Não há assentos cadastrados para este evento.</p>}

      {confirmation && <div className="success-message">{confirmation}</div>}
      {error && <div className="error-message">{error}</div>}

      <div className="seats-list">
        {seats.map((s) => {
          const idStr = s.id
          const isSelected = selected.has(idStr)
          return (
            <label key={idStr} className={`seat-item ${isSelected ? 'selected' : ''}`} style={{display: 'block', cursor: 'pointer', marginBottom: '6px'}}>
              <input type="checkbox" checked={isSelected} onChange={() => toggleSeat(idStr)} />
              {' '}
              {s.rowLabel}{s.seatNumber} — {s.category}
            </label>
          )
        })}
      </div>

      <button onClick={handleReserve} disabled={reserving} className="submit-btn">
        {reserving ? 'Reservando...' : 'Reservar assentos selecionados'}
      </button>
    </div>
  )
}
