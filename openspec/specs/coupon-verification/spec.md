## Requirements

### Requirement: Verify fixed-amount coupon

核销满减优惠券时，系统 SHALL 直接按面额抵扣，返回优惠金额。

#### Scenario: Verify valid fixed-amount coupon
- **WHEN** 操作员扫描一张有效的满减 50 元优惠券
- **THEN** 系统将券状态更新为 `USED`，核销结果返回 `discountAmount: 50.00`、`voucherType: COUPON`

#### Scenario: Verify already used coupon
- **WHEN** 操作员扫描一张已核销的优惠券
- **THEN** 系统返回错误码 4001「该券已被核销」

### Requirement: Verify percentage coupon with order amount

核销折扣率优惠券时，系统 SHALL 根据订单金额和折扣率计算实际抵扣金额。

#### Scenario: Verify percentage coupon with sufficient order
- **WHEN** 操作员核销一张折扣率 15%、面额上限 100 元的券，输入订单金额 800 元
- **THEN** 系统计算抵扣金额 = 800 × 0.15 = 120，取 min(120, 100) = 100，核销结果返回 `discountAmount: 100.00`、`originalDiscount: 120.00`、`cappedByFaceValue: true`

#### Scenario: Verify percentage coupon with small order
- **WHEN** 操作员核销同一张券，输入订单金额 400 元
- **THEN** 系统计算抵扣金额 = 400 × 0.15 = 60，未触及上限，返回 `discountAmount: 60.00`、`cappedByFaceValue: false`

#### Scenario: Verify percentage coupon without order amount
- **WHEN** 操作员核销折扣率优惠券但未提供 orderAmount
- **THEN** 系统返回错误码 400「请输订单金额」

### Requirement: Verify coupon respects minimum order

核销优惠券时，系统 SHALL 校验订单金额是否满足最低消费门槛。

#### Scenario: Order below minimum threshold
- **WHEN** 操作员核销一张 min_order_amount = 200 元的满减券，订单金额 150 元
- **THEN** 系统返回错误码 400「订单金额未达到使用门槛（最低消费 200.00 元）」

#### Scenario: Order meets minimum threshold
- **WHEN** 操作员核销同一张券，订单金额 200 元
- **THEN** 系统正常核销，返回优惠金额

### Requirement: Verification result shows coupon info

优惠券核销成功后，返回结果 SHALL 包含券类型和优惠金额信息。

#### Scenario: Admin verification result for coupon
- **WHEN** 操作员成功核销一张优惠券
- **THEN** 核销结果包含 voucherType、discountAmount、voucherCode、holderName 字段
