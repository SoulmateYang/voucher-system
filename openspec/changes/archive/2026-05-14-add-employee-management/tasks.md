## 1. 数据库迁移

- [x] 1.1 创建 Flyway 迁移脚本 V4__add_employee_fields.sql，为 sys_user 表新增 employee_no、mobile、department 字段

## 2. 后端实现

- [x] 2.1 更新 SysUser 实体，新增 employeeNo、mobile、department 属性
- [x] 2.2 创建 CreateEmployeeRequest、UpdateEmployeeRequest DTO（含校验注解）
- [x] 2.3 创建 EmployeeService，实现分页查询、新增、编辑、删除、密码重置逻辑
- [x] 2.4 创建 EmployeeController，暴露 /api/v1/employees REST 端点
- [x] 2.5 扩展 SysUserMapper，添加关键字搜索的分页查询方法

## 3. 前端管理端实现

- [x] 3.1 新增 api/employee.js 接口层（getEmployeeList、createEmployee、updateEmployee、deleteEmployee、resetPassword）
- [x] 3.2 新增 EmployeeManage.vue 员工管理页面（列表、搜索、新增/编辑弹窗）
- [x] 3.3 在 Layout.vue 侧边栏添加"员工管理"菜单项，路由注册到 /employees
- [x] 3.4 员工管理页面实现密码重置功能（二次确认 + 展示新密码）

## 4. 验证

- [x] 4.1 验证员工 CRUD 全流程：新增 → 列表展示 → 编辑 → 删除
- [x] 4.2 验证密码重置流程：重置 → 新密码可登录 → 旧密码失效
- [x] 4.3 验证必填校验、工号重复校验、菜单导航高亮
