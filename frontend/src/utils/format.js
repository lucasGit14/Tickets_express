export function formatDate(iso) {
  if (!iso) return '-'
  try {
    return new Date(iso).toLocaleString('pt-BR')
  } catch {
    return String(iso)
  }
}

export function formatPrice(value) {
  try {
    const n = typeof value === 'number' ? value : Number(value)
    if (Number.isNaN(n)) return '-'
    return n.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
  } catch {
    return '-'
  }
}

export function friendlyError(err, fallback) {
  const status = err?.response?.status
  if (status === 400) return 'Dados inválidos. Verifique e tente novamente.'
  if (status === 401) return 'Você precisa estar autenticado para continuar.'
  if (status === 403) return 'Você não tem permissão para esta ação.'
  if (status === 404) return 'Recurso não encontrado.'
  if (status === 409) return 'Conflito: o recurso já existe ou não está disponível.'
  return fallback
}
