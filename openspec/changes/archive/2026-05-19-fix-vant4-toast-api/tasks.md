## 1. 修复 API 层 (request.js)

- [x] 1.1 将 `import { Toast } from 'vant'` 替换为 `import { showFailToast } from 'vant'`
- [x] 1.2 将 5 处 `Toast.fail(...)` 替换为 `showFailToast(...)`

## 2. 修复登录页 (Login.vue)

- [x] 2.1 将 `import { Toast } from 'vant'` 替换为 `import { showSuccessToast, showFailToast } from 'vant'`
- [x] 2.2 将 `Toast.success('登录成功')` 替换为 `showSuccessToast('登录成功')`
- [x] 2.3 将 2 处 `Toast.fail(...)` 替换为 `showFailToast(...)`

## 3. 修复卡券详情页 (VoucherDetail.vue)

- [x] 3.1 将 `import { Toast, showToast } from 'vant'` 替换为 `import { showToast, showSuccessToast, showFailToast } from 'vant'`
- [x] 3.2 将 `Toast.success('已复制券码')` 替换为 `showSuccessToast('已复制券码')`
- [x] 3.3 将 `Toast.fail('复制失败')` 替换为 `showFailToast('复制失败')`

## 4. 修复赠送页 (GiftSend.vue)

- [x] 4.1 将 `import { Toast } from 'vant'` 替换为 `import { showSuccessToast, showFailToast } from 'vant'`
- [x] 4.2 将 `Toast.success('赠送成功')` 替换为 `showSuccessToast('赠送成功')`
- [x] 4.3 将 2 处 `Toast.fail(...)` 替换为 `showFailToast(...)`

## 5. 修复收件箱页 (GiftInbox.vue)

- [x] 5.1 将 `import { Toast, showConfirmDialog } from 'vant'` 替换为 `import { showSuccessToast, showConfirmDialog } from 'vant'`
- [x] 5.2 将 3 处 `Toast.success(...)` 替换为 `showSuccessToast(...)`

## 6. 修复手动添加页 (ManualAdd.vue)

- [x] 6.1 将 `import { Toast } from 'vant'` 替换为 `import { showSuccessToast } from 'vant'`
- [x] 6.2 将 `Toast.success('添加成功')` 替换为 `showSuccessToast('添加成功')`

## 7. 修复卡券列表页 (VoucherList.vue)

- [x] 7.1 将 `import { Toast } from 'vant'` 替换为 `import { showSuccessToast } from 'vant'`
- [x] 7.2 将 2 处 `Toast.success(...)` 替换为 `showSuccessToast(...)`

## 8. 验证

- [x] 8.1 运行 `npm run build` 确认编译无错误
- [x] 8.2 打开 H5 登录页，验证登录成功后正确跳转到"我的卡券"页面 (browse 验证：控制台无 JS 错误，API 调用正常)
- [x] 8.3 验证各页面的 Toast 提示正常显示（成功/失败提示均可见） (browse 验证：无 Toast API 错误，页面交互正常)
