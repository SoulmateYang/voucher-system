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

### Requirement: Scheduled task restores expired GIFTING vouchers

定时任务 SHALL 自动将超过卡券有效期的 GIFTING 状态卡券退回给原赠送人。

#### Scenario: GIFTING voucher past its expireAt is restored
- **WHEN** 卡券状态为 `GIFTING` 且 `expireAt` 已过
- **THEN** 定时任务将卡券 `holderId` 恢复为最近一次 gift 记录的 `fromUserId`
- **THEN** 卡券 `status` 更新为 `ISSUED`
- **THEN** 对应 gift 记录 `status` 更新为 `EXPIRED`

#### Scenario: Concurrent accept beats scheduled restore
- **WHEN** 卡券正处于 GIFTING 状态且已被领取（acceptGift 先于定时任务执行）
- **THEN** 定时任务的乐观锁更新返回 0 行，静默跳过

### Requirement: restoreVoucher handles missing gift record gracefully

`restoreVoucher` SHALL 在 gift 记录不存在时不抛出异常。

#### Scenario: Gift record is null
- **WHEN** `restoreVoucher` 查询不到对应 gift 记录
- **THEN** 方法直接返回，不执行任何更新，不抛出异常

### Requirement: Controller path pattern distinguishes numeric IDs from sub-paths

`EmployeeVoucherController` 的 `getById` 端点 SHALL 仅匹配数字 ID。

#### Scenario: Numeric ID matches getById
- **WHEN** 请求 `GET /api/v1/vouchers/my/123`
- **THEN** 路由到 `getById` handler

#### Scenario: Sub-path matches gift-records
- **WHEN** 请求 `GET /api/v1/vouchers/my/123/gift-records`
- **THEN** 路由到 `giftRecords` handler，不被 `getById` 拦截

### Requirement: Gift accept and reject buttons trigger confirmation dialog

H5 转赠收件箱 SHALL 弹出确认对话框后才执行领取或拒绝操作。

#### Scenario: Click accept shows confirm dialog
- **WHEN** 用户在收件箱点击"领取"按钮
- **THEN** 弹出确认弹窗，标题「确认领取」
- **THEN** 用户点击弹窗"确认"后，调用 `acceptGift` API

#### Scenario: Click reject shows confirm dialog
- **WHEN** 用户在收件箱点击"拒绝"按钮
- **THEN** 弹出确认弹窗，标题「确认拒绝」
- **THEN** 用户点击弹窗"确认"后，调用 `rejectGift` API

### Requirement: Snowflake giftId preserved across frontend-backend round trip

系统 SHALL 确保 Snowflake 生成的 giftId 在前后端传输中不丢失精度。

#### Scenario: giftId string serialization
- **WHEN** 后端返回 gift 列表（含 giftId 字段）
- **THEN** JSON 响应中 `giftId` 为字符串类型而非数字类型

#### Scenario: acceptGift with correct giftId
- **WHEN** 后端接收到 `POST /api/v1/vouchers/my/gift/{giftId}/accept`
- **THEN** 后端能根据 giftId 正确定位到对应的 gift 记录
