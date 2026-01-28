-- 1) Branch: add default_branch column (for Hibernate entity mapping)
ALTER TABLE branch
ADD COLUMN default_branch BOOLEAN NOT NULL DEFAULT FALSE;

-- Optional: keep exactly one default branch per restaurant (H2 compatible)
-- Sets the smallest branch id per restaurant as default_branch = TRUE
UPDATE branch b
SET default_branch = TRUE
WHERE b.id IN (
    SELECT MIN(b2.id)
    FROM branch b2
    GROUP BY b2.restaurant_id
);

-- 2) Customer: add role column (for admin/user login)
ALTER TABLE customer
ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- Optional: update existing admin user if present
UPDATE customer
SET role = 'ADMIN'
WHERE username = 'admin';
