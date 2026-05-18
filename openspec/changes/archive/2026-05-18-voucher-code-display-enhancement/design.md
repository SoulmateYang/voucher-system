## Context

当前管理端 `BatchManage.vue` 的"查看券码"弹窗仅通过 `el-table` 展示券码列表（券码、持有人、面额、状态）。后端 `GET /api/v1/batches/{id}/vouchers` 已返回完整 Voucher 实体（含 `expireAt`、`voucherCode` 等字段），但前端未利用这些字段。

`qrcode` 库（v1.5.4）已在 `frontend-admin/package.json` 中安装，H5 端已有 QR 码生成实现可参考。

## Goals / Non-Goals

**Goals:**
- 券码列表表格新增有效期列，格式化展示 `expireAt`
- 券码列表表格新增二维码列，为状态为 `ISSUED` 的券码渲染可扫描的二维码
- 二维码以缩略图形式展示，hover/click 时放大预览确保可扫描性
- 弹窗宽度适配新增列

**Non-Goals:**
- 不修改后端 API（已满足需求）
- 不修改 H5 端
- 不提供二维码下载/打印功能（后续迭代）
- 不新增独立券码详情页

## Decisions

1. **有效期格式**: 使用 `YYYY-MM-DD`（仅日期，不显示时分秒），与 H5 端 `VoucherList.vue` 保持一致。券过期逻辑以 `23:59:59` 为界，精确到日即可。

2. **二维码渲染方式**: 使用 `qrcode` 库的 `toCanvas` API，将 canvas 转换为 img 嵌入表格单元格。替代方案 H5 使用的 `toCanvas` 会直接创建 canvas 节点，但表格单元格内 canvas 尺寸控制不便 —— 改为 `toDataURL` 生成 base64 后由 `<img>` 标签渲染，更易控制尺寸和 hover 行为。

3. **二维码生成时机**: 在获取券码列表后批量生成 QR 码（非用户交互时逐一 lazy 生成），确保表格滚动和 hover 无延迟。券码列表数量有限（分页 50 条），性能无影响。

4. **放大预览方式**: 使用 `el-popover` 包裹二维码缩略图，hover 触发在 popover 中展示 200×200 可扫描尺寸的二维码。比 tooltip 更灵活（支持自定义内容），比 dialog 更轻量。

5. **弹窗宽度**: 从 `520px` 调整为 `700px`，容纳新增的两列（有效期 ~110px + 二维码 ~80px）。

6. **已过期/已作废券码**: 不展示二维码（已无法使用），单元格显示 `—`。

## Risks / Trade-offs

- **二维码 base64 字符串在内存中**: 50 条券码生成 50 个 base64 QR 码，每条约 2KB，总计 ~100KB，可接受
- **el-popover 在表格中的 z-index 层级**: 表格单元格内 popover 可能被表格行遮挡 —— 设置 `:z-index="2000"` 确保浮层在 dialog (z-index: 2000+) 之上
- **QR 码扫码识别率**: 缩略图（36px）过小无法扫描，依赖 popover 中 200px 完整尺寸扫码 —— 经验证 H5 端 200px QR 码可正常被微信/浏览器扫码识别
