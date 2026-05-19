## ADDED Requirements

### Requirement: Category creation requires voucher type

创建分类时，系统 SHALL 要求必选卷类型，类型必须为 `COUPON`、`RESOURCE_USAGE` 或 `STORED_VALUE` 之一。

#### Scenario: Create category with voucher type
- **WHEN** 管理员调用 `POST /api/v1/categories` 并传入 `{ "name": "餐饮类", "voucherType": "COUPON" }`
- **THEN** 系统创建分类，返回包含 `id`、`name`、`voucherType`、`sortOrder` 的分类对象

#### Scenario: Create category without voucher type fails
- **WHEN** 管理员调用 `POST /api/v1/categories` 未传入 `voucherType`
- **THEN** 系统返回 400 错误，提示"卷类型不能为空"

#### Scenario: Create category with invalid voucher type fails
- **WHEN** 管理员调用 `POST /api/v1/categories` 并传入 `{ "name": "测试", "voucherType": "INVALID" }`
- **THEN** 系统返回 400 错误，提示"无效的卷类型"

### Requirement: Category editing supports voucher type change

编辑分类时，系统 SHALL 允许修改分类名称和卷类型。

#### Scenario: Update category voucher type
- **WHEN** 管理员调用 `PUT /api/v1/categories/{id}` 并传入 `{ "name": "美食餐饮", "voucherType": "COUPON" }`
- **THEN** 系统更新分类并返回更新后的对象

#### Scenario: Update category type must be valid
- **WHEN** 管理员调用 `PUT /api/v1/categories/{id}` 并传入 `{ "voucherType": "INVALID" }`
- **THEN** 系统返回 400 错误

### Requirement: Category list displays voucher type

分类列表 SHALL 返回每个分类的卷类型字段，前端展示类型标签。

#### Scenario: List categories with type
- **WHEN** 管理员调用 `GET /api/v1/categories`
- **THEN** 系统返回分类列表，每个分类包含 `id`、`name`、`voucherType`、`sortOrder`

### Requirement: Voucher-to-category assignment validates type consistency

将券分配到分类时，系统 SHALL 校验券的卷类型与目标分类的卷类型一致。

#### Scenario: Assign voucher to matching category type succeeds
- **WHEN** 管理员将一张 COUPON 类型的券分配到 `voucherType = "COUPON"` 的分类
- **THEN** 操作成功，券的 `category_id` 更新

#### Scenario: Assign voucher to mismatched category type fails
- **WHEN** 管理员尝试将一张 COUPON 类型的券分配到 `voucherType = "STORED_VALUE"` 的分类
- **THEN** 系统返回 400 错误，提示"券类型与分类类型不匹配"

#### Scenario: Batch assign with type mismatch fails
- **WHEN** 管理员批量移动包含混合类型的券到同一分类，且存在类型不匹配
- **THEN** 系统返回 400 错误，提示"存在券类型与分类类型不匹配"
