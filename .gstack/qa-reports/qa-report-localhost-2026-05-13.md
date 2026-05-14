# QA Report — 因私卡券管理系统

**Date:** 2026-05-13
**Target:** http://localhost:3000 (Admin) / http://localhost:3001 (H5) / http://localhost:8080 (API)
**Framework:** Vue 3 SPA + Spring Boot + MySQL
**Pages visited:** 6 (Admin: login, batch, verify, reports; H5: login, voucher list)
**Tier:** Standard
**Duration:** ~30 min

## Health Score

| Category | Score | Weight | Weighted |
|----------|-------|--------|----------|
| Console | 85 | 15% | 12.75 |
| Links | 100 | 10% | 10.00 |
| Visual | 85 | 10% | 8.50 |
| Functional | 20 | 20% | 4.00 |
| UX | 60 | 15% | 9.00 |
| Performance | 90 | 10% | 9.00 |
| Content | 90 | 5% | 4.50 |
| Accessibility | 80 | 15% | 12.00 |
| **TOTAL** | | | **69.75 / 100** |

## Summary

| Severity | Count |
|----------|-------|
| Critical | 4 |
| High | 2 |
| Medium | 2 |
| Low | 0 |
| **Total** | **8** |

## Top 3 Things to Fix

1. **CRITICAL — API path mismatch makes admin frontend non-functional:** Every admin API call uses wrong paths (/api/v1/batches vs /api/v1/admin/batches). No CRUD operations work through the UI.
2. **CRITICAL — H5 employee login impossible:** Field name mismatch (employeeId vs username) + no employee accounts in database.
3. **CRITICAL — Verify flow broken:** Backend one-step verify vs frontend two-step lookup+confirm. Confirm endpoint doesn't exist.

---

## Issues Found

### ISSUE-001 [CRITICAL] — Admin frontend API paths don't reach backend

**Category:** Functional
**Pages affected:** All admin pages (batch, verify, reports)
**Evidence:** Create batch dialog stays open after clicking "创建" — API call goes to `/api/v1/batches` but backend has `/api/v1/admin/batches`. No visible error to user.
**Repro:**
1. Login as admin on :3000
2. Click "创建批次", fill form, click "创建"
3. Dialog stays open — API call silently fails
**Root cause:** `frontend-admin/src/api/batch.js` calls `/batches` (→ `/api/v1/batches`) but `VoucherBatchController` maps to `/api/v1/admin/batches`. Same for all voucher API calls in `api/voucher.js`.

### ISSUE-002 [CRITICAL] — H5 employee login fails with field name mismatch

**Category:** Functional
**Pages affected:** H5 login (/login)
**Evidence:** Login with employeeId:E001 fails. Console shows Vue unhandled error. Backend returns "用户名不能为空" because LoginRequest validates `username` field but H5 sends `employeeId`.
**Repro:**
1. Go to http://localhost:3001
2. Enter 工号:"E001", 密码:"test123", click "登 录"
3. Login fails, stays on login page

### ISSUE-003 [CRITICAL] — No employee accounts exist

**Category:** Functional
**Pages affected:** H5 app (all pages)
**Evidence:** `sys_user` table has only one admin account (id=1). No employee accounts. Even if login field name is fixed, employees can't authenticate.
**Root cause:** No employee registration, no CSV import, no seed employee accounts.

### ISSUE-004 [CRITICAL] — Verify flow: 1-step backend vs 2-step frontend

**Category:** Functional
**Pages affected:** Verify desk
**Evidence:** Frontend implements scan→lookup→confirm flow. Backend `POST /api/v1/admin/vouchers/verify` does both lookup AND state mutation in one call. Frontend's `POST /vouchers/confirm` has no backend endpoint.

### ISSUE-005 [HIGH] — Batch totalCount double-incremented

**Category:** Functional
**Pages affected:** Batch management
**Evidence:** Issued 2 vouchers via API → batch totalCount shows 4.
**Root cause:** `VoucherService.issueBatch()` dead code mutates Java object totalCount, then `VoucherBatchService.issue()` adds again via `setTotalCount + updateById`.

### ISSUE-006 [HIGH] — H5 field names don't match backend entity fields

**Category:** Functional
**Pages affected:** H5 voucher list, voucher detail
**Evidence:** H5 accesses `.resourceName`, `.expireTime`, `.issueTime`, `.resourceDescription`, `.approvalNo`, `.verificationTime` — none exist on backend entities (correct: `.resourceDesc`, `.expireAt`, `.issuedAt`, `.approveRef`, `.usedAt`). Every value will show "--" or blank.

### ISSUE-007 [MEDIUM] — Reports data pipeline broken

**Category:** Functional
**Pages affected:** Reports page
**Evidence:** Frontend calls `/reports/summary` and `/reports/daily-trend` with params `startDate`/`endDate` as YYYY-MM-DD. Backend endpoint at `/api/v1/admin/vouchers/report` expects `start`/`end` in ISO datetime format. Export button likely does client-side CSV, never calling backend EasyExcel.

### ISSUE-008 [MEDIUM] — Admin batch status toggle is UI-only placebo

**Category:** Functional
**Pages affected:** Batch management
**Evidence:** Toggle switch in batch table has API call commented out (`// await updateBatchStatus(...)`). Toggle changes local state only — refreshing reverts to server value.

---

## Screenshots

| File | Description |
|------|-------------|
| screenshots/initial-admin.png | Admin login page (clean, no errors) |
| screenshots/admin-batch-page.png | Batch management page (empty state) |

## Console Health

- **Admin login page:** No errors
- **Batch manage page:** No errors (silent API failures)
- **Verify desk page:** No errors
- **Reports page:** No errors (silent API failures)
- **H5 login page:** Vue unhandled error on login submit

## Baseline

```json
{
  "date": "2026-05-13",
  "url": "localhost:3000, localhost:3001, localhost:8080",
  "healthScore": 69.75,
  "issues": [
    {"id": "ISSUE-001", "severity": "critical", "category": "functional"},
    {"id": "ISSUE-002", "severity": "critical", "category": "functional"},
    {"id": "ISSUE-003", "severity": "critical", "category": "functional"},
    {"id": "ISSUE-004", "severity": "critical", "category": "functional"},
    {"id": "ISSUE-005", "severity": "high", "category": "functional"},
    {"id": "ISSUE-006", "severity": "high", "category": "functional"},
    {"id": "ISSUE-007", "severity": "medium", "category": "functional"},
    {"id": "ISSUE-008", "severity": "medium", "category": "functional"}
  ]
}
```

## Fix Status

All 8 issues require source code fixes (documented with file:line in /plan-eng-review). None are third-party or infrastructure issues.
