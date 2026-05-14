## ADDED Requirements

### Requirement: 员工列表分页查询
系统 SHALL 提供员工列表分页查询接口，支持按关键字搜索和分页。

#### Scenario: 默认分页查询
- **WHEN** 管理员访问员工管理页面
- **THEN** 系统返回第一页员工列表，每页默认 10 条，按创建时间降序排列

#### Scenario: 按关键字搜索
- **WHEN** 管理员输入搜索关键字（匹配姓名、工号、手机号、部门）
- **THEN** 系统返回匹配的员工列表，支持分页

#### Scenario: 仅查询员工角色
- **WHEN** 查询员工列表
- **THEN** 系统仅返回 role='EMPLOYEE' 的用户，不包含管理员账户

### Requirement: 新增员工
系统 SHALL 支持管理员新增员工，自动创建登录账户。

#### Scenario: 成功新增员工
- **WHEN** 管理员填写员工姓名、工号（必填）和手机号、部门（选填）并提交
- **THEN** 系统创建 sys_user 记录，role 设为 EMPLOYEE，enabled 为 1，密码为 BCrypt 加密的随机初始密码，返回员工信息和初始密码

#### Scenario: 工号重复校验
- **WHEN** 管理员输入的工号（username）已存在
- **THEN** 系统返回错误提示"工号已存在"，拒绝创建

#### Scenario: 必填字段校验
- **WHEN** 管理员未填写姓名或工号
- **THEN** 系统返回字段校验错误，拒绝创建

### Requirement: 编辑员工信息
系统 SHALL 支持管理员编辑已有员工的基本信息。

#### Scenario: 成功编辑员工
- **WHEN** 管理员修改员工的姓名、手机号、部门并保存
- **THEN** 系统更新对应 sys_user 记录

#### Scenario: 不可修改工号
- **WHEN** 编辑员工信息
- **THEN** 工号（username）字段不可修改

### Requirement: 删除员工
系统 SHALL 支持管理员删除员工（软删除）。

#### Scenario: 成功删除员工
- **WHEN** 管理员确认删除某员工
- **THEN** 系统将该员工 enabled 设为 0，前端列表中不再显示

#### Scenario: 不可删除自己
- **WHEN** 管理员尝试删除自己的账户
- **THEN** 系统拒绝并提示"不可删除当前登录用户"
