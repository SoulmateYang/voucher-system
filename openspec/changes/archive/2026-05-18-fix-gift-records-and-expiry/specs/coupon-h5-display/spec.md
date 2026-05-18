## ADDED Requirements

### Requirement: Scheduled task restores expired GIFTING vouchers

定时任务 SHALL 自动将超过卡券有效期的 GIFTING 状态卡券退回给原赠送人。

#### Scenario: GIFTING voucher past its expireAt is restored
- **WHEN** 卡券状态为 `GIFTING` 且 `expireAt` 已过
- **THEN** 定时任务将卡券 `holderId` 恢复为最近一次 gift 记录的 `fromUserId`
- **THEN** 卡券 `status` 更新为 `ISSUED`
- **THEN** 对应 gift 记录 `status` 更新为 `EXPIRED`

#### Scenario: GIFTING voucher with active gift window is not affected
- **WHEN** 卡券状态为 `GIFTING` 但 `expireAt` 未过
- **THEN** 定时任务不处理该卡券

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
