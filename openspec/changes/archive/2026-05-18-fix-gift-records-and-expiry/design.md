## Context

当前系统有两个缺陷：(1) `VoucherExpireTask` 定时任务只处理 `ISSUED` 状态卡券的过期，`GIFTING` 状态的卡券过期后无自动恢复机制；(2) `restoreVoucher` 在 gift 记录为 null 时抛出 NPE 导致整个恢复流程中断。

## Goals / Non-Goals

**Goals:**
- `VoucherExpireTask` 每天凌晨自动将 `expireAt` 已过的 `GIFTING` 卡券退回给原赠送人
- `restoreVoucher` 防御性检查 gift 为 null 的情况
- `EmployeeVoucherController.getById` 路径添加正则约束，确保 `/{id}` 仅匹配纯数字

**Non-Goals:**
- 不改变 gift 记录的乐观锁策略（保持现状）
- 不增加新的 API 端点
- 不修改前端 GiftRecords.vue 的核心逻辑（数据流路径已验证正确）

## Decisions

### 1. VoucherExpireTask 新增 GIFTING 处理

在现有 `expireVouchers()` 方法中增加一段逻辑：查询 `status = 'GIFTING' AND expire_at < now()` 的卡券，对其逐一恢复。

处理流程：
1. 查询所有符合条件的 GIFTING 卡券（分批处理，每批 500 条）
2. 对每张卡券查找最新的 gift 记录（按 `gift_at` 降序取第一条）
3. 将 voucher 的 `holderId`/`holderName` 恢复为 gift 记录的 `fromUserId`/`fromUserName`
4. 将 voucher 的 `status` 更新为 `ISSUED`
5. 将对应的 gift 记录更新为 `EXPIRED`

使用乐观锁（`version`）更新 voucher，防止与并发 `acceptGift` 冲突。

### 2. restoreVoucher 空指针防御

在 `restoreVoucher` 方法最前面增加：如果 gift 查询结果为 null，直接 return（数据异常，无法恢复）。

### 3. Controller 路径正则约束

将 `@GetMapping("/{id}")` 改为 `@GetMapping("/{id:[0-9]+}")`，Spring MVC 将仅对纯数字路径变量匹配该 handler，消除与 `/manual`、`/{id}/gift-records` 等路径的歧义风险。

## Risks / Trade-offs

- **GIFTING 卡券被定时任务与用户手动操作并发**：定时任务使用乐观锁更新 voucher，如果 `acceptGift` 已先修改 version，定时任务的 update 会返回 0 行，静默跳过——数据安全
- **批量处理的性能**：每天凌晨执行一次，GIFTING 状态的卡券数量通常很小（24 小时窗口内），无性能担忧
