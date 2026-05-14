## ADDED Requirements

### Requirement: H5 list APIs include voucherType in each record

The H5-facing list APIs (`listByHolder` and `listByHolderPaged`) SHALL include a `voucherType` field in every record returned, sourced from the associated `VoucherBatch`. This enables the H5 frontend to reliably distinguish between RESOURCE_USAGE and COUPON vouchers without relying on indirect heuristics.

#### Scenario: List returns voucherType for coupon
- **WHEN** the H5 calls `listByHolderPaged` or `listByHolder` for a user who holds a COUPON-type voucher
- **THEN** each record in the response SHALL contain `voucherType: "COUPON"`

#### Scenario: List returns voucherType for resource voucher
- **WHEN** the H5 calls `listByHolderPaged` or `listByHolder` for a user who holds a RESOURCE_USAGE voucher
- **THEN** each record in the response SHALL contain `voucherType: "RESOURCE_USAGE"`

#### Scenario: List handles missing batch gracefully
- **WHEN** a voucher's `batchId` references a batch that no longer exists
- **THEN** the record SHALL contain `voucherType: null` and SHALL NOT throw an exception
