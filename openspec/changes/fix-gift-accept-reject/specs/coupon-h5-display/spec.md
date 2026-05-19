## ADDED Requirements

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
