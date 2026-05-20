## Context

H5 端现有页面：登录、卡券列表、卡券详情、赠送/收件/发件、手动录入。导航仅通过卡券列表页右上角信封图标进入收件箱，缺少统一的个人中心入口。后端已有 `GET /api/v1/auth/me` 返回当前用户信息和 `AuthService.changePassword()` 方法，但缺少员工自助更新信息和修改密码的 HTTP 接口。

## Goals / Non-Goals

**Goals:**
- 新增个人中心页面作为 H5 端的"我的"入口
- 员工可查看工号、姓名、部门、手机号
- 员工可自助修改手机号、部门
- 员工可自助修改登录密码（验证原密码）
- 支持退出登录，清除 token 并跳转登录页

**Non-Goals:**
- 不涉及管理员端的员工管理功能（已由 EmployeeController 覆盖）
- 不修改登录逻辑
- 不新增第三方登录或生物识别

## Decisions

### 1. 后端接口设计：扩展现有 AuthController 而非新建 Controller

**选择**: 在 `AuthController` 中新增 `PUT /api/v1/auth/me` 和 `POST /api/v1/auth/change-password`

**理由**: 这些操作属于当前认证用户的自助服务，语义上归属 auth 资源。`GET /me` 已存在于 AuthController，保持一致性。

### 2. H5 导航：使用 Vant Tabbar 实现底部导航

**选择**: 在 App.vue 中新增 Vant Tabbar，包含"卡券"和"我的"两个 tab

**理由**: 当前 H5 端无统一导航，收件箱入口隐藏在卡券列表右上角图标中。底部 Tabbar 是移动端标准导航模式，符合设计系统的 H5 comfortable 密度和 ≥44px 触控目标要求。Vant 的 Tabbar 组件已在 main.js 中注册。

### 3. 个人信息编辑：独立页面而非内联编辑

**选择**: 点击"编辑资料"跳转到独立的编辑页面，使用 van-form

**理由**: 移动端表单编辑体验优于内联编辑，避免误操作。符合现有 H5 页面模式（如 ManualAdd.vue 用独立页面处理表单）。

### 4. 退出登录：客户端清除 + 无需服务端通知

**选择**: 前端清除 localStorage token，跳转登录页，不调用服务端登出接口

**理由**: JWT 无状态，服务端无需感知登出。当前 token 无黑名单机制，客户端清除即完成登出。如后续需要 token 失效机制可扩展。

## Risks / Trade-offs

- [Token 未失效] 退出登录后旧 token 在过期前仍可用 → 当前 token 有效期较短，风险可控；后续可通过 Redis 黑名单增强
- [导航重构] 引入 Tabbar 会改变现有页面布局 → 卡券列表、详情等页面需适配，确保内容不被 Tabbar 遮挡

## Open Questions

<!-- 无 -->
