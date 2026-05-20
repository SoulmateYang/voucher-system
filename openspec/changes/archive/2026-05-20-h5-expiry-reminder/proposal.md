## Why

员工登录 H5 端后没有卡券到期提醒，可能错过即将过期的卡券，导致资源浪费。新增登录后自动提醒功能，让员工及时知晓 7 天内即将过期的卡券，提升卡券使用率。

## What Changes

- 新增后端接口 `GET /api/v1/vouchers/my/expiring-soon`，返回当前员工 7 天内即将过期的卡券数量和列表
- H5 登录成功后自动查询即将过期的卡券
- 如有即将过期的卡券，弹出 Vant Dialog 提醒，展示过期券数并引导查看
- 仅提醒状态为 ISSUED（有效）且 expireAt 在未来 7 天内的卡券

## Capabilities

### New Capabilities

- `expiry-reminder-api`: 后端接口，查询当前用户 7 天内即将过期的有效卡券
- `h5-login-reminder`: H5 端登录成功后的到期提醒弹窗

### Modified Capabilities

<!-- 无已有 spec 需要修改 -->

## Impact

- **后端**: VoucherService 新增 `getExpiringSoon` 方法，EmployeeVoucherController 新增接口
- **前端 H5**: 修改 `Login.vue`，在登录成功后调用到期提醒接口并展示弹窗
