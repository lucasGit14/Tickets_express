# Tickets Express — Frontend

Interface React do Tickets Express (Vite + Axios + React Router).

## Pré-requisitos

- Node.js 20+
- Backend rodando em `http://localhost:8080` (ou URL em `VITE_API_URL`)

## Configuração

```bash
cp .env.example .env
```

```
VITE_API_URL=http://localhost:8080/api
```

## Scripts

```bash
npm install
npm run dev      # http://localhost:5173
npm run lint
npm run build
npm run preview
```

## Rotas principais

| Rota | Acesso | Descrição |
|------|--------|-----------|
| `/events` | Público | Eventos publicados |
| `/events/:id` | Público | Detalhe + seleção de assentos |
| `/payment/:id` | Autenticado | Pagamento simulado |
| `/my-tickets` | CUSTOMER | Meus ingressos |
| `/tickets/:id` | Autenticado | Detalhe + QR Code |
| `/my-events` | ORGANIZER | Eventos do organizador |
| `/events/new` | ORGANIZER | Criar evento |
| `/events/:id/edit` | ORGANIZER | Editar, assentos, reservas |
| `/validate` | GATEKEEPER | Validação de ingresso |
| `/login` / `/register` | Público | Autenticação |

## Autenticação

- Token JWT em `localStorage`
- Perfil restaurado com `GET /api/auth/me` após reload
- Logout limpa token e usuário
- Rotas protegidas por autenticação e role (`ProtectedRoute`)
