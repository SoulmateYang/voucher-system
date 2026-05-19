## ADDED Requirements

### Requirement: Admin can edit voucher expireAt and remark

系统 SHALL 提供管理端接口，允许管理员修改卡券的 `expireAt`（有效期）和 `remark`（备注）。`voucherCode`、`status`、`holderId`、`faceValue` 等安全敏感字段 SHALL NOT 通过此接口修改。

#### Scenario: Edit expireAt and remark successfully
- **WHEN** 管理员调用 `PUT /api/v1/vouchers/{id}` 并传入 `{ "expireAt": "2026-12-31T23:59:59", "remark": "延长至年底" }`
- **THEN** 系统更新该券的 `expireAt` 和 `remark` 字段，返回更新后的完整券信息
- **THEN** 系统在 `audit_log` 表中记录一条 `operation = "EDIT_VOUCHER"` 的审计日志

#### Scenario: Attempt to edit voucherCode is ignored
- **WHEN** 管理员调用 `PUT /api/v1/vouchers/{id}` 并尝试传入 `{ "voucherCode": "HACKED_CODE", "remark": "test" }`
- **THEN** 系统忽略 `voucherCode` 字段，仅更新 `remark`，核销码保持不变

#### Scenario: Edit non-existent voucher returns 404
- **WHEN** 管理员调用 `PUT /api/v1/vouchers/99999`
- **THEN** 系统返回 404 错误

#### Scenario: Admin UI edit dialog
- **WHEN** 管理员在券码列表中点击某条券码的"编辑"按钮
- **THEN** 系统弹出编辑对话框，预填当前有效期和备注
- **THEN** "核销码"字段显示但为只读状态（灰色、不可编辑）
- **THEN** 管理员修改后点击"保存"，对话框关闭，列表刷新

### Requirement: Edit audit trail

系统 SHALL 在每次编辑操作时记录操作人、操作时间、变更前后的值。

#### Scenario: Audit log records field changes
- **WHEN** 管理员将某券的有效期从 "2026-06-30" 改为 "2026-12-31"
- **THEN** `audit_log` 表的 `detail` 字段包含 `{"field":"expireAt","oldValue":"2026-06-30T...","newValue":"2026-12-31T..."}`
- **THEN** `audit_log.operatorId` 记录当前登录管理员 ID
