-- Seed complete data: 1 ORGANIZER, 2 CUSTOMERS, 1 GATEKEEPER (already exists), and 1 PUBLISHED event with seats

-- Insert ORGANIZER
INSERT INTO users (id, name, email, password_hash, role, created_at) VALUES
  ('10000000-0000-0000-0000-000000000001', 'Organizador Teste', 'organizador@example.com', '$2a$10$N9qo8uLOickgx2ZMRZo5ieU1rR1Xw1Z0qE1m7uJ9yO7aZ6uQ9vG6', 'ORGANIZER', now())
ON CONFLICT (email) DO NOTHING;

-- Insert CUSTOMER 1
INSERT INTO users (id, name, email, password_hash, role, created_at) VALUES
  ('20000000-0000-0000-0000-000000000001', 'Cliente João', 'cliente1@example.com', '$2a$10$N9qo8uLOickgx2ZMRZo5ieU1rR1Xw1Z0qE1m7uJ9yO7aZ6uQ9vG6', 'CUSTOMER', now())
ON CONFLICT (email) DO NOTHING;

-- Insert CUSTOMER 2
INSERT INTO users (id, name, email, password_hash, role, created_at) VALUES
  ('20000000-0000-0000-0000-000000000002', 'Cliente Maria', 'cliente2@example.com', '$2a$10$N9qo8uLOickgx2ZMRZo5ieU1rR1Xw1Z0qE1m7uJ9yO7aZ6uQ9vG6', 'CUSTOMER', now())
ON CONFLICT (email) DO NOTHING;

-- Insert PUBLISHED event with seats
INSERT INTO events (id, organizer_id, tmdb_movie_id, title, poster_url, synopsis, starts_at, venue, address, price, status, created_at) VALUES
  ('30000000-0000-0000-0000-000000000001', 
   '10000000-0000-0000-0000-000000000001', 
   12345, 
   'Show de Rock Festival', 
   'https://image.tmdb.org/t/p/w500/poster.jpg', 
   'Um espetáculo incrível de rock com as melhores bandas do país.', 
   now() + interval '7 days', 
   'Arena Music Hall', 
   'Av. Paulista, 1000 - São Paulo', 
   150.00, 
   'PUBLISHED', 
   now())
ON CONFLICT DO NOTHING;

-- Insert seats for the event
INSERT INTO seats (id, event_id, row_label, seat_number, category) VALUES
  ('40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'A', 1, 'VIP'),
  ('40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', 'A', 2, 'VIP'),
  ('40000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000001', 'A', 3, 'VIP'),
  ('40000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000001', 'B', 1, 'STANDARD'),
  ('40000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000001', 'B', 2, 'STANDARD'),
  ('40000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-000000000001', 'B', 3, 'STANDARD'),
  ('40000000-0000-0000-0000-000000000007', '30000000-0000-0000-0000-000000000001', 'C', 1, 'STANDARD'),
  ('40000000-0000-0000-0000-000000000008', '30000000-0000-0000-0000-000000000001', 'C', 2, 'STANDARD'),
  ('40000000-0000-0000-0000-000000000009', '30000000-0000-0000-0000-000000000001', 'C', 3, 'STANDARD'),
  ('40000000-0000-0000-0000-000000000010', '30000000-0000-0000-0000-000000000001', 'D', 1, 'ECONOMY')
ON CONFLICT DO NOTHING;
