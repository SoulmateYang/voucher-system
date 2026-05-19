## 1. 数据库迁移 (V7)

- [x] 1.1 创建 V7__add_voucher_category.sql：新建 `voucher_category` 表 (id, name, sort_order, created_at, updated_at) + `voucher` 表新增 `category_id` 可空外键列
- [x] 1.2 插入默认分类数据（餐饮类、购物类、娱乐类、储值类）

## 2. 后端 — 分类管理 CRUD

- [x] 2.1 创建 `VoucherCategory` 实体类 (com/example/voucher/entity/VoucherCategory.java)
- [x] 2.2 创建 `VoucherCategoryMapper` 接口 (MyBatis-Plus BaseMapper)
- [x] 2.3 创建 `CategoryController` (GET/POST/PUT/DELETE /api/v1/categories)，含名称重复校验
- [x] 2.4 创建 `VoucherCategoryService` 接口 + 实现类（CRUD 逻辑 + 删除时清理 voucher.category_id）

## 3. 后端 — Voucher 实体扩展 + Mapper 增强

- [x] 3.1 `Voucher.java` 新增 `categoryId` 字段
- [x] 3.2 `VoucherListRow.java` 新增 `categoryId` + `categoryName` 字段
- [x] 3.3 `VoucherMapper.java` 新增 `batchUpdateCategory` 方法（批量更新 categoryId）
- [x] 3.4 `VoucherMapper.xml` selectPagedWithBatch 查询 SQL 增加 LEFT JOIN voucher_category 和 category_id 筛选条件

## 4. 后端 — 卡券编辑接口

- [x] 4.1 `VoucherService` 新增 `updateVoucher(Long id, LocalDateTime expireAt, String remark)` 方法（白名单字段更新）
- [x] 4.2 `VoucherService` 新增 `updateVoucher` 时写入 audit_log 审计日志
- [x] 4.3 `VoucherController` 新增 `PUT /api/v1/vouchers/{id}` 编辑端点
- [x] 4.4 `VoucherService` 新增 `assignCategory(Long voucherId, Long categoryId)` 方法
- [x] 4.5 `VoucherService` 新增 `batchAssignCategory(List<Long> voucherIds, Long categoryId)` 方法（上限 500）
- [x] 4.6 `VoucherController` 新增 `PUT /api/v1/vouchers/{id}/category` 和 `PUT /api/v1/vouchers/category/batch`
- [x] 4.7 `VoucherController` 的列表查询 `GET /api/v1/vouchers` 增加 `categoryId` 可选参数

## 5. 后端 — 单元测试

- [x] 5.1 编写 `VoucherCategoryServiceTest` 覆盖分类 CRUD + 重复名称校验 + 删除清理券关联
- [x] 5.2 编写 `VoucherServiceTest` 覆盖编辑（正常/非法字段忽略）、单券归类、批量移动、批量上限

## 6. 管理端前端 — API 层

- [x] 6.1 创建 `frontend-admin/src/api/category.js`（getCategoryList, createCategory, updateCategory, deleteCategory）
- [x] 6.2 `frontend-admin/src/api/voucher.js` 新增 `updateVoucher(id, data)`, `assignCategory(id, categoryId)`, `batchAssignCategory(voucherIds, categoryId)`, `getVoucherList` 增加 `categoryId` 参数

## 7. 管理端前端 — 分类管理页面

- [x] 7.1 创建 `frontend-admin/src/views/CategoryManage.vue`：分类列表表格（名称、排序、操作）+ 新建/编辑弹窗 + 删除确认
- [x] 7.2 `frontend-admin/src/router/index.js` 新增 `/categories` 路由
- [x] 7.3 `frontend-admin/src/views/Layout.vue` 侧边栏新增"分类管理"菜单项

## 8. 管理端前端 — 券码列表增强

- [x] 8.1 `EmployeeVouchers.vue` 新增分类筛选下拉框（使用 el-select，选项从 API 加载）
- [x] 8.2 `EmployeeVouchers.vue` 新增"编辑"按钮 + 编辑弹窗（有效期日期选择器 + 备注输入框，核销码只读展示）
- [x] 8.3 `EmployeeVouchers.vue` 新增每行"归类"下拉选择器（可快速将单券归入/移出分类）
- [x] 8.4 `EmployeeVouchers.vue` 新增表头复选框 + "批量移动"按钮 + 批量移动弹窗（选择目标分类）
- [x] 8.5 `EmployeeVouchers.vue` 列表列新增"分类"列，展示分类名称（后端已返回 categoryName，前端自动展示）

## 9. 验证

- [x] 9.1 后端 `mvn test` 确认所有测试通过 (103 tests, 0 failures)
- [x] 9.2 管理端 `npm run build` 确认编译无错误
- [x] 9.3 整体联调：创建分类 → 编辑卡券 → 单券归类 → 批量移动 → 筛选验证 (API 层验证通过，前端编译通过)
