## Why

员工管理页面「工号」列为空，卡券发放时无法搜索员工——这两处缺陷阻断了基本工作流。根因：一是 `employee_no` 字段为后加列、存量数据为 NULL，前端表格无回退逻辑；二是发放卡券对话框只有手工输入框，未接入已有的员工搜索 API。

## What Changes

- **员工管理表格**: 「工号」列从直接绑定 `employeeNo` 改为自定义模板，当 `employeeNo` 为空时回退显示 `username`（原始工号字段），与编辑弹框的回退逻辑保持一致
- **卡券发放对话框**: 将当前的手工输入「工号 + 姓名」方式替换为员工搜索选择器——输入关键字后调用 `GET /api/v1/employees?keyword=` 搜索，展示匹配员工列表，选中后自动填充工号和姓名
- **后端数据补齐**（可选）: 若存量数据迁移不完整，提供一次性的 SQL 脚本将 `employee_no IS NULL` 的记录的 `employee_no` 更新为 `username`

## Capabilities

### New Capabilities
- `employee-id-display`: 员工管理表格「工号」列正确展示，优先显示 `employeeNo`，为空时回退至 `username`
- `employee-search-issuance`: 卡券发放时通过关键字搜索员工（支持工号/姓名/手机号/部门），从搜索结果中选择发放对象

### Modified Capabilities
<!-- 本次不改变已有 capability 的 spec 级别行为 -->

## Impact

- `frontend-admin/src/views/EmployeeManage.vue` — 「工号」表格列模板
- `frontend-admin/src/views/BatchManage.vue` — 发放卡券对话框，新增员工搜索选择器
- `frontend-admin/src/api/employee.js` — 已有 API 无需修改（BatchManage 直接复用）
- 可选：SQL 迁移脚本 `V5__backfill_employee_no.sql`
