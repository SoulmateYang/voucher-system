-- V8: Category-type binding + stored-value card support

-- 1. voucher_category: add voucher_type column
ALTER TABLE voucher_category ADD COLUMN voucher_type VARCHAR(32) NOT NULL DEFAULT 'COUPON' COMMENT '券类型: COUPON/RESOURCE_USAGE/STORED_VALUE';
-- Update existing default categories: 餐饮/购物/娱乐 → COUPON, 储值 → STORED_VALUE
UPDATE voucher_category SET voucher_type = 'STORED_VALUE' WHERE id = 4;

-- 2. voucher_batch: add category_id and bonus_value
ALTER TABLE voucher_batch ADD COLUMN category_id BIGINT DEFAULT NULL COMMENT '关联分类';
ALTER TABLE voucher_batch ADD COLUMN bonus_value DECIMAL(10,2) DEFAULT NULL COMMENT '储值卡赠送金额';
ALTER TABLE voucher_batch ADD INDEX idx_category (category_id);

-- 3. voucher: add balance fields for stored-value cards
ALTER TABLE voucher ADD COLUMN initial_balance DECIMAL(10,2) DEFAULT NULL COMMENT '初始余额(储值卡)';
ALTER TABLE voucher ADD COLUMN remaining_balance DECIMAL(10,2) DEFAULT NULL COMMENT '剩余余额(储值卡)';
-- Backfill: existing vouchers set initial/remaining balance from face_value
UPDATE voucher SET initial_balance = face_value, remaining_balance = face_value WHERE face_value IS NOT NULL;

-- 4. voucher_consumption: stored-value card deduction records
CREATE TABLE voucher_consumption (
    id BIGINT PRIMARY KEY,
    voucher_id BIGINT NOT NULL COMMENT '卡券ID',
    voucher_code VARCHAR(32) NOT NULL COMMENT '券码',
    consume_amount DECIMAL(10,2) NOT NULL COMMENT '本次消费金额',
    balance_before DECIMAL(10,2) NOT NULL COMMENT '消费前余额',
    balance_after DECIMAL(10,2) NOT NULL COMMENT '消费后余额',
    order_amount DECIMAL(10,2) DEFAULT NULL COMMENT '订单金额',
    operator_id VARCHAR(64) NOT NULL COMMENT '核销操作人ID',
    operator_name VARCHAR(64) COMMENT '核销操作人姓名',
    remark VARCHAR(255) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_voucher (voucher_id),
    INDEX idx_voucher_code (voucher_code),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='储值卡消费明细';
