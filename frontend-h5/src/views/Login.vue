<template>
  <div class="login-page">
    <div class="login-header">
      <h1 class="login-title">因私卡券</h1>
      <p class="login-subtitle">员工登录</p>
    </div>

    <van-form ref="formRef" class="login-form" @submit="preventDefault">
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

      <div class="login-submit">
        <van-button
          round
          block
          type="primary"
          :loading="submitting"
          loading-text="登录中..."
          class="login-button"
          @click="handleLogin"
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
import { showSuccessToast, showFailToast } from 'vant';
import { login } from '../api/voucher';
import { setToken } from '../api/request';

const router = useRouter();
const formRef = ref(null);

const form = reactive({
  employeeId: '',
  password: '',
});
const submitting = ref(false);

function preventDefault(e) {
  e.preventDefault();
}

async function handleLogin() {
  if (!formRef.value) return;

  try {
    await formRef.value.validate();
  } catch {
    return;
  }

  submitting.value = true;
  try {
    const res = await login(form.employeeId, form.password);
    const token = res?.data?.token || res?.token;
    if (token) {
      setToken(token);
      showSuccessToast('登录成功');
      await router.replace({ name: 'VoucherList' });
    } else {
      showFailToast('登录失败，未获取到凭证');
    }
  } catch (err) {
    if (!err.response) {
      showFailToast('网络异常，请稍后重试');
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

.login-submit {
  padding: var(--spacing-md) 0;
}

.login-button {
  height: 48px !important;
  font-size: var(--font-size-card-title) !important;
  border-radius: var(--radius-sm) !important;
}
</style>
