## ADDED Requirements

### Requirement: Admin can create coupon batch

管理员创建优惠券批次时，系统 SHALL 支持选择券类型为 `COUPON`，并配置折扣规则。

#### Scenario: Create fixed-amount coupon batch
- **WHEN** 管理员创建批次，选择券类型为「优惠券」，折扣类型为「满减」，填写优惠金额 50 元、最低订单金额 200 元
- **THEN** 系统创建 `voucher_batch` 记录，voucher_type = `COUPON`, discount_type = `FIXED_AMOUNT`, discount_value = 50.00, min_order_amount = 200.00

#### Scenario: Create percentage coupon batch
- **WHEN** 管理员创建批次，选择券类型为「优惠券」，折扣类型为「折扣率」，填写折扣率 15%、最高抵扣 100 元、最低订单金额 500 元
- **THEN** 系统创建 `voucher_batch` 记录，voucher_type = `COUPON`, discount_type = `PERCENTAGE`, discount_value = 15.00, face_value = 100.00, min_order_amount = 500.00

#### Scenario: Create resource usage batch (unchanged)
- **WHEN** 管理员创建批次，选择券类型为「因私使用」
- **THEN** 系统创建 `voucher_batch` 记录，voucher_type = `RESOURCE_USAGE`，折扣相关字段为 NULL

### Requirement: Coupon issuance inherits batch discount rules

批量发放优惠券时，系统 SHALL 根据批次折扣规则自动计算每张券的面额。

#### Scenario: Issue fixed-amount coupons
- **WHEN** 管理员从一个满减 50 元的优惠券批次向 10 名员工发放券
- **THEN** 系统创建 10 条 `voucher` 记录，每条 face_value = 50.00，status = `ISSUED`

#### Scenario: Issue percentage coupons with face value cap
- **WHEN** 管理员从一个折扣率 15%、面额上限 100 元的批次发放券
- **THEN** 系统创建 voucher 记录，face_value = 100.00（取批次配置的 face_value 上限）

### Requirement: Admin can view coupons in batch list

批次列表 SHALL 显示券类型标识，区分优惠券与资源券。

#### Scenario: Batch list shows voucher type
- **WHEN** 管理员查看批次列表
- **THEN** 每条批次记录显示券类型标签（「因私使用」为蓝色、「优惠券」为橙色），优惠券额外显示折扣规则摘要
