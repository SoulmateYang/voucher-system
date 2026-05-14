# TODOS — 因私卡券管理系统

## Pending

### TODO-1: CSV import for employee bulk entry
- **What:** Add CSV file upload to the issue-voucher flow. Admin uploads a CSV (employeeId, employeeName columns), system parses and populates the employee list in the issue dialog.
- **Why:** The plan specifies CSV import as the primary bulk-entry method. Currently only manual per-employee entry exists in the UI, making batch operations tedious.
- **Pros:** Dramatically faster bulk issuance. Matches plan spec. Reduces manual data entry errors.
- **Cons:** Adds file upload handling, CSV parsing edge cases (encoding, BOM, empty rows), and validation UI.
- **Context:** Backend needs a CSV parse endpoint or frontend can parse client-side with Papa Parse. The batch issue API already accepts employee arrays, so the backend change is minimal.
- **Depends on:** —
- **Blocked by:** —

### TODO-2: Batch status toggle backend endpoint
- **What:** Add a `PUT /api/v1/admin/batches/{id}/status` endpoint that changes batch status between ACTIVE/PAUSED. Wire it to the frontend toggle in BatchManage.vue.
- **Why:** The toggle UI exists but the API call is commented out. Toggling a batch only changes local state — refreshing the page reverts it. Without this, there's no way to pause a batch to prevent further issuance.
- **Pros:** Closes a visible feature gap. Low effort. Prevents accidental issuance from deprecated batches.
- **Cons:** Needs status transition validation (can't re-activate FINISHED batches).
- **Context:** VoucherBatch entity has status field with ACTIVE/PAUSED/FINISHED values. The frontend toggle is in BatchManage.vue:251-263 with the API call commented out at line 255.
- **Depends on:** —
- **Blocked by:** —

### TODO-3: H5 employee usage records view
- **What:** Add a "使用记录" page in the H5 app showing the employee's voucher usage history (which vouchers were used, when, for what resource). Currently stubbed as "功能开发中" toast.
- **Why:** The plan's architecture diagram lists "使用记录" as an H5 feature. The tabbar already has a button for it, creating an expectation that's not met.
- **Pros:** Completes the H5 feature set. Employees can track their usage history. Low effort since verification_log data already exists.
- **Cons:** Needs a new API endpoint to query verification_log by holder_id. Minor UI work.
- **Context:** The `verification_log` table already tracks voucher_id, holder_id, verified_at. A simple endpoint returning recent verifications for the authenticated employee is sufficient.
- **Depends on:** —
- **Blocked by:** —
