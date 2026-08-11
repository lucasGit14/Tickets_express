create table users (
                       id uuid primary key,
                       name varchar(120) not null,
                       email varchar(180) not null unique,
                       password_hash varchar(100) not null,
                       role varchar(20) not null check (role in ('ORGANIZER','CUSTOMER','GATEKEEPER')),
                       created_at timestamptz not null default now()
);

create table events (
                        id uuid primary key,
                        organizer_id uuid not null references users(id),
                        tmdb_movie_id bigint not null,
                        title varchar(255) not null,
                        poster_url varchar(500),
                        synopsis text,
                        starts_at timestamptz not null,
                        venue varchar(160) not null,
                        address varchar(255) not null,
                        price numeric(12,2) not null check (price > 0),
                        status varchar(20) not null check (status in ('DRAFT','PUBLISHED','CANCELLED')),
                        created_at timestamptz not null default now()
);

create table seats (
                       id uuid primary key,
                       event_id uuid not null references events(id) on delete cascade,
                       row_label varchar(5) not null,
                       seat_number integer not null check (seat_number > 0),
                       category varchar(30) not null default 'STANDARD',
                       unique (event_id, row_label, seat_number)
);

create table reservations (
                              id uuid primary key,
                              customer_id uuid not null references users(id),
                              event_id uuid not null references events(id),
                              status varchar(20) not null check (status in ('PENDING','PAID','DECLINED','EXPIRED','CANCELLED')),
                              expires_at timestamptz,
                              total_amount numeric(12,2) not null,
                              payment_reference varchar(80),
                              created_at timestamptz not null default now()
);

create table reservation_seats (
                                   reservation_id uuid not null references reservations(id) on delete cascade,
                                   seat_id uuid not null references seats(id),
                                   primary key (reservation_id, seat_id),
                                   unique (seat_id)
);

create table tickets (
                         id uuid primary key,
                         reservation_id uuid not null references reservations(id),
                         seat_id uuid not null unique references seats(id),
                         code_hash varchar(64) not null unique,
                         status varchar(20) not null check (status in ('ACTIVE','USED','CANCELLED')),
                         validated_at timestamptz,
                         validated_by uuid references users(id),
                         share_token_hash varchar(64) unique,
                         share_expires_at timestamptz,
                         created_at timestamptz not null default now()
);

create index ix_events_public_search on events(status, starts_at);
create index ix_reservations_customer on reservations(customer_id, created_at desc);