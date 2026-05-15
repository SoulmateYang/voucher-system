## ADDED Requirements

### Requirement: 报表汇总统计

系统 SHALL 提供 `GET /api/v1/reports/summary` 端点，返回指定日期范围内的发放、核销、过期、作废汇总计数。

#### Scenario: 查询有数据的日期范围

- **WHEN** 管理员请求 `GET /api/v1/reports/summary?startDate=2026-01-01&endDate=2026-01-31`
- **THEN** 系统返回 `{"code": 0, "data": {"totalIssued": 50, "totalUsed": 30, "totalExpired": 5, "totalCancelled": 2}}`

#### Scenario: 查询无数据的日期范围

- **WHEN** 管理员请求无任何券记录的日期范围
- **THEN** 系统返回 `{"code": 0, "data": {"totalIssued": 0, "totalUsed": 0, "totalExpired": 0, "totalCancelled": 0}}`

### Requirement: 每日趋势数据

系统 SHALL 提供 `GET /api/v1/reports/daily-trend` 端点，返回指定日期范围内每日的发放、核销、过期、作废数量及有效存量。

#### Scenario: 查询每日趋势

- **WHEN** 管理员请求 `GET /api/v1/reports/daily-trend?startDate=2026-01-01&endDate=2026-01-07`
- **THEN** 系统返回 `{"code": 0, "data": [{"date": "2026-01-01", "issuedCount": 10, "usedCount": 3, "expiredCount": 0, "cancelledCount": 0, "totalValid": 7}, ...]}`，按日期升序排列

#### Scenario: 有效存量正确计算

- **WHEN** 某日期发放 10 张、核销 3 张、过期 1 张
- **THEN** 该日 totalValid = 截至当日累计发放 - 累计核销 - 累计过期 - 累计作废

### Requirement: 接口鉴权

`/api/v1/reports/*` 端点 SHALL 要求管理员角色认证。

#### Scenario: 未登录访问被拒绝

- **WHEN** 未携带有效 token 请求 reports 接口
- **THEN** 系统返回 HTTP 401
