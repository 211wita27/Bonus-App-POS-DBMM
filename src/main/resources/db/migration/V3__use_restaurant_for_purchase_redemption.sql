-- 1) Add restaurant_id columns
ALTER TABLE purchase ADD COLUMN restaurant_id BIGINT;
ALTER TABLE redemption ADD COLUMN restaurant_id BIGINT;

-- 2) Copy data from branch
UPDATE purchase
SET restaurant_id = (
    SELECT branch.restaurant_id
    FROM branch
    WHERE branch.id = purchase.branch_id
);

UPDATE redemption
SET restaurant_id = (
    SELECT branch.restaurant_id
    FROM branch
    WHERE branch.id = redemption.branch_id
);

-- 3) Set NOT NULL
ALTER TABLE purchase ALTER COLUMN restaurant_id SET NOT NULL;
ALTER TABLE redemption ALTER COLUMN restaurant_id SET NOT NULL;

-- 4) Add foreign keys to restaurant
ALTER TABLE purchase
    ADD CONSTRAINT fk_purchase_restaurant
    FOREIGN KEY (restaurant_id) REFERENCES restaurant (id);

ALTER TABLE redemption
    ADD CONSTRAINT fk_redemption_restaurant
    FOREIGN KEY (restaurant_id) REFERENCES restaurant (id);

-- 5) Add indexes
CREATE INDEX idx_purchase_restaurant ON purchase (restaurant_id);
CREATE INDEX idx_redemption_restaurant ON redemption (restaurant_id);

-- 6) Drop old foreign keys
-- (H2 removes the related indexes automatically)
ALTER TABLE purchase DROP CONSTRAINT fk_purchase_branch;
ALTER TABLE redemption DROP CONSTRAINT fk_redemption_branch;

-- 7) Drop old columns
ALTER TABLE purchase DROP COLUMN branch_id;
ALTER TABLE redemption DROP COLUMN branch_id;
