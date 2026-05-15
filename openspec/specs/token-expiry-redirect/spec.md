## Requirements

### Requirement: Token 过期自动跳转登录页

当用户的 JWT token 过期或无效时，系统 SHALL 自动清除本地登录态并跳转到登录页面，同时向用户展示"登录已过期，请重新登录"提示。

#### Scenario: Admin 端 API 请求返回 401

- **WHEN** Admin 端发起 API 请求，后端返回 HTTP 401 状态码
- **THEN** 系统清除 localStorage 中的 `admin_token` 和 `admin_user`
- **THEN** 页面跳转到 `/login`
- **THEN** 显示"登录已过期，请重新登录"错误提示

#### Scenario: H5 端 API 请求返回 401

- **WHEN** H5 端发起 API 请求，后端返回 HTTP 401 状态码
- **THEN** 系统清除 localStorage 中的 `card_voucher_token`
- **THEN** 页面跳转到 `#/login`
- **THEN** 显示"登录已过期，请重新登录"错误提示

#### Scenario: 后端认证失败时的 HTTP 状态码

- **WHEN** 请求携带过期或无效的 JWT token
- **THEN** 后端 SHALL 返回 HTTP 401 状态码（而非 200）
- **THEN** 响应体 SHALL 包含 `{"code": 401, "message": "未登录或Token已过期"}`

#### Scenario: 后端权限不足时的 HTTP 状态码

- **WHEN** 已认证用户访问无权限的资源
- **THEN** 后端 SHALL 返回 HTTP 403 状态码（而非 200）
- **THEN** 响应体 SHALL 包含 `{"code": 403, "message": "无权限"}`

### Requirement: 前端防御性处理业务错误码 401

前端响应拦截器 SHALL 在成功回调中对业务错误码 401 做防御性处理，确保即使后端在 HTTP 200 响应中返回 401 业务错误码，也能正确清理登录态并跳转。

#### Scenario: Admin 端收到 HTTP 200 但业务 code 为 401

- **WHEN** Admin 端收到 HTTP 200 响应
- **THEN** 系统检查 `response.data.code`
- **WHEN** `code === 401`
- **THEN** 系统清除 `admin_token` 和 `admin_user`
- **THEN** 页面跳转到 `/login`
- **THEN** 显示"登录已过期，请重新登录"错误提示

#### Scenario: H5 端收到 HTTP 200 但业务 code 为 401

- **WHEN** H5 端收到 HTTP 200 响应
- **THEN** 系统检查 `response.data.code`
- **WHEN** `code === 401`
- **THEN** 系统清除 `card_voucher_token`
- **THEN** 页面跳转到 `#/login`
- **THEN** 显示"登录已过期，请重新登录"错误提示
