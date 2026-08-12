import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';

export function TicketDetailPage() {
  const { id } = useParams();
  const [ticket, setTicket] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem('token');
    fetch(`http://localhost:8080/api/tickets/${id}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
        .then((res) => res.json())
        .then((data) => setTicket(data))
        .catch((err) => console.error('Erro ao carregar ingresso:', err));
  }, [id]);

  return (
      <div className="min-h-screen bg-slate-50 py-10 px-6">
        <div className="max-w-md mx-auto bg-white rounded-3xl border border-slate-200/60 shadow-xl overflow-hidden p-6 space-y-6 text-center">
          <div>
          <span className="text-[10px] font-black uppercase px-3 py-1 rounded-full bg-emerald-100 text-emerald-700">
            Ingresso Válido
          </span>
            <h1 className="text-2xl font-black text-slate-900 mt-2">{ticket?.eventTitle || 'Detalhes do Ingresso'}</h1>
            <p className="text-xs text-slate-400 font-mono">Código: #{id}</p>
          </div>

          <div className="bg-slate-50 p-6 rounded-2xl border border-slate-200 flex justify-center items-center">
            <div className="w-40 h-40 bg-slate-900 rounded-xl flex items-center justify-center text-white font-mono text-xs font-bold">
              [ QR CODE ]
            </div>
          </div>

          <div className="text-left bg-slate-50/50 p-4 rounded-xl border border-slate-100 space-y-1 text-xs">
            <p><strong className="text-slate-700">Data:</strong> {ticket?.eventDate || 'A definir'}</p>
            <p><strong className="text-slate-700">Local:</strong> {ticket?.eventLocation || 'A definir'}</p>
          </div>

          <Link
              to="/my-tickets"
              className="block w-full border border-slate-200 text-slate-600 font-bold py-3 rounded-xl hover:bg-slate-50 transition-all text-xs"
          >
            Voltar para Meus Ingressos
          </Link>
        </div>
      </div>
  );
}

export default TicketDetailPage;