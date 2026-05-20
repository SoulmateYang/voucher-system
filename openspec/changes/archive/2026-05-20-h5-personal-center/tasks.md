## 1. 后端接口

- [x] 1.1 新增 `PUT /api/v1/auth/me` 接口：AuthController 添加 updateMe 方法，AuthService 添加 updateCurrentUser 方法，接收 mobile 和 department 参数更新当前用户信息
- [x] 1.2 新增 `POST /api/v1/auth/change-password` 接口：AuthController 暴露 changePassword 端点，封装 oldPassword/newPassword 请求体，调用已有的 AuthService.changePassword 方法

## 2. H5 前端 API 层

- [x] 2.1 在 `api/voucher.js` 中新增 getProfile、updateProfile、changePassword 三个 API 函数

## 3. H5 个人中心页面

- [x] 3.1 创建 `views/Profile.vue`：顶部展示员工头像占位和姓名，CellGroup 展示工号/姓名/部门/手机号，底部 CellGroup 提供编辑资料/修改密码/收件箱/发件箱入口，退出登录按钮
- [x] 3.2 创建 `views/EditProfile.vue`：使用 van-form + van-field，姓名和工号只读，手机号和部门可编辑，提交调用 updateProfile API
- [x] 3.3 创建 `views/ChangePassword.vue`：使用 van-form + van-field，原密码/新密码/确认新密码三个字段，前端校验两次密码一致和最小长度，提交调用 changePassword API

## 4. 路由与导航

- [x] 4.1 在 `router/index.js` 中新增 Profile、EditProfile、ChangePassword 路由（均需认证）
- [x] 4.2 在 `App.vue` 中集成 Vant Tabbar：包含"卡券"和"我的"两个 tab，使用 `<router-view>` 配合 keep-alive 或条件渲染，确保登录页不显示 Tabbar

## 5. 退出登录与收尾

- [x] 5.1 实现退出登录：Profile.vue 中点击退出登录弹出 Dialog 确认，确认后调用 removeToken 清除凭证，跳转登录页
- [x] 5.2 验证完整流程：登录 → 查看个人中心 → 编辑资料 → 修改密码 → 退出登录 → 重新登录
