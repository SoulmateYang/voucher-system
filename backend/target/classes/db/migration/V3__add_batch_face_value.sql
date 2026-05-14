-- V2 遗漏：voucher_batch 也需要 face_value 列存储百分比折扣券的面额上限
ALTER TABLE voucher_batch
    ADD COLUMN face_value DECIMAL(10,2) COMMENT '优惠券面额上限（百分比折扣券使用）';
