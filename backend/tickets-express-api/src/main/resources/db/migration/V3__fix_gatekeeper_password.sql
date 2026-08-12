-- V3: Update gatekeeper password_hash to a proper BCrypt hash for 'gatekeeper'
UPDATE users
SET password_hash = '$2a$10$aqT0zWYBc9q7Gc20sPHy4eWjWmvbhJlW5BfSJBDXL33qqrPQusD/m'
WHERE email = 'gatekeeper@example.com';
