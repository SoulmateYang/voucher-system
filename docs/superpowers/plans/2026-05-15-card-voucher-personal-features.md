# 因私卡券个人端功能增强 — 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在现有企业卡券系统上增强 H5 个人端功能：卡券赠送、搜索筛选、收藏置顶、手动录入、离线缓存

**Architecture:** 沿用现有分层架构（Controller → Service → Mapper → Entity），H5 端新增页面和组件。新增 `voucher_gift` 表管理赠送记录，通过懒校验处理超时。所有新端点挂在 `/api/v1/vouchers/my/` 下，复用现有鉴权。

**Tech Stack:** Spring Boot 3.2.5 + MyBatis-Plus 3.5.6 + MySQL (Flyway) / Vue 3 + Vant 4 + Vue Router 4

---

## 文件结构

### 新建文件

| 文件 | 职责 |
|---|---|
| `backend/.../entity/VoucherGift.java` | 赠送记录实体 |
| `backend/.../mapper/VoucherGiftMapper.java` | 赠送记录 Mapper |
| `backend/.../service/GiftService.java` | 赠送业务逻辑（含懒校验超时处理） |
| `backend/.../controller/GiftController.java` | 赠送 API 端点 |
| `backend/.../dto/GiftVoucherRequest.java` | 赠送请求 DTO |
| `backend/.../dto/ManualVoucherRequest.java` | 手动录入请求 DTO |
| `backend/.../db/migration/V6__personal_features.sql` | 数据库迁移 |
| `frontend-h5/src/views/GiftSend.vue` | 赠送发起页 |
| `frontend-h5/src/views/GiftInbox.vue` | 赠送收件箱页 |
| `frontend-h5/src/views/ManualAdd.vue` | 手动录入页 |

### 修改文件

| 文件 | 改动 |
|---|---|
| `backend/.../entity/Voucher.java` | 加 transferable/source/imageUrl/isFavorite/isPinned/pinnedAt |
| `backend/.../entity/VoucherBatch.java` | 加 transferable |
| `backend/.../service/VoucherService.java` | 加 favorite/pin/manual 方法，listByHolderPaged 支持筛选排序 |
| `backend/.../controller/EmployeeVoucherController.java` | 加 favorite/pin/manual 端点 |
| `backend/.../dto/CreateBatchRequest.java` | 加 transferable |
| `backend/.../service/VoucherBatchService.java` | create/issue 传递 transferable |
| `frontend-h5/src/router/index.js` | 加 GiftSend/GiftInbox/ManualAdd 路由 |
| `frontend-h5/src/api/voucher.js` | 加 gift/manual/favorite/pin API |
| `frontend-h5/src/views/VoucherList.vue` | 加搜索栏、筛选、收藏/置顶按钮、排序 |
| `frontend-h5/src/views/VoucherDetail.vue` | 加赠送按钮、离线缓存 |

---

## Task 1: 数据库迁移

**Files:**
- Create: `backend/src/main/resources/db/migration/V6__personal_features.sql`

- [ ] **Step 1: 编写 V6 迁移 SQL**

```sql
-- V6: personal features - gift, favorite, pin, manual entry

ALTER TABLE voucher
    ADD COLUMN transferable TINYINT DEFAULT 1,
    ADD COLUMN source VARCHAR(16) DEFAULT 'BATCH',
    ADD COLUMN image_url VARCHAR(255),
    ADD COLUMN is_favorite TINYINT DEFAULT 0,
    ADD COLUMN is_pinned TINYINT DEFAULT 0,
    ADD COLUMN pinned_at DATETIME;

ALTER TABLE voucher_batch
    ADD COLUMN transferable TINYINT DEFAULT 1;

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

CREATE INDEX idx_gift_from ON voucher_gift(from_user_id, status);
CREATE INDEX idx_gift_to ON voucher_gift(to_user_id, status);
CREATE INDEX idx_gift_voucher ON voucher_gift(voucher_id);
```

- [ ] **Step 2: 提交**

```bash
git add backend/src/main/resources/db/migration/V6__personal_features.sql
git commit -m "feat: add V6 migration for personal features (gift, favorite, pin, manual)"
```

---

## Task 2: Voucher 实体更新

**Files:**
- Modify: `backend/src/main/java/com/example/voucher/entity/Voucher.java`
- Modify: `backend/src/main/java/com/example/voucher/entity/VoucherBatch.java`

- [ ] **Step 1: 更新 Voucher 实体**

在 `Voucher.java` 的 `remark` 字段后添加新字段：

```java
private Integer transferable;

private String source;

private String imageUrl;

private Integer isFavorite;

private Integer isPinned;

private LocalDateTime pinnedAt;
```

- [ ] **Step 2: 更新 VoucherBatch 实体**

在 `VoucherBatch.java` 的 `faceValue` 字段后添加：

```java
private Integer transferable;
```

- [ ] **Step 3: 验证编译**

```bash
cd backend && mvn compile -q
```

- [ ] **Step 4: 提交**

```bash
git add backend/src/main/java/com/example/voucher/entity/Voucher.java backend/src/main/java/com/example/voucher/entity/VoucherBatch.java
git commit -m "feat: add transferable, favorite, pin, manual fields to entities"
```

---

## Task 3: VoucherGift 实体与 Mapper

**Files:**
- Create: `backend/src/main/java/com/example/voucher/entity/VoucherGift.java`
- Create: `backend/src/main/java/com/example/voucher/mapper/VoucherGiftMapper.java`
- Create: `backend/src/main/java/com/example/voucher/dto/GiftVoucherRequest.java`

- [ ] **Step 1: 创建 VoucherGift 实体**

```java
package com.example.voucher.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("voucher_gift")
public class VoucherGift {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long voucherId;

    private String fromUserId;

    private String fromUserName;

    private String toUserId;

    private String toUserName;

    private String message;

    private String status;

    private LocalDateTime giftAt;

    private LocalDateTime handledAt;

    private LocalDateTime expireAt;

    private LocalDateTime createdAt;
}
```

- [ ] **Step 2: 创建 VoucherGiftMapper**

```java
package com.example.voucher.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.voucher.entity.VoucherGift;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VoucherGiftMapper extends BaseMapper<VoucherGift> {
}
```

- [ ] **Step 3: 创建 GiftVoucherRequest DTO**

```java
package com.example.voucher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GiftVoucherRequest {

    @NotNull(message = "卡券ID不能为空")
    private Long voucherId;

    @NotBlank(message = "接收人工号不能为空")
    private String toEmployeeId;

    private String message;
}
```

- [ ] **Step 4: 验证编译**

```bash
cd backend && mvn compile -q
```

- [ ] **Step 5: 提交**

```bash
git add backend/src/main/java/com/example/voucher/entity/VoucherGift.java backend/src/main/java/com/example/voucher/mapper/VoucherGiftMapper.java backend/src/main/java/com/example/voucher/dto/GiftVoucherRequest.java
git commit -m "feat: add VoucherGift entity, mapper, and gift request DTO"
```

---

## Task 4: GiftService — 赠送核心逻辑

**Files:**
- Create: `backend/src/main/java/com/example/voucher/service/GiftService.java`

- [ ] **Step 1: 创建 GiftService**

```java
package com.example.voucher.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.voucher.common.BusinessException;
import com.example.voucher.entity.Voucher;
import com.example.voucher.entity.VoucherGift;
import com.example.voucher.entity.SysUser;
import com.example.voucher.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GiftService {

    private final VoucherGiftMapper giftMapper;
    private final VoucherMapper voucherMapper;
    private final VoucherBatchMapper batchMapper;
    private final SysUserMapper sysUserMapper;

    /** 赠送卡券 */
    @Transactional
    public Map<String, Object> gift(Long voucherId, String fromUserId, String fromUserName,
                                     String toEmployeeId, String message) {
        Voucher voucher = voucherMapper.selectById(voucherId);
        if (voucher == null) {
            throw new BusinessException("卡券不存在");
        }
        if (!voucher.getHolderId().equals(fromUserId)) {
            throw new BusinessException("无权操作该卡券");
        }
        if ("USED".equals(voucher.getStatus())) {
            throw new BusinessException("已使用的卡券不可赠送");
        }
        if ("EXPIRED".equals(voucher.getStatus())) {
            throw new BusinessException("已过期的卡券不可赠送");
        }
        if ("CANCELLED".equals(voucher.getStatus())) {
            throw new BusinessException("已作废的卡券不可赠送");
        }
        if ("GIFTING".equals(voucher.getStatus())) {
            throw new BusinessException("该卡券正在赠送中");
        }
        if (voucher.getTransferable() != null && voucher.getTransferable() == 0) {
            throw new BusinessException("该卡券不可转赠");
        }

        // 查找接收人
        SysUser toUser = sysUserMapper.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, toEmployeeId)
                .eq(SysUser::getEnabled, 1)
        );
        if (toUser == null) {
            throw new BusinessException("接收人不存在");
        }
        if (toUser.getUsername().equals(fromUserId)) {
            throw new BusinessException("不可赠送给自己");
        }

        // 更新卡券状态
        voucher.setStatus("GIFTING");
        voucherMapper.updateById(voucher);

        // 创建赠送记录
        VoucherGift gift = new VoucherGift();
        gift.setVoucherId(voucher.getId());
        gift.setFromUserId(fromUserId);
        gift.setFromUserName(fromUserName);
        gift.setToUserId(toUser.getUsername());
        gift.setToUserName(toUser.getRealName());
        gift.setMessage(message);
        gift.setStatus("PENDING");
        gift.setGiftAt(LocalDateTime.now());
        gift.setExpireAt(LocalDateTime.now().plusHours(24));
        giftMapper.insert(gift);

        Map<String, Object> result = new HashMap<>();
        result.put("giftId", gift.getId());
        result.put("expireAt", gift.getExpireAt().toString());
        return result;
    }

    /** 撤销赠送 */
    @Transactional
    public void cancelGift(Long giftId, String userId) {
        VoucherGift gift = giftMapper.selectById(giftId);
        if (gift == null) {
            throw new BusinessException("赠送记录不存在");
        }
        if (!gift.getFromUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        if (!"PENDING".equals(gift.getStatus())) {
            throw new BusinessException("该赠送已处理，无法撤销");
        }
        if (LocalDateTime.now().isAfter(gift.getExpireAt())) {
            gift.setStatus("EXPIRED");
            gift.setHandledAt(LocalDateTime.now());
            giftMapper.updateById(gift);
            restoreVoucher(gift.getVoucherId());
            throw new BusinessException("该赠送已超时");
        }

        gift.setStatus("CANCELLED");
        gift.setHandledAt(LocalDateTime.now());
        giftMapper.updateById(gift);

        restoreVoucher(gift.getVoucherId());
    }

    /** 接收赠送 */
    @Transactional
    public void acceptGift(Long giftId, String userId) {
        VoucherGift gift = giftMapper.selectById(giftId);
        if (gift == null) {
            throw new BusinessException("赠送记录不存在");
        }
        if (!gift.getToUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        if (!"PENDING".equals(gift.getStatus())) {
            throw new BusinessException("该赠送已处理");
        }
        if (LocalDateTime.now().isAfter(gift.getExpireAt())) {
            gift.setStatus("EXPIRED");
            gift.setHandledAt(LocalDateTime.now());
            giftMapper.updateById(gift);
            restoreVoucher(gift.getVoucherId());
            throw new BusinessException("该赠送已超时");
        }

        // 转移卡券归属
        Voucher voucher = voucherMapper.selectById(gift.getVoucherId());
        voucher.setHolderId(gift.getToUserId());
        voucher.setHolderName(gift.getToUserName());
        voucher.setStatus("ISSUED");
        voucherMapper.updateById(voucher);

        gift.setStatus("ACCEPTED");
        gift.setHandledAt(LocalDateTime.now());
        giftMapper.updateById(gift);
    }

    /** 拒绝赠送 */
    @Transactional
    public void rejectGift(Long giftId, String userId) {
        VoucherGift gift = giftMapper.selectById(giftId);
        if (gift == null) {
            throw new BusinessException("赠送记录不存在");
        }
        if (!gift.getToUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        if (!"PENDING".equals(gift.getStatus())) {
            throw new BusinessException("该赠送已处理");
        }
        if (LocalDateTime.now().isAfter(gift.getExpireAt())) {
            gift.setStatus("EXPIRED");
            gift.setHandledAt(LocalDateTime.now());
            giftMapper.updateById(gift);
            restoreVoucher(gift.getVoucherId());
            throw new BusinessException("该赠送已超时");
        }

        gift.setStatus("REJECTED");
        gift.setHandledAt(LocalDateTime.now());
        giftMapper.updateById(gift);

        restoreVoucher(gift.getVoucherId());
    }

    /** 查询收件箱（含懒校验超时） */
    public List<Map<String, Object>> getInbox(String userId) {
        // 懒校验：把当前用户待接收但超时的记录标记为 EXPIRED
        processExpiredGifts(userId);

        List<VoucherGift> gifts = giftMapper.selectList(
            new LambdaQueryWrapper<VoucherGift>()
                .eq(VoucherGift::getToUserId, userId)
                .orderByDesc(VoucherGift::getGiftAt)
        );
        return buildGiftResultList(gifts);
    }

    /** 查询发件箱（含懒校验超时） */
    public List<Map<String, Object>> getOutbox(String userId) {
        // 懒校验：把当前用户发出但超时的记录标记为 EXPIRED
        List<VoucherGift> expiredGifts = giftMapper.selectList(
            new LambdaQueryWrapper<VoucherGift>()
                .eq(VoucherGift::getFromUserId, userId)
                .eq(VoucherGift::getStatus, "PENDING")
                .lt(VoucherGift::getExpireAt, LocalDateTime.now())
        );
        for (VoucherGift g : expiredGifts) {
            g.setStatus("EXPIRED");
            g.setHandledAt(LocalDateTime.now());
            giftMapper.updateById(g);
            restoreVoucher(g.getVoucherId());
        }

        List<VoucherGift> gifts = giftMapper.selectList(
            new LambdaQueryWrapper<VoucherGift>()
                .eq(VoucherGift::getFromUserId, userId)
                .orderByDesc(VoucherGift::getGiftAt)
        );
        return buildGiftResultList(gifts);
    }

    private void processExpiredGifts(String userId) {
        List<VoucherGift> expiredGifts = giftMapper.selectList(
            new LambdaQueryWrapper<VoucherGift>()
                .eq(VoucherGift::getToUserId, userId)
                .eq(VoucherGift::getStatus, "PENDING")
                .lt(VoucherGift::getExpireAt, LocalDateTime.now())
        );
        for (VoucherGift g : expiredGifts) {
            g.setStatus("EXPIRED");
            g.setHandledAt(LocalDateTime.now());
            giftMapper.updateById(g);
            restoreVoucher(g.getVoucherId());
        }
    }

    private void restoreVoucher(Long voucherId) {
        Voucher voucher = voucherMapper.selectById(voucherId);
        if (voucher != null && "GIFTING".equals(voucher.getStatus())) {
            // 找到该卡券最初的赠送记录，获取赠送方信息
            VoucherGift gift = giftMapper.selectOne(
                new LambdaQueryWrapper<VoucherGift>()
                    .eq(VoucherGift::getVoucherId, voucherId)
                    .orderByDesc(VoucherGift::getGiftAt)
                    .last("LIMIT 1")
            );
            voucher.setHolderId(gift.getFromUserId());
            voucher.setHolderName(gift.getFromUserName());
            voucher.setStatus("ISSUED");
            voucherMapper.updateById(voucher);
        }
    }

    private List<Map<String, Object>> buildGiftResultList(List<VoucherGift> gifts) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (VoucherGift g : gifts) {
            Voucher voucher = voucherMapper.selectById(g.getVoucherId());
            Map<String, Object> item = new HashMap<>();
            item.put("giftId", g.getId());
            item.put("voucherId", g.getVoucherId());
            item.put("fromUserId", g.getFromUserId());
            item.put("fromUserName", g.getFromUserName());
            item.put("toUserId", g.getToUserId());
            item.put("toUserName", g.getToUserName());
            item.put("message", g.getMessage());
            item.put("status", g.getStatus());
            item.put("giftAt", g.getGiftAt().toString());
            item.put("expireAt", g.getExpireAt().toString());
            if (g.getHandledAt() != null) {
                item.put("handledAt", g.getHandledAt().toString());
            }
            if (voucher != null) {
                item.put("voucherCode", voucher.getVoucherCode());
                item.put("faceValue", voucher.getFaceValue());
                item.put("remark", voucher.getRemark());
                item.put("expireAt", voucher.getExpireAt() != null ? voucher.getExpireAt().toString() : null);
            }
            result.add(item);
        }
        return result;
    }
}
```

- [ ] **Step 2: 验证编译**

```bash
cd backend && mvn compile -q
```

- [ ] **Step 3: 提交**

```bash
git add backend/src/main/java/com/example/voucher/service/GiftService.java
git commit -m "feat: add GiftService with lazy expiry validation"
```

---

## Task 5: GiftController — 赠送 API 端点

**Files:**
- Create: `backend/src/main/java/com/example/voucher/controller/GiftController.java`

- [ ] **Step 1: 创建 GiftController**

```java
package com.example.voucher.controller;

import com.example.voucher.common.Result;
import com.example.voucher.dto.GiftVoucherRequest;
import com.example.voucher.service.AuthService;
import com.example.voucher.service.GiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vouchers/my/gift")
@RequiredArgsConstructor
public class GiftController {

    private final GiftService giftService;
    private final AuthService authService;

    @PostMapping
    public Result<Object> gift(@Valid @RequestBody GiftVoucherRequest request,
                                Authentication auth) {
        var user = authService.getCurrentUser(auth.getName());
        String userId = user != null ? user.getUsername() : auth.getName();
        String userName = user != null ? user.getRealName() : auth.getName();
        return Result.success(giftService.gift(
            request.getVoucherId(), userId, userName,
            request.getToEmployeeId(), request.getMessage()
        ));
    }

    @PostMapping("/{giftId}/cancel")
    public Result<Void> cancelGift(@PathVariable Long giftId, Authentication auth) {
        var user = authService.getCurrentUser(auth.getName());
        String userId = user != null ? user.getUsername() : auth.getName();
        giftService.cancelGift(giftId, userId);
        return Result.success();
    }

    @GetMapping("/inbox")
    public Result<Object> inbox(Authentication auth) {
        var user = authService.getCurrentUser(auth.getName());
        String userId = user != null ? user.getUsername() : auth.getName();
        return Result.success(giftService.getInbox(userId));
    }

    @PostMapping("/{giftId}/accept")
    public Result<Void> accept(@PathVariable Long giftId, Authentication auth) {
        var user = authService.getCurrentUser(auth.getName());
        String userId = user != null ? user.getUsername() : auth.getName();
        giftService.acceptGift(giftId, userId);
        return Result.success();
    }

    @PostMapping("/{giftId}/reject")
    public Result<Void> reject(@PathVariable Long giftId, Authentication auth) {
        var user = authService.getCurrentUser(auth.getName());
        String userId = user != null ? user.getUsername() : auth.getName();
        giftService.rejectGift(giftId, userId);
        return Result.success();
    }

    @GetMapping("/outbox")
    public Result<Object> outbox(Authentication auth) {
        var user = authService.getCurrentUser(auth.getName());
        String userId = user != null ? user.getUsername() : auth.getName();
        return Result.success(giftService.getOutbox(userId));
    }
}
```

- [ ] **Step 2: 确保 SecurityConfig 覆盖新端点**

`/api/v1/vouchers/my/gift/**` 已被 `/api/v1/vouchers/my/**` 规则覆盖为 `authenticated()`，无需修改。

- [ ] **Step 3: 验证编译**

```bash
cd backend && mvn compile -q
```

- [ ] **Step 4: 提交**

```bash
git add backend/src/main/java/com/example/voucher/controller/GiftController.java
git commit -m "feat: add GiftController with gift/cancel/inbox/accept/reject/outbox endpoints"
```

---

## Task 6: H5 赠送 API 封装

**Files:**
- Modify: `frontend-h5/src/api/voucher.js`

- [ ] **Step 1: 添加赠送相关 API**

在 `voucher.js` 末尾追加：

```js
/** 赠送卡券 */
export function giftVoucher(data) {
  return request.post('/vouchers/my/gift', data);
}

/** 撤销赠送 */
export function cancelGift(giftId) {
  return request.post(`/vouchers/my/gift/${giftId}/cancel`);
}

/** 收件箱 */
export function getGiftInbox() {
  return request.get('/vouchers/my/gift/inbox');
}

/** 发件箱 */
export function getGiftOutbox() {
  return request.get('/vouchers/my/gift/outbox');
}

/** 接收赠送 */
export function acceptGift(giftId) {
  return request.post(`/vouchers/my/gift/${giftId}/accept`);
}

/** 拒绝赠送 */
export function rejectGift(giftId) {
  return request.post(`/vouchers/my/gift/${giftId}/reject`);
}
```

- [ ] **Step 2: 提交**

```bash
git add frontend-h5/src/api/voucher.js
git commit -m "feat: add gift API functions to H5"
```

---

## Task 7: H5 路由 — 新页面注册

**Files:**
- Modify: `frontend-h5/src/router/index.js`

- [ ] **Step 1: 添加赠送和录入路由**

在 routes 数组中 `VoucherDetail` 路由后追加：

```js
  {
    path: '/gift-send/:id',
    name: 'GiftSend',
    component: () => import('../views/GiftSend.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/gift-inbox',
    name: 'GiftInbox',
    component: () => import('../views/GiftInbox.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/gift-outbox',
    name: 'GiftOutbox',
    component: () => import('../views/GiftInbox.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/manual-add',
    name: 'ManualAdd',
    component: () => import('../views/ManualAdd.vue'),
    meta: { requiresAuth: true },
  },
```

- [ ] **Step 2: 提交**

```bash
git add frontend-h5/src/router/index.js
git commit -m "feat: add GiftSend, GiftInbox, ManualAdd routes to H5"
```

---

## Task 8: H5 赠送发起页

**Files:**
- Create: `frontend-h5/src/views/GiftSend.vue`

- [ ] **Step 1: 创建 GiftSend.vue**

```vue
<template>
  <div class="gift-send-page">
    <van-nav-bar title="赠送卡券" left-arrow @click-left="goBack" fixed placeholder />

    <div class="gift-content">
      <!-- 卡券预览 -->
      <div class="voucher-preview card" :class="{ 'is-coupon': voucher.voucherType === 'COUPON' }">
        <div v-if="voucher.voucherType === 'COUPON'" class="coupon-value">
          <span class="coupon-symbol">¥</span>
          <span class="coupon-amount">{{ voucher.faceValue }}</span>
        </div>
        <div class="voucher-name">{{ voucher.remark || '卡券' }}</div>
        <div class="voucher-expire">有效期至 {{ formatDate(voucher.expireAt) }}</div>
      </div>

      <!-- 接收人 -->
      <div class="form-section card">
        <van-field
          v-model="toEmployeeId"
          label="接收人工号"
          placeholder="请输入对方工号"
          :rules="[{ required: true, message: '请输入工号' }]"
        />
        <van-field
          v-model="message"
          label="留言"
          placeholder="选填，给对方留言"
          maxlength="100"
          type="textarea"
          rows="2"
          autosize
        />
      </div>

      <div class="gift-action">
        <van-button type="primary" block round @click="onGift" :loading="submitting">
          确认赠送
        </van-button>
        <p class="gift-notice">赠送后对方需在 24 小时内领取，超时自动退回</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Toast } from 'vant';
import { getVoucherDetail, giftVoucher } from '../api/voucher';

const route = useRoute();
const router = useRouter();

const voucher = ref({});
const toEmployeeId = ref('');
const message = ref('');
const submitting = ref(false);

function formatDate(dateStr) {
  if (!dateStr) return '--';
  const date = new Date(dateStr);
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

async function fetchVoucher() {
  try {
    const res = await getVoucherDetail(route.params.id);
    voucher.value = res?.data || res;
  } catch {
    Toast.fail('加载卡券信息失败');
    router.back();
  }
}

async function onGift() {
  if (!toEmployeeId.value.trim()) {
    Toast.fail('请输入接收人工号');
    return;
  }
  submitting.value = true;
  try {
    await giftVoucher({
      voucherId: voucher.value.id,
      toEmployeeId: toEmployeeId.value.trim(),
      message: message.value.trim(),
    });
    Toast.success('赠送成功');
    router.replace({ name: 'VoucherList' });
  } catch {
    // error handled by interceptor
  } finally {
    submitting.value = false;
  }
}

function goBack() {
  router.back();
}

onMounted(() => {
  fetchVoucher();
});
</script>

<style scoped>
.gift-send-page {
  min-height: 100vh;
  background: var(--color-bg);
}

.gift-content {
  padding: var(--spacing-md);
}

.voucher-preview {
  text-align: center;
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-md);
}

.voucher-preview.is-coupon {
  background: #fff7e6;
  border-color: #ffd666;
}

.coupon-value {
  margin-bottom: var(--spacing-xs);
}

.coupon-symbol {
  font-size: 16px;
  font-weight: 700;
  color: #ee0a24;
}

.coupon-amount {
  font-size: 32px;
  font-weight: 700;
  color: #ee0a24;
  font-family: 'JetBrains Mono', 'SF Mono', monospace;
}

.voucher-name {
  font-size: var(--font-size-card-title);
  font-weight: 600;
  color: var(--color-text-primary);
}

.voucher-expire {
  margin-top: var(--spacing-xs);
  font-size: var(--font-size-small);
  color: var(--color-text-secondary);
}

.form-section {
  margin-bottom: var(--spacing-md);
}

.gift-action {
  padding: 0 var(--spacing-sm);
}

.gift-notice {
  margin-top: var(--spacing-md);
  text-align: center;
  font-size: var(--font-size-small);
  color: var(--color-text-secondary);
  line-height: 1.5;
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add frontend-h5/src/views/GiftSend.vue
git commit -m "feat: add H5 GiftSend page"
```

---

## Task 9: H5 赠送收件箱页

**Files:**
- Create: `frontend-h5/src/views/GiftInbox.vue`

- [ ] **Step 1: 创建 GiftInbox.vue**

```vue
<template>
  <div class="gift-inbox-page">
    <van-nav-bar
      :title="isOutbox ? '发出的赠送' : '收到的赠送'"
      left-arrow
      @click-left="goBack"
      fixed
      placeholder
    />

    <van-tabs v-model:active="activeTab" @change="onTabChange">
      <van-tab title="收到的" />
      <van-tab title="发出的" />
    </van-tabs>

    <div class="gift-content">
      <template v-if="loading">
        <van-skeleton title round row="3" v-for="n in 3" :key="n" class="skeleton-item" />
      </template>

      <template v-else-if="list.length === 0">
        <div class="empty-state">
          <van-icon name="envelop-o" size="48" color="#c8c9cc" />
          <p class="empty-text">{{ isOutbox ? '暂无发出的赠送' : '暂无收到的赠送' }}</p>
        </div>
      </template>

      <template v-else>
        <div
          v-for="item in list"
          :key="item.giftId"
          class="gift-card card"
        >
          <div class="gift-card-body">
            <div class="gift-voucher-info">
              <div v-if="item.faceValue" class="gift-face-value">¥{{ item.faceValue }}</div>
              <div class="gift-voucher-name">{{ item.remark || '卡券' }}</div>
              <div class="gift-voucher-code mono">{{ item.voucherCode }}</div>
            </div>
            <div class="gift-meta">
              <span v-if="isOutbox">接收人：{{ item.toUserName }}</span>
              <span v-else>赠送人：{{ item.fromUserName }}</span>
              <span class="gift-time">{{ formatDate(item.giftAt) }}</span>
            </div>
            <div v-if="item.message" class="gift-message">留言：{{ item.message }}</div>
            <van-tag :type="statusType(item.status)" size="small">{{ statusLabel(item.status) }}</van-tag>
          </div>
          <div v-if="item.status === 'PENDING' && !isOutbox" class="gift-card-actions">
            <van-button size="small" plain type="danger" @click="onReject(item)">拒绝</van-button>
            <van-button size="small" type="primary" @click="onAccept(item)">领取</van-button>
          </div>
          <div v-if="item.status === 'PENDING' && isOutbox" class="gift-card-actions">
            <van-button size="small" plain type="danger" @click="onCancel(item)">撤销</van-button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Toast, Dialog } from 'vant';
import { getGiftInbox, getGiftOutbox, acceptGift, rejectGift, cancelGift } from '../api/voucher';

const router = useRouter();
const activeTab = ref(0);
const list = ref([]);
const loading = ref(false);

const isOutbox = computed(() => activeTab.value === 1);

const STATUS_MAP = {
  PENDING: { label: '待处理', type: 'warning' },
  ACCEPTED: { label: '已领取', type: 'success' },
  REJECTED: { label: '已拒绝', type: 'danger' },
  CANCELLED: { label: '已撤销', type: '' },
  EXPIRED: { label: '已超时', type: '' },
};

function statusLabel(s) { return STATUS_MAP[s]?.label || s; }
function statusType(s) { return STATUS_MAP[s]?.type || ''; }

function formatDate(dateStr) {
  if (!dateStr) return '--';
  const date = new Date(dateStr);
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  const h = String(date.getHours()).padStart(2, '0');
  const min = String(date.getMinutes()).padStart(2, '0');
  return `${y}-${m}-${d} ${h}:${min}`;
}

async function fetchList() {
  loading.value = true;
  try {
    const fn = isOutbox.value ? getGiftOutbox : getGiftInbox;
    const res = await fn();
    list.value = res?.data || [];
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false;
  }
}

async function onAccept(item) {
  try {
    await Dialog.confirm({ title: '确认领取', message: '领取后卡券将归入你的卡券库' });
  } catch { return; }
  try {
    await acceptGift(item.giftId);
    Toast.success('领取成功');
    fetchList();
  } catch { /* handled */ }
}

async function onReject(item) {
  try {
    await Dialog.confirm({ title: '确认拒绝', message: '拒绝后卡券将退回赠送方' });
  } catch { return; }
  try {
    await rejectGift(item.giftId);
    Toast.success('已拒绝');
    fetchList();
  } catch { /* handled */ }
}

async function onCancel(item) {
  try {
    await Dialog.confirm({ title: '确认撤销', message: '撤销后卡券将回到你的卡券库' });
  } catch { return; }
  try {
    await cancelGift(item.giftId);
    Toast.success('已撤销');
    fetchList();
  } catch { /* handled */ }
}

function onTabChange() {
  fetchList();
}

function goBack() {
  router.back();
}

onMounted(() => {
  const fromRoute = router.currentRoute.value;
  if (fromRoute.name === 'GiftOutbox') {
    activeTab.value = 1;
  }
  fetchList();
});
</script>

<style scoped>
.gift-inbox-page {
  min-height: 100vh;
  background: var(--color-bg);
}

.gift-content {
  padding: var(--spacing-md);
}

.skeleton-item {
  margin-bottom: var(--spacing-sm);
  padding: var(--spacing-md);
  background: var(--color-card);
  border-radius: var(--radius-md);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px var(--spacing-md);
}

.empty-text {
  margin-top: var(--spacing-md);
  font-size: var(--font-size-body);
  color: var(--color-text-secondary);
}

.gift-card {
  margin-bottom: var(--spacing-sm);
}

.gift-card-body {
  position: relative;
}

.gift-voucher-info {
  margin-bottom: var(--spacing-xs);
}

.gift-face-value {
  font-size: 20px;
  font-weight: 700;
  color: #ee0a24;
  font-family: 'JetBrains Mono', 'SF Mono', monospace;
}

.gift-voucher-name {
  font-size: var(--font-size-card-title);
  font-weight: 600;
  color: var(--color-text-primary);
}

.gift-voucher-code {
  font-size: var(--font-size-small);
  color: var(--color-text-secondary);
  word-break: break-all;
}

.gift-meta {
  font-size: var(--font-size-small);
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-2xs);
  display: flex;
  justify-content: space-between;
}

.gift-time {
  color: var(--color-text-placeholder);
}

.gift-message {
  font-size: var(--font-size-small);
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-xs);
}

.gift-card-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-sm);
  padding-top: var(--spacing-sm);
  border-top: 1px solid var(--color-border-light);
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add frontend-h5/src/views/GiftInbox.vue
git commit -m "feat: add H5 GiftInbox page with accept/reject/cancel"
```

---

## Task 10: VoucherDetail 添加赠送入口

**Files:**
- Modify: `frontend-h5/src/views/VoucherDetail.vue`

- [ ] **Step 1: 在详情页添加赠送按钮**

在 `VoucherDetail.vue` 的 QR 区域和 info-card 之间插入赠送按钮。找到 `<!-- Detail info card -->` 注释，在其前面插入：

```vue
        <!-- Gift action -->
        <div v-if="canGift" class="gift-section card">
          <van-button type="warning" block round @click="onGift">
            赠送给好友
          </van-button>
        </div>
```

- [ ] **Step 2: 在 script 中添加赠送逻辑**

在 `script setup` 中添加：

```js
import { computed } from 'vue';

const canGift = computed(() => {
  return voucher.value?.status === 'ISSUED';
});

function onGift() {
  router.push({ name: 'GiftSend', params: { id: voucher.value.id } });
}
```

注意：`useRouter` 已在文件中引入，`computed` 需加到 import 中。

- [ ] **Step 3: 添加赠送区域样式**

在 `<style scoped>` 中追加：

```css
.gift-section {
  margin-bottom: var(--spacing-sm);
  padding: var(--spacing-md);
}
```

- [ ] **Step 4: 提交**

```bash
git add frontend-h5/src/views/VoucherDetail.vue
git commit -m "feat: add gift button to VoucherDetail page"
```

---

## Task 11: VoucherList 过滤 GIFTING 状态卡券 + 收件箱入口

**Files:**
- Modify: `frontend-h5/src/views/VoucherList.vue`

- [ ] **Step 1: 在导航栏添加收件箱图标**

在 `van-nav-bar` 标签上添加右侧图标：

```vue
    <van-nav-bar title="我的卡券" fixed placeholder>
      <template #right>
        <van-icon name="envelop-o" size="20" @click="goToInbox" />
      </template>
    </van-nav-bar>
```

- [ ] **Step 2: 添加 goToInbox 方法和右上角新增入口**

在 `script setup` 中添加：

```js
function goToInbox() {
  router.push({ name: 'GiftInbox' });
}
```

- [ ] **Step 3: 在页面底部添加浮动按钮**

在 `van-pull-refresh` 结束标签后、`</div>` 前添加：

```vue
      <!-- Float action buttons -->
      <div class="float-actions">
        <van-button
          icon="add-o"
          type="primary"
          round
          class="float-btn"
          @click="goToManualAdd"
        />
      </div>
```

对应的 script 方法：

```js
function goToManualAdd() {
  router.push({ name: 'ManualAdd' });
}
```

样式：

```css
.float-actions {
  position: fixed;
  right: 16px;
  bottom: 80px;
  z-index: 10;
}

.float-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  box-shadow: var(--shadow-lg);
}
```

注意：`ManualAdd` 路由将在 Task 15 创建，先预留入口。当前点击跳转不会报错（router.push 目标是延迟注册的）。

- [ ] **Step 4: 提交**

```bash
git add frontend-h5/src/views/VoucherList.vue
git commit -m "feat: add inbox icon and manual add float button to VoucherList"
```

---

## Task 12: VoucherService — favorite/pin/manual 方法

**Files:**
- Modify: `backend/src/main/java/com/example/voucher/service/VoucherService.java`

- [ ] **Step 1: 添加 favorite/pin 切换方法**

在 `VoucherService.java` 末尾（最后一个 `}` 前）添加：

```java
    public void toggleFavorite(Long voucherId, String holderId) {
        Voucher voucher = voucherMapper.selectById(voucherId);
        if (voucher == null || !voucher.getHolderId().equals(holderId)) {
            throw new BusinessException("卡券不存在或无权操作");
        }
        voucher.setIsFavorite(voucher.getIsFavorite() != null && voucher.getIsFavorite() == 1 ? 0 : 1);
        voucherMapper.updateById(voucher);
    }

    public void togglePin(Long voucherId, String holderId) {
        Voucher voucher = voucherMapper.selectById(voucherId);
        if (voucher == null || !voucher.getHolderId().equals(holderId)) {
            throw new BusinessException("卡券不存在或无权操作");
        }
        if (voucher.getIsPinned() != null && voucher.getIsPinned() == 1) {
            voucher.setIsPinned(0);
            voucher.setPinnedAt(null);
        } else {
            voucher.setIsPinned(1);
            voucher.setPinnedAt(LocalDateTime.now());
        }
        voucherMapper.updateById(voucher);
    }

    @Transactional
    public Map<String, Object> addManual(Voucher voucher, String holderId, String holderName) {
        voucher.setHolderId(holderId);
        voucher.setHolderName(holderName);
        voucher.setStatus("ISSUED");
        voucher.setVersion(0);
        voucher.setSource("MANUAL");
        voucher.setTransferable(1);
        voucher.setIssuedAt(LocalDateTime.now());
        voucher.setCreatedAt(LocalDateTime.now());
        voucher.setUpdatedAt(LocalDateTime.now());

        // generate voucher code with retry
        for (int i = 0; i < 3; i++) {
            voucher.setVoucherCode(voucherCodeUtil.generate());
            try {
                voucherMapper.insert(voucher);
                break;
            } catch (DuplicateKeyException e) {
                if (i == 2) throw new BusinessException("券码生成冲突，请重试");
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", voucher.getId());
        result.put("voucherCode", voucher.getVoucherCode());
        return result;
    }
```

- [ ] **Step 2: 修改 listByHolderPaged 支持筛选和排序**

将 `listByHolderPaged` 方法签名改为：

```java
    public Map<String, Object> listByHolderPaged(String holderId, int page, int size,
                                                  String keyword, String status, String voucherType) {
```

方法体内，构建查询条件替代原有简单 LambdaQueryWrapper：

```java
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Voucher> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(Voucher::getHolderId, holderId);

        // 排除赠送中的卡券
        wrapper.ne(Voucher::getStatus, "GIFTING");

        if (status != null && !status.isEmpty()) {
            wrapper.eq(Voucher::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Voucher::getRemark, keyword);
        }

        // 排序：置顶优先 → 收藏优先 → 过期时间升序
        wrapper.orderByDesc(Voucher::getIsPinned)
               .orderByDesc(Voucher::getIsFavorite)
               .orderByAsc(Voucher::getExpireAt);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Voucher> pageResult =
            voucherMapper.selectPage(p, wrapper);
```

对于 voucherType 筛选，需要在获取结果后过滤（因为 voucherType 来自 batch 表）。在构建 records 循环中增加过滤：

```java
        for (Voucher v : vouchers) {
            String vt = batchTypeMap.get(v.getBatchId());
            if (voucherType != null && !voucherType.isEmpty() && !voucherType.equals(vt)) {
                continue;
            }
            // ... existing item building code
        }
```

同时在 item map 中添加新字段：

```java
            item.put("isFavorite", v.getIsFavorite());
            item.put("isPinned", v.getIsPinned());
            item.put("source", v.getSource());
            item.put("transferable", v.getTransferable());
```

**注意：** 由于 voucherType 过滤在内存中完成，分页 total 可能不准确。对于个人端数据量（每用户通常不超过几百条），此方案可接受。

- [ ] **Step 3: 验证编译**

```bash
cd backend && mvn compile -q
```

- [ ] **Step 4: 提交**

```bash
git add backend/src/main/java/com/example/voucher/service/VoucherService.java
git commit -m "feat: add favorite, pin, manual entry methods; enhance list query with filter and sort"
```

---

## Task 13: EmployeeVoucherController 新增端点

**Files:**
- Modify: `backend/src/main/java/com/example/voucher/controller/EmployeeVoucherController.java`

- [ ] **Step 1: 更新 list 端点支持筛选、添加 favorite/pin/manual 端点**

替换整个文件：

```java
package com.example.voucher.controller;

import com.example.voucher.common.Result;
import com.example.voucher.entity.Voucher;
import com.example.voucher.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/vouchers/my")
@RequiredArgsConstructor
public class EmployeeVoucherController {

    private final VoucherService voucherService;
    private final com.example.voucher.service.AuthService authService;

    @GetMapping
    public Result<Object> list(Authentication auth,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "20") int size,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) String voucherType) {
        var user = authService.getCurrentUser(auth.getName());
        String holderId = user != null ? user.getUsername() : auth.getName();
        return Result.success(voucherService.listByHolderPaged(holderId, page, size,
            keyword, status, voucherType));
    }

    @GetMapping("/{id}")
    public Result<Object> getById(@PathVariable Long id, Authentication auth) {
        var user = authService.getCurrentUser(auth.getName());
        String holderId = user != null ? user.getUsername() : auth.getName();
        Voucher voucher = voucherService.getById(id);
        if (voucher == null || !voucher.getHolderId().equals(holderId)) {
            return Result.error(403, "无权查看该券");
        }
        return Result.success(voucherService.getDetailById(id));
    }

    @PostMapping("/{id}/favorite")
    public Result<Void> toggleFavorite(@PathVariable Long id, Authentication auth) {
        var user = authService.getCurrentUser(auth.getName());
        String holderId = user != null ? user.getUsername() : auth.getName();
        voucherService.toggleFavorite(id, holderId);
        return Result.success();
    }

    @PostMapping("/{id}/pin")
    public Result<Void> togglePin(@PathVariable Long id, Authentication auth) {
        var user = authService.getCurrentUser(auth.getName());
        String holderId = user != null ? user.getUsername() : auth.getName();
        voucherService.togglePin(id, holderId);
        return Result.success();
    }

    @PostMapping("/manual")
    public Result<Object> addManual(@RequestBody ManualVoucherRequest request,
                                     Authentication auth) {
        var user = authService.getCurrentUser(auth.getName());
        String holderId = user != null ? user.getUsername() : auth.getName();
        String holderName = user != null ? user.getRealName() : auth.getName();

        Voucher voucher = new Voucher();
        voucher.setRemark(request.getName());
        voucher.setFaceValue(request.getFaceValue());
        if (request.getExpireAt() != null && !request.getExpireAt().isEmpty()) {
            voucher.setExpireAt(LocalDate.parse(request.getExpireAt(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")).atTime(LocalTime.MAX));
        }
        return Result.success(voucherService.addManual(voucher, holderId, holderName));
    }
}
```

- [ ] **Step 2: 创建 ManualVoucherRequest DTO**

```java
package com.example.voucher.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ManualVoucherRequest {

    @NotBlank(message = "卡券名称不能为空")
    private String name;

    private String voucherType;

    private BigDecimal faceValue;

    private String expireAt;

    private String voucherCode;

    private String remark;
}
```

- [ ] **Step 3: 验证编译**

```bash
cd backend && mvn compile -q
```

- [ ] **Step 4: 提交**

```bash
git add backend/src/main/java/com/example/voucher/controller/EmployeeVoucherController.java backend/src/main/java/com/example/voucher/dto/ManualVoucherRequest.java
git commit -m "feat: add favorite, pin, manual endpoints to EmployeeVoucherController"
```

---

## Task 14: A1 — H5 搜索筛选 + A2 收藏置顶

**Files:**
- Modify: `frontend-h5/src/views/VoucherList.vue`
- Modify: `frontend-h5/src/api/voucher.js`

- [ ] **Step 1: 在 voucher.js 添加 favorite/pin API**

```js
/** 切换收藏 */
export function toggleFavorite(id) {
  return request.post(`/vouchers/my/${id}/favorite`);
}

/** 切换置顶 */
export function togglePin(id) {
  return request.post(`/vouchers/my/${id}/pin`);
}

/** 手动录入卡券 */
export function addManualVoucher(data) {
  return request.post('/vouchers/my/manual', data);
}
```

- [ ] **Step 2: 替换 VoucherList.vue 完整内容**

由于改动量大（搜索栏 + 筛选 + 收藏/置顶按钮 + 排序），完整替换文件：

```vue
<template>
  <div class="voucher-list-page">
    <van-nav-bar title="我的卡券" fixed placeholder>
      <template #right>
        <van-icon name="envelop-o" size="20" @click="goToInbox" />
      </template>
    </van-nav-bar>

    <!-- Search and filter bar -->
    <div class="search-bar">
      <van-search
        v-model="keyword"
        placeholder="搜索卡券"
        shape="round"
        @search="onSearch"
      />
      <div class="filter-row">
        <van-dropdown-menu>
          <van-dropdown-item v-model="statusFilter" :options="statusOptions" @change="onFilterChange" />
          <van-dropdown-item v-model="typeFilter" :options="typeOptions" @change="onFilterChange" />
        </van-dropdown-menu>
      </div>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <template v-if="loading && vouchers.length === 0">
        <div class="skeleton-list">
          <van-skeleton title round row="4" v-for="n in 4" :key="n" class="skeleton-item" />
        </div>
      </template>

      <template v-else-if="error && vouchers.length === 0">
        <div class="error-state">
          <van-icon name="warn-o" size="48" color="#c8c9cc" />
          <p class="error-text">加载失败</p>
          <van-button type="primary" size="small" @click="fetchVouchers" class="retry-btn">重新加载</van-button>
        </div>
      </template>

      <template v-else-if="!loading && vouchers.length === 0">
        <div class="empty-state">
          <van-icon name="coupon-o" size="64" color="#c8c9cc" />
          <p class="empty-title">还没有卡券</p>
          <p class="empty-desc">如有需要请联系管理员申请</p>
        </div>
      </template>

      <template v-else>
        <van-list
          v-model:loading="listLoading"
          :finished="listFinished"
          finished-text="没有更多了"
          @load="onLoadMore"
        >
          <div class="voucher-cards">
            <div
              v-for="item in vouchers"
              :key="item.id"
              class="voucher-card card"
              :class="{ 'is-coupon': item.voucherType === 'COUPON', 'is-pinned': item.isPinned }"
              @click="goToDetail(item.id)"
            >
              <div class="voucher-card-header">
                <div class="voucher-card-left">
                  <div v-if="item.voucherType === 'COUPON'" class="coupon-value">
                    <span class="coupon-symbol">¥</span>
                    <span class="coupon-amount">{{ item.faceValue }}</span>
                  </div>
                  <span class="voucher-card-title">
                    <van-icon v-if="item.isPinned" name="star" size="14" color="#fa8c16" class="pin-icon" />
                    {{ item.remark || '卡券' }}
                  </span>
                </div>
                <van-tag :class="statusTagClass(item.status)" size="small">
                  {{ statusLabel(item.status) }}
                </van-tag>
              </div>
              <div class="voucher-card-meta">
                <span class="meta-label">有效期至</span>
                <span class="meta-value">{{ formatDate(item.expireAt) }}</span>
              </div>
              <div class="voucher-card-footer">
                <van-icon
                  :name="item.isFavorite ? 'like' : 'like-o'"
                  :color="item.isFavorite ? '#ee0a24' : '#c8c9cc'"
                  size="18"
                  @click.stop="onToggleFavorite(item)"
                />
                <van-icon
                  :name="item.isPinned ? 'star' : 'star-o'"
                  :color="item.isPinned ? '#fa8c16' : '#c8c9cc'"
                  size="18"
                  @click.stop="onTogglePin(item)"
                />
              </div>
            </div>
          </div>
        </van-list>
      </template>
    </van-pull-refresh>

    <div class="float-actions">
      <van-button icon="add-o" type="primary" round class="float-btn" @click="goToManualAdd" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Toast } from 'vant';
import { getMyVouchers, toggleFavorite, togglePin } from '../api/voucher';

const router = useRouter();

const vouchers = ref([]);
const loading = ref(false);
const error = ref(false);
const refreshing = ref(false);
const listLoading = ref(false);
const listFinished = ref(false);

const keyword = ref('');
const statusFilter = ref('');
const typeFilter = ref('');

const queryParams = ref({ page: 1, size: 20 });

const statusOptions = [
  { text: '全部状态', value: '' },
  { text: '有效', value: 'ISSUED' },
  { text: '已使用', value: 'USED' },
  { text: '已过期', value: 'EXPIRED' },
  { text: '已作废', value: 'CANCELLED' },
];

const typeOptions = [
  { text: '全部类型', value: '' },
  { text: '资源使用', value: 'RESOURCE_USAGE' },
  { text: '优惠券', value: 'COUPON' },
];

const STATUS_MAP = {
  ISSUED: { label: '有效', class: 'tag-valid' },
  USED: { label: '已使用', class: 'tag-used' },
  EXPIRED: { label: '已过期', class: 'tag-expired' },
  CANCELLED: { label: '已作废', class: 'tag-revoked' },
};

function statusLabel(s) { return STATUS_MAP[s]?.label || s; }
function statusTagClass(s) { return STATUS_MAP[s]?.class || ''; }

function formatDate(dateStr) {
  if (!dateStr) return '--';
  const date = new Date(dateStr);
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

async function fetchVouchers(reset = false) {
  if (reset) {
    queryParams.value.page = 1;
    listFinished.value = false;
    vouchers.value = [];
  }

  loading.value = true;
  error.value = false;

  try {
    const params = {
      page: queryParams.value.page,
      size: queryParams.value.size,
    };
    if (keyword.value) params.keyword = keyword.value;
    if (statusFilter.value) params.status = statusFilter.value;
    if (typeFilter.value) params.voucherType = typeFilter.value;

    const res = await getMyVouchers(params);
    const records = res?.data?.records || res?.data || [];
    const total = res?.data?.total || records.length;

    if (reset) {
      vouchers.value = records;
    } else {
      vouchers.value = [...vouchers.value, ...records];
    }

    if (vouchers.value.length >= total) {
      listFinished.value = true;
    }
  } catch (err) {
    error.value = true;
  } finally {
    loading.value = false;
    listLoading.value = false;
    refreshing.value = false;
  }
}

function onSearch() { fetchVouchers(true); }
function onFilterChange() { fetchVouchers(true); }
function onRefresh() { fetchVouchers(true); }

function onLoadMore() {
  queryParams.value.page += 1;
  listLoading.value = true;
  fetchVouchers(false);
}

async function onToggleFavorite(item) {
  try {
    await toggleFavorite(item.id);
    item.isFavorite = item.isFavorite ? 0 : 1;
    Toast.success(item.isFavorite ? '已收藏' : '已取消收藏');
  } catch { /* handled */ }
}

async function onTogglePin(item) {
  try {
    await togglePin(item.id);
    item.isPinned = item.isPinned ? 0 : 1;
    Toast.success(item.isPinned ? '已置顶' : '已取消置顶');
  } catch { /* handled */ }
}

function goToDetail(id) { router.push({ name: 'VoucherDetail', params: { id } }); }
function goToInbox() { router.push({ name: 'GiftInbox' }); }
function goToManualAdd() { router.push({ name: 'ManualAdd' }); }

onMounted(() => { fetchVouchers(true); });
</script>

<style scoped>
.voucher-list-page { min-height: 100vh; background: var(--color-bg); }

.search-bar {
  background: var(--color-card);
  padding-bottom: var(--spacing-xs);
  border-bottom: 1px solid var(--color-border-light);
}

.filter-row { padding: 0 var(--spacing-md); }

.skeleton-list { padding: var(--spacing-md); }
.skeleton-item {
  margin-bottom: var(--spacing-sm);
  padding: var(--spacing-md);
  background: var(--color-card);
  border-radius: var(--radius-md);
}

.error-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px var(--spacing-md);
}

.empty-title { margin-top: var(--spacing-md); font-size: var(--font-size-card-title); color: var(--color-text-primary); }
.empty-desc { margin-top: var(--spacing-xs); font-size: var(--font-size-small); color: var(--color-text-secondary); }
.error-text { margin-top: var(--spacing-md); font-size: var(--font-size-body); color: var(--color-text-secondary); }
.retry-btn { margin-top: var(--spacing-md); min-width: 120px; }

.voucher-cards { padding: var(--spacing-sm) var(--spacing-md); }

.voucher-card {
  margin-bottom: var(--spacing-sm);
  cursor: pointer;
  transition: box-shadow 0.2s ease;
}

.voucher-card:active { box-shadow: var(--shadow-md); }

.voucher-card.is-pinned {
  border-color: #ffd666;
}

.pin-icon {
  margin-right: 4px;
  vertical-align: middle;
}

.voucher-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: var(--spacing-sm);
}

.voucher-card-left { flex: 1; min-width: 0; }

.coupon-value { display: flex; align-items: baseline; margin-bottom: 4px; }
.coupon-symbol { font-size: 14px; font-weight: 700; color: #ee0a24; margin-right: 2px; }
.coupon-amount { font-size: 24px; font-weight: 700; color: #ee0a24; font-family: 'JetBrains Mono', 'SF Mono', monospace; line-height: 1; }

.voucher-card.is-coupon { background: #fff7e6; border-color: #ffd666; }

.voucher-card-title {
  font-size: var(--font-size-card-title);
  font-weight: 600;
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.voucher-card-meta {
  display: flex;
  align-items: center;
  font-size: var(--font-size-small);
  margin-bottom: var(--spacing-xs);
}

.meta-label { color: var(--color-text-secondary); margin-right: var(--spacing-xs); }
.meta-value { color: var(--color-text-primary); }

.voucher-card-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-md);
  padding-top: var(--spacing-xs);
}

.float-actions {
  position: fixed;
  right: 16px;
  bottom: 80px;
  z-index: 10;
}

.float-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  box-shadow: var(--shadow-lg);
}
</style>
```

- [ ] **Step 3: 提交**

```bash
git add frontend-h5/src/views/VoucherList.vue frontend-h5/src/api/voucher.js
git commit -m "feat: add search, filter, favorite, pin to H5 VoucherList"
```

---

## Task 15: D1 — H5 手动录入页

**Files:**
- Create: `frontend-h5/src/views/ManualAdd.vue`

- [ ] **Step 1: 创建 ManualAdd.vue**

```vue
<template>
  <div class="manual-add-page">
    <van-nav-bar title="添加卡券" left-arrow @click-left="goBack" fixed placeholder />

    <div class="form-content">
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field
            v-model="form.name"
            label="卡券名称"
            placeholder="请输入卡券名称"
            :rules="[{ required: true, message: '请输入名称' }]"
          />
          <van-field
            v-model="form.voucherType"
            label="卡券类型"
            placeholder="请选择"
            is-link
            readonly
            @click="showTypePicker = true"
            :rules="[{ required: true, message: '请选择类型' }]"
          />
          <van-field
            v-if="form.voucherType === 'COUPON'"
            v-model="form.faceValue"
            label="面值"
            placeholder="请输入面值"
            type="number"
          />
          <van-field
            v-model="form.expireAt"
            label="有效期"
            placeholder="请选择"
            is-link
            readonly
            @click="showDatePicker = true"
          />
          <van-field
            v-model="form.voucherCode"
            label="核销码"
            placeholder="选填"
          />
          <van-field
            v-model="form.remark"
            label="备注"
            placeholder="选填"
            type="textarea"
            rows="2"
            autosize
          />
        </van-cell-group>

        <div class="submit-area">
          <van-button type="primary" block round native-type="submit" :loading="submitting">
            确认添加
          </van-button>
        </div>
      </van-form>
    </div>

    <!-- Type picker -->
    <van-popup v-model:show="showTypePicker" position="bottom">
      <van-picker
        :columns="typeColumns"
        @confirm="onTypeConfirm"
        @cancel="showTypePicker = false"
      />
    </van-popup>

    <!-- Date picker -->
    <van-popup v-model:show="showDatePicker" position="bottom">
      <van-date-picker
        :min-date="minDate"
        @confirm="onDateConfirm"
        @cancel="showDatePicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { Toast } from 'vant';
import { addManualVoucher } from '../api/voucher';

const router = useRouter();
const submitting = ref(false);
const showTypePicker = ref(false);
const showDatePicker = ref(false);

const form = reactive({
  name: '',
  voucherType: '',
  faceValue: '',
  expireAt: '',
  voucherCode: '',
  remark: '',
});

const typeColumns = [
  { text: '资源使用券', value: 'RESOURCE_USAGE' },
  { text: '优惠券', value: 'COUPON' },
];

const minDate = new Date();

function onTypeConfirm({ selectedOptions }) {
  form.voucherType = selectedOptions[0]?.text || '';
  showTypePicker.value = false;
}

function onDateConfirm(date) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  form.expireAt = `${y}-${m}-${d}`;
  showDatePicker.value = false;
}

async function onSubmit() {
  submitting.value = true;
  try {
    await addManualVoucher({
      name: form.name,
      voucherType: form.voucherType,
      faceValue: form.faceValue ? parseFloat(form.faceValue) : null,
      expireAt: form.expireAt,
      voucherCode: form.voucherCode,
      remark: form.remark,
    });
    Toast.success('添加成功');
    router.replace({ name: 'VoucherList' });
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false;
  }
}

function goBack() { router.back(); }
</script>

<style scoped>
.manual-add-page {
  min-height: 100vh;
  background: var(--color-bg);
}

.form-content {
  padding: var(--spacing-md);
}

.submit-area {
  margin-top: var(--spacing-lg);
  padding: 0 var(--spacing-sm);
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add frontend-h5/src/views/ManualAdd.vue
git commit -m "feat: add H5 ManualAdd page for manual voucher entry"
```

---

## Task 16: D2 — 离线缓存

**Files:**
- Modify: `frontend-h5/src/views/VoucherDetail.vue`

- [ ] **Step 1: 在 fetchDetail 成功后缓存数据**

在 `fetchDetail` 函数中，`voucher.value = data;` 之后添加：

```js
    // 缓存到 localStorage 用于离线查看
    try {
      const cache = {
        voucherCode: data.voucherCode,
        remark: data.remark,
        voucherType: data.voucherType,
        faceValue: data.faceValue,
        status: data.status,
        expireAt: data.expireAt,
        cachedAt: new Date().toISOString(),
      };
      localStorage.setItem(`voucher_cache_${id}`, JSON.stringify(cache));
    } catch { /* ignore quota errors */ }
```

- [ ] **Step 2: 在网络失败时尝试读取缓存**

在 `fetchDetail` 的 catch 块中，`error.value = true;` 之前添加离线读取逻辑：

```js
    // 尝试从缓存读取
    try {
      const cached = localStorage.getItem(`voucher_cache_${id}`);
      if (cached) {
        const data = JSON.parse(cached);
        voucher.value = {
          ...data,
          voucherCode: data.voucherCode,
          remark: data.remark + '（离线）',
          voucherType: data.voucherType,
          faceValue: data.faceValue,
          status: data.status,
          expireAt: data.expireAt,
          isOffline: true,
        };
        loading.value = false;
      }
    } catch { /* ignore */ }
```

- [ ] **Step 3: 模板中显示离线提示**

在 `detail-content` div 开头（voucher-header 前）添加：

```vue
          <div v-if="voucher.isOffline" class="offline-banner">
            <van-icon name="warn-o" size="16" />
            <span>离线模式 — 显示缓存数据</span>
          </div>
```

样式：

```css
.offline-banner {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm) var(--spacing-md);
  margin-bottom: var(--spacing-sm);
  background: #fff7e6;
  border-radius: var(--radius-md);
  font-size: var(--font-size-small);
  color: #fa8c16;
}
```

- [ ] **Step 4: 提交**

```bash
git add frontend-h5/src/views/VoucherDetail.vue
git commit -m "feat: add offline cache support to VoucherDetail"
```

---

## Task 17: CreateBatchRequest 添加 transferable

**Files:**
- Modify: `backend/src/main/java/com/example/voucher/dto/CreateBatchRequest.java`
- Modify: `backend/src/main/java/com/example/voucher/service/VoucherBatchService.java`

- [ ] **Step 1: CreateBatchRequest 添加 transferable**

在 `CreateBatchRequest.java` 中添加：

```java
    private Integer transferable;
```

- [ ] **Step 2: VoucherBatchService.create 传递 transferable**

在 `VoucherBatchService.java` 的 `create` 方法中，构建 `VoucherBatch` 对象时添加：

```java
        if (request.getTransferable() != null) {
            batch.setTransferable(request.getTransferable());
        }
```

- [ ] **Step 3: VoucherBatchService.issue 传递 transferable**

在 `VoucherBatchService.java` 的 `issue` 方法（或最终调用 `VoucherService.issueBatch`）中，确保发券时将 `batch.getTransferable()` 赋值给 `voucher.setTransferable()`。

查看 `VoucherService.issueSingle` 方法——需要在其中添加：

```java
        voucher.setTransferable(batch.getTransferable());
```

- [ ] **Step 4: 验证编译并提交**

```bash
cd backend && mvn compile -q
```

```bash
git add backend/src/main/java/com/example/voucher/dto/CreateBatchRequest.java backend/src/main/java/com/example/voucher/service/VoucherBatchService.java backend/src/main/java/com/example/voucher/service/VoucherService.java
git commit -m "feat: propagate transferable from batch to voucher on create and issue"
```

---

## Task 18: 集成验证

- [ ] **Step 1: 启动后端验证编译和启动**

```bash
cd backend && mvn spring-boot:run
```

检查启动日志无异常，确认 V6 迁移执行成功。

- [ ] **Step 2: 启动前端验证编译**

```bash
cd frontend-h5 && npm run dev
```

确认页面可访问，路由正常。

- [ ] **Step 3: 端到端验证清单**

| 验证项 | 操作 |
|---|---|
| 搜索筛选 | 列表页关键词搜索、状态筛选、类型筛选 |
| 收藏置顶 | 列表页点击收藏/置顶，刷新后排序正确 |
| 赠送发起 | 详情页点击赠送 → 输入工号 → 确认 |
| 赠送撤销 | 发件箱中撤销 |
| 赠送接收 | 收件箱中领取/拒绝 |
| 手动录入 | 浮动按钮 → 填表 → 提交 |
| 离线缓存 | 详情页断网刷新 → 显示离线数据 |

---

## 实施顺序

```
Task 1 (迁移) → Task 2 (实体) → Task 3 (Gift实体/Mapper)
→ Task 4 (GiftService) → Task 5 (GiftController)
→ Task 6 (H5 API) → Task 7 (H5路由) → Task 8 (GiftSend页)
→ Task 9 (GiftInbox页) → Task 10 (详情赠送入口)
→ Task 11 (列表入口) → Task 12 (VoucherService增强)
→ Task 13 (Controller增强) → Task 14 (H5搜索/筛选/收藏/置顶)
→ Task 15 (手动录入页) → Task 16 (离线缓存)
→ Task 17 (Batch transferable) → Task 18 (集成验证)
```
