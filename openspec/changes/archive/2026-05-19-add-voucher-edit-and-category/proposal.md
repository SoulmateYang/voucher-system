## Why

当前系统创建卡券后无法修改任何信息（如延长有效期、更正备注），管理员只能作废后重新发放。同时系统缺乏分类体系，所有卡券扁平化展示，当批次和券码数量增长时，管理员和员工难以按业务场景（餐饮、购物、娱乐、储值等）快速筛选和组织。这两个能力是卡券管理系统的核心刚需。

## What Changes

### 新增卡券编辑功能
- 管理端新增 PUT `/api/v1/vouchers/{id}` 接口，支持修改 `expireAt`（有效期）和 `remark`（备注）
- `voucherCode`（核销码）、`status`（状态）、`holderId`（持有人）等安全敏感字段不可通过此接口修改
- 管理端券码列表新增"编辑"按钮，点击弹出编辑对话框

### 新增卡券分类管理
- 新增 `voucher_category` 表（id, name, sort_order, created_at, updated_at）
- 新增 `category_id` 字段到 `voucher` 表（可空外键）
- 新增分类 CRUD 接口：`GET/POST/PUT/DELETE /api/v1/categories`
- 新增券码归类接口：`PUT /api/v1/vouchers/{id}/category`（单个）和 `PUT /api/v1/vouchers/category/batch`（批量）
- 管理端新增"分类管理"页面，支持自定义分类名称
- 管理端券码列表新增分类筛选、单券归类、批量移动功能

## Capabilities

### New Capabilities
- `voucher-edit`: 卡券信息编辑——管理员修改有效期和备注，核销码等安全字段不可编辑
- `voucher-category`: 卡券分类管理——自定义分类 CRUD、单券归类、批量移动、分类筛选

### Modified Capabilities
<!-- 此变更不修改现有 spec 行为——编辑和分类是新增功能，不影响现有接口的契约 -->

## Impact

- **数据库**: 新建 `voucher_category` 表，`voucher` 表新增 `category_id` 列（Flyway 迁移脚本 V7）
- **后端新增**:
  - `VoucherCategory` entity + mapper + service
  - `CategoryController`（CRUD 接口）
  - `VoucherController` 新增 `PUT /{id}` 编辑接口、`PUT /{id}/category`、`PUT /category/batch`
  - `VoucherMapper` 新增批量更新 category、按分类筛选查询
- **管理端前端新增/修改**:
  - 新增 `CategoryManage.vue` 分类管理页面
  - 修改 `EmployeeVouchers.vue` 券码列表——新增编辑弹窗、分类筛选、归类按钮、批量移动
  - `Layout.vue` 侧边栏新增"分类管理"菜单项
  - API 层新增 `category.js`、`voucher.js` 新增编辑/归类接口
- **H5 端**: 无影响（编辑和分类为管理端功能）
