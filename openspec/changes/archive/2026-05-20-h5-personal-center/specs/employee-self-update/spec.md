## ADDED Requirements

### Requirement: 员工自助更新个人信息
系统 SHALL 提供 `PUT /api/v1/auth/me` 接口，允许当前登录员工更新自己的个人信息。

#### Scenario: 成功更新个人信息
- **WHEN** 员工通过认证后请求 `PUT /api/v1/auth/me`，请求体包含手机号和部门
- **THEN** 系统更新当前用户的 mobile 和 department 字段，返回更新后的用户信息（id、username、realName、mobile、department）

#### Scenario: 未认证访问拒绝
- **WHEN** 未携带有效 token 请求 `PUT /api/v1/auth/me`
- **THEN** 系统返回 401 状态码

#### Scenario: 不允许修改工号和姓名
- **WHEN** 请求体中包含 username 或 realName 字段
- **THEN** 系统忽略这些字段，仅更新 mobile 和 department

### Requirement: 员工自助修改密码
系统 SHALL 提供 `POST /api/v1/auth/change-password` 接口，允许当前登录员工修改自己的密码。

#### Scenario: 成功修改密码
- **WHEN** 员工通过认证后请求 `POST /api/v1/auth/change-password`，请求体包含正确的原密码和新密码
- **THEN** 系统验证原密码正确后更新密码，返回成功

#### Scenario: 原密码错误
- **WHEN** 请求中原密码与当前密码不匹配
- **THEN** 系统返回错误"原密码错误"

#### Scenario: 新密码长度不足
- **WHEN** 请求中新密码长度少于 6 位
- **THEN** 系统返回校验错误

#### Scenario: 未认证访问拒绝
- **WHEN** 未携带有效 token 请求 `POST /api/v1/auth/change-password`
- **THEN** 系统返回 401 状态码
