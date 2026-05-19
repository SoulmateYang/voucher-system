## MODIFIED Requirements

### Requirement: Admin can manage voucher categories

系统 SHALL 提供分类 CRUD 接口，管理员可创建、查看、编辑、删除自定义卡券分类。创建和编辑分类时 SHALL 必选卷类型。

#### Scenario: Create a new category
- **WHEN** 管理员调用 `POST /api/v1/categories` 并传入 `{ "name": "餐饮类", "voucherType": "COUPON" }`
- **THEN** 系统创建分类并返回包含 `id`、`name`、`voucherType`、`sortOrder` 的分类对象

#### Scenario: List all categories
- **WHEN** 管理员调用 `GET /api/v1/categories`
- **THEN** 系统返回所有分类列表，按 `sortOrder` 升序排列，每条包含 `voucherType` 字段

#### Scenario: Update category name and type
- **WHEN** 管理员调用 `PUT /api/v1/categories/{id}` 并传入 `{ "name": "美食餐饮", "voucherType": "COUPON" }`
- **THEN** 系统更新分类名称和类型并返回更新后的对象

#### Scenario: Delete category clears voucher references
- **WHEN** 管理员调用 `DELETE /api/v1/categories/{id}`
- **THEN** 系统删除该分类，并将所有关联此分类的券的 `category_id` 置为 NULL
- **THEN** 系统返回 204 No Content

#### Scenario: Duplicate category name is rejected
- **WHEN** 管理员尝试创建同名分类
- **THEN** 系统返回 400 错误，提示"分类名称已存在"

#### Scenario: Category without voucher type is rejected
- **WHEN** 管理员创建分类未传入 `voucherType`
- **THEN** 系统返回 400 错误，提示"卷类型不能为空"

### Requirement: Admin can assign category to a single voucher

系统 SHALL 提供接口将单张券归入指定分类或从分类中移除。分配时 SHALL 校验券类型与目标分类类型一致。

#### Scenario: Assign category to a voucher
- **WHEN** 管理员调用 `PUT /api/v1/vouchers/{id}/category` 并传入 `{ "categoryId": 1 }`
- **THEN** 该券的 `category_id` 更新为 1，返回更新后的券信息

#### Scenario: Assign voucher to mismatched category type fails
- **WHEN** 管理员尝试将 COUPON 类型的券分配到 STORED_VALUE 类型的分类
- **THEN** 系统返回 400 错误，提示"券类型与分类类型不匹配"

#### Scenario: Remove category from a voucher
- **WHEN** 管理员调用 `PUT /api/v1/vouchers/{id}/category` 并传入 `{ "categoryId": null }`
- **THEN** 该券的 `category_id` 置为 NULL，分类被移除

### Requirement: Admin can batch move vouchers to a category

系统 SHALL 提供批量接口，允许管理员将多张券一次性归入同一分类。批量移动 SHALL 校验所有券类型与目标分类类型一致。

#### Scenario: Batch move vouchers to category
- **WHEN** 管理员调用 `PUT /api/v1/vouchers/category/batch` 并传入 `{ "voucherIds": [1, 2, 3], "categoryId": 2 }`
- **THEN** 三张券的 `category_id` 全部更新为 2，返回 `{ "affectedCount": 3 }`

#### Scenario: Batch move with type mismatch fails
- **WHEN** 批量移动中包含类型不匹配的券
- **THEN** 系统返回 400 错误，提示"存在券类型与分类类型不匹配"

#### Scenario: Batch move limit enforcement
- **WHEN** 管理员尝试批量移动超过 500 张券
- **THEN** 系统返回 400 错误，提示"单次最多操作 500 条"

### Requirement: Voucher list supports category filtering

管理端券码列表 SHALL 支持按分类筛选，并能查看每张券所属分类。

#### Scenario: Filter vouchers by category
- **WHEN** 管理员在券码列表选择某分类筛选
- **THEN** 列表仅展示 `category_id` 匹配该分类的券

#### Scenario: Show category name and type in voucher list row
- **WHEN** 管理员查看券码列表
- **THEN** 每行显示该券所属分类名称和卷类型标签，未归类的券显示 "—"

#### Scenario: Admin UI category management page
- **WHEN** 管理员点击侧边栏"分类管理"
- **THEN** 进入分类管理页面，展示分类列表（含卷类型标签），每行有编辑和删除按钮
- **THEN** 顶部有"新建分类"按钮，弹出表单包含分类名称和卷类型选择
- **THEN** 列表按 sortOrder 排序

#### Scenario: Admin UI batch move
- **WHEN** 管理员在券码列表勾选多条券码，点击"批量移动"
- **THEN** 弹出分类选择器（仅显示与选中券类型匹配的分类），选择目标分类后确认，所有选中券归入该分类
