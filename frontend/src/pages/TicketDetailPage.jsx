import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { ticketAPI } from '../api/client'
import { formatDate, friendlyError } from '../utils/format'

export function TicketDetailPage() {
  const { id } = useParams()
  const [ticket, setTicket] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [transferEmail, setTransferEmail] = useState('')
  const [transferring, setTransferring] = useState(false)
  const [message, setMessage] = useState('')

  useEffect(() => {
    let mounted = true

    ticketAPI
      .getById(id)
      .then((res) => {
        if (!mounted) return
        setTicket(res.data)
      })
      .catch(() => {
        if (!mounted) return
        setError('Não foi possível carregar o ingresso.')
      })
      .finally(() => {
        if (mounted) setLoading(false)
      })

    return () => {
      mounted = false
    }
  }, [id])

  const handleTransfer = async (e) => {
    e.preventDefault()
    setMessage('')
    setError('')
    setTransferring(true)
    try {
      const res = await ticketAPI.transfer(id, transferEmail)
      setTicket(res.data)
      setMessage('Ingresso transferido com sucesso.')
      setTransferEmail('')
    } catch (err) {
      setError(friendlyError(err, 'Não foi possível transferir o ingresso.'))
    } finally {
      setTransferring(false)
    }
  }

  if (loading) {
    return (
      <div className="page-container">
        <p className="state-message">Carregando ingresso...</p>
      </div>
    )
  }

  if (error && !ticket) {
    return (
      <div className="page-container">
        <div className="error-message">{error}</div>
      </div>
    )
  }

  if (!ticket) {
    return (
      <div className="page-container">
        <p className="state-message">Ingresso não encontrado.</p>
      </div>
    )
  }

  const qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(ticket.code)}`

  return (
    <div className="page-container">
      <h2>Ingresso</h2>

      {message && <div className="success-message">{message}</div>}
      {error && <div className="error-message">{error}</div>}

      <div className="ticket-detail">
        <div className="qr-block">
          <img src={qrUrl} alt={`QR Code do ingresso ${ticket.code}`} />
          <p className="ticket-code">{ticket.code}</p>
        </div>

        <div className="info-block">
          <p>
            Evento: <strong>{ticket.eventTitle}</strong>
          </p>
          <p>
            Assento: {ticket.rowLabel}
            {ticket.seatNumber}
            {ticket.seatCategory ? ` — ${ticket.seatCategory}` : ''}
          </p>
          <p>Status: {ticket.status}</p>
          <p>Titular: {ticket.ownerName}</p>
          <p>Criado em: {formatDate(ticket.createdAt)}</p>
          {ticket.usedAt && <p>Utilizado em: {formatDate(ticket.usedAt)}</p>}
        </div>
      </div>

      {ticket.status === 'VALID' && (
        <form onSubmit={handleTransfer} className="transfer-form">
          <h3>Transferir ingresso</h3>
          <div className="form-group">
            <label htmlFor="transferEmail">E-mail do destinatário</label>
            <input
              id="transferEmail"
              type="email"
              value={transferEmail}
              onChange={(e) => setTransferEmail(e.target.value)}
              required
              placeholder="destinatario@email.com"
            />
          </div>
          <button type="submit" className="submit-btn action-btn" disabled={transferring}>
            {transferring ? 'Transferindo...' : 'Transferir'}
          </button>
        </form>
      )}
    </div>
  )
}
