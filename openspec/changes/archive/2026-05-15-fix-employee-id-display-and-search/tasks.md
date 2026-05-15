## 1. 员工管理表格工号列修复

- [x] 1.1 修改 `EmployeeManage.vue` 第 37 行「工号」列，从 `prop="employeeNo"` 改为自定义模板 `row.employeeNo || row.username || '-'`，与编辑弹框回退逻辑一致
- [x] 1.2 构建 Admin 前端并验证：打开员工管理页面，确认所有员工工号列均有显示

## 2. 卡券发放员工搜索功能

- [x] 2.1 在 `BatchManage.vue` 中导入 `getEmployeeList` from `../api/employee`
- [x] 2.2 将发放对话框中的「工号」输入框替换为 `el-autocomplete` 远程搜索组件，支持输入关键字搜索员工（300ms debounce）
- [x] 2.3 搜索结果下拉展示「工号 | 姓名 | 部门」，选中后自动填充 `employeeId` 和 `employeeName`
- [x] 2.4 构建 Admin 前端并验证：打开卡券管理 → 发放卡券 → 输入关键字搜索员工 → 选中员工 → 确认发放

## 3. 可选：数据库存量数据补齐

- [x] 3.1 创建 `V5__backfill_employee_no.sql` Flyway 迁移脚本，将 `employee_no IS NULL` 的记录的 `employee_no` 更新为 `username`
