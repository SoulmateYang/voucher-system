## ADDED Requirements

### Requirement: 查询即将到期的卡券
系统 SHALL 提供 `GET /api/v1/vouchers/my/expiring-soon` 接口，返回当前用户 7 天内即将到期的有效卡券。

#### Scenario: 成功查询有即将到期的卡券
- **WHEN** 已认证员工请求该接口，且存在状态为 ISSUED、过期时间在未来 7 天内的卡券
- **THEN** 系统返回这些卡券的列表，每条包含 id、voucherCode、remark、expireAt，以及即将到期的总数

#### Scenario: 无即将到期的卡券
- **WHEN** 已认证员工请求该接口，但不存在即将到期的有效卡券
- **THEN** 系统返回空列表，count 为 0

#### Scenario: 未认证访问拒绝
- **WHEN** 未携带有效 token 请求该接口
- **THEN** 系统返回 401 状态码

### Requirement: 查询范围限定
系统 SHALL 仅查询状态为 ISSUED 且 expireAt 大于当前时间且小于等于当前时间 +7 天的卡券。

#### Scenario: 不包含已过期卡券
- **WHEN** 员工存在状态为 ISSUED 但 expireAt 已过期的卡券
- **THEN** 这些卡券不出现在到期提醒结果中

#### Scenario: 不包含已使用/已作废卡券
- **WHEN** 员工存在状态为 USED 或 CANCELLED 的卡券
- **THEN** 这些卡券不出现在到期提醒结果中
