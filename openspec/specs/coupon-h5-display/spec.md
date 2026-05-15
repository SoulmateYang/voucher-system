## Requirements

### Requirement: H5 coupon list displays coupon-specific info

员工在 H5 端查看券列表时，系统 SHALL 对优惠券展示面额、使用条件等特有信息。

#### Scenario: Coupon card shows face value
- **WHEN** 员工查看包含优惠券的券列表
- **THEN** 优惠券卡片展示面额（如「¥50」）作为主视觉元素，标签显示「优惠券」

#### Scenario: Coupon card shows usage condition
- **WHEN** 员工查看一张有最低消费要求的优惠券
- **THEN** 券卡片底部显示使用条件（如「满 ¥200 可用」）

#### Scenario: Resource voucher card unchanged
- **WHEN** 员工查看因私使用券
- **THEN** 券卡片显示资源描述，与现有样式一致

### Requirement: H5 coupon detail page shows full coupon info

员工查看优惠券详情时，系统 SHALL 展示完整的优惠信息。

#### Scenario: Fixed-amount coupon detail
- **WHEN** 员工查看一张满减 50 元优惠券详情
- **THEN** 页面显示券码、面额 ¥50.00、券类型「满减券」、有效期、使用条件「最低消费 ¥200.00」、券状态

#### Scenario: Percentage coupon detail
- **WHEN** 员工查看一张折扣率 15% 优惠券详情
- **THEN** 页面显示券码、折扣率「8.5折」、最高抵扣 ¥100.00、使用条件「最低消费 ¥500.00」、有效期、状态

### Requirement: H5 coupon visual distinction

优惠券与因私使用券在 H5 端 SHALL 有视觉差异。

#### Scenario: Coupon card uses distinct color scheme
- **WHEN** 员工查看券列表
- **THEN** 优惠券卡片使用 #fff7e6 背景 + #fa8c16 强调色（遵循 DESIGN.md Warning 色板），因私使用券使用默认卡片样式
