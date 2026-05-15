## Why

Token 过期或无效时，用户停留在当前页面未跳转到登录页，导致用户看到无意义的错误提示或空白页面，无法重新登录。根因是后端认证入口 `writeError` 方法始终返回 HTTP 200，前端 401 拦截逻辑从未被触发。

## What Changes

- **后端**: `SecurityConfig.writeError` 方法改为返回真实的 HTTP 状态码（401/403），而非固定 200
- **前端 Admin**: 响应成功拦截器增加对业务错误码 401 的处理，清理 token 并跳转登录页
- **前端 H5**: 响应成功拦截器增加业务错误码检查，401 时清理 token 并跳转登录页
- **前端 H5**: 补充缺失的 403 处理，与 Admin 端行为对齐

## Capabilities

### New Capabilities
- `token-expiry-redirect`: token 过期或无效时，系统自动清理登录态并跳转到登录页，同时向用户提示"登录已过期，请重新登录"

### Modified Capabilities
<!-- 本次修复不改变已有功能的 spec 级别行为 -->

## Impact

- `backend/.../config/SecurityConfig.java` — `writeError` 方法
- `frontend-admin/src/api/request.js` — 响应成功拦截器
- `frontend-h5/src/api/request.js` — 响应成功拦截器
