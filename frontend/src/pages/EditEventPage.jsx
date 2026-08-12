import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';

export function EditEventPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    date: '',
    location: '',
    price: '',
    imageUrl: '',
  });

  useEffect(() => {
    const token = localStorage.getItem('token');
    fetch(`http://localhost:8080/api/events/${id}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
        .then((res) => res.json())
        .then((data) => setFormData(data))
        .catch((err) => console.error('Erro ao carregar dados do evento:', err));
  }, [id]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const token = localStorage.getItem('token');
      await fetch(`http://localhost:8080/api/events/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
        body: JSON.stringify(formData),
      });
      navigate('/my-events');
    } catch (err) {
      console.error('Erro ao atualizar evento:', err);
    }
  };

  return (
      <div className="min-h-screen bg-slate-50 py-10 px-6">
        <div className="max-w-2xl mx-auto bg-white rounded-2xl border border-slate-200/60 shadow-xl p-8 space-y-6">
          <div>
            <h1 className="text-2xl font-black text-slate-900">Editar Evento #{id}</h1>
            <p className="text-xs text-slate-500 font-medium">Atualize as informações do seu evento</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-1">
              <label className="text-xs font-bold text-slate-700 uppercase tracking-wider">Título do Evento</label>
              <input
                  type="text"
                  value={formData.title || ''}
                  onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                  required
                  className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-violet-600 text-sm"
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-1">
                <label className="text-xs font-bold text-slate-700 uppercase tracking-wider">Data e Horário</label>
                <input
                    type="text"
                    value={formData.date || ''}
                    onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                    required
                    className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-violet-600 text-sm"
                />
              </div>

              <div className="space-y-1">
                <label className="text-xs font-bold text-slate-700 uppercase tracking-wider">Preço (R$)</label>
                <input
                    type="number"
                    step="0.01"
                    value={formData.price || ''}
                    onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                    required
                    className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-violet-600 text-sm"
                />
              </div>
            </div>

            <div className="space-y-1">
              <label className="text-xs font-bold text-slate-700 uppercase tracking-wider">Local</label>
              <input
                  type="text"
                  value={formData.location || ''}
                  onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                  required
                  className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-violet-600 text-sm"
              />
            </div>

            <div className="space-y-1">
              <label className="text-xs font-bold text-slate-700 uppercase tracking-wider">Descrição</label>
              <textarea
                  rows="4"
                  value={formData.description || ''}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-violet-600 text-sm resize-none"
              ></textarea>
            </div>

            <div className="flex gap-3 pt-2">
              <button
                  type="button"
                  onClick={() => navigate('/my-events')}
                  className="w-1/2 border border-slate-200 text-slate-600 font-bold py-3 rounded-xl hover:bg-slate-50 transition-all text-sm"
              >
                Cancelar
              </button>
              <button
                  type="submit"
                  className="w-1/2 bg-violet-600 hover:bg-violet-700 text-white font-extrabold py-3 rounded-xl transition-all shadow-md shadow-violet-200 text-sm"
              >
                Salvar Alterações
              </button>
            </div>
          </form>
        </div>
      </div>
  );
}

export default EditEventPage;