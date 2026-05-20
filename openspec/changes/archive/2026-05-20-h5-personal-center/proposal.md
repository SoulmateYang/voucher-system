## Why

H5 端目前缺少个人中心页面，员工无法查看自己的工号、姓名、部门、手机号等基本信息，也无法自助修改个人信息或退出登录。这导致员工必须通过管理员才能完成基本的个人信息维护，增加了沟通成本。新增个人中心是 H5 端作为员工自助工具的必备功能。

## What Changes

- H5 端新增"个人中心"页面，展示员工工号、姓名、部门、手机号
- 支持员工自助修改手机号、部门等个人信息（工号不可修改）
- 支持员工修改登录密码（需验证原密码）
- 支持退出登录（清除 token 并跳转登录页）
- 后端新增 `PUT /api/v1/auth/me` 接口，支持员工更新个人信息
- 后端新增 `POST /api/v1/auth/change-password` 接口，支持员工修改密码
- H5 端导航栏新增"我的"入口，整合收件箱和发件箱入口到个人中心

## Capabilities

### New Capabilities

- `h5-profile-view`: H5 端个人中心页面，查看员工个人信息（工号/姓名/部门/手机号）
- `h5-profile-edit`: H5 端编辑个人信息功能，支持修改手机号、部门等字段
- `h5-change-password`: H5 端修改密码功能，需验证原密码
- `h5-logout`: H5 端退出登录功能，清除凭证并跳转登录页
- `employee-self-update`: 后端员工自助更新个人信息和密码接口

### Modified Capabilities

<!-- 无已有 spec 需要修改 -->

## Impact

- **前端 H5**: 新增 `views/Profile.vue`、`views/EditProfile.vue`、`views/ChangePassword.vue`，修改 `router/index.js` 新增路由，修改 `App.vue` 或 `VoucherList.vue` 新增导航入口
- **后端**: 修改 `AuthController.java` 新增 `PUT /me` 和 `POST /change-password` 接口，修改 `AuthService.java`
- **依赖**: 无新增依赖，使用现有 Vant UI 组件库
