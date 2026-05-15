## Context

当前系统通过 `voucher_batch.voucher_type` 字段区分券类型，默认值 `RESOURCE_USAGE`。整个发券流水线（批次→实例→核销→审计）已跑通。新增 `COUPON` 类型只需在现有骨架基础上叠加差异化字段与逻辑，不影响现有 `RESOURCE_USAGE` 流程。

约束：
- Java 17, Spring Boot 3.2, MyBatis-Plus 3.5.6, Flyway 迁移
- Vue 3 + Element Plus (Admin) / Vant 4 (H5)
- 遵循 DESIGN.md：Industrial/Utilitarian 风格，主色 #1989fa

## Goals / Non-Goals

**Goals:**
- 支持创建 `COUPON` 类型的券批次，配置折扣规则（满减 or 折扣率）
- 发放优惠券时自动计算面额（满减=固定值，折扣率=需核销时按订单金额计算）
- 核销流程兼容优惠券，返回优惠金额
- H5 端优惠券展示与资源券有视觉区分

**Non-Goals:**
- 优惠券与订单系统对接（订单金额由核销台人工输入或后续对接）
- 优惠券叠加规则
- 优惠券使用次数限制（每券限用一次已由 status 状态机保证）
- 批量创建券码（已有批量发放逻辑，复用即可）

## Decisions

### Decision 1: 优惠券折扣数据挂在批次上，券实例仅存面额

**选型**: `voucher_batch` 新增 `discount_type` (FIXED_AMOUNT / PERCENTAGE)、`discount_value` (DECIMAL)、`min_order_amount` (DECIMAL)。`voucher` 新增 `face_value` (DECIMAL)。

**替代方案**: 把所有折扣信息复制到每张券实例上——数据冗余、批次级规则变更后历史券口径不一致。

**原因**: 批次是折扣规则的自然归属，面额是每张券的独立属性便于核销时直接展示。

### Decision 2: 核销时满减券直接抵扣，折扣券需输入订单金额

**选型**: 核销接口 `/api/v1/vouchers/confirm` 的请求体增加可选 `orderAmount` 字段。满减券忽略该字段；折扣券必须提供，服务端按 `orderAmount × (discountValue/100)` 计算实际抵扣金额，上限为 `face_value`。

**原因**: 折扣券的价值天然依赖订单金额，不能提前固化为面额。面额仅作为理论上限占位。

### Decision 3: 券类型枚举由 VARCHAR 约束驱动，不创建独立枚举表

**选型**: `voucher_batch.voucher_type` 保持 VARCHAR(32)，后端通过常量类 `VoucherType` 校验。

**原因**: 当前仅 2 种类型，枚举表过度设计。Java 常量 + MyBatis-Plus 校验即可。

### Decision 4: Flyway 新增 V2 迁移脚本，非破坏性变更

**选型**: 新增 `V2__add_coupon_fields.sql`，ALTER TABLE 添加字段（全部 SET DEFAULT NULL，不影响现有行）。

**原因**: 遵循已有 Flyway 命名约定。非破坏性变更确保现有 `RESOURCE_USAGE` 数据不受影响。

## Risks / Trade-offs

- **[低风险] 核销逻辑分支增加**: `VoucherService.verify()` 内按 voucher_type 分支处理优惠券逻辑 → 函数长度可能超标。Mitigation：优惠券核销逻辑抽取私有方法，保持 verify() ≤ 80 行
- **[低风险] 券码生成复用**: `VoucherCodeUtil` 不区分券类型，券码格式统一 → 无法通过券码识别类型。可接受，券码是唯一标识而非分类字段，类型信息在 DB 中
- **[低风险] 前端批次表单复杂度**: 券类型切换时需动态显示/隐藏折扣配置区块 → Element Plus 的 v-if 即可处理，无需额外组件

## Open Questions

- 折扣券的 `face_value`（理论上限）是否需要在创建批次时指定？→ 建议满减券 face_value = discount_value，折扣券 face_value = 用户输入的理论最大抵扣额
