INSERT INTO app_user (email, password_hash, role, enabled, customer_id, restaurant_id, created_at, updated_at)
SELECT 'restaurant@restaurant',
       '$2a$10$Uxjx7FgGxJhgVl5ESGv.bO2CRlbXC987Xnkn9lpnO9PhyUv50XSXO',
       'RESTAURANT',
       TRUE,
       NULL,
       (SELECT id FROM restaurant WHERE code = 'DEMO'),
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM app_user WHERE email = 'restaurant@restaurant'
);
