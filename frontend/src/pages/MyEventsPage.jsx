import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';

export function MyEventsPage() {
    const [events, setEvents] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Busca apenas os eventos criados pelo organizador
        const token = localStorage.getItem('token');
        fetch('http://localhost:8080/api/events/my-events', {
          headers: { 'Authorization': `Bearer ${token}` }
        })
            .then((res) => res.json())
            .then((data) => {
                setEvents(Array.isArray(data) ? data : []);
                setLoading(false);
            })
            .catch((err) => {
                console.error("Erro ao buscar meus eventos:", err);
                setLoading(false);
            });
    }, []);

    return (
        <div className="min-h-screen bg-slate-50 py-10 px-6">
            <div className="max-w-7xl mx-auto space-y-6">
                <div className="flex items-center justify-between">
                    <div>
                        <h1 className="text-3xl font-black text-slate-900">Meus Eventos Criados</h1>
                        <p className="text-xs text-slate-500 font-medium">Gerencie os eventos cadastrados por você</p>
                    </div>
                    <Link
                        to="/events/new"
                        className="bg-violet-600 hover:bg-violet-700 text-white text-sm font-bold px-4 py-2.5 rounded-xl transition-all shadow-md shadow-violet-200"
                    >
                        + Criar Novo Evento
                    </Link>
                </div>

                {loading ? (
                    <div className="text-center py-16 text-slate-500 font-medium">Carregando seus eventos...</div>
                ) : events.length === 0 ? (
                    <div className="bg-white rounded-2xl border border-slate-200/60 p-12 text-center text-slate-500">
                        Você ainda não cadastrou nenhum evento.
                    </div>
                ) : (
                    <div className="bg-white rounded-2xl border border-slate-200/60 shadow-sm overflow-hidden">
                        <div className="divide-y divide-slate-100">
                            {events.map((event) => (
                                <div key={event.id} className="p-6 flex items-center justify-between hover:bg-slate-50/50 transition-colors">
                                    <div className="space-y-1">
                                        <h3 className="font-extrabold text-slate-900 text-base">{event.title}</h3>
                                        <p className="text-xs text-slate-500">📅 {event.date || 'Data a definir'} • 📍 {event.location || 'Local a definir'}</p>
                                    </div>
                                    <div className="flex items-center gap-3">
                                        <Link
                                            to={`/events/${event.id}/edit`}
                                            className="text-xs font-bold border border-slate-200 hover:bg-slate-100 text-slate-700 px-3 py-1.5 rounded-xl transition-all"
                                        >
                                            Editar
                                        </Link>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}

export default MyEventsPage;