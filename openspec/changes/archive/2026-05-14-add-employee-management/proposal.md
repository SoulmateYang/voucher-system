## Why

当前系统缺少员工信息维护入口，管理员无法查看、编辑员工基本信息，也无法为员工重置登录密码。所有员工数据依赖 `sys_user` 表手动维护，运营效率低且容易出错。同时 TODOS.md 已规划"CSV批量导入员工"需求。

## What Changes

- 新增**员工管理页面**（PC 管理端），支持员工列表查询、新增、编辑、删除
- 新增**密码重置功能**，管理员可为指定员工重置密码
- 新增后端 **EmployeeController** 提供员工 CRUD REST API
- 扩展 `sys_user` 表，增加员工号、手机号、部门等字段
- 左侧导航菜单新增"员工管理"入口

## Capabilities

### New Capabilities
- `employee-crud`: 员工信息增删改查，包含列表分页搜索、新增/编辑表单、删除确认
- `employee-password-reset`: 管理员为员工重置登录密码，重置后生成随机密码或设为默认密码

### Modified Capabilities
<!-- 无现有 capability 需要修改 -->

## Impact

- **后端**: 新增 `EmployeeController`、`EmployeeService`、数据库迁移脚本（扩展 sys_user 表字段）
- **前端管理端**: 新增 `EmployeeManage.vue` 页面、路由注册、侧边栏菜单项、`api/employee.js` 接口层
- **数据库**: `sys_user` 表新增 employee_no、mobile、department 字段
- **无 BREAKING 变更**: 所有新增字段均可为空，不影响现有功能
