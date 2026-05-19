## Context

当前系统中，卷类型（`voucher_batch.voucher_type`）和分类（`voucher.category_id`）是两个独立维度。卷类型决定券的行为（核销逻辑、折扣计算），分类只是管理标签。这导致：
- 分类缺乏业务语义，无法约束不同类型的券归入不匹配的分类
- 储值卡（企业常见福利场景）缺乏系统支持
- 批次创建时需要分别选择类型和分类，容易不一致

目标：分类成为卷类型的唯一定义来源，批次通过分类确定类型，同时新增储值卡类型。

## Goals / Non-Goals

**Goals:**
- `voucher_category` 新增 `voucher_type` 字段，作为卷类型的唯一来源
- 新增 STORED_VALUE 储值卡类型，支持充值赠送、多次核销扣减、余额追踪、消费明细
- 批次创建改为选分类，类型由分类自动派生
- 分配分类时校验券类型一致性
- 历史数据平滑迁移

**Non-Goals:**
- 储值卡不支持退款/撤销核销（本期不做）
- 储值卡不支持跨券余额合并/转移
- 不修改 H5 端核销流程（储值卡核销仅在管理端操作）
- 不引入多币种或汇率

## Decisions

### 1. 类型的唯一来源是分类

`voucher_category.voucher_type` 是卷类型的唯一定义点。`voucher_batch.voucher_type` 在批次创建时从分类同步（冗余存储，用于查询性能），但不可独立修改。

**选择理由**: 避免两个维度不一致，简化创建流程（选分类即确定一切）。

**备选方案**: 批次保留独立类型字段，分配分类时校验一致性。被拒绝——增加了不一致的风险和额外的校验逻辑。

### 2. 储值卡余额模型：双字段追踪

```
voucher.initial_balance = batch.face_value + batch.bonus_value
voucher.remaining_balance = 递减至 0
```

每次核销扣减 `remaining_balance`，归零时状态变为 `EXHAUSTED`。两条字段比单字段 + 消费记录聚合查询更简单直接。

**选择理由**: 性能好（查余额无需 SUM），逻辑清晰（余额就是余额），初始值保留便于展示"充50送5"。

### 3. 储值卡核销：独立消费明细表

新建 `voucher_consumption` 表记录每笔扣减，而不是复用 `verification_log` 加字段。`verification_log` 保持原有的一次性核销语义。

**选择理由**: 储值卡一笔消费 vs 优惠券一次核销是不同概念。分表避免 `verification_log` 膨胀和混杂。同时也为将来可能的储值卡特性（退款、明细导出）留出空间。

### 4. 批次创建：categoryId 替代 voucherType

`POST /api/v1/batches` 的 `voucherType` 参数改为 `categoryId`。服务端根据分类的 `voucher_type` 自动填充 `voucher_batch.voucher_type`。

**BREAKING**: 现有调用方需要从传 `voucherType` 改为传 `categoryId`。

### 5. 历史数据迁移策略

- 现有 4 个默认分类：餐饮类/购物类/娱乐类 → `COUPON`，储值类 → `STORED_VALUE`
- 现有批次：保持 `voucher_type` 不变，`category_id` 设为 NULL（不强制回溯关联）
- 现有券：`initial_balance` 和 `remaining_balance` 对于 COUPON/RESOURCE_USAGE 回填 `face_value`（保证字段非 NULL）
- 新创建的批次必须关联分类

### 6. 券类型从分类派生（发券时确定，不随分类变动）

券的类型在发券时从 `batch.category_id` → `category.voucher_type` 确定，通过 batch 传递。一旦发放，即使后续修改分类的类型，已发券不受影响。

**选择理由**: 历史数据的类型不可变，业务上券的类型不应因管理操作而改变。

## Risks / Trade-offs

- **分类被删除时批次失去类型来源**: 现有机制已处理——分类被删除时 `voucher.category_id` 置 NULL。批次 `voucher_type` 保留冗余值，不受影响。
- **储值卡余额并发扣减**: 使用乐观锁（`voucher.version` 已有），核销时 `UPDATE WHERE version = ?`，冲突则重试。与现有优惠券核销一致。
- **BREAKING 变更影响前端**: 前端批次创建表单需同步升级，旧版本客户端调用新接口会失败。部署时前后端需同步上线。
- **面额语义变化**: `face_value` 在储值卡语境下表示"初始充值金额"，`bonus_value` 表示"赠送金额"，两者含义不同但字段复用。通过 `voucher_type` 区分语义。

## Migration Plan

1. 执行数据库迁移（新增列、新表）
2. 回填历史数据
3. 部署后端（新接口兼容旧参数一段时间？— No，BREAKING 直接切换）
4. 部署前端
5. 验证：创建各类型分类 → 创建各类型批次 → 发券 → 核销（储值卡多次核销验证余额）

回滚：还原 migration（新列可留空不影响旧逻辑），旧版代码 `voucherType` 参数不校验 NULL。
