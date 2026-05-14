-- 优惠券折扣规则 (批次级)
ALTER TABLE voucher_batch
    ADD COLUMN discount_type VARCHAR(16) COMMENT 'FIXED_AMOUNT/PERCENTAGE',
    ADD COLUMN discount_value DECIMAL(10,2) COMMENT '折扣金额或折扣率',
    ADD COLUMN min_order_amount DECIMAL(10,2) COMMENT '最低订单金额门槛';

-- 券实例面额
ALTER TABLE voucher
    ADD COLUMN face_value DECIMAL(10,2) COMMENT '优惠券面额/最高抵扣额';
