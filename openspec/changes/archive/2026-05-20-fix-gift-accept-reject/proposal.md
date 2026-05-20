## Why

H5 端用户收到赠送卡券后，点击"领取"或"拒绝"按钮无任何响应——两个独立 Bug 叠加导致功能完全不可用：(1) `Dialog` 组件未在 `main.js` 中全局注册，`Dialog.confirm()` 在部分 Vant 4 配置下静默挂起，点击按钮后确认弹窗无法弹出；(2) Snowflake 主键通过 `Map<String, Object>` 序列化时，Jackson 的 `ToStringSerializer` 未作用于嵌套 Map 中的 Long 值，导致 `giftId` 在 JavaScript 中丢失精度，后端收到错误的 ID 后抛出「赠送记录不存在」。

## What Changes

- **前端 `main.js`**：新增 `Dialog` 组件的全局注册（`app.use(Dialog)`），确保 `Dialog.confirm()` 可靠弹出
- **后端 `application.yml`**：新增 `spring.jackson.serialization.write-numbers-as-strings: true`，全局将 Long 序列化为字符串，彻底解决 Snowflake ID 精度丢失问题（替代仅对 Bean 字段生效的 `ToStringSerializer`）
- 移除 `JacksonConfig` 中不完整的 `longToStringCustomizer`（已被全局配置替代）

## Capabilities

### Modified Capabilities
- `coupon-h5-display`: 修复赠送卡券领取/拒绝功能不可用的问题

## Impact

- **前端 `main.js`**：新增 1 行 import + 1 行 `app.use`
- **后端 `application.yml`**：新增 2 行配置
- **后端 `JacksonConfig.java`**：移除 `longToStringCustomizer` Bean（已被全局配置替代）
- **后端 `application.yml`（test）**：同步添加全局配置
- 无新增依赖，无 BREAKING 变更
