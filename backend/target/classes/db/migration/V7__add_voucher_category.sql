-- V7: Add voucher category management
CREATE TABLE voucher_category (
    id BIGINT PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Add category_id column to voucher table
ALTER TABLE voucher ADD COLUMN category_id BIGINT DEFAULT NULL;
ALTER TABLE voucher ADD INDEX idx_category (category_id);

-- Insert default categories
INSERT INTO voucher_category (id, name, sort_order) VALUES (1, '餐饮类', 1);
INSERT INTO voucher_category (id, name, sort_order) VALUES (2, '购物类', 2);
INSERT INTO voucher_category (id, name, sort_order) VALUES (3, '娱乐类', 3);
INSERT INTO voucher_category (id, name, sort_order) VALUES (4, '储值类', 4);
