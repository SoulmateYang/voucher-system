## Why

H5 移动端登录成功后会显示"登录成功"提示，但页面停留在登录页，不会自动跳转到"我的卡券"页面。用户需手动刷新或重新进入才能看到券列表，体验很差。

## What Changes

- 修复 `Login.vue` 中登录成功后的路由跳转逻辑，确保 `router.replace` 正确执行

## Capabilities

### New Capabilities
<!-- 无新增能力 — 纯 bug 修复 -->

### Modified Capabilities
<!-- 无 spec 级别变更 — 修复现有行为的 bug -->

## Impact

- **前端 H5**: `views/Login.vue` — 登录成功后的跳转逻辑
