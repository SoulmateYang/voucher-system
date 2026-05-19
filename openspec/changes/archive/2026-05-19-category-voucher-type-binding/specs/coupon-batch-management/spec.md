## MODIFIED Requirements

### Requirement: Admin can create coupon batch

管理员创建批次时，系统 SHALL 要求选择分类而不是直接传券类型。券类型由所选分类的 `voucherType` 自动确定。

#### Scenario: Create coupon batch via category
- **WHEN** 管理员创建批次，选择分类为「餐饮类」（voucherType = COUPON），折扣类型为「满减」，填写优惠金额 50 元、最低订单金额 200 元
- **THEN** 系统创建 `voucher_batch` 记录，voucher_type = `COUPON`（从分类同步）, category_id = 餐饮类ID, discount_type = `FIXED_AMOUNT`, discount_value = 50.00, min_order_amount = 200.00

#### Scenario: Create percentage coupon batch via category
- **WHEN** 管理员创建批次，选择分类为「购物类」（voucherType = COUPON），折扣类型为「折扣率」，填写折扣率 15%、最高抵扣 100 元、最低订单金额 500 元
- **THEN** 系统创建 `voucher_batch` 记录，voucher_type = `COUPON`, discount_type = `PERCENTAGE`, discount_value = 15.00, face_value = 100.00, min_order_amount = 500.00

#### Scenario: Create resource usage batch via category
- **WHEN** 管理员创建批次，选择分类为「因公出行」（voucherType = RESOURCE_USAGE）
- **THEN** 系统创建 `voucher_batch` 记录，voucher_type = `RESOURCE_USAGE`，折扣相关字段为 NULL

#### Scenario: Create stored-value batch via category
- **WHEN** 管理员创建批次，选择分类为「储值卡」（voucherType = STORED_VALUE），设置充值金额 50、赠送金额 5
- **THEN** 系统创建 `voucher_batch` 记录，voucher_type = `STORED_VALUE`, face_value = 50.00, bonus_value = 5.00

#### Scenario: Create batch without category fails
- **WHEN** 管理员创建批次未传入 `categoryId`
- **THEN** 系统返回 400 错误，提示"分类不能为空"

## ADDED Requirements

### Requirement: Batch creation form shows category-selector with type hint

前端批次创建表单 SHALL 先选分类，根据分类的 `voucherType` 条件显示对应的表单字段。

#### Scenario: Show coupon fields when category type is COUPON
- **WHEN** 管理员选择 voucherType 为 COUPON 的分类
- **THEN** 显示折扣类型、折扣值、最低消费等字段

#### Scenario: Show stored-value fields when category type is STORED_VALUE
- **WHEN** 管理员选择 voucherType 为 STORED_VALUE 的分类
- **THEN** 显示充值金额、赠送金额字段，隐藏折扣字段

#### Scenario: Show minimal fields when category type is RESOURCE_USAGE
- **WHEN** 管理员选择 voucherType 为 RESOURCE_USAGE 的分类
- **THEN** 仅显示资源描述字段，隐藏折扣和储值字段
