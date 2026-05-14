# 因私卡券管理系统

企业内部因私卡券管理工具，覆盖卡券批次管理、发放、核销、审计全流程。

## 系统架构

```
┌──────────────────────────┬──────────────────────┬─────────────────────┐
│   frontend-admin         │   frontend-h5        │   backend           │
│   PC 管理端 (Vue 3)       │   员工移动端 (Vue 3)    │   Spring Boot 3.2   │
│   Element Plus           │   Vant UI            │   MyBatis-Plus      │
│   Port 5173              │   Port 5174 (默认)    │   MySQL + Flyway    │
│                          │                      │   JWT Auth          │
└──────────────────────────┴──────────────────────┴─────────────────────┘
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2.5, Java 17 |
| ORM | MyBatis-Plus 3.5.6 |
| 数据库 | MySQL, Flyway 迁移 |
| 认证 | JWT (jjwt 0.12), Spring Security, BCrypt |
| 工具库 | Hutool 5.8, EasyExcel 3.3 |
| PC 管理端 | Vue 3 + Element Plus + Axios + qrcode.js |
| H5 移动端 | Vue 3 + Vant 4 + Axios + qrcodejs2 |

## 数据库模型

```
┌─────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│  voucher_batch  │     │     voucher      │     │ verification_log │
│  (卡券批次)       │────▶│    (卡券实例)      │────▶│   (核销记录)       │
├─────────────────┤     ├──────────────────┤     ├──────────────────┤
│ id (Snowflake)  │     │ id (Snowflake)   │     │ id              │
│ batch_name      │     │ batch_id (FK)    │     │ voucher_id      │
│ voucher_type    │     │ voucher_code     │     │ voucher_code    │
│ resource_desc   │     │ holder_id        │     │ holder_id/name  │
│ total_count     │     │ holder_name      │     │ operator_id     │
│ valid_days      │     │ status           │     │ verified_at     │
│ status (A/P/F)  │     │ version (乐观锁)   │     └──────────────────┘
└─────────────────┘     │ issued_at        │
                        │ expire_at        │     ┌──────────────────┐
                        │ used_at          │     │    audit_log     │
                        │ approve_ref      │     │   (审计日志)       │
                        └──────────────────┘     └──────────────────┘

                        ┌──────────────────┐
                        │    sys_user      │
                        │   (系统用户)       │
                        │ ADMIN / EMPLOYEE │
                        └──────────────────┘
```

## 核心业务流程

```
管理员创建批次 → 选定员工发放卡券 → 生成 32 位券码 (Snowflake + Base32)
                                       │
                                 员工 H5 端查看
                                       │
                                 出示二维码 / 券码
                                       │
                         管理员核销台扫码确认 → 核销日志 + 审计日志
```

## 券码生成

32 位券码格式：`前缀(2) + Snowflake→Base32(26) + 校验和(4)`，带 3 次冲突重试。

## API 路由

| 路径 | 权限 | 说明 |
|------|------|------|
| `POST /api/v1/auth/login` | 公开 | 登录 |
| `POST /api/v1/batches` | ADMIN | 创建批次 |
| `GET /api/v1/batches` | ADMIN | 批次列表 |
| `POST /api/v1/batches/issue` | ADMIN | 批量发放卡券 |
| `POST /api/v1/vouchers/lookup` | ADMIN | 查询券信息 |
| `POST /api/v1/vouchers/confirm` | ADMIN | 确认核销 |
| `GET /api/v1/vouchers/today-records` | ADMIN | 今日核销记录 |
| `PUT /api/v1/vouchers/{id}/cancel` | ADMIN | 作废卡券 |
| `GET /api/v1/vouchers/report` | ADMIN | 统计报表 |
| `GET /api/v1/vouchers/export` | ADMIN | 导出 Excel |
| `GET /api/v1/vouchers/my/**` | 需认证 | 员工查看自己的券 |
| `/api/v1/callback/**` | 公开 | 外部系统回调 |

## 安全机制

- BCrypt 密码加密
- JWT 无状态认证（24 小时有效期）
- MyBatis-Plus 乐观锁（`voucher.version`）防并发核销
- 审计日志全链路追踪（ISSUE / VERIFY / CANCEL / EXPIRE）

## 设计规范

详见 [DESIGN.md](DESIGN.md)。Industrial / Utilitarian 风格，主色 `#1989fa`，功能优先、零装饰。

## 快速开始

### 后端

```bash
cd backend
# 确保 MySQL 运行，创建数据库 voucher_system
# 配置环境变量: DB_PASSWORD, JWT_SECRET, CALLBACK_SECRET
./mvnw spring-boot:run
```

### PC 管理端

```bash
cd frontend-admin
npm install
npm run dev       # http://localhost:5173
```

### H5 移动端

```bash
cd frontend-h5
npm install
npm run dev       # http://localhost:5174
```

### 默认账号

预置管理员：`admin` / `admin123`

## 待办事项

| 编号 | 内容 | 说明 |
|------|------|------|
| TODO-1 | CSV 批量导入员工 | Admin 上传 CSV 解析员工列表 |
| TODO-2 | 批次状态切换 API | `PUT /api/v1/admin/batches/{id}/status` |
| TODO-3 | H5 员工使用记录 | 核销历史查询页面 |

详见 [TODOS.md](TODOS.md)。
