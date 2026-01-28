-- Demo dataset with one admin and a highlighted customer account

-- Ensure exactly one admin user and one demo login user
DELETE FROM app_user WHERE role = 'ADMIN';
DELETE FROM app_user WHERE LOWER(email) IN ('admin@bonusapp.local', 'admin@admin', 'max.muster@example.com', 'user@user');
DELETE FROM customer WHERE LOWER(email) IN ('admin@bonus.local', 'admin@admin', 'user@user');
DELETE FROM customer WHERE username = 'admin';
DELETE FROM customer WHERE role = 'ADMIN';

INSERT INTO customer (external_id, first_name, last_name, email, username, password, phone_number, status, role, created_at, updated_at)
SELECT 'ADM-001', 'Admin', 'Admin', 'admin@admin', 'admin',
       '$2a$10$6RZj5MAuSMYfT8lQcX4WlOx5zPJMvA9i2EHsunl5QJ/caPKnbmp6.',
       NULL, 'ACTIVE', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE email = 'admin@admin');

INSERT INTO customer (external_id, first_name, last_name, email, username, password, phone_number, status, role, created_at, updated_at)
SELECT 'CUST-100', 'Demo', 'User', 'user@user', 'user',
       '$2a$10$OccgmYAnzdQx9ppoP3SJeOHAkpP2CyMvvixCw/.uquBmLzpd1YVDO',
       '+43699111111', 'ACTIVE', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE email = 'user@user');

INSERT INTO app_user (email, password_hash, role, enabled, customer_id, restaurant_id, created_at, updated_at)
SELECT 'admin@admin',
       '$2a$10$6RZj5MAuSMYfT8lQcX4WlOx5zPJMvA9i2EHsunl5QJ/caPKnbmp6.',
       'ADMIN', TRUE,
       (SELECT id FROM customer WHERE email = 'admin@admin'),
       NULL,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'admin@admin');

INSERT INTO app_user (email, password_hash, role, enabled, customer_id, restaurant_id, created_at, updated_at)
SELECT 'user@user',
       '$2a$10$OccgmYAnzdQx9ppoP3SJeOHAkpP2CyMvvixCw/.uquBmLzpd1YVDO',
       'CUSTOMER', TRUE,
       (SELECT id FROM customer WHERE email = 'user@user'),
       (SELECT id FROM restaurant WHERE code = 'DEMO'),
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'user@user');

-- Restaurants
INSERT INTO restaurant (name, code, contact_email, contact_phone, default_currency, timezone, active, created_at, updated_at)
SELECT 'Bistro Central', 'BISTRO', 'hello@bistro.local', '+431111111', 'EUR', 'Europe/Vienna', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM restaurant WHERE code = 'BISTRO');

INSERT INTO restaurant (name, code, contact_email, contact_phone, default_currency, timezone, active, created_at, updated_at)
SELECT 'Pizza Porto', 'PIZZA', 'team@pizza.local', '+3901122233', 'EUR', 'Europe/Rome', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM restaurant WHERE code = 'PIZZA');

INSERT INTO restaurant (name, code, contact_email, contact_phone, default_currency, timezone, active, created_at, updated_at)
SELECT 'Sushi Harbor', 'SUSHI', 'hello@sushi.local', '+813123456', 'JPY', 'Asia/Tokyo', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM restaurant WHERE code = 'SUSHI');

INSERT INTO restaurant (name, code, contact_email, contact_phone, default_currency, timezone, active, created_at, updated_at)
SELECT 'Cafe Lumiere', 'CAFE', 'bonjour@cafe.local', '+331987654', 'EUR', 'Europe/Paris', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM restaurant WHERE code = 'CAFE');

INSERT INTO restaurant (name, code, contact_email, contact_phone, default_currency, timezone, active, created_at, updated_at)
SELECT 'Grill House', 'GRILL', 'hello@grill.local', '+493012345', 'EUR', 'Europe/Berlin', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM restaurant WHERE code = 'GRILL');

-- Branches
INSERT INTO branch (restaurant_id, branch_code, name, address_line, city, country, postal_code, default_branch)
SELECT (SELECT id FROM restaurant WHERE code = 'BISTRO'), 'HQ', 'Bistro Central HQ', 'Ringstrasse 1', 'Vienna', 'AT', '1010', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM branch
    WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'BISTRO') AND branch_code = 'HQ'
);

INSERT INTO branch (restaurant_id, branch_code, name, address_line, city, country, postal_code, default_branch)
SELECT (SELECT id FROM restaurant WHERE code = 'BISTRO'), 'WEST', 'Bistro West', 'Annenstrasse 5', 'Graz', 'AT', '8020', FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM branch
    WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'BISTRO') AND branch_code = 'WEST'
);

INSERT INTO branch (restaurant_id, branch_code, name, address_line, city, country, postal_code, default_branch)
SELECT (SELECT id FROM restaurant WHERE code = 'PIZZA'), 'HQ', 'Pizza Porto HQ', 'Via Roma 12', 'Rome', 'IT', '00100', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM branch
    WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'PIZZA') AND branch_code = 'HQ'
);

INSERT INTO branch (restaurant_id, branch_code, name, address_line, city, country, postal_code, default_branch)
SELECT (SELECT id FROM restaurant WHERE code = 'PIZZA'), 'EAST', 'Pizza Porto East', 'Corso Como 9', 'Milan', 'IT', '20154', FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM branch
    WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'PIZZA') AND branch_code = 'EAST'
);

INSERT INTO branch (restaurant_id, branch_code, name, address_line, city, country, postal_code, default_branch)
SELECT (SELECT id FROM restaurant WHERE code = 'SUSHI'), 'HQ', 'Sushi Harbor HQ', 'Chuo 3-4-5', 'Tokyo', 'JP', '104-0061', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM branch
    WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'SUSHI') AND branch_code = 'HQ'
);

INSERT INTO branch (restaurant_id, branch_code, name, address_line, city, country, postal_code, default_branch)
SELECT (SELECT id FROM restaurant WHERE code = 'SUSHI'), 'BERLIN', 'Sushi Harbor Berlin', 'Friedrichstrasse 77', 'Berlin', 'DE', '10117', FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM branch
    WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'SUSHI') AND branch_code = 'BERLIN'
);

INSERT INTO branch (restaurant_id, branch_code, name, address_line, city, country, postal_code, default_branch)
SELECT (SELECT id FROM restaurant WHERE code = 'CAFE'), 'HQ', 'Cafe Lumiere HQ', 'Rue de Rivoli 18', 'Paris', 'FR', '75001', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM branch
    WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'CAFE') AND branch_code = 'HQ'
);

INSERT INTO branch (restaurant_id, branch_code, name, address_line, city, country, postal_code, default_branch)
SELECT (SELECT id FROM restaurant WHERE code = 'GRILL'), 'HQ', 'Grill House HQ', 'Unter den Linden 9', 'Berlin', 'DE', '10117', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM branch
    WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'GRILL') AND branch_code = 'HQ'
);

INSERT INTO branch (restaurant_id, branch_code, name, address_line, city, country, postal_code, default_branch)
SELECT (SELECT id FROM restaurant WHERE code = 'GRILL'), 'MALL', 'Grill House Mall', 'Leopoldstrasse 20', 'Munich', 'DE', '80802', FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM branch
    WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'GRILL') AND branch_code = 'MALL'
);

-- Point rules
INSERT INTO point_rule (restaurant_id, name, description, rule_type, multiplier, amount_threshold, base_points, valid_from, valid_until, active)
SELECT (SELECT id FROM restaurant WHERE code = 'DEMO'), 'Default Points', '1 point per currency unit', 'MULTIPLIER', 1.00, 1.00, 0, DATE '2024-01-01', NULL, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM point_rule
    WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'DEMO') AND name = 'Default Points'
);

INSERT INTO point_rule (restaurant_id, name, description, rule_type, multiplier, amount_threshold, base_points, valid_from, valid_until, active)
SELECT (SELECT id FROM restaurant WHERE code = 'BISTRO'), 'Default Points', '1 point per euro', 'MULTIPLIER', 1.00, 1.00, 0, DATE '2024-01-01', NULL, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM point_rule
    WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'BISTRO') AND name = 'Default Points'
);

INSERT INTO point_rule (restaurant_id, name, description, rule_type, multiplier, amount_threshold, base_points, valid_from, valid_until, active)
SELECT (SELECT id FROM restaurant WHERE code = 'PIZZA'), 'Default Points', '1 point per euro', 'MULTIPLIER', 1.00, 1.00, 0, DATE '2024-01-01', NULL, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM point_rule
    WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'PIZZA') AND name = 'Default Points'
);

INSERT INTO point_rule (restaurant_id, name, description, rule_type, multiplier, amount_threshold, base_points, valid_from, valid_until, active)
SELECT (SELECT id FROM restaurant WHERE code = 'SUSHI'), 'Default Points', '1 point per yen', 'MULTIPLIER', 1.00, 1.00, 0, DATE '2024-01-01', NULL, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM point_rule
    WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'SUSHI') AND name = 'Default Points'
);

INSERT INTO point_rule (restaurant_id, name, description, rule_type, multiplier, amount_threshold, base_points, valid_from, valid_until, active)
SELECT (SELECT id FROM restaurant WHERE code = 'CAFE'), 'Default Points', '1 point per euro', 'MULTIPLIER', 1.00, 1.00, 0, DATE '2024-01-01', NULL, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM point_rule
    WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'CAFE') AND name = 'Default Points'
);

INSERT INTO point_rule (restaurant_id, name, description, rule_type, multiplier, amount_threshold, base_points, valid_from, valid_until, active)
SELECT (SELECT id FROM restaurant WHERE code = 'GRILL'), 'Default Points', '1 point per euro', 'MULTIPLIER', 1.00, 1.00, 0, DATE '2024-01-01', NULL, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM point_rule
    WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'GRILL') AND name = 'Default Points'
);

-- Rewards
INSERT INTO reward (restaurant_id, reward_code, name, description, reward_type, cost_points, valid_from, valid_until, active)
SELECT (SELECT id FROM restaurant WHERE code = 'DEMO'), 'DESSERT', 'Dessert', 'Sweet treat', 'PRODUCT', 70, CURRENT_DATE, NULL, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM reward
    WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'DEMO') AND reward_code = 'DESSERT'
);

INSERT INTO reward (restaurant_id, reward_code, name, description, reward_type, cost_points, valid_from, valid_until, active)
SELECT (SELECT id FROM restaurant WHERE code = 'BISTRO'), 'FREE-APP', 'Free Appetizer', 'Starter of the day', 'PRODUCT', 60, CURRENT_DATE, NULL, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM reward
    WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'BISTRO') AND reward_code = 'FREE-APP'
);

INSERT INTO reward (restaurant_id, reward_code, name, description, reward_type, cost_points, valid_from, valid_until, active)
SELECT (SELECT id FROM restaurant WHERE code = 'BISTRO'), 'WINE-5', 'Wine Tasting', 'Five euro wine voucher', 'VOUCHER', 120, CURRENT_DATE, NULL, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM reward
    WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'BISTRO') AND reward_code = 'WINE-5'
);

INSERT INTO reward (restaurant_id, reward_code, name, description, reward_type, cost_points, valid_from, valid_until, active)
SELECT (SELECT id FROM restaurant WHERE code = 'PIZZA'), 'SLICE', 'Extra Slice', 'One extra slice', 'PRODUCT', 40, CURRENT_DATE, NULL, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM reward
    WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'PIZZA') AND reward_code = 'SLICE'
);

INSERT INTO reward (restaurant_id, reward_code, name, description, reward_type, cost_points, valid_from, valid_until, active)
SELECT (SELECT id FROM restaurant WHERE code = 'PIZZA'), 'PIZZA-10', 'Pizza Voucher', 'Ten euro voucher', 'VOUCHER', 150, CURRENT_DATE, NULL, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM reward
    WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'PIZZA') AND reward_code = 'PIZZA-10'
);

INSERT INTO reward (restaurant_id, reward_code, name, description, reward_type, cost_points, valid_from, valid_until, active)
SELECT (SELECT id FROM restaurant WHERE code = 'SUSHI'), 'MAKI', 'Maki Set', '6-piece maki', 'PRODUCT', 90, CURRENT_DATE, NULL, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM reward
    WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'SUSHI') AND reward_code = 'MAKI'
);

INSERT INTO reward (restaurant_id, reward_code, name, description, reward_type, cost_points, valid_from, valid_until, active)
SELECT (SELECT id FROM restaurant WHERE code = 'SUSHI'), 'MATCHA', 'Matcha Drink', 'Ceremonial matcha', 'PRODUCT', 70, CURRENT_DATE, NULL, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM reward
    WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'SUSHI') AND reward_code = 'MATCHA'
);

INSERT INTO reward (restaurant_id, reward_code, name, description, reward_type, cost_points, valid_from, valid_until, active)
SELECT (SELECT id FROM restaurant WHERE code = 'CAFE'), 'FREE-COFFEE', 'Free Coffee', 'Barista coffee', 'PRODUCT', 30, CURRENT_DATE, NULL, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM reward
    WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'CAFE') AND reward_code = 'FREE-COFFEE'
);

INSERT INTO reward (restaurant_id, reward_code, name, description, reward_type, cost_points, valid_from, valid_until, active)
SELECT (SELECT id FROM restaurant WHERE code = 'CAFE'), 'BRUNCH', 'Brunch Upgrade', 'Weekend brunch bonus', 'PRODUCT', 110, CURRENT_DATE, NULL, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM reward
    WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'CAFE') AND reward_code = 'BRUNCH'
);

INSERT INTO reward (restaurant_id, reward_code, name, description, reward_type, cost_points, valid_from, valid_until, active)
SELECT (SELECT id FROM restaurant WHERE code = 'GRILL'), 'BBQ', 'BBQ Plate', 'Smoked BBQ plate', 'PRODUCT', 140, CURRENT_DATE, NULL, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM reward
    WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'GRILL') AND reward_code = 'BBQ'
);

INSERT INTO reward (restaurant_id, reward_code, name, description, reward_type, cost_points, valid_from, valid_until, active)
SELECT (SELECT id FROM restaurant WHERE code = 'GRILL'), 'GRILL-10', 'Grill Voucher', 'Ten euro voucher', 'VOUCHER', 160, CURRENT_DATE, NULL, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM reward
    WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'GRILL') AND reward_code = 'GRILL-10'
);

-- Additional customers
INSERT INTO customer (external_id, first_name, last_name, email, username, password, phone_number, status, role, created_at, updated_at)
SELECT 'CUST-101', 'Lena', 'Huber', 'lena.huber@example.com', 'lena',
       '$2a$10$OccgmYAnzdQx9ppoP3SJeOHAkpP2CyMvvixCw/.uquBmLzpd1YVDO',
       '+43699123456', 'ACTIVE', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE email = 'lena.huber@example.com');

INSERT INTO customer (external_id, first_name, last_name, email, username, password, phone_number, status, role, created_at, updated_at)
SELECT 'CUST-102', 'Samuel', 'Klein', 'samuel.klein@example.com', 'samuel',
       '$2a$10$OccgmYAnzdQx9ppoP3SJeOHAkpP2CyMvvixCw/.uquBmLzpd1YVDO',
       '+43699234567', 'ACTIVE', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE email = 'samuel.klein@example.com');

INSERT INTO customer (external_id, first_name, last_name, email, username, password, phone_number, status, role, created_at, updated_at)
SELECT 'CUST-103', 'Fatima', 'Ali', 'fatima.ali@example.com', 'fatima',
       '$2a$10$OccgmYAnzdQx9ppoP3SJeOHAkpP2CyMvvixCw/.uquBmLzpd1YVDO',
       '+43699345678', 'ACTIVE', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE email = 'fatima.ali@example.com');

INSERT INTO customer (external_id, first_name, last_name, email, username, password, phone_number, status, role, created_at, updated_at)
SELECT 'CUST-104', 'Noah', 'Gruber', 'noah.gruber@example.com', 'noah',
       '$2a$10$OccgmYAnzdQx9ppoP3SJeOHAkpP2CyMvvixCw/.uquBmLzpd1YVDO',
       '+43699456789', 'ACTIVE', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE email = 'noah.gruber@example.com');

INSERT INTO customer (external_id, first_name, last_name, email, username, password, phone_number, status, role, created_at, updated_at)
SELECT 'CUST-105', 'Mia', 'Weber', 'mia.weber@example.com', 'mia',
       '$2a$10$OccgmYAnzdQx9ppoP3SJeOHAkpP2CyMvvixCw/.uquBmLzpd1YVDO',
       '+43699567890', 'ACTIVE', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE email = 'mia.weber@example.com');

INSERT INTO customer (external_id, first_name, last_name, email, username, password, phone_number, status, role, created_at, updated_at)
SELECT 'CUST-106', 'Leo', 'Maier', 'leo.maier@example.com', 'leo',
       '$2a$10$OccgmYAnzdQx9ppoP3SJeOHAkpP2CyMvvixCw/.uquBmLzpd1YVDO',
       '+43699678901', 'ACTIVE', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE email = 'leo.maier@example.com');

INSERT INTO customer (external_id, first_name, last_name, email, username, password, phone_number, status, role, created_at, updated_at)
SELECT 'CUST-107', 'Eva', 'Lang', 'eva.lang@example.com', 'eva',
       '$2a$10$OccgmYAnzdQx9ppoP3SJeOHAkpP2CyMvvixCw/.uquBmLzpd1YVDO',
       '+43699789012', 'ACTIVE', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE email = 'eva.lang@example.com');

-- Loyalty accounts
INSERT INTO loyalty_account (account_number, customer_id, restaurant_id, status, tier, current_points, created_at, updated_at)
SELECT 'ACCT-U001', (SELECT id FROM customer WHERE email = 'user@user'), (SELECT id FROM restaurant WHERE code = 'DEMO'),
       'ACTIVE', 'STANDARD', 130, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM loyalty_account
    WHERE customer_id = (SELECT id FROM customer WHERE email = 'user@user')
      AND restaurant_id = (SELECT id FROM restaurant WHERE code = 'DEMO')
);

INSERT INTO loyalty_account (account_number, customer_id, restaurant_id, status, tier, current_points, created_at, updated_at)
SELECT 'ACCT-U002', (SELECT id FROM customer WHERE email = 'user@user'), (SELECT id FROM restaurant WHERE code = 'SUSHI'),
       'ACTIVE', 'STANDARD', 80, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM loyalty_account
    WHERE customer_id = (SELECT id FROM customer WHERE email = 'user@user')
      AND restaurant_id = (SELECT id FROM restaurant WHERE code = 'SUSHI')
);

INSERT INTO loyalty_account (account_number, customer_id, restaurant_id, status, tier, current_points, created_at, updated_at)
SELECT 'ACCT-101', (SELECT id FROM customer WHERE email = 'lena.huber@example.com'), (SELECT id FROM restaurant WHERE code = 'BISTRO'),
       'ACTIVE', 'STANDARD', 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM loyalty_account
    WHERE customer_id = (SELECT id FROM customer WHERE email = 'lena.huber@example.com')
      AND restaurant_id = (SELECT id FROM restaurant WHERE code = 'BISTRO')
);

INSERT INTO loyalty_account (account_number, customer_id, restaurant_id, status, tier, current_points, created_at, updated_at)
SELECT 'ACCT-102', (SELECT id FROM customer WHERE email = 'samuel.klein@example.com'), (SELECT id FROM restaurant WHERE code = 'PIZZA'),
       'ACTIVE', 'STANDARD', 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM loyalty_account
    WHERE customer_id = (SELECT id FROM customer WHERE email = 'samuel.klein@example.com')
      AND restaurant_id = (SELECT id FROM restaurant WHERE code = 'PIZZA')
);

INSERT INTO loyalty_account (account_number, customer_id, restaurant_id, status, tier, current_points, created_at, updated_at)
SELECT 'ACCT-103', (SELECT id FROM customer WHERE email = 'fatima.ali@example.com'), (SELECT id FROM restaurant WHERE code = 'CAFE'),
       'ACTIVE', 'STANDARD', 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM loyalty_account
    WHERE customer_id = (SELECT id FROM customer WHERE email = 'fatima.ali@example.com')
      AND restaurant_id = (SELECT id FROM restaurant WHERE code = 'CAFE')
);

INSERT INTO loyalty_account (account_number, customer_id, restaurant_id, status, tier, current_points, created_at, updated_at)
SELECT 'ACCT-104', (SELECT id FROM customer WHERE email = 'noah.gruber@example.com'), (SELECT id FROM restaurant WHERE code = 'GRILL'),
       'ACTIVE', 'STANDARD', 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM loyalty_account
    WHERE customer_id = (SELECT id FROM customer WHERE email = 'noah.gruber@example.com')
      AND restaurant_id = (SELECT id FROM restaurant WHERE code = 'GRILL')
);

INSERT INTO loyalty_account (account_number, customer_id, restaurant_id, status, tier, current_points, created_at, updated_at)
SELECT 'ACCT-105', (SELECT id FROM customer WHERE email = 'mia.weber@example.com'), (SELECT id FROM restaurant WHERE code = 'DEMO'),
       'ACTIVE', 'STANDARD', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM loyalty_account
    WHERE customer_id = (SELECT id FROM customer WHERE email = 'mia.weber@example.com')
      AND restaurant_id = (SELECT id FROM restaurant WHERE code = 'DEMO')
);

INSERT INTO loyalty_account (account_number, customer_id, restaurant_id, status, tier, current_points, created_at, updated_at)
SELECT 'ACCT-106', (SELECT id FROM customer WHERE email = 'leo.maier@example.com'), (SELECT id FROM restaurant WHERE code = 'SUSHI'),
       'ACTIVE', 'STANDARD', 120, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM loyalty_account
    WHERE customer_id = (SELECT id FROM customer WHERE email = 'leo.maier@example.com')
      AND restaurant_id = (SELECT id FROM restaurant WHERE code = 'SUSHI')
);

INSERT INTO loyalty_account (account_number, customer_id, restaurant_id, status, tier, current_points, created_at, updated_at)
SELECT 'ACCT-107', (SELECT id FROM customer WHERE email = 'eva.lang@example.com'), (SELECT id FROM restaurant WHERE code = 'BISTRO'),
       'ACTIVE', 'STANDARD', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM loyalty_account
    WHERE customer_id = (SELECT id FROM customer WHERE email = 'eva.lang@example.com')
      AND restaurant_id = (SELECT id FROM restaurant WHERE code = 'BISTRO')
);

-- Purchases
INSERT INTO purchase (loyalty_account_id, restaurant_id, purchase_number, total_amount, currency, purchased_at, notes)
SELECT (SELECT id FROM loyalty_account WHERE account_number = 'ACCT-U001'), (SELECT id FROM restaurant WHERE code = 'DEMO'),
       'PUR-DEMO-1001', 120.00, 'EUR', CURRENT_TIMESTAMP, 'Lunch order'
WHERE NOT EXISTS (SELECT 1 FROM purchase WHERE purchase_number = 'PUR-DEMO-1001');

INSERT INTO purchase (loyalty_account_id, restaurant_id, purchase_number, total_amount, currency, purchased_at, notes)
SELECT (SELECT id FROM loyalty_account WHERE account_number = 'ACCT-U001'), (SELECT id FROM restaurant WHERE code = 'DEMO'),
       'PUR-DEMO-1002', 60.00, 'EUR', CURRENT_TIMESTAMP, 'Dinner order'
WHERE NOT EXISTS (SELECT 1 FROM purchase WHERE purchase_number = 'PUR-DEMO-1002');

INSERT INTO purchase (loyalty_account_id, restaurant_id, purchase_number, total_amount, currency, purchased_at, notes)
SELECT (SELECT id FROM loyalty_account WHERE account_number = 'ACCT-U002'), (SELECT id FROM restaurant WHERE code = 'SUSHI'),
       'PUR-SUSHI-2001', 80.00, 'JPY', CURRENT_TIMESTAMP, 'Takeaway'
WHERE NOT EXISTS (SELECT 1 FROM purchase WHERE purchase_number = 'PUR-SUSHI-2001');

INSERT INTO purchase (loyalty_account_id, restaurant_id, purchase_number, total_amount, currency, purchased_at, notes)
SELECT (SELECT id FROM loyalty_account WHERE account_number = 'ACCT-103'), (SELECT id FROM restaurant WHERE code = 'CAFE'),
       'PUR-CAFE-3001', 45.00, 'EUR', CURRENT_TIMESTAMP, 'Brunch'
WHERE NOT EXISTS (SELECT 1 FROM purchase WHERE purchase_number = 'PUR-CAFE-3001');

INSERT INTO purchase (loyalty_account_id, restaurant_id, purchase_number, total_amount, currency, purchased_at, notes)
SELECT (SELECT id FROM loyalty_account WHERE account_number = 'ACCT-106'), (SELECT id FROM restaurant WHERE code = 'SUSHI'),
       'PUR-SUSHI-2002', 120.00, 'JPY', CURRENT_TIMESTAMP, 'Chef special'
WHERE NOT EXISTS (SELECT 1 FROM purchase WHERE purchase_number = 'PUR-SUSHI-2002');

-- Point ledger entries
INSERT INTO point_ledger (loyalty_account_id, entry_type, points, balance_after, occurred_at, description, purchase_id, point_rule_id)
SELECT (SELECT id FROM loyalty_account WHERE account_number = 'ACCT-U001'), 'EARN', 120, 120, CURRENT_TIMESTAMP, 'Earned on purchase',
       (SELECT id FROM purchase WHERE purchase_number = 'PUR-DEMO-1001'),
       (SELECT id FROM point_rule WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'DEMO') AND name = 'Default Points')
WHERE NOT EXISTS (
    SELECT 1 FROM point_ledger
    WHERE loyalty_account_id = (SELECT id FROM loyalty_account WHERE account_number = 'ACCT-U001')
      AND description = 'Earned on purchase'
      AND purchase_id = (SELECT id FROM purchase WHERE purchase_number = 'PUR-DEMO-1001')
);

INSERT INTO point_ledger (loyalty_account_id, entry_type, points, balance_after, occurred_at, description, purchase_id, point_rule_id)
SELECT (SELECT id FROM loyalty_account WHERE account_number = 'ACCT-U001'), 'REDEEM', -50, 70, CURRENT_TIMESTAMP, 'Reward redemption',
       NULL, NULL
WHERE NOT EXISTS (
    SELECT 1 FROM point_ledger
    WHERE loyalty_account_id = (SELECT id FROM loyalty_account WHERE account_number = 'ACCT-U001')
      AND description = 'Reward redemption'
      AND balance_after = 70
);

INSERT INTO point_ledger (loyalty_account_id, entry_type, points, balance_after, occurred_at, description, purchase_id, point_rule_id)
SELECT (SELECT id FROM loyalty_account WHERE account_number = 'ACCT-U001'), 'EARN', 60, 130, CURRENT_TIMESTAMP, 'Earned on purchase',
       (SELECT id FROM purchase WHERE purchase_number = 'PUR-DEMO-1002'),
       (SELECT id FROM point_rule WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'DEMO') AND name = 'Default Points')
WHERE NOT EXISTS (
    SELECT 1 FROM point_ledger
    WHERE loyalty_account_id = (SELECT id FROM loyalty_account WHERE account_number = 'ACCT-U001')
      AND description = 'Earned on purchase'
      AND purchase_id = (SELECT id FROM purchase WHERE purchase_number = 'PUR-DEMO-1002')
);

INSERT INTO point_ledger (loyalty_account_id, entry_type, points, balance_after, occurred_at, description, purchase_id, point_rule_id)
SELECT (SELECT id FROM loyalty_account WHERE account_number = 'ACCT-U002'), 'EARN', 80, 80, CURRENT_TIMESTAMP, 'Earned on purchase',
       (SELECT id FROM purchase WHERE purchase_number = 'PUR-SUSHI-2001'),
       (SELECT id FROM point_rule WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'SUSHI') AND name = 'Default Points')
WHERE NOT EXISTS (
    SELECT 1 FROM point_ledger
    WHERE loyalty_account_id = (SELECT id FROM loyalty_account WHERE account_number = 'ACCT-U002')
      AND purchase_id = (SELECT id FROM purchase WHERE purchase_number = 'PUR-SUSHI-2001')
);

INSERT INTO point_ledger (loyalty_account_id, entry_type, points, balance_after, occurred_at, description, purchase_id, point_rule_id)
SELECT (SELECT id FROM loyalty_account WHERE account_number = 'ACCT-103'), 'EARN', 45, 45, CURRENT_TIMESTAMP, 'Earned on purchase',
       (SELECT id FROM purchase WHERE purchase_number = 'PUR-CAFE-3001'),
       (SELECT id FROM point_rule WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'CAFE') AND name = 'Default Points')
WHERE NOT EXISTS (
    SELECT 1 FROM point_ledger
    WHERE loyalty_account_id = (SELECT id FROM loyalty_account WHERE account_number = 'ACCT-103')
      AND purchase_id = (SELECT id FROM purchase WHERE purchase_number = 'PUR-CAFE-3001')
);

INSERT INTO point_ledger (loyalty_account_id, entry_type, points, balance_after, occurred_at, description, purchase_id, point_rule_id)
SELECT (SELECT id FROM loyalty_account WHERE account_number = 'ACCT-103'), 'REDEEM', -30, 15, CURRENT_TIMESTAMP, 'Reward redemption',
       NULL, NULL
WHERE NOT EXISTS (
    SELECT 1 FROM point_ledger
    WHERE loyalty_account_id = (SELECT id FROM loyalty_account WHERE account_number = 'ACCT-103')
      AND description = 'Reward redemption'
      AND balance_after = 15
);

INSERT INTO point_ledger (loyalty_account_id, entry_type, points, balance_after, occurred_at, description, purchase_id, point_rule_id)
SELECT (SELECT id FROM loyalty_account WHERE account_number = 'ACCT-106'), 'EARN', 120, 120, CURRENT_TIMESTAMP, 'Earned on purchase',
       (SELECT id FROM purchase WHERE purchase_number = 'PUR-SUSHI-2002'),
       (SELECT id FROM point_rule WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'SUSHI') AND name = 'Default Points')
WHERE NOT EXISTS (
    SELECT 1 FROM point_ledger
    WHERE loyalty_account_id = (SELECT id FROM loyalty_account WHERE account_number = 'ACCT-106')
      AND purchase_id = (SELECT id FROM purchase WHERE purchase_number = 'PUR-SUSHI-2002')
);

-- Redemptions
INSERT INTO redemption (loyalty_account_id, reward_id, restaurant_id, ledger_entry_id, status, redeemed_at, notes, points_spent, redemption_code)
SELECT (SELECT id FROM loyalty_account WHERE account_number = 'ACCT-U001'),
       (SELECT id FROM reward WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'DEMO') AND reward_code = 'WELCOME-DRINK'),
       (SELECT id FROM restaurant WHERE code = 'DEMO'),
       (SELECT id FROM point_ledger WHERE loyalty_account_id = (SELECT id FROM loyalty_account WHERE account_number = 'ACCT-U001') AND balance_after = 70 AND entry_type = 'REDEEM'),
       'COMPLETED', CURRENT_TIMESTAMP, 'Welcome drink redeemed', 50, 'RDUSER001'
WHERE NOT EXISTS (SELECT 1 FROM redemption WHERE redemption_code = 'RDUSER001');

INSERT INTO redemption (loyalty_account_id, reward_id, restaurant_id, ledger_entry_id, status, redeemed_at, notes, points_spent, redemption_code)
SELECT (SELECT id FROM loyalty_account WHERE account_number = 'ACCT-103'),
       (SELECT id FROM reward WHERE restaurant_id = (SELECT id FROM restaurant WHERE code = 'CAFE') AND reward_code = 'FREE-COFFEE'),
       (SELECT id FROM restaurant WHERE code = 'CAFE'),
       (SELECT id FROM point_ledger WHERE loyalty_account_id = (SELECT id FROM loyalty_account WHERE account_number = 'ACCT-103') AND balance_after = 15 AND entry_type = 'REDEEM'),
       'COMPLETED', CURRENT_TIMESTAMP, 'Coffee redemption', 30, 'RDCAFE01'
WHERE NOT EXISTS (SELECT 1 FROM redemption WHERE redemption_code = 'RDCAFE01');
