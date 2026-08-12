import { useEffect, useState } from 'react'
import { Link, useParams, useNavigate } from 'react-router-dom'
import { reservationAPI } from '../api/client'
import { formatDate, formatPrice, friendlyError } from '../utils/format'

export function PaymentPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [reservation, setReservation] = useState(null)
  const [loading, setLoading] = useState(true)
  const [paying, setPaying] = useState(false)
  const [error, setError] = useState('')
  const [paid, setPaid] = useState(false)

  useEffect(() => {
    let mounted = true

    reservationAPI
      .getById(id)
      .then((res) => {
        if (!mounted) return
        setReservation(res.data)
        if (res.data.status === 'PAID') setPaid(true)
      })
      .catch(() => {
        if (!mounted) return
        setError('Não foi possível carregar a reserva.')
      })
      .finally(() => {
        if (mounted) setLoading(false)
      })

    return () => {
      mounted = false
    }
  }, [id])

  const handlePay = async () => {
    setError('')
    setPaying(true)
    try {
      const res = await reservationAPI.pay(id)
      setReservation(res.data)
      setPaid(true)
    } catch (err) {
      setError(friendlyError(err, 'Não foi possível concluir o pagamento.'))
    } finally {
      setPaying(false)
    }
  }

  const handleCancel = async () => {
    setError('')
    setPaying(true)
    try {
      await reservationAPI.cancel(id)
      navigate('/events')
    } catch (err) {
      setError(friendlyError(err, 'Não foi possível cancelar a reserva.'))
    } finally {
      setPaying(false)
    }
  }

  if (loading) {
    return (
      <div className="page-container">
        <p className="state-message">Carregando reserva...</p>
      </div>
    )
  }

  if (error && !reservation) {
    return (
      <div className="page-container">
        <div className="error-message">{error}</div>
      </div>
    )
  }

  if (!reservation) {
    return (
      <div className="page-container">
        <p className="state-message">Reserva não encontrada.</p>
      </div>
    )
  }

  if (paid) {
    return (
      <div className="page-container">
        <div className="success-message">Pagamento confirmado!</div>
        <h2>Confirmação</h2>
        <p>
          Evento: <strong>{reservation.eventTitle}</strong>
        </p>
        <p>Total: {formatPrice(reservation.totalAmount)}</p>
        <p>Status: {reservation.status}</p>
        {reservation.paymentReference && (
          <p>Referência: {reservation.paymentReference}</p>
        )}

        {reservation.tickets?.length > 0 && (
          <div className="list-block">
            <h3>Seus ingressos</h3>
            <ul className="simple-list">
              {reservation.tickets.map((t) => (
                <li key={t.id}>
                  <Link to={`/tickets/${t.id}`}>
                    {t.code} — {t.rowLabel}
                    {t.seatNumber}
                  </Link>
                </li>
              ))}
            </ul>
          </div>
        )}

        <div className="btn-row">
          <Link to="/my-tickets" className="submit-btn action-btn">
            Ver meus ingressos
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="page-container">
      <h2>Pagamento</h2>
      <p className="state-message">
        Esta é uma simulação de pagamento. Clique em &quot;Pagar&quot; para
        confirmar a reserva.
      </p>

      {error && <div className="error-message">{error}</div>}

      <div className="info-block">
        <p>
          Evento: <strong>{reservation.eventTitle}</strong>
        </p>
        <p>Status: {reservation.status}</p>
        <p>Total: {formatPrice(reservation.totalAmount)}</p>
        <p>Expira em: {formatDate(reservation.expiresAt)}</p>
        <p>Assentos: {reservation.seatIds?.length || 0}</p>
      </div>

      <div className="btn-row">
        <button
          type="button"
          className="submit-btn action-btn"
          onClick={handlePay}
          disabled={paying || reservation.status !== 'PENDING'}
        >
          {paying ? 'Processando...' : 'Pagar agora'}
        </button>
        <button
          type="button"
          className="secondary-btn"
          onClick={handleCancel}
          disabled={paying}
        >
          Cancelar reserva
        </button>
      </div>
    </div>
  )
}
