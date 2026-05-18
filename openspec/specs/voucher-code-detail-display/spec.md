## Requirements

### Requirement: Voucher code list shows validity period

管理端查看券码列表时，系统 SHALL 为每条券码展示有效期。

#### Scenario: Display validity period for each voucher
- **WHEN** 管理员打开批次"查看券码"弹窗
- **THEN** 券码列表表格显示"有效期"列，格式为 `YYYY-MM-DD`
- **THEN** 有效期数据来源于 `voucher.expireAt`

#### Scenario: Expired voucher validity display
- **WHEN** 券码状态为 `EXPIRED` 且 `expireAt` 已过去
- **THEN** 有效期仍正常展示其 `expireAt` 日期，不做额外标记

### Requirement: Voucher code list shows QR code

管理端查看券码列表时，系统 SHALL 为状态为 `ISSUED` 的券码生成并展示二维码。

#### Scenario: Generate QR code for valid voucher
- **WHEN** 券码状态为 `ISSUED`（有效）
- **THEN** 券码在"二维码"列展示缩略图（36×36px），编码内容为 `voucher.voucherCode`
- **THEN** 鼠标悬停时通过 popover 展示 200×200px 可扫描尺寸的二维码

#### Scenario: No QR code for non-issuable vouchers
- **WHEN** 券码状态为 `USED`、`EXPIRED` 或 `CANCELLED`
- **THEN** "二维码"列展示 `—`，不生成二维码

#### Scenario: QR code popover interaction
- **WHEN** 管理员将鼠标悬停在二维码缩略图上
- **THEN** 弹出 200×200px 的二维码图片，可被扫码设备识别
- **THEN** 鼠标移出后 popover 自动关闭

### Requirement: Admin can view vouchers in batch detail

管理员通过批次列表进入券码详情时，系统 SHALL 展示包含有效期和二维码的完整券码信息。

#### Scenario: View voucher list for a batch
- **WHEN** 管理员点击某批次的"查看券码"按钮
- **THEN** 系统展示弹窗，弹窗宽度为 700px，包含以下列：券码、持有人、面额（仅优惠券批次）、有效期、二维码、状态
- **THEN** 弹窗标题格式为「券码列表 - {批次名称}」
