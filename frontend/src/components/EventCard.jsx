import { Link } from 'react-router-dom';

export function EventCard({ event }) {
    if (!event) return null;

    return (
        <Link
            to={`/events/${event.id}`}
            className="group bg-white rounded-2xl overflow-hidden border border-slate-200/60 shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300 flex flex-col justify-between"
        >
            <div>
                <div className="relative aspect-video overflow-hidden bg-slate-100">
                    <img
                        src={event.imageUrl || 'https://via.placeholder.com/600x337'}
                        alt={event.title || 'Evento'}
                        className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                    />
                    <span className="absolute top-3 left-3 bg-white/90 backdrop-blur-md text-violet-700 text-xs font-extrabold px-3 py-1 rounded-full shadow-sm">
            {event.date || 'Em breve'}
          </span>
                </div>
                <div className="p-5 space-y-2">
                    <h3 className="font-extrabold text-slate-900 text-base line-clamp-2 leading-snug group-hover:text-violet-600 transition-colors">
                        {event.title}
                    </h3>
                    <p className="text-xs text-slate-500 font-medium">📍 {event.location || 'Local a definir'}</p>
                </div>
            </div>
            <div className="px-5 py-3.5 border-t border-slate-100 flex justify-between items-center bg-slate-50/50">
                <span className="text-xs text-slate-400 font-medium">A partir de</span>
                <span className="font-black text-violet-700 text-sm">
          {event.price ? `R$ ${Number(event.price).toFixed(2)}` : 'Grátis'}
        </span>
            </div>
        </Link>
    );
}