import { useState, useEffect } from 'react';
import { EventCard } from '../components/EventCard';

export function EventsPage() {
    const [events, setEvents] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch('http://localhost:8080/api/events')
            .then((res) => res.json())
            .then((data) => {
                setEvents(Array.isArray(data) ? data : []);
                setLoading(false);
            })
            .catch((err) => {
                console.error("Erro ao buscar eventos:", err);
                setLoading(false);
            });
    }, []);

    return (
        <div className="min-h-screen bg-slate-50 py-10 px-6">
            <div className="max-w-7xl mx-auto space-y-8">
                <div className="flex items-center justify-between">
                    <div>
                        <h1 className="text-3xl font-black text-slate-900">Todos os Eventos</h1>
                        <p className="text-xs text-slate-500 font-medium">Confira a lista completa de eventos disponíveis</p>
                    </div>
                    <span className="text-xs font-bold bg-violet-100 text-violet-700 px-3 py-1 rounded-full">
            {events.length} eventos
          </span>
                </div>

                {loading ? (
                    <div className="text-center py-16 text-slate-500 font-medium">Carregando eventos...</div>
                ) : events.length === 0 ? (
                    <div className="bg-white rounded-2xl border border-slate-200/60 p-12 text-center text-slate-500">
                        Nenhum evento encontrado.
                    </div>
                ) : (
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                        {events.map((event) => (
                            <EventCard key={event.id} event={event} />
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}

export default EventsPage;