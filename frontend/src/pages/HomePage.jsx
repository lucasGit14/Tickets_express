import { EventCard } from '../components/EventCard';

export function HomePage({ events = [] }) {
    return (
        <div className="min-h-screen bg-slate-50">
            {/* Banner Hero */}
            <section className="bg-gradient-to-b from-violet-900 to-indigo-900 text-white py-16 px-6">
                <div className="max-w-7xl mx-auto text-center space-y-4">
                    <h1 className="text-4xl md:text-6xl font-black tracking-tight">
                        Encontre os melhores <span className="bg-gradient-to-r from-violet-400 to-indigo-300 bg-clip-text text-transparent">eventos</span>
                    </h1>
                    <p className="text-slate-300 max-w-xl mx-auto text-sm md:text-base">
                        Garanta seus ingressos de forma rápida, segura e 100% digital.
                    </p>
                </div>
            </section>

            {/* Grid de Eventos */}
            <main className="max-w-7xl mx-auto px-6 py-12">
                <div className="flex items-center justify-between mb-8">
                    <h2 className="text-2xl font-extrabold text-slate-900">Eventos em Destaque</h2>
                    <span className="text-xs font-bold bg-violet-100 text-violet-700 px-3 py-1 rounded-full">
            {events.length} disponíveis
          </span>
                </div>

                {events.length === 0 ? (
                    <div className="text-center py-16 bg-white rounded-2xl border border-slate-200/60 shadow-sm">
                        <p className="text-slate-500 font-medium">Nenhum evento encontrado no momento.</p>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                        {events.map((event) => (
                            <EventCard key={event.id} event={event} />
                        ))}
                    </div>
                )}
            </main>
        </div>
    );
}
export default HomePage;
