## Context

H5 端登录流程：Login.vue 调 `/auth/login` → 获取 token → 存 localStorage → router.replace 到 VoucherList。Voucher 实体有 `expireAt` 字段和 `status` 字段（ISSUED=有效）。已有 `VoucherMapper.selectByHolder` 可按持有者查询卡券。

## Goals / Non-Goals

**Goals:**
- 登录成功后自动查询当前用户 7 天内到期的有效卡券
- 存在即将到期的卡券时弹出提醒弹窗，展示数量和引导
- 提醒弹窗可关闭，不强制跳转

**Non-Goals:**
- 不做定时推送/后台通知（仅登录时提醒）
- 不修改登录接口返回值
- 不涉及管理员端的到期监控

## Decisions

### 1. 后端接口：复用 VoucherService，简单查询

**选择**: 在 VoucherService 中新增方法，通过 MyBatis Plus LambdaQueryWrapper 查询 `holderId = 当前用户 AND status = 'ISSUED' AND expireAt <= NOW() + 7天 AND expireAt > NOW()`

**理由**: 查询条件简单，无需新增 Mapper XML。Voucher 表已有 holderId/status/expireAt 索引支持。

### 2. 提醒方式：Vant Dialog.alert

**选择**: 登录成功获取到 token 后，调用到期查询接口，如有结果则弹出 `showDialog({ title, message })` 展示提醒

**理由**: Dialog.alert 是 Vant 原生组件，无需额外依赖。模态弹窗比轻提示更显眼，确保员工不会错过到期提醒。

### 3. 提醒时机：Token 获取后、路由跳转前

**选择**: `setToken(token)` 后 → 调到期查询 API → 如有到期券则弹窗 → 用户关闭弹窗后 → `router.replace` 跳转

**理由**: 确保提醒在用户进入主页面之前展示，最大化关注度。token 已设置，API 调用可正常鉴权。

## Risks / Trade-offs

- [每次登录都弹窗] 如用户频繁登录退出，每次都弹窗可能烦人 → 可接受，当前为低频操作；后续可按天去重
- [API 调用延迟] 登录后多一次网络请求，略微延迟进入主页 → 可接受，查询条件简单、返回数据少
