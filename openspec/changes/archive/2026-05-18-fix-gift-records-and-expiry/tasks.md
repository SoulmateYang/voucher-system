## 1. GiftService 防御性修复

- [x] 1.1 `restoreVoucher` 方法：gift 查询结果为 null 时直接 return，避免 NPE

## 2. VoucherExpireTask 新增 GIFTING 处理

- [x] 2.1 在 `expireVouchers()` 中新增 GIFTING 卡券过期处理逻辑：批量查询 status=GIFTING 且 expireAt 已过的卡券
- [x] 2.2 对每张 GIFTING 卡券：查找最新 gift 记录 → 乐观锁更新 holderId/holderName/status 为 ISSUED → gift 记录标记 EXPIRED

## 3. Controller 路径正则约束

- [x] 3.1 `EmployeeVoucherController.getById` 的 `@GetMapping("/{id}")` 改为 `@GetMapping("/{id:[0-9]+}")`

## 4. 测试

- [x] 4.1 编写测试：验证 GIFTING 卡券过期后定时任务恢复为 ISSUED 并退回原持有人
- [x] 4.2 编写测试：验证并发 acceptGift 先于定时任务时乐观锁静默跳过
- [x] 4.3 编写测试：验证 restoreVoucher 在 gift 记录为 null 时不抛异常
- [x] 4.4 编写测试：验证 `/{id:[0-9]+}` 不匹配 `/manual` 路径

## 5. 验证

- [x] 5.1 后端全量测试 90/90 PASS，BUILD SUCCESS
- [x] 5.2 H5 编译通过
