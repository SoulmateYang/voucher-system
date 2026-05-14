## 1. Backend — Null safety for batch lookup

- [x] 1.1 Add null check after `batchMapper.selectById()` in `lookup()` — throw `BusinessException("批次不存在")`
- [x] 1.2 Add null check after `batchMapper.selectById()` in `verify()` — throw `BusinessException("批次不存在")`
- [x] 1.3 Add null check after `batchMapper.selectById()` in `getDetailById()` — return null (maintaining existing "voucher not found" semantics)

## 2. Backend — Error code consistency

- [x] 2.1 Change error code `400` to `4005` for coupon order-amount validation in `verify()` (percentage coupon without order amount)
- [x] 2.2 Change error code `400` to `4005` for coupon min-order validation in `verify()` (order below minimum)

## 3. Backend — Add voucherType to H5 list API responses

- [x] 3.1 Modify `listByHolder()` to return `List<Map<String, Object>>` instead of `List<Voucher>`, including `voucherType` from batch lookup (batch query to avoid N+1)
- [x] 3.2 Modify `listByHolderPaged()` to include `voucherType` in each record by batch-loading types for the current page
- [x] 3.3 Verify `EmployeeVoucherController` and any other callers of `listByHolder` / `listByHolderPaged` are compatible with the updated return types

## 4. Frontend H5 — Fix VoucherList.vue and VoucherDetail.vue

- [x] 4.1 Update `VoucherList.vue` to use `voucherType === 'COUPON'` instead of `faceValue > 0` for all coupon detection
- [x] 4.2 Fix `VoucherList.vue` tag label to always show `statusLabel(item.status)` regardless of coupon type
- [x] 4.3 Add a separate coupon indicator in `VoucherList.vue` (keep the `.is-coupon` card class for visual differentiation)
- [x] 4.4 Update `VoucherDetail.vue` to use `voucherType === 'COUPON'` instead of `faceValue > 0` for coupon detection (if applicable — verify current usage)

## 5. Verification

- [x] 5.1 Start backend and verify `lookup`, `verify`, `listByHolder`, `listByHolderPaged` APIs return correct data for both coupon and resource vouchers
- [x] 5.2 Verify H5 VoucherList shows correct status labels for all voucher states (issued, used, expired, cancelled) for both types
- [x] 5.3 Verify H5 VoucherDetail renders correctly for both coupon and resource vouchers
