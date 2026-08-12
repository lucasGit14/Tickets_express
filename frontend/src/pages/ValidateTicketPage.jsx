import { useState } from 'react';

export function ValidateTicketPage() {
  const [ticketId, setTicketId] = useState('');
  const [result, setResult] = useState(null);

  const handleValidate = async (e) => {
    e.preventDefault();
    if (!ticketId) return;

    const token = localStorage.getItem('token');
    try {
      const response = await fetch(`http://localhost:8080/api/tickets/validate/${ticketId}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const data = await response.json();
      setResult({
        valid: data.valid || true,
        code: ticketId,
        message: data.message || 'Ingresso liberado para entrada!',
      });
    } catch (err) {
      setResult({
        valid: false,
        code: ticketId,
        message: 'Erro ao validar ingresso',
      });
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
                  value={ticketId}
                  onChange={(e) => setTicketId(e.target.value)}
                  placeholder="Ex: TCK-12345"
                  required
                  className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-violet-600 text-sm font-mono text-center"
              />
            </div>

            <button
                type="submit"
                className="w-full bg-violet-600 hover:bg-violet-700 text-white font-extrabold py-3.5 rounded-xl transition-all shadow-md shadow-violet-200 active:scale-95 text-sm"
            >
              Validar Ingresso
            </button>
          </form>

          {result && (
              <div className={`p-4 rounded-xl text-center space-y-1 border ${
                  result.valid ? 'bg-emerald-50 border-emerald-200 text-emerald-800' : 'bg-rose-50 border-rose-200 text-rose-800'
              }`}>
                <p className="font-extrabold text-sm">{result.message}</p>
                <p className="text-xs font-mono">Código: #{result.code}</p>
              </div>
          )}
        </div>
      </div>
  );
}

export default ValidateTicketPage;