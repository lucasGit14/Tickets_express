import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';

export function PaymentPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [event, setEvent] = useState(null);
  const [loading, setLoading] = useState(true);
  const [paymentMethod, setPaymentMethod] = useState('CARD');

  useEffect(() => {
    fetch(`http://localhost:8080/api/events/${id}`)
        .then((res) => res.json())
        .then((data) => {
          setEvent(data);
          setLoading(false);
        })
        .catch((err) => {
          console.error('Erro ao carregar dados:', err);
          setLoading(false);
        });
  }, [id]);

  const handleConfirmPayment = async () => {
    const token = localStorage.getItem('token');
    try {
      const response = await fetch(`http://localhost:8080/api/tickets/purchase/${id}`, {
        method: 'POST',
        headers: { 
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ paymentMethod })
      });
      if (response.ok) {
        alert('Pagamento aprovado com sucesso!');
        navigate('/my-tickets');
      } else {
        const errorData = await response.json();
        const errorMessage = errorData.message || errorData.error || 'Erro ao processar pagamento';
        console.error('Erro no pagamento:', errorData);
        alert(errorMessage);
      }
    } catch (err) {
      console.error('Erro ao processar pagamento:', err);
      alert('Erro ao processar pagamento');
    }
  };

  if (loading) return <div className="text-center py-20 text-slate-500">Carregando dados da compra...</div>;

  return (
      <div className="min-h-screen bg-slate-50 py-10 px-6">
        <div className="max-w-xl mx-auto bg-white rounded-2xl border border-slate-200/60 shadow-xl p-8 space-y-6">
          <div>
            <h1 className="text-2xl font-black text-slate-900">Finalizar Compra</h1>
            <p className="text-xs text-slate-500 font-medium">Revise os detalhes antes de confirmar o pagamento</p>
          </div>

          {event && (
              <div className="bg-slate-50 rounded-xl p-4 border border-slate-200/60 space-y-2">
                <h3 className="font-extrabold text-slate-900 text-base">{event.title}</h3>
                <p className="text-xs text-slate-500">📍 {event.location}</p>
                <p className="text-xs text-slate-500">📅 {event.date}</p>
                <div className="flex justify-between items-center pt-2 border-t border-slate-200">
                  <span className="text-xs font-bold text-slate-600">Total:</span>
                  <span className="text-lg font-black text-violet-700">
                {event.price ? `R$ ${Number(event.price).toFixed(2)}` : 'Grátis'}
              </span>
                </div>
              </div>
          )}

          <div className="space-y-3">
            <label className="text-xs font-bold text-slate-700 uppercase tracking-wider block">Forma de Pagamento</label>
            <div className="grid grid-cols-2 gap-3">
              <button 
                type="button" 
                onClick={() => setPaymentMethod('CARD')}
                className={`p-3 border-2 rounded-xl text-xs font-bold text-center ${paymentMethod === 'CARD' ? 'border-violet-600 bg-violet-50 text-violet-700' : 'border-slate-200 text-slate-600'}`}
              >
                💳 Cartão
              </button>
              <button 
                type="button" 
                onClick={() => setPaymentMethod('PIX')}
                className={`p-3 border-2 rounded-xl text-xs font-bold text-center ${paymentMethod === 'PIX' ? 'border-violet-600 bg-violet-50 text-violet-700' : 'border-slate-200 text-slate-600'}`}
              >
                💠 PIX
              </button>
            </div>
          </div>

          <button
              onClick={handleConfirmPayment}
              className="w-full bg-violet-600 hover:bg-violet-700 text-white font-extrabold py-3.5 rounded-xl transition-all shadow-md shadow-violet-200 active:scale-95 text-sm"
          >
            Pagar Agora
          </button>
        </div>
      </div>
  );
}

export default PaymentPage;