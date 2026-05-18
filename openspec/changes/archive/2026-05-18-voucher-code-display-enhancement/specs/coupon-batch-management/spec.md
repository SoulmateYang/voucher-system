## ADDED Requirements

### Requirement: Voucher code detail dialog shows complete information

批次管理页面的"查看券码"弹窗 SHALL 展示每张券码的完整信息，包括有效期和二维码。

#### Scenario: Dialog displays validity period column
- **WHEN** 管理员在批次管理页面点击"查看券码"按钮
- **THEN** 券码列表表格展示"有效期"列，显示每张券的 `expireAt`，格式为 `YYYY-MM-DD`

#### Scenario: Dialog displays QR code column
- **WHEN** 管理员在批次管理页面点击"查看券码"按钮
- **THEN** 券码列表表格展示"二维码"列，状态为 `ISSUED` 的券码显示可 hover 放大的二维码缩略图（36×36px），其他状态显示 `—`

#### Scenario: Dialog width accommodates new columns
- **WHEN** "查看券码"弹窗打开
- **THEN** 弹窗宽度为 700px，适配所有列（券码、持有人、面额、有效期、二维码、状态）
