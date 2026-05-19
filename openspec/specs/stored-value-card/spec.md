## ADDED Requirements

### Requirement: Admin can create stored-value card batch

管理员创建储值卡批次时，系统 SHALL 支持设置充值金额、赠送金额和有效天数。

#### Scenario: Create stored-value batch with bonus
- **WHEN** 管理员创建批次，选择分类为「储值卡」类型，设置充值金额 50 元、赠送金额 5 元、有效天数 365
- **THEN** 系统创建 `voucher_batch` 记录，voucher_type = `STORED_VALUE`, face_value = 50.00, bonus_value = 5.00, valid_days = 365

#### Scenario: Create stored-value batch without bonus
- **WHEN** 管理员创建储值卡批次，赠送金额为空或 0
- **THEN** 系统创建批次，bonus_value = 0，券初始余额等于 face_value

### Requirement: Stored-value card issuance sets initial balance

发放储值卡时，系统 SHALL 根据批次充值金额和赠送金额计算每张券的初始余额。

#### Scenario: Issue stored-value cards
- **WHEN** 管理员从一个充值 50 送 5 的储值卡批次向员工发放券
- **THEN** 系统创建 voucher 记录，initial_balance = 55.00，remaining_balance = 55.00，status = `ISSUED`

### Requirement: Stored-value card supports partial redemption

核销储值卡时，系统 SHALL 根据消费金额扣减余额，券状态保持 ISSUED 直到余额归零。

#### Scenario: Partial redemption deducts balance
- **WHEN** 操作员核销一张余额 55.00 的储值卡，输入消费金额 23.50
- **THEN** 系统将券的 remaining_balance 更新为 31.50，券状态保持 `ISSUED`
- **THEN** 核销结果返回 `deductAmount: 23.50`、`remainingBalance: 31.50`、`voucherType: STORED_VALUE`

#### Scenario: Full redemption exhausts the card
- **WHEN** 操作员核销一张余额 31.50 的储值卡，输入消费金额 31.50
- **THEN** 系统将 remaining_balance 更新为 0，status 更新为 `EXHAUSTED`
- **THEN** 核销结果返回 `deductAmount: 31.50`、`remainingBalance: 0`、`status: EXHAUSTED`

#### Scenario: Redemption amount exceeds balance fails
- **WHEN** 操作员核销一张余额 55.00 的储值卡，输入消费金额 60.00
- **THEN** 系统返回错误码 4005「余额不足，当前余额 55.00 元」

#### Scenario: Stored-value card requires order amount
- **WHEN** 操作员核销储值卡未提供消费金额
- **THEN** 系统返回错误码 4005「请输入消费金额」

### Requirement: Stored-value card consumption records

每次储值卡核销扣减，系统 SHALL 写入消费明细记录。

#### Scenario: Consumption record is created on redemption
- **WHEN** 操作员核销储值卡扣减 23.50
- **THEN** 系统在 `voucher_consumption` 表插入一条记录，包含 voucher_id、consume_amount = 23.50、balance_before = 55.00、balance_after = 31.50、operator_id、operator_name、created_at

### Requirement: Admin can view stored-value card consumption history

系统 SHALL 提供接口查询单张储值卡的消费明细，按时间倒序排列。

#### Scenario: Query consumption history
- **WHEN** 管理员调用 `GET /api/v1/vouchers/{id}/consumptions`
- **THEN** 系统返回该储值卡的所有消费记录列表，按 created_at 倒序，每条包含消费金额、消费前余额、消费后余额、操作员、时间

#### Scenario: UI displays consumption history page
- **WHEN** 管理员在储值卡详情点击"消费明细"
- **THEN** 进入消费明细页面，顶部展示卡片信息（初始余额、剩余余额、状态），下方为消费记录表格

### Requirement: EXHAUSTED cards cannot be redeemed

余额归零的储值卡 SHALL 不可再次核销。

#### Scenario: Redeem exhausted card fails
- **WHEN** 操作员尝试核销一张 status = `EXHAUSTED` 的储值卡
- **THEN** 系统返回错误码 4001「该券已用完」
