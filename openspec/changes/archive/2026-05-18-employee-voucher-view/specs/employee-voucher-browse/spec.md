## ADDED Requirements

### Requirement: Admin can access employee voucher browse page

管理端 SHALL 提供"员工卡券"菜单入口，管理员可进入卡券全局浏览页面。

#### Scenario: Menu item visible in sidebar
- **WHEN** 管理员登录管理端
- **THEN** 侧边栏显示"员工卡券"菜单项（位于统计报表和员工管理之间）
- **THEN** 点击后跳转至 `/employee-vouchers` 路由

#### Scenario: Page displays filter bar and table
- **WHEN** 管理员进入员工卡券页面
- **THEN** 页面显示筛选区域（工号、姓名、关键词、券类型、到期起始、到期截止）和空态表格

### Requirement: Admin can filter vouchers by employee attributes

系统 SHALL 支持按员工工号和姓名筛选卡券。

#### Scenario: Filter by employee ID
- **WHEN** 管理员在工号输入框输入关键词并触发查询
- **THEN** 表格展示 `holderId` 包含该关键词的卡券，分页显示

#### Scenario: Filter by employee name
- **WHEN** 管理员在姓名输入框输入关键词并触发查询
- **THEN** 表格展示 `holderName` 包含该关键词的卡券，分页显示

#### Scenario: Combined employee filters
- **WHEN** 管理员同时输入工号和姓名
- **THEN** 表格展示同时满足工号和姓名条件的卡券（AND 逻辑）

### Requirement: Admin can filter vouchers by voucher attributes

系统 SHALL 支持按卡券名称/券码、券类型和到期时间范围筛选。

#### Scenario: Filter by keyword
- **WHEN** 管理员在关键词输入框输入券码或卡券名称片段
- **THEN** 表格展示 `voucherCode` 或 `remark` 包含该关键词的卡券

#### Scenario: Filter by voucher type
- **WHEN** 管理员选择券类型为「优惠券」
- **THEN** 表格仅展示类型为 `COUPON` 的卡券（含面额列）
- **WHEN** 管理员选择「因私使用」
- **THEN** 表格仅展示类型为 `RESOURCE_USAGE` 的卡券

#### Scenario: Filter by expiry date range
- **WHEN** 管理员选择到期起始日期和截止日期
- **THEN** 表格展示 `expireAt` 在该日期范围内的卡券

### Requirement: Voucher list table shows complete information

卡券列表表格 SHALL 展示完整字段。

#### Scenario: Table columns
- **WHEN** 管理员进入员工卡券页面
- **THEN** 表格包含以下列：券码（monospace）、持有人姓名、工号、卡券名称、券类型标签、面额（优惠券）、有效期（YYYY-MM-DD）、状态标签
- **THEN** 表格支持分页，默认每页 20 条

### Requirement: Backend API supports global voucher query

后端 SHALL 提供 `GET /api/v1/vouchers` 接口支持多条件分页查询。

#### Scenario: Query with filters
- **WHEN** 前端请求 `GET /api/v1/vouchers?holderName=张&voucherType=COUPON&page=1&pageSize=20`
- **THEN** 后端返回符合条件的卡券分页数据，每行包含 voucher 字段 + `batchName` + `voucherType`

#### Scenario: Empty query returns all vouchers
- **WHEN** 前端请求 `GET /api/v1/vouchers` 无任何筛选参数
- **THEN** 后端返回所有卡券按创建时间倒序分页
- **THEN** 返回数据结构为 `{ code: 0, data: { records: [...], total: N, current: 1, size: 20 } }`
