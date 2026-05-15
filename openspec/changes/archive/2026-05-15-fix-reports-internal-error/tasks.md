## 1. Backend — Mapper 查询补充

- [x] 1.1 在 `VoucherMapper.java` 新增 `dailyExpiredCount` 方法：按 `expire_at` 日期统计到期券数
- [x] 1.2 在 `VoucherMapper.java` 新增 `dailyCancelledCount` 方法：按 `cancelled_at` 日期统计作废券数

## 2. Backend — Service 数据转换

- [x] 2.1 在 `ReportService.java` 新增 `getSummary()` 方法：将 `countByStatus` 原始列表转换为 `{totalIssued, totalUsed, totalExpired, totalCancelled}` 格式
- [x] 2.2 在 `ReportService.java` 新增 `getDailyTrend()` 方法：合并 issue/verify/expired/cancelled 四个数据源为按日聚合的 `[{date, issuedCount, usedCount, expiredCount, cancelledCount, totalValid}]` 格式

## 3. Backend — Controller 新端点

- [x] 3.1 新建 `ReportsController.java`，注册 `GET /api/v1/reports/summary` 和 `GET /api/v1/reports/daily-trend` 两个端点，接受 `startDate`/`endDate` 参数（YYYY-MM-DD 格式），调用 ReportService 并返回 `Result.success()`

## 4. Verification

- [x] 4.1 构建 Admin 前端，确认无编译错误
- [x] 4.2 审查后端代码，确认 SQL 查询、数据转换、Controller 参数绑定逻辑正确
