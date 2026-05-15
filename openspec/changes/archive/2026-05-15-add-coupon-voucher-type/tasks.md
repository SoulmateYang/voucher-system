## 1. Database Migration

- [x] 1.1 Create Flyway V2 migration script: ALTER TABLE voucher_batch ADD discount_type, discount_value, min_order_amount columns; ALTER TABLE voucher ADD face_value column; all nullable with defaults NULL

## 2. Backend — Entity & DTO

- [x] 2.1 Update VoucherBatch entity: add discountType (String), discountValue (BigDecimal), minOrderAmount (BigDecimal) fields
- [x] 2.2 Update Voucher entity: add faceValue (BigDecimal) field
- [x] 2.3 Create VoucherType constants class with COUPON, RESOURCE_USAGE and static validation method
- [x] 2.4 Update CreateBatchRequest DTO: add discountType, discountValue, minOrderAmount, faceValue fields with validation annotations
- [x] 2.5 Update IssueVoucherRequest: ensure batchId lookup passes discount context through to issueSingle

## 3. Backend — Service Layer

- [x] 3.1 Update VoucherBatchService.create(): persist coupon discount fields when voucherType is COUPON, validate discountType is FIXED_AMOUNT or PERCENTAGE
- [x] 3.2 Update VoucherService.issueSingle(): set faceValue on new Voucher based on batch discountType (FIXED_AMOUNT → discountValue, PERCENTAGE → faceValue cap from batch)
- [x] 3.3 Update VoucherService.verify(): add coupon verification logic — check voucherType, validate minOrderAmount if applicable, calculate discountAmount for PERCENTAGE coupons (capped by faceValue), require orderAmount for percentage coupons
- [x] 3.4 Update VoucherService.lookup(): include coupon-specific fields (faceValue, discountType, minOrderAmount) in lookup result

## 4. Backend — Controller

- [x] 4.1 Update VoucherController.confirm(): accept optional orderAmount in request body
- [x] 4.2 Update VoucherBatchController.create(): pass coupon fields through to service

## 5. Frontend Admin — Batch Management

- [x] 5.1 Update BatchManage.vue create/edit dialog: add voucherType selector (因私使用 / 优惠券), show/hide coupon discount form fields based on selection
- [x] 5.2 Update batch list columns: show voucherType tag with distinct colors (优惠券 orange, 因私使用 blue)
- [x] 5.3 Add batch detail view: show discount rules summary for coupon batches

## 6. Frontend Admin — Verify Desk

- [x] 6.1 Update VerifyDesk.vue: add orderAmount input field (shown only when scanned voucher is percentage coupon)
- [x] 6.2 Update verify result display: show discountAmount, voucherType in confirmation result

## 7. Frontend H5 — Voucher Display

- [x] 7.1 Update VoucherList.vue: coupon cards show faceValue prominently, discount type badge, usage condition text
- [x] 7.2 Update VoucherDetail.vue: full coupon info display — faceValue, discountType, discountValue, minOrderAmount, colored background per DESIGN.md
- [x] 7.3 Update qrcode display: ensure QR code still renders voucherCode for all voucher types

## 8. Verification (manual — requires MySQL + Maven + npm)

- [x] 8.1 Start backend and verify POST /api/v1/batches creates coupon batch with all discount fields
- [x] 8.2 Verify POST /api/v1/batches/issue creates vouchers with correct faceValue
- [x] 8.3 Verify POST /api/v1/vouchers/confirm works for fixed-amount coupon, percentage coupon, and rejects below-minimum orders
- [x] 8.4 Start admin frontend and walk through create coupon batch → issue → verify flow
- [x] 8.5 Start H5 frontend and verify coupon list/detail display
