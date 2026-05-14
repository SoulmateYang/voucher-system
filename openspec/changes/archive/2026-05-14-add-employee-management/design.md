## Context

当前系统通过 `sys_user` 表管理所有用户（管理员 + 员工），但仅有 username/password/real_name/role/enabled 五个业务字段。员工数据缺乏结构化信息（工号、手机号、部门），且无专门的管理界面。管理员无法查看员工列表、编辑员工信息或重置密码。

系统技术栈：Spring Boot 3.2.5 + MyBatis-Plus + MySQL（后端），Vue 3 + Element Plus（管理端前端）。

## Goals / Non-Goals

**Goals:**
- 管理员可在 PC 端查看员工列表（分页、搜索）
- 管理员可新增、编辑、删除员工
- 管理员可为员工重置登录密码
- 扩展 `sys_user` 表，增加员工号、手机号、部门字段
- 侧边栏新增"员工管理"菜单入口

**Non-Goals:**
- CSV 批量导入员工（已列为 TODO-1，后续单独实现）
- 员工自助修改个人信息（牵涉权限复杂度，本期不做）
- H5 端员工管理（本期仅 PC 管理端）
- 角色/权限体系重构（沿用现有 ADMIN/EMPLOYEE 二分）

## Decisions

### D1: 扩展 sys_user 表而非新建 employee 表

**选择**: 在 `sys_user` 上新增字段（employee_no, mobile, department），继续复用现有用户体系。

**备选**: 新建独立的 `employee` 表，与 `sys_user` 通过 user_id 关联。

**理由**: 当前系统规模小，EMPLOYEE 角色用户就是员工，拆表引入不必要的联表复杂度。`sys_user` 已有 username（工号）、real_name（姓名）、enabled（状态），补充少量字段即可满足需求。创建券码时的 holderId/holderName 绑定也可直接沿用 sys_user 的 id 和 real_name。

### D2: API 端点设计

**选择**: 新增 `/api/v1/employees` 端点，由 `EmployeeController` 处理：

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/v1/employees` | 分页查询员工列表 |
| GET | `/api/v1/employees/{id}` | 查询单个员工 |
| POST | `/api/v1/employees` | 新增员工 |
| PUT | `/api/v1/employees/{id}` | 编辑员工信息 |
| DELETE | `/api/v1/employees/{id}` | 删除员工（软删除，设 enabled=0） |
| POST | `/api/v1/employees/{id}/reset-password` | 重置密码 |

**理由**: RESTful 风格，与现有 VoucherBatchController、VoucherController 保持一致。密码重置作为 employee 的子资源，语义清晰。

### D3: 密码重置机制

**选择**: 管理员触发后，后端生成 8 位随机密码，BCrypt 加密存储，明文通过响应返回给管理员。

**备选**: 重置为固定默认密码（如 123456）；发送邮件/短信通知员工。

**理由**: 随机密码更安全，管理员可线下告知员工。固定默认密码有安全隐患。邮件/短信通知依赖外部服务，本期不引入。明文仅返回一次，后续接口不暴露密码。

### D4: 删除策略

**选择**: 软删除 — 将 `enabled` 设为 0，保留数据。

**理由**: 员工可能有已发放的券码关联，物理删除会导致数据完整性问题。与现有 enabled 字段语义一致。

## Risks / Trade-offs

- **[风险] 密码重置返回明文** → 仅在管理员操作时通过 HTTPS 传输，响应日志不记录密码字段
- **[风险] sys_user 表字段膨胀** → 新增 3 个字段均为可选（DEFAULT NULL），不影响现有管理员用户
- **[风险] 删除员工后其券码仍可用** → 券码核销时不校验 enabled 状态，属已知行为；后续可在核销环节增加校验

## Migration Plan

1. 执行 Flyway 迁移脚本 V4，新增 3 个字段（均为 NULL 可空）
2. 部署后端（新增 Controller/Service/Mapper）
3. 部署前端（新增页面、路由、菜单）
4. **回滚**: 新字段可空，回滚只需下线新代码，数据库字段可保留不影响旧逻辑
