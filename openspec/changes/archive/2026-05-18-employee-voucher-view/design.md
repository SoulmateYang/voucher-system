## Context

管理端当前仅支持按批次查看券码（`BatchManage.vue`）和单码扫码查询（`VerifyDesk.vue`），缺乏跨批次、按员工维度的全局券码浏览能力。现有侧边栏有 4 个菜单项（卡券管理、核销台、统计报表、员工管理），需要新增第 5 项。

后端 `VoucherService.listByHolderPaged` 已支持按持有人查询和分页，但限定单个 `holderId`。需新增一个不限定持有人的全局查询方法，并支持按员工姓名、工号、券类型等条件筛选。

## Goals / Non-Goals

**Goals:**
- 管理端新增"员工卡券"菜单和页面，展示所有已发放的卡券（跨批次、跨员工）
- 支持按员工姓名、工号、卡券名称/券码、券类型、到期时间范围筛选
- 支持分页浏览（默认每页 20 条）
- 后端接口支持上述所有筛选条件的动态组合

**Non-Goals:**
- 不在此页面提供核销、作废等操作（仅浏览）
- 不提供导出功能（后续迭代）
- 不修改 H5 端

## Decisions

### 1. API 设计

新端点 `GET /api/v1/vouchers`，query params:

| 参数 | 类型 | 说明 |
|------|------|------|
| holderId | String | 工号，模糊匹配 |
| holderName | String | 姓名，模糊匹配 |
| keyword | String | 券码或卡券名称（remark），模糊匹配 |
| voucherType | String | `COUPON` / `RESOURCE_USAGE` |
| expireStart | String | 到期起始日期 (YYYY-MM-DD) |
| expireEnd | String | 到期截止日期 (YYYY-MM-DD) |
| page | int | 页码，默认 1 |
| pageSize | int | 每页条数，默认 20 |

返回 `PageResult<VoucherListRow>`，`VoucherListRow` 包含：voucher 所有字段 + `batchName` + `voucherType`。

**替代方案**: 分为两个接口（查询列表 + 查询批次信息再合并）—— 放弃，多余的 HTTP 往返。

### 2. 券类型筛选实现

`voucherType` 存储在 `voucher_batch` 表而非 `voucher` 表。后端查询时使用 LEFT JOIN：

```sql
SELECT v.*, b.batch_name, b.voucher_type
FROM voucher v
LEFT JOIN voucher_batch b ON v.batch_id = b.id
WHERE ...
ORDER BY v.created_at DESC
LIMIT ?, ?
```

使用 MyBatis `@Select` 注解 + 动态 SQL（`<script>` + `<if>`）实现条件组合。

**替代方案**: 在 `LambdaQueryWrapper.apply()` 中写子查询 `batch_id IN (SELECT id FROM voucher_batch WHERE voucher_type = ?)` —— 放弃，JOIN 更清晰且一次查询即可拿到 batchName 和 voucherType。

### 3. 前端筛选栏布局

使用 `el-form` inline 布局，一行排列筛选字段：

```
[工号输入框] [姓名输入框] [关键词输入框] [券类型下拉] [到期开始日期] [到期截止日期] [搜索按钮] [重置按钮]
```

筛选条件变化后自动触发查询（watch + debounce 300ms），避免手动点击搜索按钮。

### 4. 菜单图标与位置

新增菜单项放在"统计报表"和"员工管理"之间，使用 `Collection` 图标（Element Plus），路由 `/employee-vouchers`。

### 5. 分页策略

后端使用 MyBatis-Plus `Page` + `IPage`，前端使用 `el-pagination`。默认每页 20 条，与批次管理页面一致。

## Risks / Trade-offs

- **LEFT JOIN 可能影响大表性能**: 初期数据量小（< 10万条），单表索引即可覆盖。后续数据增长时可在 `voucher.batch_id` 和 `voucher.holder_id` 上建立索引
- **券类型筛选依赖 JOIN**: 与批次表耦合，若 batch 被删除则 voucher 的 voucherType 为 NULL——业务上批次不允许删除，风险低
