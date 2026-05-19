## 1. Database Migration

- [x] 1.1 创建 V8 migration：voucher_category 加 voucher_type 列，回填历史数据
- [x] 1.2 创建 V8 migration：voucher_batch 加 category_id、bonus_value 列
- [x] 1.3 创建 V8 migration：voucher 加 initial_balance、remaining_balance 列，回填 face_value
- [x] 1.4 创建 V8 migration：新建 voucher_consumption 表（id, voucher_id, voucher_code, consume_amount, balance_before, balance_after, order_amount, operator_id, operator_name, remark, created_at）

## 2. Backend — Entity & Constants

- [x] 2.1 VoucherCategory 实体新增 voucherType 字段
- [x] 2.2 VoucherBatch 实体新增 categoryId、bonusValue 字段
- [x] 2.3 Voucher 实体新增 initialBalance、remainingBalance 字段
- [x] 2.4 新建 VoucherConsumption 实体
- [x] 2.5 VoucherType 常量类新增 STORED_VALUE，更新 ALL_TYPES，新增 isStoredValue() 方法

## 3. Backend — Category Service

- [x] 3.1 CategoryController: POST/PUT 接口新增 voucherType 参数校验
- [x] 3.2 VoucherCategoryService: create/update 方法新增 voucherType 处理
- [x] 3.3 CategoryController: 分类列表返回 voucherType 字段

## 4. Backend — Batch Service

- [x] 4.1 CreateBatchRequest: voucherType 改为 categoryId，新增 bonusValue 字段
- [x] 4.2 VoucherBatchService.create: 根据 categoryId 查分类 → 自动设置 voucherType；校验 category 存在；处理 STORED_VALUE 的 bonusValue
- [x] 4.3 VoucherBatchService.create: COUPON 类型校验折扣字段，STORED_VALUE 类型校验 face_value

## 5. Backend — Voucher Issuance

- [x] 5.1 VoucherService.issueSingle: STORED_VALUE 类型时 initialBalance = faceValue + bonusValue, remainingBalance = initialBalance
- [x] 5.2 VoucherListRow 新增 voucherType 字段来自 batch join

## 6. Backend — Stored Value Card Verification

- [x] 6.1 VoucherService.verify: 识别 STORED_VALUE 类型，走储值卡核销分支
- [x] 6.2 储值卡核销逻辑：校验 orderAmount 非空 → 校验 remainingBalance >= orderAmount → 乐观锁扣减 remainingBalance → 余额归零改状态为 EXHAUSTED
- [x] 6.3 核销时写入 VoucherConsumption 记录（consume_amount, balance_before, balance_after）
- [x] 6.4 核销结果返回 deductAmount、remainingBalance（区别于优惠券的 discountAmount）
- [x] 6.5 券状态校验增加 EXHAUSTED = 不可核销

## 7. Backend — Category Assignment Validation

- [x] 7.1 VoucherService.assignCategory: 校验券类型与目标分类类型一致
- [x] 7.2 VoucherService.batchAssignCategory: 批量校验所有选中券类型一致

## 8. Backend — Consumption History API

- [x] 8.1 VoucherConsumptionMapper: 按 voucher_id 倒序查询消费记录
- [x] 8.2 VoucherController: GET /api/v1/vouchers/{id}/consumptions 返回消费明细列表
- [x] 8.3 VoucherDetail 接口对 STORED_VALUE 类型返回 initialBalance、remainingBalance

## 9. Backend — Tests

- [x] 9.1 VoucherTypeTest: 新增 STORED_VALUE 有效性、isStoredValue 测试
- [x] 9.2 VoucherCategoryServiceTest: 新增创建分类带类型、类型校验测试
- [x] 9.3 VoucherBatchServiceTest: 新增分类关联批次、储值卡批次创建测试
- [x] 9.4 VoucherServiceTest: 新增储值卡发放（余额正确）、核销（扣减正确、余额不足、余额归零变 EXHAUSTED）、消费记录写入测试
- [x] 9.5 分类分配类型校验测试（匹配成功、不匹配失败）

## 10. Frontend Admin — Category Management

- [x] 10.1 CategoryManage.vue: 表格新增「卷类型」列，显示标签（优惠券/因私使用/储值卡）
- [x] 10.2 CategoryManage.vue: 新建/编辑对话框新增卷类型下拉选择
- [x] 10.3 category.js API: createCategory/updateCategory 增加 voucherType 参数

## 11. Frontend Admin — Batch Management

- [x] 11.1 BatchManage.vue: 创建批次表单中「卷类型」改为「分类」下拉选择，选项显示「分类名（类型标签）」
- [x] 11.2 BatchManage.vue: 选择分类后根据 voucherType 条件显示折扣字段／储值卡字段／无额外字段
- [x] 11.3 BatchManage.vue: 储值卡字段：充值金额 + 赠送金额
- [x] 11.4 batch.js API: createBatch 传 categoryId 替代 voucherType

## 12. Frontend Admin — Employee Vouchers

- [x] 12.1 EmployeeVouchers.vue: 批量移动分类时仅显示与选中券类型匹配的分类
- [x] 12.2 EmployeeVouchers.vue: 列表每行显示券类型标签

## 13. Frontend Admin — Consumption History Page

- [x] 13.1 新建 ConsumptionHistory.vue: 页面顶部储值卡信息卡片（初始余额、剩余余额、状态）
- [x] 13.2 页面下方消费记录表格（时间、消费金额、消费前余额、消费后余额、核销员、备注）
- [x] 13.3 添加路由 /consumption/:voucherId
- [x] 13.4 consumption.js API: getConsumptionHistory(voucherId)

## 14. Frontend H5 — Stored Value Card Display

- [x] 14.1 VoucherList.vue: 储值卡样式适配，显示剩余余额而非面额
- [x] 14.2 VoucherDetail.vue: 储值卡详情展示初始余额、剩余余额、赠送金额

## 15. Integration & Verification

- [ ] 15.1 端到端验证：创建储值卡分类 → 创建储值卡批次（充50送5）→ 发券 → 多次核销验证余额变化 → 余额归零验证 EXHAUSTED
- [ ] 15.2 验证类型校验：COUPON 券无法分配到 STORED_VALUE 分类
- [ ] 15.3 验证历史数据：旧分类已回填类型，旧批次/券不受影响
