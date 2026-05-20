## Why

当前前端项目（管理端和H5端）缺乏环境区分能力：API地址硬编码为相对路径，仅通过开发代理工作；管理员端虽已有 `build:test` 脚本但缺少对应的环境配置文件；H5端没有任何多环境构建支持。这导致部署到不同环境时需要手动修改代码或依赖反向代理，无法通过打包命令直接生成对应环境的构建产物。

## What Changes

- 为管理端（frontend-admin）新增 `.env.dev`、`.env.test` 环境配置文件，支持通过 `--mode` 切换后端API地址
- 为H5端（frontend-h5）新增 `.env.dev`、`.env.test` 环境配置文件及对应的 `build:dev`、`build:test` 构建脚本
- 两个前端的 API 客户端改为从环境变量 `VITE_API_BASE_URL` 读取后端地址，不再硬编码
- Vite 开发代理保持 `/api` 前缀代理到本地后端，不影响开发体验
- 列出多环境打包命令文档，开发/测试/生产各有独立的构建命令

## Capabilities

### New Capabilities

- `frontend-env-config`: 前端多环境构建配置 — 管理端和H5端通过 `.env` 文件和Vite mode机制，在不同构建命令下自动切换后端API地址

### Modified Capabilities

<!-- No existing specs require modification — this is purely additive configuration -->

## Impact

- **frontend-admin**: 新增 `.env.dev`、`.env.test`，修改 `src/api/request.js`（baseURL从硬编码改为环境变量），`package.json` 补全脚本
- **frontend-h5**: 新增 `.env.dev`、`.env.test`，修改 `src/api/request.js`（baseURL从硬编码改为环境变量），`package.json` 新增脚本
- **后端**: 无需变更（已有 profile 机制和多环境 yml）
- **无破坏性变更**: 默认 dev 环境保持现有行为不变
