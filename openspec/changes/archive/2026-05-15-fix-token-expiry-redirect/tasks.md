## 1. 后端修复

- [x] 1.1 修改 `SecurityConfig.writeError` 方法，将 `res.setStatus(200)` 改为 `res.setStatus(code)`，使认证错误返回正确的 HTTP 状态码
- [x] 1.2 重新编译并启动后端，验证 `/api/v1/batches` 等受保护接口在无 token 时返回 HTTP 401

## 2. Admin 前端加固

- [x] 2.1 在 `frontend-admin/src/api/request.js` 响应成功拦截器中，在通用 `res.code !== 0` 检查前增加 `res.code === 401` 判断：清除 `admin_token` 和 `admin_user`，跳转 `/login`，显示"登录已过期，请重新登录"
- [x] 2.2 构建 Admin 前端并验证：登录后手动清除 localStorage 中的 token，访问受保护页面，应跳转登录页

## 3. H5 前端修复

- [x] 3.1 在 `frontend-h5/src/api/request.js` 响应成功拦截器中增加 `res.code !== 0` 业务错误码检查：401 时清除 token 并跳转 `#/login`，其他错误码显示错误提示
- [x] 3.2 构建 H5 前端并验证：登录后手动清除 localStorage 中的 token，访问受保护页面，应跳转登录页
