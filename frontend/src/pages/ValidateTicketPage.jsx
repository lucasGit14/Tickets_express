import { useState } from 'react'
import { ticketAPI } from '../api/client'
import { formatDate, friendlyError } from '../utils/format'

export function ValidateTicketPage() {
  const [code, setCode] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [result, setResult] = useState(null)

  const handleValidate = async (e) => {
    e.preventDefault()
    setError('')
    setResult(null)
    setLoading(true)

    try {
      const res = await ticketAPI.validate(code.trim())
      setResult(res.data)
    } catch (err) {
      setError(friendlyError(err, 'Não foi possível validar o ingresso.'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page-container">
      <h2>Validar ingresso</h2>
      <p className="state-message">
        Informe o código do ingresso para validação na entrada.
      </p>

      {error && <div className="error-message">{error}</div>}

      <form onSubmit={handleValidate} className="validate-form">
        <div className="form-group">
          <label htmlFor="code">Código do ingresso</label>
          <input
            id="code"
            type="text"
            value={code}
            onChange={(e) => setCode(e.target.value)}
            required
            placeholder="Cole ou digite o código"
          />
        </div>
        <button type="submit" className="submit-btn action-btn" disabled={loading}>
          {loading ? 'Validando...' : 'Validar'}
        </button>
      </form>

      {result && (
        <div className="success-message result-block">
          <h3>Ingresso válido</h3>
          <p>Evento: {result.eventTitle}</p>
          <p>
            Assento: {result.rowLabel}
            {result.seatNumber}
          </p>
          <p>Titular: {result.ownerName}</p>
          <p>Status: {result.status}</p>
          {result.usedAt && <p>Utilizado em: {formatDate(result.usedAt)}</p>}
        </div>
      )}
    </div>
  )
}
