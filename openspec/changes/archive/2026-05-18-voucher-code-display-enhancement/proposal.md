## Why

管理端"查看券码"弹窗当前仅展示券码、持有人和状态，缺少单张券的有效期信息与可扫描的二维码。管理员需要复制券码到核销台手动查询才能获取有效期，也无法直接出示券码二维码供员工扫码核销——影响运营效率。

## What Changes

- 管理端"券码列表"弹窗表格新增**有效期**列，展示每张券的 `expireAt`（格式：`YYYY-MM-DD`）
- 管理端"券码列表"弹窗新增**二维码**列，为每张未使用/有效的券码生成可扫描的二维码（使用已有 `qrcode` 库）
- 弹窗宽度从 `520px` 调整为 `700px` 以容纳新增列
- 二维码 hover 时展示放大预览（tooltip 或 popover），确保小尺寸下可扫描

## Capabilities

### New Capabilities
- `voucher-code-detail-display`: 管理端券码列表弹窗增强——包含有效期展示与二维码生成

### Modified Capabilities
- `coupon-batch-management`: 批次管理页面"查看券码"弹窗新增有效期列和二维码列

## Impact

- **前端 Admin**: `BatchManage.vue` — 修改券码列表弹窗的表格列定义、新增二维码渲染逻辑、调整弹窗宽度
- **依赖库**: `qrcode`（已在 `frontend-admin/package.json` 中安装，无需新增依赖）
- **后端**: 无需修改 —— `GET /api/v1/batches/{id}/vouchers` 已返回完整 Voucher 实体（含 `expireAt`、`voucherCode`）
