-- 将存量数据中 employee_no 为 NULL 的记录用 username（原始工号）补齐
-- username 是 V1 定义的必填字段，作为工号的主数据源
UPDATE sys_user SET employee_no = username WHERE employee_no IS NULL;
