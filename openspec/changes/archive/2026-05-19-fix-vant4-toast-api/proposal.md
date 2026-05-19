## Why

Vant 4.x 将 Toast API 从实例方法（`Toast.success()` / `Toast.fail()`）改为独立函数（`showSuccessToast()` / `showFailToast()`）。H5 端代码仍在使用旧的 Vant 2/3 风格 API，导致运行时抛出 `TypeError: Toast.fail is not a function`，登录成功后无法跳转到"我的卡券"页面。这是一个阻断性 bug，影响所有 Toast 提示的用户交互流程。

## What Changes

- 将所有 `Toast.success('...')` 调用替换为 `showSuccessToast('...')`
- 将所有 `Toast.fail('...')` 调用替换为 `showFailToast('...')`
- 更新各文件的 import 语句，从 `vant` 导入 `showSuccessToast` 和 `showFailToast`（Vant 4 官方 API）
- `frontend-h5/src/main.js` 中 `app.use(Toast)` 保持不变，因为 `Toast` 组件注册仍然需要

## Capabilities

### New Capabilities

<!-- 此修复为纯 bugfix，不引入新能力 -->

### Modified Capabilities

<!-- 此修复不改变任何 spec 级别的行为需求 -->

## Impact

- **Affected files (7 files, ~18 处调用点)**:
  - `frontend-h5/src/views/Login.vue` — 1 处 `Toast.success` + 2 处 `Toast.fail`
  - `frontend-h5/src/views/VoucherDetail.vue` — 1 处 `Toast.success` + 1 处 `Toast.fail`
  - `frontend-h5/src/views/GiftSend.vue` — 1 处 `Toast.success` + 2 处 `Toast.fail`
  - `frontend-h5/src/views/GiftInbox.vue` — 3 处 `Toast.success`
  - `frontend-h5/src/views/ManualAdd.vue` — 1 处 `Toast.success`
  - `frontend-h5/src/views/VoucherList.vue` — 2 处 `Toast.success`
  - `frontend-h5/src/api/request.js` — 5 处 `Toast.fail`
- **Breaking**: 无，Vant 版本不变，仅修正 API 调用方式
