## Why

当前系统仅支持「因私使用」单一券类型，无法满足企业内部优惠券（如餐饮折扣、购物满减）的发放需求。券批次表已预留 `voucher_type` 字段，扩展优惠券类型可复用现有批次→券实例→核销基础设施，增量成本低。

## What Changes

- 新增 `COUPON` 券类型，与现有 `RESOURCE_USAGE` 并存，通过 `voucher_batch.voucher_type` 区分
- 批次表增加优惠券特有字段：折扣类型（满减/折扣）、优惠金额/折扣率、使用门槛
- 券实例增加面额字段，记录每张券的实际优惠价值
- 核销流程兼容优惠券场景，核销时返回优惠金额信息
- H5 端券详情页展示优惠券专有信息（面额、使用条件）
- PC 管理端批次创建支持选择券类型，优惠券需额外填写折扣规则

## Capabilities

### New Capabilities

- `coupon-batch-management`: 优惠券批次创建与管理，含折扣规则配置
- `coupon-verification`: 优惠券核销，核销时计算并展示优惠金额
- `coupon-h5-display`: H5 端优惠券展示，含面额、使用条件、有效期等视觉区分

### Modified Capabilities

（无，现有功能无需求级变更）

## Impact

- **数据库**: `voucher_batch` 表新增 3 个字段；`voucher` 表新增 `face_value` 字段；需新增 Flyway 迁移脚本 V2
- **后端**: VoucherBatch entity/mapper/service、VoucherService.verify()、VoucherCodeUtil（复用，无需改）
- **前台 Admin**: BatchManage.vue 创建/编辑批次对话框、VerifyDesk.vue 核销结果展示
- **前台 H5**: VoucherDetail.vue 券详情、VoucherList.vue 券列表
- **不涉及**: 审计日志表结构（现有字段通用）、JWT 认证、报表导出（可后续单独支持）
