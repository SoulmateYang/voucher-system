## Why

管理端目前只能在批次管理中按批次查看券码，或在核销台逐张扫码查询。当管理员需要回答"某员工名下有哪些券"或"最近哪些券即将过期"时，必须逐个批次翻找——缺少一个统一按员工和券属性筛选的全局视图。

## What Changes

- 管理端侧边栏新增**「员工卡券」**菜单项
- 新增 `/employee-vouchers` 路由与 `EmployeeVouchers.vue` 页面组件
- 页面包含筛选区域（员工姓名、工号、卡券名称、券类型、到期时间范围）和分页表格
- 表格列：券码、持有人姓名、工号、卡券名称、券类型、面额（优惠券）、有效期、状态
- 后端新增 `GET /api/v1/vouchers` 接口，支持分页与多条件筛选
- 后端新增 `VoucherService.listAllPaged` 方法，基于动态 `LambdaQueryWrapper` 实现

## Capabilities

### New Capabilities
- `employee-voucher-browse`: 管理端按员工和券属性全局浏览、筛选、分页查看所有卡券

### Modified Capabilities
<!-- None — existing capabilities unchanged, this is a new page/endpoint -->

## Impact

- **前端 Admin**: 新增 `EmployeeVouchers.vue`，修改 `Layout.vue`（侧边栏菜单），修改 `router/index.js`（路由），新增 `api/voucher.js` 方法
- **后端**: `VoucherController` 新增 `GET /vouchers` 端点，`VoucherService` 新增 `listAllPaged` 方法，`VoucherMapper` 依赖已有的 `BaseMapper.selectPage`
- **无新依赖**，无 BREAKING 变更
