## ADDED Requirements

### Requirement: 多环境构建支持

管理端和H5端各自的项目 MUST 支持通过 `--mode` 参数生成不同目标环境的构建产物，且后端 API 地址通过构建时环境变量注入，无需修改业务代码。

#### Scenario: 开发模式使用 Vite proxy 代理

- **WHEN** 开发者执行 `npm run dev`
- **THEN** Vite 以 development 模式启动，加载 `.env.development`，API 请求走 Vite proxy 转发至本地后端

#### Scenario: 管理端构建测试环境产物

- **WHEN** 构建者执行 `npm run build:test`（管理端）
- **THEN** Vite 以 test 模式构建，加载 `.env.test` 中的 `VITE_API_BASE_URL`，产物中 API 地址指向测试服务器

#### Scenario: H5端构建测试环境产物

- **WHEN** 构建者执行 `npm run build:test`（H5端）
- **THEN** Vite 以 test 模式构建，加载 `.env.test` 中的 `VITE_API_BASE_URL`，产物中 API 地址指向测试服务器

#### Scenario: 构建时环境变量缺失兜底

- **WHEN** 构建时未设置 `VITE_API_BASE_URL` 环境变量
- **THEN** API 客户端使用默认值 `/api/v1`，系统正常运行不致崩溃
