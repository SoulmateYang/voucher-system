## 1. 修复登录跳转

- [x] 1.1 修改 `Login.vue`：移除 `van-form @submit`，改为登录按钮 `@click` 手动触发，手动调用 `van-form.validate()`，await router.replace 确保导航完成
- [x] 1.2 清理 `Login.vue` 中未使用的 `rememberLogin` 变量

## 2. 验证

- [x] 2.1 构建验证：前端 H5 编译通过
- [x] 2.2 逻辑验证：登录成功 → 自动跳转券列表、token 过期 → 跳回登录页
