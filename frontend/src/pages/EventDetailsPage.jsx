import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';

export function EventDetailsPage() {
  const { id } = useParams();
  const [event, setEvent] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch(`http://localhost:8080/api/events/${id}`)
        .then((res) => res.json())
        .then((data) => {
          setEvent(data);
          setLoading(false);
        })
        .catch((err) => {
          console.error("Erro ao carregar evento:", err);
          setLoading(false);
        });
  }, [id]);

  if (loading) {
    return <div className="text-center py-20 text-slate-500 font-medium">Carregando detalhes...</div>;
  }

  if (!event) {
    return <div className="text-center py-20 text-slate-500 font-medium">Evento não encontrado.</div>;
  }

  return (
      <div className="min-h-screen bg-slate-50 py-10 px-6">
        <div className="max-w-4xl mx-auto bg-white rounded-3xl border border-slate-200/60 shadow-xl overflow-hidden">
          <div className="relative aspect-video w-full bg-slate-900">
            <img
                src={event.imageUrl || 'https://via.placeholder.com/1200x600'}
                alt={event.title}
                className="w-full h-full object-cover"
            />
          </div>

          <div className="p-8 space-y-6">
            <div className="space-y-2">
            <span className="text-xs font-bold text-violet-600 bg-violet-50 px-3 py-1 rounded-full">
              📅 {event.date || 'Data a definir'}
            </span>
              <h1 className="text-3xl font-black text-slate-900">{event.title}</h1>
              <p className="text-sm text-slate-500 font-medium">📍 {event.location || 'Local a definir'}</p>
            </div>

            <hr className="border-slate-100" />

            <div className="space-y-2">
              <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider">Sobre o evento</h3>
              <p className="text-slate-600 text-sm leading-relaxed">{event.description || 'Sem descrição informada.'}</p>
            </div>

            <div className="flex items-center justify-between pt-4 border-t border-slate-100">
              <div>
                <span className="text-xs text-slate-400 font-medium block">Valor do ingresso</span>
                <span className="text-2xl font-black text-violet-700">
                {event.price ? `R$ ${Number(event.price).toFixed(2)}` : 'Grátis'}
              </span>
              </div>

              <Link
                  to={`/payment/${event.id}`}
                  className="bg-violet-600 hover:bg-violet-700 text-white font-extrabold px-8 py-3.5 rounded-xl transition-all shadow-md shadow-violet-200 active:scale-95"
              >
                Garantir Ingresso
              </Link>
            </div>
          </div>
        </div>
      </div>
  );
}

export default EventDetailsPage;