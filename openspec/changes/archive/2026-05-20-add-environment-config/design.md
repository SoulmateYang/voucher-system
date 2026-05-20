## Context

当前两个前端项目的 API 地址硬编码在 `src/api/request.js` 中（`/api/v1`），开发时依赖 Vite proxy 转发，生产部署依赖反向代理或同域部署。管理员端 `package.json` 已有 `build:test` 脚本但缺少对应的 `.env.test` 文件，H5 端只有单一 `build` 脚本。后端已有 Maven profile 机制（dev/test），无需变更。

约束：Vite 构建时通过 `--mode` 决定加载哪个 `.env.[mode]` 文件；只有 `VITE_` 前缀的变量暴露给客户端代码。

## Goals / Non-Goals

**Goals:**
- 两个前端项目支持通过 `--mode` 参数生成不同环境（dev/test）的构建产物
- 后端 API 地址通过环境变量配置，每个环境可独立设置
- 开发模式保持现有 Vite proxy 体验不变
- `package.json` 提供清晰的构建脚本命名

**Non-Goals:**
- 不涉及 CI/CD 流水线配置
- 不修改后端配置
- 不添加生产环境部署相关配置（仅 dev/test）

## Decisions

### 1. 环境配置文件命名策略

选择：`.env.development`（dev 默认） + `.env.test`（test 构建） + `.env`（兜底公共变量）

Vite 默认行为：
- `vite` / `vite dev` → mode=`development`，加载 `.env.development`
- `vite build` → mode=`production`，加载 `.env.production`
- `vite build --mode test` → mode=`test`，加载 `.env.test`
- `.env` 作为所有 mode 的兜底

`.env.development` 设置 `VITE_API_BASE_URL=/api/v1` 保持相对路径（走 proxy），`.env.test` 设置完整后端 URL。

### 2. API 地址变量设计

选择：单一变量 `VITE_API_BASE_URL`，开发环境值 `/api/v1`，测试环境值为完整 URL

替代方案（已拒绝）：拆分为 `VITE_API_HOST` + `VITE_API_PREFIX` 两个变量。对于纯路径切换的场景，单一变量更简洁且不易出错。

### 3. request.js 改造方式

选择：将 `baseURL` 从硬编码字符串改为 `import.meta.env.VITE_API_BASE_URL || '/api/v1'`，保留兜底默认值

这样即使忘记创建 `.env` 文件，程序也不会崩溃。

### 4. package.json 脚本命名

| 项目 | 命令 | mode | 加载文件 |
|------|------|------|---------|
| admin | `dev` | development | `.env.development` |
| admin | `build:dev` | development | `.env.development` |
| admin | `build:test` | test | `.env.test` |
| h5 | `dev` | development | `.env.development` |
| h5 | `build:dev` | development | `.env.development` |
| h5 | `build:test` | test | `.env.test` |

## Risks / Trade-offs

- **`.env.test` 包含服务器地址会提交到 Git** → 测试环境地址通常不敏感，生产环境配置按需后续添加；如需保密可后续引入 `.env.test.local`（gitignore）
- **环境变量在构建时固化** → 这是 Vite 的默认行为，符合预期；运行时切换需额外方案，不在本次范围
