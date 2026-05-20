## 1. 后端接口

- [x] 1.1 在 VoucherService 中新增 `getExpiringSoon(holderId)` 方法：查询 status=ISSUED、expireAt 在当前到 +7 天之间的卡券，返回券列表和总数
- [x] 1.2 在 EmployeeVoucherController 中新增 `GET /api/v1/vouchers/my/expiring-soon` 接口，调用 VoucherService.getExpiringSoon

## 2. H5 前端 API 层

- [x] 2.1 在 `api/voucher.js` 中新增 `getExpiringSoon()` 函数

## 3. H5 登录提醒

- [x] 3.1 修改 `Login.vue`：在 setToken 后、router.replace 前，调用 getExpiringSoon 查询到期卡券，如有结果则用 showDialog 弹窗提醒，用户关闭弹窗后再跳转；接口失败则静默跳过

## 4. 验证

- [x] 4.1 验证完整流程：登录 → 到期提醒弹窗（如有到期券）→ 关闭弹窗 → 进入卡券列表；无到期券时直接进入列表
