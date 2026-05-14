## Context

H5 移动端 `Login.vue` 使用 Vant 4 `van-form` + `@submit` 事件处理登录。登录 API 调用成功、token 已存储到 localStorage、Toast 显示"登录成功"后，`router.replace` 调用却未实际跳转到券列表页面。

对比 PC 管理端 `Login.vue`（使用 `el-form` + 手动 `@click` 处理），H5 端的差异点：
1. H5 依赖 `van-form @submit` 事件驱动，而非手动按钮点击
2. H5 的 `router.replace` 未 `await`，是 fire-and-forget 模式
3. H5 的 catch 块仅处理 `!err.response` 情况，错误覆盖不完整

## Goals / Non-Goals

**Goals:**
- 登录成功后自动跳转到"我的卡券"页面
- 保持现有 UI/UX 不变（Vant 组件、Toast 提示）

**Non-Goals:**
- 不改变后端 API
- 不改变 token 存储方式
- 不新增功能（rememberLogin 等留待后续）

## Decisions

### D1: 修复方案 — 改用 `@click` 手动处理登录

**选择**: 移除 `van-form @submit="handleLogin"`，改为在登录按钮上使用 `@click="handleLogin"`，手动调用表单验证。与 PC 管理端保持一致的模式。

**备选**: 保留 `@submit`，在 `handleLogin` 中加 `event.preventDefault()`、接收 values 参数、await router.replace。

**理由**: `van-form @submit` 在某些移动端浏览器或 WebView 中可能存在异步 handler 后未正确阻止默认表单提交的问题。手动 `@click` 方案彻底避开这个风险，且与 PC 管理端 Login.vue 的模式一致，降低维护差异。

### D2: 导航方式

**选择**: 使用 `await router.replace({ name: 'VoucherList' })`，确保导航完成。

**理由**: `router.replace` 返回 Promise，不 await 时导航失败（如路由守卫拦截）会变成 unhandled rejection，静默失败。加上 await 后可在 catch 中捕获并提示用户。

## Risks / Trade-offs

- **[风险] 改变事件绑定方式可能引入新问题** → 手动调用 `van-form` 的 `validate()` 方法是 Vant 4 官方支持的用法，风险低
- **[风险] 异步表单验证** → `van-form` 的 validate 返回 Promise，使用 await 正确处理
