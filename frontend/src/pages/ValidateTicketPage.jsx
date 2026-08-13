import { useState } from 'react';

export function ValidateTicketPage() {
  const [ticketCode, setTicketCode] = useState('');
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleValidate = async (e) => {
    e.preventDefault();
    if (!ticketCode) return;

    setLoading(true);
    const token = localStorage.getItem('token');
    try {
      const response = await fetch('http://localhost:8080/api/tickets/validate', {
        method: 'POST',
        headers: { 
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ code: ticketCode })
      });
      const data = await response.json();
      setResult(data);
    } catch (err) {
      setResult({
        status: 'INVALID',
        message: 'Erro ao validar ingresso',
        code: ticketCode
      });
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'VALID':
        return 'bg-emerald-100 text-emerald-700 border-emerald-200';
      case 'ALREADY_USED':
        return 'bg-amber-100 text-amber-700 border-amber-200';
      case 'WRONG_EVENT':
        return 'bg-orange-100 text-orange-700 border-orange-200';
      case 'INVALID':
      default:
        return 'bg-rose-100 text-rose-700 border-rose-200';
    }
  };

  const getStatusLabel = (status) => {
    switch (status) {
      case 'VALID': return 'VÁLIDO';
      case 'ALREADY_USED': return 'JÁ UTILIZADO';
      case 'WRONG_EVENT': return 'EVENTO INCORRETO';
      case 'INVALID': return 'INVÁLIDO';
      default: return 'INVÁLIDO';
    }
  };

  return (
      <div className="min-h-screen bg-slate-50 py-10 px-6">
        <div className="max-w-md mx-auto bg-white rounded-2xl border border-slate-200/60 shadow-xl p-8 space-y-6">
          <div className="text-center">
            <h1 className="text-2xl font-black text-slate-900">Portaria / Validação</h1>
            <p className="text-xs text-slate-500 font-medium">Digite o código do ingresso para validar a entrada</p>
          </div>

          <form onSubmit={handleValidate} className="space-y-4">
            <div className="space-y-1">
              <label className="text-xs font-bold text-slate-700 uppercase tracking-wider">Código do Ingresso</label>
              <input
                  type="text"
                  value={ticketCode}
                  onChange={(e) => setTicketCode(e.target.value)}
                  placeholder="Ex: TCK-12345"
                  required
                  className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-violet-600 text-sm font-mono text-center"
              />
            </div>

            <button
                type="submit"
                disabled={loading}
                className="w-full bg-violet-600 hover:bg-violet-700 disabled:bg-slate-400 text-white font-extrabold py-3.5 rounded-xl transition-all shadow-md shadow-violet-200 active:scale-95 text-sm"
            >
              {loading ? 'Validando...' : 'Validar Ingresso'}
            </button>
          </form>

          {result && (
              <div className={`p-4 rounded-xl text-center space-y-3 border ${getStatusBadge(result.status)}`}>
                <span className={`text-[10px] font-black uppercase px-2.5 py-1 rounded-full border ${getStatusBadge(result.status)}`}>
                  {getStatusLabel(result.status)}
                </span>
                <p className="font-extrabold text-sm">{result.message}</p>
                {result.code && <p className="text-xs font-mono">Código: #{result.code}</p>}
                {result.eventTitle && <p className="text-xs text-slate-600">Evento: {result.eventTitle}</p>}
                {result.ownerName && <p className="text-xs text-slate-600">Proprietário: {result.ownerName}</p>}
              </div>
          )}
        </div>
      </div>
  );
}

export default ValidateTicketPage;