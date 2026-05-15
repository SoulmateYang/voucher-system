## Why

统计报表页面访问时报"内部错误"——前端调用的 `/api/v1/reports/summary` 和 `/api/v1/reports/daily-trend` 两个接口在后端根本不存在，404 经 GlobalExceptionHandler 兜底后展示为内部错误。此外后端虽有 `/api/v1/vouchers/report` 接口，但返回格式与前端期望完全不一致。

## What Changes

- **新增 ReportsController**: 在 `/api/v1/reports` 下提供 `/summary` 和 `/daily-trend` 两个端点，返回前端期望的数据格式
- **更新 ReportService**: 新增 `getSummary()` 和 `getDailyTrend()` 方法，将 Mapper 原始聚合数据转换为前端约定的格式
- **更新 VoucherMapper**: 新增过期、作废按日统计的 SQL 查询，补全每日趋势所需的全部维度
- **前端 API 路径不变**: 前端 `voucher.js` 中已有的 `/reports/summary` 和 `/reports/daily-trend` 路径保持不变，仅需后端对齐

## Capabilities

### New Capabilities
- `reports-api`: 统计报表 API，提供按日期范围的汇总统计（发放/核销/过期/作废）和每日趋势数据

### Modified Capabilities
<!-- 无现有 capability 的 spec 级别变更 -->

## Impact

- `backend/.../controller/` — 新增 `ReportsController.java`
- `backend/.../service/ReportService.java` — 新增 `getSummary()`、`getDailyTrend()` 方法
- `backend/.../mapper/VoucherMapper.java` — 新增 `dailyExpiredCount`、`dailyCancelledCount` SQL 查询
- `frontend-admin/src/api/voucher.js` — 无需修改（路径已对）
- `frontend-admin/src/views/Reports.vue` — 无需修改
