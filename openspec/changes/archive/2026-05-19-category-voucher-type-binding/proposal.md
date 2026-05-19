## Why

分类和卷类型当前完全独立，导致分类缺乏业务语义（只是分组标签），且储值卡作为常见的企业福利场景没有系统支持。将类型定义收敛到分类，让分类成为卷类型的唯一来源，同时补齐储值卡能力。

## What Changes

- 分类新增 `voucherType` 字段，创建/编辑分类时必选卷类型（COUPON / RESOURCE_USAGE / STORED_VALUE）
- 新增储值卡（STORED_VALUE）类型，支持充值赠送、多次核销扣减余额、余额归零自动标记 EXHAUSTED
- 批次创建改为选择分类（类型由分类自动确定），新增 `bonusValue` 字段支持充值赠送
- 券新增 `initialBalance`、`remainingBalance` 字段追踪储值卡余额
- 新增 `voucher_consumption` 表记录每笔储值卡消费明细，提供独立查询页面
- 分配分类时校验券类型与目标分类类型一致性
- **BREAKING**: `POST /api/v1/batches` 请求参数从 `voucherType` 改为 `categoryId`，不再直接传类型
- 历史数据迁移：现有分类按语义分配类型（餐饮/购物/娱乐→COUPON，储值→STORED_VALUE），现有券的余额字段回填

## Capabilities

### New Capabilities

- `stored-value-card`: 储值卡全生命周期——创建批次（充值赠送）、发券（初始余额）、多次核销扣减、余额追踪、EXHAUSTED 状态、消费明细查询
- `category-type-binding`: 分类与卷类型绑定——分类表新增 voucher_type，创建/编辑分类时选择类型，CRUD 接口升级，列表展示类型

### Modified Capabilities

- `voucher-category`: 分类表结构新增 `voucher_type` 字段，接口增加类型参数和校验
- `coupon-batch-management`: 批次创建改为 `categoryId` 替代 `voucherType`，新增 `bonusValue`，券类型由分类派生
- `coupon-verification`: 核销接口支持储值卡部分扣减——接受消费金额、校验余额、写入消费明细、余额归零切换状态

## Impact

- **数据库**: voucher_category 加 voucher_type；voucher_batch 加 category_id + bonus_value；voucher 加 initial_balance + remaining_balance；新建 voucher_consumption 表
- **后端**: VoucherType、VoucherCategoryService、VoucherBatchService、VoucherService、controller 层多处修改
- **前端 admin**: CategoryManage、BatchManage、EmployeeVouchers、新增储值卡消费明细页面
- **前端 h5**: VoucherList、VoucherDetail 需适配储值卡展示（余额显示、消费记录入口）
- **测试**: 新增储值卡核销、余额扣减、类型校验等测试用例
