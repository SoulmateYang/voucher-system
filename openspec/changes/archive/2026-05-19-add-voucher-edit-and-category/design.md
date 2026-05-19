## Context

当前 `Voucher` 实体有 22 个字段，但只有 `cancel` 接口可以变更状态，其余字段写入后不可修改。管理员在以下场景中需要编辑能力：有效期录入错误需要延长、备注需要补充说明。同时系统缺乏分类体系——所有卡券按批次或类型（`RESOURCE_USAGE` / `COUPON`）区分，无法满足"餐饮类""购物类"等业务分类需求。

## Goals / Non-Goals

**Goals:**
- 管理端可修改卡券的 `expireAt`（有效期）和 `remark`（备注），需记录操作审计
- 管理端可创建、编辑、删除自定义分类
- 管理端可将单个券或批量券归入分类、从分类移除、在分类间移动
- 券码列表支持按分类筛选

**Non-Goals:**
- 不修改 `voucherCode`（核销码）、`status`（状态变更走现有 cancel/verify 流程）、`holderId`（持有人不可变更）
- 不涉及 H5 端——编辑和分类是纯管理端功能
- 不改变现有批次的 `voucherType`（RESOURCE_USAGE / COUPON）逻辑——分类是独立维度，与类型正交

## Decisions

### 数据模型：独立的 `voucher_category` 表 + `voucher.category_id` 外键

- 分类存储为独立表而非枚举，支持管理员自定义名称，灵活性最高
- `category_id` 放在 `voucher` 表上，方便单条查询时直接 JOIN，避免多对多关系表的额外查询开销
- 一张券只能属于一个分类（业务需求是归类，不是标签）
- 备选：JSON/ENUM 列存储分类 → 拒绝，不可扩展且排序/查询困难

### API 设计：RESTful CRUD + 批量操作

- 分类 CRUD：标准 REST `/api/v1/categories`
- 单券归类：`PUT /api/v1/vouchers/{id}/category` body: `{ categoryId: Long|null }`
- 批量移动：`PUT /api/v1/vouchers/category/batch` body: `{ voucherIds: Long[], categoryId: Long }`
- 编辑：`PUT /api/v1/vouchers/{id}` body: `{ expireAt: String, remark: String }`
- 备选：合入 `PUT /api/v1/vouchers/{id}` 同时支持 categoryId → 拒绝，编辑和归类是独立操作，分离端点更清晰

### 安全：字段级白名单 + 审计日志

- 编辑接口仅接受 `expireAt` 和 `remark`，其余字段即使用了也不生效（白名单模式）
- 编辑操作写入 `audit_log` 表（已有表结构，operation 字段记录 "EDIT_VOUCHER"）
- 备用方案：用 DTO 类型系统限制 → 配合 service 层白名单双重保障

### 前端：分类管理独立页面 + 券码列表增强

- 侧边栏新增"分类管理"入口，独立页面管理 CRUD
- `EmployeeVouchers.vue` 增加：分类筛选下拉、行内归类选择器、表头复选框 + 批量移动按钮、编辑按钮 + 弹窗

## Risks / Trade-offs

- [分类被删除时的数据一致性] → `voucher.category_id` 设为可空外键，删除分类时将该分类下所有券的 `category_id` 置为 NULL，不级联删除
- [批量移动大量券时的性能] → 单次批量上限 500 条，超出提示分批操作
- [编辑有效期可能导致已过期券变为有效] → 这是预期行为——管理员应有权纠正输入错误，审计日志记录变更即可
