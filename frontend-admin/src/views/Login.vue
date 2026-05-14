<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <h2 class="login-title">因私卡券管理系统</h2>
        <p class="login-subtitle">企业内部卡券核销管理平台</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @keyup.enter="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            size="large"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <el-alert
        v-if="errorMsg"
        :title="errorMsg"
        type="error"
        show-icon
        :closable="true"
        @close="errorMsg = ''"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '../api/auth'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref(null)
const loading = ref(false)
const errorMsg = ref('')

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  errorMsg.value = ''

  try {
    const res = await login({
      username: form.username,
      password: form.password,
    })
    const { token, user } = res.data
    authStore.setToken(token)
    authStore.setUser(user)
    router.replace('/batch')
  } catch (err) {
    errorMsg.value = err.response?.data?.message || err.message || '登录失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background-color: var(--color-page-bg, #f7f8fa);
}

.login-card {
  width: 400px;
  background: var(--color-card-bg, #ffffff);
  border-radius: var(--radius-md, 8px);
  border: 1px solid var(--color-border, #ebedf0);
  padding: var(--space-xl, 32px) var(--space-lg, 24px);
  box-shadow: var(--shadow-md, 0 2px 8px rgba(0, 0, 0, 0.06));
}

.login-header {
  text-align: center;
  margin-bottom: var(--space-xl, 32px);
}

.login-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text-primary, #323233);
  margin-bottom: var(--space-xs, 8px);
}

.login-subtitle {
  font-size: 14px;
  color: var(--color-text-secondary, #969799);
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
}
</style>
