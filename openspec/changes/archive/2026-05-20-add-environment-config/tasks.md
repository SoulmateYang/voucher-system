## 1. 管理端环境配置

- [x] 1.1 创建 `.env.development`，设置 `VITE_API_BASE_URL=/api/v1`
- [x] 1.2 创建 `.env.test`，设置 `VITE_API_BASE_URL=http://101.42.92.205:8081/api/v1`
- [x] 1.3 修改 `src/api/request.js`，将 `baseURL` 从硬编码改为 `import.meta.env.VITE_API_BASE_URL || '/api/v1'`
- [x] 1.4 补全 `package.json` 中的 `build:dev` 脚本（`vite build --mode development`）

## 2. H5端环境配置

- [x] 2.1 创建 `.env.development`，设置 `VITE_API_BASE_URL=/api/v1`
- [x] 2.2 创建 `.env.test`，设置 `VITE_API_BASE_URL=http://101.42.92.205:8081/api/v1`
- [x] 2.3 修改 `src/api/request.js`，将 `baseURL` 从硬编码改为 `import.meta.env.VITE_API_BASE_URL || '/api/v1'`
- [x] 2.4 在 `package.json` 中新增 `build:dev`（`vite build --mode development`）和 `build:test`（`vite build --mode test`）脚本

## 3. 验证

- [x] 3.1 验证管理端 `npm run dev` 开发模式正常启动，API 走 proxy
- [x] 3.2 验证管理端 `npm run build:test` 构建产物中 API 地址指向测试服务器
- [x] 3.3 验证 H5 端 `npm run dev` 开发模式正常启动，API 走 proxy
- [x] 3.4 验证 H5 端 `npm run build:test` 构建产物中 API 地址指向测试服务器
