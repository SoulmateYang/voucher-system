## Context

Vant 4.x 中 Toast API 发生 breaking change：`Toast.success()` / `Toast.fail()` 实例方法被移除，改为独立的具名导出函数 `showSuccessToast()` / `showFailToast()`。当前 H5 端 7 个文件中存在 18 处旧 API 调用，导致运行时错误。

## Goals / Non-Goals

**Goals:**
- 将所有 Toast 调用迁移到 Vant 4 官方 API
- 保证迁移后功能和用户体验一致（不改变 Toast 的文案、样式、行为）

**Non-Goals:**
- 不升级/降级 Vant 版本
- 不改变 Toast 以外的任何功能
- 不引入新的 Toast 封装层

## Decisions

**直接使用 `showSuccessToast` / `showFailToast`，不封装内部工具函数**

- 理由：Vant 4 官方 API 语义清晰，7 个文件 18 处调用不需要额外抽象层。封装会增加维护成本和理解负担
- 备选：创建 `toast.ts` 工具函数统一管理 —— 拒绝了，因为对于简单 API 调用这是过度设计

**保留 `app.use(Toast)` 注册不变**

- `Toast` 在 Vant 4 中仍是 Vue 组件，`app.use(Toast)` 用于注册全局样式和挂载节点，不受此次修改影响

## Risks / Trade-offs

- 风险：后续如果有文件仍然使用旧 API → Vant 4 在运行时直接抛 TypeError，会在开发和测试阶段立即发现
- 无回归风险：仅修改函数调用名，参数（文案字符串）完全不变
