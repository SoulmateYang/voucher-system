## Context

两个独立 Bug 导致 H5 端赠送卡券的领取和拒绝功能完全不可用：

1. **前端 Dialog 未注册**：`GiftInbox.vue` 使用 `Dialog.confirm()` 做二次确认，但 `Dialog` 组件未在 `main.js` 中全局注册。Vant 4 的函数式调用要求组件预先 `app.use()`，否则 `Dialog.confirm()` 返回的 Promise 既不 resolve 也不 reject，点击按钮无任何响应。

2. **Snowflake ID 精度丢失**：MyBatis-Plus 生成的 Snowflake ID（约 1.8×10¹⁸）超出 JavaScript `Number.MAX_SAFE_INTEGER`（约 9×10¹⁵）。现有的 `JacksonConfig.longToStringCustomizer` 仅对 Bean 属性中的直接 `Long` 字段生效，但 `GiftService.buildGiftResultList` 将 `giftId` 包装在 `Map<String, Object>` 中返回——Jackson 不保证对 Map value 应用类型级序列化器。

## Goals / Non-Goals

**Goals:**
- 用户点击领取/拒绝按钮后，确认弹窗可靠弹出
- `giftId` 在前后端传输中保持精确，后端能正确查找到 gift 记录

**Non-Goals:**
- 不修改 GiftInbox.vue 的业务逻辑
- 不修改 GiftService 或 GiftController
- 不修改 VoucherGift 实体

## Decisions

### 1. 全局 Long→String 序列化

使用 Spring Boot 配置属性 `spring.jackson.serialization.write-numbers-as-strings: true`，将**所有** Long 值序列化为 JSON 字符串。这比针对特定字段加 `@JsonSerialize` 更彻底，且覆盖了 Map value 中的 Long。

**替代方案对比**：
- `ToStringSerializer` + `@JsonSerialize(using=...)` 在每个实体字段上 —— 繁琐且遗漏 Map 场景
- `ObjectMapper.configure(JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS)` —— 等价于 yml 配置
- 前后端统一用 String 类型 ID —— 改动量大

**选择**：全局配置 `write-numbers-as-strings: true`，一劳永逸。

### 2. Dialog 全局注册

在 `main.js` 中 `import { Dialog } from 'vant'` 并 `app.use(Dialog)`。

Vant 4 的 `Dialog` 函数式调用（`Dialog.confirm()`）依赖组件已注册才能渲染。注册后不影响已有代码。

### 3. 清理 JacksonConfig

移除 `JacksonConfig.longToStringCustomizer` Bean——全局配置已覆盖所有 Long 场景，该 Bean 变为冗余。

## Risks / Trade-offs

- **所有 Long 字段都变成字符串**：前端接收到的所有 JSON 数字都会变成字符串（包括 `total`、`pageSize` 等分页计数字段）。前端已有 `request.js` 响应处理，不会引入新问题——分页组件 `el-pagination` / `van-pagination` 通常接受数字或数字字符串（Vant 支持字符串数字）。
- **性能**：字符串序列化略慢于数字，但差异可忽略（微秒级）。
