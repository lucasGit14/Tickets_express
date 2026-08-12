import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';

export function MyTicketsPage() {
    const [tickets, setTickets] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const token = localStorage.getItem('token');
        fetch('http://localhost:8080/api/tickets/my-tickets', {
          headers: { 'Authorization': `Bearer ${token}` }
        })
            .then((res) => res.json())
            .then((data) => {
                setTickets(Array.isArray(data) ? data : []);
                setLoading(false);
            })
            .catch((err) => {
                console.error("Erro ao buscar meus ingressos:", err);
                setLoading(false);
            });
    }, []);
    return (
        <div className="min-h-screen bg-slate-50 py-10 px-6">
            <div className="max-w-5xl mx-auto space-y-6">
                <div>
                    <h1 className="text-3xl font-black text-slate-900">Meus Ingressos</h1>
                    <p className="text-xs text-slate-500 font-medium">Apresente o QR Code na portaria do evento</p>
                </div>

                {loading ? (
                    <div className="text-center py-16 text-slate-500 font-medium">Carregando seus ingressos...</div>
                ) : tickets.length === 0 ? (
                    <div className="bg-white rounded-2xl border border-slate-200/60 p-12 text-center text-slate-500">
                        Você ainda não possui nenhum ingresso garantido.
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        {tickets.map((ticket) => (
                            <div key={ticket.id} className="bg-white rounded-2xl border border-slate-200/60 shadow-sm p-6 flex justify-between items-center gap-4">
                                <div className="space-y-2">
                  <span className={`text-[10px] font-black uppercase px-2.5 py-1 rounded-full ${
                      ticket.status === 'VALID' ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-600'
                  }`}>
                    {ticket.status === 'VALID' ? 'Válido' : 'Utilizado'}
                  </span>
                                    <h3 className="font-extrabold text-slate-900 text-base">{ticket.eventTitle || 'Nome do Evento'}</h3>
                                    <p className="text-xs text-slate-500">📅 {ticket.eventDate || 'Data a definir'}</p>
                                    <p className="text-xs text-slate-400 font-mono">Cód: #{ticket.id}</p>
                                </div>

                                <div className="bg-slate-50 p-3 rounded-xl border border-slate-200 flex flex-col items-center justify-center min-w-[100px] h-[100px]">
                                    {ticket.qrCodeUrl ? (
                                        <img src={ticket.qrCodeUrl} alt="QR Code" className="w-16 h-16" />
                                    ) : (
                                        <div className="w-16 h-16 bg-slate-900 rounded flex items-center justify-center text-[10px] text-white font-mono">
                                            QR CODE
                                        </div>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}

export default MyTicketsPage;