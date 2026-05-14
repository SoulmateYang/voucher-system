<template>
  <div class="login-page">
    <div class="login-header">
      <h1 class="login-title">因私卡券</h1>
      <p class="login-subtitle">员工登录</p>
    </div>

    <van-form @submit="handleLogin" class="login-form">
      <van-field
        v-model="form.employeeId"
        name="employeeId"
        label="工号"
        placeholder="请输入工号"
        :rules="[{ required: true, message: '请输入工号' }]"
        maxlength="20"
        clearable
      />

      <van-field
        v-model="form.password"
        type="password"
        name="password"
        label="密码"
        placeholder="请输入密码"
        :rules="[{ required: true, message: '请输入密码' }]"
        maxlength="64"
      />

      <div class="login-options">
        <van-checkbox v-model="rememberLogin" shape="square" icon-size="16px">
          <span class="remember-text">记住登录</span>
        </van-checkbox>
      </div>

      <div class="login-submit">
        <van-button
          round
          block
          type="primary"
          native-type="submit"
          :loading="submitting"
          loading-text="登录中..."
          class="login-button"
        >
          登 录
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Toast } from 'vant';
import { login } from '../api/voucher';
import { setToken } from '../api/request';

const router = useRouter();

const form = reactive({
  employeeId: '',
  password: '',
});
const rememberLogin = ref(false);
const submitting = ref(false);

async function handleLogin() {
  submitting.value = true;
  try {
    const res = await login(form.employeeId, form.password);
    const token = res?.data?.token || res?.token;
    if (token) {
      setToken(token);
      Toast.success('登录成功');
      router.replace({ name: 'VoucherList' });
    } else {
      Toast.fail('登录失败，未获取到凭证');
    }
  } catch (err) {
    // Toast is already shown by the response interceptor in request.js.
    // Only show a fallback if no response was received.
    if (!err.response) {
      Toast.fail('网络异常，请稍后重试');
    }
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  padding: var(--spacing-md);
  background: var(--color-bg);
}

.login-header {
  margin-top: 60px;
  margin-bottom: 40px;
  text-align: center;
}

.login-title {
  font-size: var(--font-size-section-title);
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-xs);
}

.login-subtitle {
  font-size: var(--font-size-body);
  color: var(--color-text-secondary);
}

.login-form {
  width: 100%;
}

.login-options {
  display: flex;
  align-items: center;
  padding: var(--spacing-sm) var(--spacing-md);
}

.remember-text {
  font-size: var(--font-size-body);
  color: var(--color-text-secondary);
}

.login-submit {
  padding: var(--spacing-md) 0;
}

.login-button {
  height: 48px !important;
  font-size: var(--font-size-card-title) !important;
  border-radius: var(--radius-sm) !important;
}
</style>
