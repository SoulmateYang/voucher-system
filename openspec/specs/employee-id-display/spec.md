## Requirements

### Requirement: 员工管理表格工号列正确展示

员工管理页面表格的「工号」列 SHALL 在 `employeeNo` 为空时回退显示 `username` 字段值，确保所有员工的工号均可见。

#### Scenario: 有 employeeNo 的员工显示 employeeNo

- **WHEN** 员工的 `employeeNo` 不为空
- **THEN**「工号」列展示 `employeeNo` 值

#### Scenario: employeeNo 为空的存量员工回退显示 username

- **WHEN** 员工的 `employeeNo` 为空但 `username` 不为空
- **THEN**「工号」列展示 `username` 值

#### Scenario: 两字段均为空时的最终回退

- **WHEN** 员工的 `employeeNo` 和 `username` 均为空
- **THEN**「工号」列展示 `-`
