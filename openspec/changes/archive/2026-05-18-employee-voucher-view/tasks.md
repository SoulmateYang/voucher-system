## 1. 后端 — DTO 与 Mapper

- [x] 1.1 创建 `VoucherListRow` DTO，包含 Voucher 字段 + `batchName` + `voucherType`
- [x] 1.2 在 `VoucherMapper` 中新增 `selectPagedWithBatch` 方法，使用 `@Select` + 动态 SQL（LEFT JOIN voucher_batch），支持多条件拼接和分页

## 2. 后端 — Service 与 Controller

- [x] 2.1 在 `VoucherService` 中新增 `listAllPaged` 方法，接收所有筛选参数，调用 Mapper 返回 `IPage<VoucherListRow>`
- [x] 2.2 在 `VoucherController` 中新增 `GET /api/v1/vouchers` 端点，接收 query params 并调用 `listAllPaged`，返回分页结果

## 3. 前端 — API 与路由

- [x] 3.1 在 `api/voucher.js` 中新增 `getVoucherList(params)` 函数，调用 `GET /vouchers`
- [x] 3.2 在 `router/index.js` 中新增 `/employee-vouchers` 子路由，meta.title 为「员工卡券」
- [x] 3.3 在 `Layout.vue` 侧边栏新增「员工卡券」菜单项，使用 `Collection` 图标，`index="/employee-vouchers"`，放在统计报表和员工管理之间

## 4. 前端 — 页面组件

- [x] 4.1 创建 `EmployeeVouchers.vue`，使用 Reports.vue 的页面布局模式（page-header + card + table）
- [x] 4.2 实现筛选栏：工号输入框、姓名输入框、关键词输入框、券类型下拉（全部/优惠券/因私使用）、到期起始日期、到期截止日期、搜索按钮、重置按钮
- [x] 4.3 实现 el-table，列包含：券码（monospace）、持有人姓名、工号、卡券名称（remark）、券类型标签、面额（优惠券时显示）、有效期（YYYY-MM-DD）、状态标签
- [x] 4.4 实现分页组件（el-pagination），绑定 page/pageSize/total
- [x] 4.5 实现查询逻辑：组装 params → 调用 getVoucherList → 更新表格数据和 total，支持筛选条件变化自动查询

## 5. 验证

- [x] 5.1 启动后端和管理端，验证菜单显示和页面加载
- [x] 5.2 验证各筛选条件独立和组合查询正确
- [x] 5.3 验证分页功能正常
- [x] 5.4 验证面额列仅在优惠券时显示
