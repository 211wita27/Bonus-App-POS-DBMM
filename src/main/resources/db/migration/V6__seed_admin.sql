INSERT INTO customer (first_name, last_name, email, username, password, status, role, created_at, updated_at)
SELECT 'Admin', 'Admin', 'admin@bonus.local', 'Admin', '123', 'ACTIVE', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM customer WHERE LOWER(username) = 'admin'
);
