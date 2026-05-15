## Context

前端 `Reports.vue` 调用 `GET /api/v1/reports/summary` 和 `GET /api/v1/reports/daily-trend`，但后端不存在这两个端点。后端仅有 `/api/v1/vouchers/report`，且返回格式为原始聚合数据（`statusCounts`、`dailyIssue`、`dailyVerify`），与前端期望的 `{totalIssued, totalUsed, totalExpired, totalCancelled}` 和 `[{date, issuedCount, usedCount, expiredCount, cancelledCount, totalValid}]` 不兼容。

## Goals / Non-Goals

**Goals:**
- `GET /api/v1/reports/summary?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD` 返回汇总统计
- `GET /api/v1/reports/daily-trend?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD` 返回每日趋势数组
- 返回数据格式与前端 `Reports.vue` 现有绑定字段完全对齐

**Non-Goals:**
- 不修改前端任何代码（API 路径和数据格式保持不变）
- 不修改现有 `/api/v1/vouchers/report` 端点
- 不引入缓存或性能优化

## Decisions

### 决策 1：新增 ReportsController 而非修改前端路径

**选择**: 新增 `ReportsController` 监听 `/api/v1/reports`，保持前端路径不变。

**理由**:
- 前端路径 `/reports/summary` 符合 RESTful 资源命名直觉
- 新增控制器是纯增量操作，零风险影响现有 `/api/v1/vouchers/*` 路由
- 前端无需重新构建部署

**替代方案**: 修改前端 `voucher.js` 指向 `/vouchers/report`。
- 否决理由：后端返回格式不兼容，改路径治标不治本。

### 决策 2：端点接受 String 日期而非 LocalDateTime

**选择**: Controller 接受 `@RequestParam String startDate` + `String endDate`（格式 `YYYY-MM-DD`），手动解析为 `LocalDate` 后转为 `LocalDateTime` 范围（startDate 00:00:00 ~ endDate 23:59:59）。

**理由**: 前端 `value-format="YYYY-MM-DD"` 发送的就是此格式，Controller 直接匹配，无需前端适配。

### 决策 3：Summary 数据转换方式

**选择**: 从 `countByStatus` 返回的 `[{status: "ISSUED", cnt: 10}, ...]` 列表中遍历提取各状态计数。

```java
long totalIssued = 0, totalUsed = 0, totalExpired = 0, totalCancelled = 0;
for (Map<String, Object> row : statusCounts) {
    switch ((String) row.get("status")) {
        case "ISSUED":   totalIssued = (long) row.get("cnt"); break;
        case "USED":     totalUsed = (long) row.get("cnt"); break;
        case "EXPIRED":  totalExpired = (long) row.get("cnt"); break;
        case "CANCELLED": totalCancelled = (long) row.get("cnt"); break;
    }
}
```

### 决策 4：Daily Trend 数据合并方式

**选择**: 将四个数据源（issue/verify/expired/cancelled）按日期合并为 TreeMap 保证日期有序。

1. 从四个 Mapper 方法分别获取每日计数列表
2. 收集所有日期 → 遍历四个列表填入计数
3. 计算 `totalValid = 截止当日累计发放 - 累计核销 - 累计过期 - 累计作废`
4. 输出 `[{date, issuedCount, usedCount, expiredCount, cancelledCount, totalValid}]`

## Risks / Trade-offs

- **[低风险] 并发下 totalValid 可能短暂不一致**: 在报表查询期间如有新核销发生，累计值可能有微小偏差。→ 报表为统计快照场景，可接受最终一致性。

- **[低风险] 大日期范围性能**: 日跨度超过 1 年时每日趋势行数较多。→ 不作限制，后续按需加分页参数。

## Migration Plan

1. 新增 `VoucherMapper` 两个查询方法（dailyExpiredCount、dailyCancelledCount）
2. 在 `ReportService` 新增 `getSummary()`、`getDailyTrend()` 方法
3. 新增 `ReportsController` 注册 `/api/v1/reports/summary` 和 `/api/v1/reports/daily-trend`
4. 重启后端，前端刷新即可恢复

**回滚**: 删除新增的三个文件即可。
