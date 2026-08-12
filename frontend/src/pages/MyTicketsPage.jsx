import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ticketAPI } from '../api/client'
import { formatDate } from '../utils/format'

export function MyTicketsPage() {
  const [tickets, setTickets] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let mounted = true

    ticketAPI
      .listMine()
      .then((res) => {
        if (!mounted) return
        setTickets(res.data || [])
      })
      .catch(() => {
        if (!mounted) return
        setError('Não foi possível carregar seus ingressos.')
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
      <h2>Meus Ingressos</h2>

      {loading && <p className="state-message">Carregando ingressos...</p>}
      {error && <div className="error-message">{error}</div>}
      {!loading && !error && tickets.length === 0 && (
        <p className="state-message">Você ainda não possui ingressos.</p>
      )}

      <div className="tickets-grid">
        {tickets.map((t) => (
          <Link key={t.id} to={`/tickets/${t.id}`} className="ticket-card">
            <h3>{t.eventTitle}</h3>
            <p>
              Assento: {t.rowLabel}
              {t.seatNumber}
              {t.seatCategory ? ` (${t.seatCategory})` : ''}
            </p>
            <p>Status: {t.status}</p>
            <p className="muted">Criado em {formatDate(t.createdAt)}</p>
          </Link>
        ))}
      </div>
    </div>
  )
}
