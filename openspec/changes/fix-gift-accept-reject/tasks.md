## 1. 前端 Dialog 注册

- [x] 1.1 `main.js` 中 import `Dialog` from `vant` 并 `app.use(Dialog)`

## 2. 后端 Long→String 全局序列化

- [x] 2.1 尝试 `spring.jackson.serialization.write-numbers-as-strings` → Spring Boot 3.2.5 不支持此属性，改用显式 JacksonConfig
- [x] 2.2 修复 `JacksonConfig`：新增 `Long.TYPE` 序列化器（覆盖原始 long）
- [x] 2.3 JacksonConfig 保留并增强（移除泛型 `JsonWriteFeature` 引用，Jackson 2.15 类路径不兼容）

## 3. 验证

- [x] 3.1 后端全量测试 90/90 PASS, BUILD SUCCESS
- [x] 3.2 H5 编译通过，页面加载无控制台错误
- [x] 3.3 Dialog 组件已全局注册，`Dialog.confirm()` 可正常弹出
