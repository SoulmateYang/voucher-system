## Why

Code review of the coupon feature branch identified 4 issues that impact stability, user experience, and code consistency. Fixing these before merging ensures the feature is robust and the codebase remains maintainable.

## What Changes

- Add null-safety checks for `batchMapper.selectById()` in VoucherService (NPE risk in `lookup`, `verify`, `getDetailById`)
- Fix VoucherList.vue to always show actual voucher status label instead of always displaying "优惠券" for coupons
- Add `voucherType` field to H5 list API responses so the frontend can distinguish coupon types reliably, replacing the fragile `faceValue > 0` check
- Unify error codes in `verify()` — change coupon validation errors from `400` to a domain-specific code (`4005`)

## Capabilities

### New Capabilities
- `voucher-list-type-field`: H5 list APIs (`listByHolder`, `listByHolderPaged`) include `voucherType` in each record's response, enabling reliable type-based rendering in VoucherList.vue and VoucherDetail.vue

### Modified Capabilities
<!-- No existing spec-level requirements are changing -->

## Impact

- **Backend**: `VoucherService.java` (null checks, error codes, list API response enrichment)
- **Frontend H5**: `VoucherList.vue` (status label fix, `voucherType`-based checks), `VoucherDetail.vue` (`voucherType`-based checks)
- **No API breaking changes** — all modifications are additive (extra fields in responses) or behavioral fixes
