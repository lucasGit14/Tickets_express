alter table tickets
    add column if not exists code_raw varchar(64);

alter table tickets
    add column if not exists owner_id uuid references users(id);

update tickets t
set owner_id = r.customer_id
from reservations r
where t.reservation_id = r.id
  and t.owner_id is null;

update tickets
set code_raw = replace(gen_random_uuid()::text, '-', '')
where code_raw is null;

alter table tickets
    alter column owner_id set not null;

alter table tickets
    alter column code_raw set not null;

create unique index if not exists ux_tickets_code_raw on tickets(code_raw);

alter table tickets drop constraint if exists tickets_status_check;

update tickets set status = 'VALID' where status = 'ACTIVE';

alter table tickets
    add constraint tickets_status_check check (status in ('VALID', 'USED', 'CANCELLED'));

create table if not exists ticket_transfers (
    id uuid primary key,
    ticket_id uuid not null references tickets(id),
    from_user_id uuid not null references users(id),
    to_user_id uuid not null references users(id),
    transferred_at timestamptz not null default now()
);

create index if not exists ix_ticket_transfers_ticket on ticket_transfers(ticket_id, transferred_at desc);
