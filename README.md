# Tickets Express

Sistema de venda e validação de ingressos para eventos, com papéis de **CUSTOMER**, **ORGANIZER** e **GATEKEEPER**.

## Objetivo

Permitir que organizadores criem e gerenciem eventos e assentos, clientes reservem e paguem ingressos (pagamento simulado) e porteiros validem códigos de ingresso na entrada.

## Arquitetura

```
frontend/          React + Vite + Axios + React Router
backend/           Spring Boot 4.x + Java 21 + JPA + Security JWT
PostgreSQL         Persistência + Flyway
Docker Compose     Postgres (+ opcionalmente API e frontend)
```

Fluxo principal:

1. ORGANIZER cria evento (DRAFT), cadastra assentos e publica.
2. CUSTOMER escolhe assentos → reserva **PENDING** (expira em 15 min).
3. CUSTOMER confirma pagamento simulado → reserva **PAID** + geração de ingressos.
4. CUSTOMER visualiza ingressos e QR Code.
5. GATEKEEPER valida o código e marca o ingresso como **USED**.

## Requisitos

- Java 21+
- Maven Wrapper (`mvnw` / `mvnw.cmd`)
- Node.js 20+ e npm
- Docker e Docker Compose (recomendado para PostgreSQL)
- PostgreSQL 16 (se rodar sem Docker)

## Instalação

```bash
git clone <url-do-repositorio>
cd Tickets_express
cp .env.example .env
```

## Configuração do PostgreSQL

Variáveis padrão (locais):

| Variável | Valor padrão |
|----------|--------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/tickets_express` |
| `DB_USER` | `tickets_express` |
| `DB_PASSWORD` | `tickets_express_local` |

O schema é aplicado automaticamente pelo Flyway na subida da API.

## Execução com Docker

Somente banco:

```bash
docker compose up -d postgres
```

Stack completa (Postgres + backend + frontend):

```bash
docker compose up --build
```

- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- Health: http://localhost:8080/actuator/health

## Execução do backend

```bash
cd backend/tickets-express-api
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

## Execução do frontend

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

Acesse http://localhost:5173

## Usuários de teste (Seed Data)

Os seguintes usuários são criados automaticamente pelo Flyway (V5) ao iniciar a aplicação:

| Papel | Email | Senha |
|-------|-------|-------|
| ORGANIZER | `organizador@example.com` | `organizador` |
| CUSTOMER 1 | `cliente1@example.com` | `cliente1` |
| CUSTOMER 2 | `cliente2@example.com` | `cliente2` |
| GATEKEEPER | `gatekeeper@example.com` | `gatekeeper` |

**Nota:** A senha para todos os usuários de teste é a mesma do email (sem o @example.com).

Não versionamos senhas de produção. Segredos JWT/QR usam defaults apenas para desenvolvimento local (veja `.env.example`).

## Fluxo completo de uso

1. Cadastre um ORGANIZER e um CUSTOMER.
2. Entre como ORGANIZER → **Meus Eventos** → criar evento → cadastrar assentos → **Publicar**.
3. Entre como CUSTOMER → **Eventos** → selecionar assentos → reservar → pagar.
4. Em **Meus Ingressos**, abra o ingresso e mostre o QR Code / código.
5. Entre como GATEKEEPER → **Validar** → informe o código.

## Autenticação JWT e roles

- Login: `POST /api/auth/login` → `{ token, id, name, email, role }`
- Sessão no frontend: token em `localStorage` + restauração via `GET /api/auth/me`
- Header: `Authorization: Bearer <token>`
- Roles:
  - **CUSTOMER**: reservar, pagar, listar/transferir ingressos
  - **ORGANIZER**: criar/gerenciar apenas os próprios eventos
  - **GATEKEEPER**: validar ingressos; visualizar eventos não publicados quando necessário

## Comandos de testes

Backend:

```bash
cd backend/tickets-express-api
.\mvnw.cmd test
```

Frontend:

```bash
cd frontend
npm run lint
npm run build
```

## Comandos de build

```bash
# Backend
cd backend/tickets-express-api
.\mvnw.cmd -DskipTests package

# Frontend
cd frontend
npm run build
```

## Documentação da API

- OpenAPI: [`docs/openapi.yaml`](docs/openapi.yaml)

## Estrutura do repositório

```
Tickets_express/
├── backend/tickets-express-api/
├── frontend/
├── docs/openapi.yaml
├── docker-compose.yml
├── .env.example
└── README.md
```

## Uso de IA

O uso de inteligência artificial neste projeto limitou-se a algumas linhas de código na parte do Front-End, utilizando a ferramenta Codex.
