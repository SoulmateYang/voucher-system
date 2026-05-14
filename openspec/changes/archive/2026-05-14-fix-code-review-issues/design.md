## Context

The coupon feature branch (`feature_first_version`) passed code review with 4 issues identified. All are isolated to a few files and don't require architectural changes — they're defensive coding, UI logic, and consistency fixes.

Current state:
- `VoucherService.lookup()`, `verify()`, `getDetailById()` call `batchMapper.selectById()` without null check
- `VoucherService.verify()` uses error code `400` for coupon validation vs `4001-4004` elsewhere
- `VoucherList.vue` and `VoucherDetail.vue` use `faceValue > 0` to detect coupon type
- H5 list APIs return raw `Voucher` entities without `voucherType`
- `VoucherList.vue` tag label ignores status for coupons

## Goals / Non-Goals

**Goals:**
- Eliminate NPE risk from null batch references in VoucherService
- Fix VoucherList.vue status label to show actual voucher status for all types
- Replace fragile `faceValue > 0` proxy with explicit `voucherType` field in H5 views
- Unify error codes in `verify()` to use domain-specific codes consistently

**Non-Goals:**
- Changing the API contract beyond adding response fields
- Refactoring the overall service/controller architecture
- Adding new database columns or migrations
- Modifying admin frontend (BatchManage, VerifyDesk) — those already receive `voucherType` from APIs

## Decisions

### 1. Null check pattern: return `BusinessException` rather than allowing NPE

When `batchMapper.selectById()` returns null, throw `BusinessException` with a clear message (e.g., "批次不存在"). This matches the existing pattern used for null voucher checks in the same methods.

**Alternative considered**: Using `Optional` — rejected as it adds overhead without benefit in this codebase, which consistently uses null checks with exceptions.

### 2. H5 list APIs: add `voucherType` to response inline rather than creating DTOs

The `listByHolder` method returns `List<Voucher>` entities, and `listByHolderPaged` returns entity pages. Rather than creating new DTO classes (over-engineered for a bug fix), we enrich the existing `Map<String, Object>` response for `listByHolderPaged` and wrap `listByHolder` entries. Actually, since `listByHolder` returns raw entities and the H5 uses them directly via JSON serialization, we'll add a transient `voucherType` field to the response map or create a lightweight transformation.

**Decision**: For `listByHolderPaged`, batch-load voucher types in a single query (avoiding N+1) and add `voucherType` to each record map. For `listByHolder`, do the same transformation. This is a localized change in VoucherService.

### 3. Error code: use `4005` for coupon validation errors

Reserve `4005` for coupon-related business rule violations (order amount not provided, minimum order not met). This aligns with the existing `4001-4004` range.

### 4. VoucherList.vue tag: show status label for all types, add a separate coupon indicator

The tag text always shows the voucher status (e.g., "已发放", "已使用"). Coupon identity is communicated visually through card styling (`.is-coupon` class with `#fff7e6` background) and the face value display, which already differentiate coupons from resource vouchers.

## Risks / Trade-offs

- **Batch-loading voucher types for list queries adds a DB round-trip**: Mitigated by batching — one query for all batch IDs, then in-memory mapping. For paged queries, the batch set is bounded by page size.
- **Null batch legacy data**: Batches created before the `voucher_type` column will have `'RESOURCE_USAGE'` via the DEFAULT. No special handling needed.
