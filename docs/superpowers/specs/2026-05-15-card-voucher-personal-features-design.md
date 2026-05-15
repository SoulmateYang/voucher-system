# 因私卡券系统 — 个人端功能增强设计

日期：2026-05-15 | 基于：因私卡券系统功能设计.md | 策略：方案二（高价值功能先行）

## 背景

当前系统是企业管理模式（管理员创建批次→发券→核销），H5 端仅支持卡券列表查看和详情展示。需在现有架构上增强个人端（H5 + 后端）功能，按优先级：B（赠送转让）→ A（管理增强）→ D（拓展体验）。

---

## B1. 卡券赠送

### 流程

```
赠送方                        接收方
  ├─ 选择卡券
  ├─ 输入对方工号/手机号
  ├─ (可选)添加留言
  ├─ 确认赠送 → voucher.status=GIFTING
  │              voucher.holder_id 暂时不变
  │              gift_record 生成
  │
  │              ┌─ 收到消息 ────┤
  │              ├─ 立即领取 → 卡券归入接收方
  │              ├─ 拒绝 → 卡券退回赠送方
  │              └─ 暂存（不操作）
  │
  └─ 24h 内可撤销 ──────────────┘
```

### 规则

- 已使用/已过期/已作废/赠送中的卡券不可赠送
- batch.transferable=0 的卡券不可赠送（创建批次时可设置，默认允许）
- 赠送方撤销或 24h 超时未处理，卡券自动恢复
- voucher 新增中间状态 `GIFTING`

### 数据库变更

**voucher 表：**
```sql
ALTER TABLE voucher ADD COLUMN transferable TINYINT DEFAULT 1;
ALTER TABLE voucher ADD COLUMN source VARCHAR(16) DEFAULT 'BATCH';  -- BATCH / MANUAL
ALTER TABLE voucher ADD COLUMN image_url VARCHAR(255);
ALTER TABLE voucher ADD COLUMN remark VARCHAR(255);
```

**voucher_batch 表：**
```sql
ALTER TABLE voucher_batch ADD COLUMN transferable TINYINT DEFAULT 1;
```

**voucher_gift 表（新建）：**
```sql
CREATE TABLE voucher_gift (
  id            BIGINT PRIMARY KEY,
  voucher_id    BIGINT NOT NULL,
  from_user_id  VARCHAR(64) NOT NULL,
  from_user_name VARCHAR(64) NOT NULL,
  to_user_id    VARCHAR(64) NOT NULL,
  to_user_name  VARCHAR(64) NOT NULL,
  message       VARCHAR(200),
  status        VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  gift_at       DATETIME NOT NULL,
  handled_at    DATETIME,
  expire_at     DATETIME NOT NULL,
  created_at    DATETIME DEFAULT CURRENT_TIMESTAMP
);
-- status: PENDING / ACCEPTED / REJECTED / CANCELLED / EXPIRED
```

### API

| 方法 | 端点 | 说明 |
|---|---|---|
| POST | `/vouchers/my/gift` | 赠送卡券 |
| POST | `/vouchers/my/gift/{giftId}/cancel` | 撤销赠送 |
| GET | `/vouchers/my/gift/inbox` | 收到的赠送（待接收） |
| POST | `/vouchers/my/gift/{giftId}/accept` | 接收赠送 |
| POST | `/vouchers/my/gift/{giftId}/reject` | 拒绝赠送 |
| GET | `/vouchers/my/gift/outbox` | 发出的赠送记录 |

### 定时任务

每小时扫描 `voucher_gift` 表中 status=PENDING 且 expire_at < now 的记录，自动标记为 EXPIRED，恢复对应卡券状态。

### H5 改动

- 卡券详情页：卡券可赠送时显示"赠送"按钮
- 新增赠送页：选择接收人（搜索员工）、留言、确认
- 新增接收页：待接收卡片列表，操作按钮（领取/拒绝）
- 卡券列表：赠送中的卡券不显示

---

## A1. 搜索与筛选

### H5 端

卡券列表顶部搜索栏：关键词搜索 + 状态筛选 + 类型筛选。

### API

`GET /vouchers/my` 新增 query 参数：

| 参数 | 类型 | 说明 |
|---|---|---|
| keyword | string | 模糊匹配资源描述 |
| status | string | ISSUED / USED / EXPIRED / CANCELLED |
| voucherType | string | RESOURCE_USAGE / COUPON |

后端 SQL 动态拼接条件。

---

## A2. 收藏与置顶

### 数据库

```sql
ALTER TABLE voucher ADD COLUMN is_favorite TINYINT DEFAULT 0;
ALTER TABLE voucher ADD COLUMN is_pinned TINYINT DEFAULT 0;
ALTER TABLE voucher ADD COLUMN pinned_at DATETIME;
```

### API

| 方法 | 端点 | 说明 |
|---|---|---|
| POST | `/vouchers/my/{id}/favorite` | 切换收藏 |
| POST | `/vouchers/my/{id}/pin` | 切换置顶 |

### 排序规则

置顶优先 → 收藏优先 → 过期时间升序（快过期在前）。

---

## D1. 手动录入

### API

| 方法 | 端点 | 说明 |
|---|---|---|
| POST | `/vouchers/my/manual` | 手动录入卡券 |

请求体：
```json
{
  "name": "卡券名称",
  "voucherType": "RESOURCE_USAGE | COUPON",
  "faceValue": 100.00,
  "expireAt": "2026-12-31",
  "voucherCode": "核销码（选填）",
  "remark": "备注（选填）"
}
```

录入的卡券 batch_id 为空，source=MANUAL，自动生成券码。

### H5

新增录入页面，表单字段：名称、类型、面值、有效期、核销码、备注。卡券图片上传后续迭代。

---

## D2. 离线使用

纯前端：卡券详情页打开时缓存券码/名称/类型/有效期到 localStorage，无网络时从缓存读取，顶部显示"离线模式"提示。

---

## 实施顺序

1. B1 卡券赠送（后端 + H5）
2. A1 搜索筛选（后端 + H5）
3. A2 收藏置顶（后端 + H5）
4. D1 手动录入（后端 + H5）
5. D2 离线缓存（纯前端）
