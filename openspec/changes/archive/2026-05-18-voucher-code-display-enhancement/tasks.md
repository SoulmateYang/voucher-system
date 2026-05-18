## 1. 有效期列

- [x] 1.1 在券码列表 `el-table` 中新增"有效期"列（`el-table-column`），prop 绑定 `expireAt`
- [x] 1.2 添加 `formatDate` 辅助函数，将 `expireAt` 格式化为 `YYYY-MM-DD` 展示

## 2. 二维码列

- [x] 2.1 在 `BatchManage.vue` `<script setup>` 中导入 `QRCode` from `qrcode`
- [x] 2.2 新增 `generateQRDataUrl` 函数，接收 voucherCode 返回 base64 data URL（使用 `QRCode.toDataURL`，120px 尺寸，errorCorrectionLevel: 'M'）
- [x] 2.3 在获取券码列表后（`handleViewDetail`），为每条状态为 `ISSUED` 的券码预生成 QR data URL 存入属性
- [x] 2.4 在券码列表 `el-table` 中新增"二维码"列，ISSUED 状态展示 36×36px QR 缩略图，其他状态展示 `—`

## 3. 二维码 hover 放大预览

- [x] 3.1 在二维码缩略图外包裹 `el-popover`，trigger 为 `hover`，placement 为 `left`
- [x] 3.2 popover 内容展示 200×200px 的 QR 码图片，设置 `:z-index="2100"` 避免被 dialog 遮挡

## 4. 弹窗布局调整

- [x] 4.1 将 `el-dialog` 的 `width` 从 `520px` 调整为 `700px`
- [x] 4.2 调整 `el-table` 的 `max-height` 为 `500` 以适配更高弹窗空间

## 5. 验证

- [ ] 5.1 启动管理端，验证"查看券码"弹窗所有列正确展示（有效期格式、QR 缩略图、非 ISSUED 券码显示 `—`）
- [ ] 5.2 验证 QR 码 hover 放大预览可正常触发和关闭
- [ ] 5.3 使用手机扫码设备验证 popover 中 QR 码可被识别
