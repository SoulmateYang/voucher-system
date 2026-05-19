## ADDED Requirements

### Requirement: Verify stored-value card with partial deduction

核销储值卡时，系统 SHALL 根据消费金额扣减余额，写入消费明细记录，余额归零时自动标记 EXHAUSTED。

#### Scenario: Partial redemption succeeds
- **WHEN** 操作员核销一张余额 55.00 的储值卡，输入消费金额 23.50
- **THEN** 系统将 remaining_balance 更新为 31.50，status 保持 `ISSUED`
- **THEN** 核销结果返回 `deductAmount: 23.50`、`remainingBalance: 31.50`、`voucherType: STORED_VALUE`、`status: ISSUED`

#### Scenario: Full redemption exhausts the card
- **WHEN** 操作员核销一张余额 31.50 的储值卡，输入消费金额 31.50
- **THEN** 系统将 remaining_balance 更新为 0，status 更新为 `EXHAUSTED`
- **THEN** 核销结果返回 `deductAmount: 31.50`、`remainingBalance: 0`、`status: EXHAUSTED`

#### Scenario: Redemption amount exceeds balance
- **WHEN** 操作员核销一张余额 55.00 的储值卡，输入消费金额 60.00
- **THEN** 系统返回错误码 4005「余额不足，当前余额 55.00 元」

#### Scenario: Stored-value card requires order amount
- **WHEN** 操作员核销储值卡未提供 orderAmount
- **THEN** 系统返回错误码 4005「请输入消费金额」

#### Scenario: Verify EXHAUSTED card fails
- **WHEN** 操作员尝试核销一张 status = `EXHAUSTED` 的储值卡
- **THEN** 系统返回错误码 4001「该券已用完」

### Requirement: Verification result distinguishes stored-value from coupon

核销结果 SHALL 根据券类型返回不同的字段。

#### Scenario: Stored-value verification result
- **WHEN** 操作员成功核销储值卡
- **THEN** 核销结果包含 voucherType = `STORED_VALUE`、deductAmount、remainingBalance、status、voucherCode、holderName，不包含 discountAmount

#### Scenario: Coupon verification result (unchanged)
- **WHEN** 操作员成功核销优惠券
- **THEN** 核销结果包含 voucherType = `COUPON`、discountAmount、voucherCode、holderName，不包含 remainingBalance

## MODIFIED Requirements

### Requirement: Verify coupon respects minimum order

核销优惠券时，系统 SHALL 校验订单金额是否满足最低消费门槛。储值卡 SHALL NOT 校验最低消费。

#### Scenario: Order below minimum threshold (coupon)
- **WHEN** 操作员核销一张 min_order_amount = 200 元的满减券，订单金额 150 元
- **THEN** 系统返回错误码 4005「订单金额未达到使用门槛（最低消费 200.00 元）」

#### Scenario: Order meets minimum threshold (coupon)
- **WHEN** 操作员核销同一张券，订单金额 200 元
- **THEN** 系统正常核销，返回优惠金额

#### Scenario: Stored-value card ignores minimum order
- **WHEN** 操作员核销储值卡，批次设置了 min_order_amount
- **THEN** 系统忽略该门槛，基于余额直接扣减
