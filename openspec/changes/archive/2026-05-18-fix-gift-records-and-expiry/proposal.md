## Why

两个缺陷导致转赠功能不可靠：(1) 用户从卡券详情页进入「使用记录」查看转赠记录时页面无法正常展示数据；(2) 赠送超时（24小时未领取）后，系统仅在用户手动访问收件箱/发件箱时才触发卡券退回——若双方均未访问，卡券永久卡在 GIFTING 状态，既不在发起人名下也不在接收人名下，实际丢失。

## What Changes

- **修复 `VoucherExpireTask` 定时任务**：新增对 `GIFTING` 状态卡券的处理，每天 00:05 批量将 `expireAt` 已过的 GIFTING 卡券恢复为 ISSUED 并退回给原持有人
- **修复 `restoreVoucher` 空指针风险**：增加 gift 记录为 null 时的防御检查
- **修复转赠记录页面数据展示**：优化 GiftRecords.vue 的加载状态和 API 响应数据解析，确保 gift 记录能正常渲染
- **修复 `EmployeeVoucherController.getById` 路径模式**：将 `/{id}` 的 `@GetMapping` 限定为仅匹配数字 ID，避免与 `/{id}/gift-records` 等子路径产生潜在歧义

## Capabilities

### New Capabilities
<!-- none — this is a bug fix, no new capabilities -->

### Modified Capabilities
- `coupon-h5-display`: 转赠记录页面 GiftRecords 数据展示修复，定时任务覆盖 GIFTING 卡券回收

## Impact

- **后端 `VoucherExpireTask.java`**：新增 GIFTING → ISSUED 恢复逻辑（约 15 行）
- **后端 `GiftService.java`**：`restoreVoucher` 增加 null 防御（约 3 行）
- **后端 `EmployeeVoucherController.java`**：`getById` 路径加正则约束（1 行）
- **前端 `GiftRecords.vue`**：优化数据解析与加载状态
- **无新增依赖**，无 BREAKING 变更
