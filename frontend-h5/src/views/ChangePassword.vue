<template>
  <div class="change-password-page">
    <van-nav-bar
      title="修改密码"
      left-text="返回"
      left-arrow
      fixed
      placeholder
      @click-left="$router.back()"
    />

    <van-form ref="formRef" class="password-form" @submit="handleSubmit">
      <van-field
        v-model="form.oldPassword"
        type="password"
        name="oldPassword"
        label="原密码"
        placeholder="请输入原密码"
        :rules="[{ required: true, message: '请输入原密码' }]"
        maxlength="64"
      />
      <van-field
        v-model="form.newPassword"
        type="password"
        name="newPassword"
        label="新密码"
        placeholder="请输入新密码"
        :rules="[
          { required: true, message: '请输入新密码' },
          { validator: validatePassword, message: '密码长度不能少于6位' },
        ]"
        maxlength="64"
      />
      <van-field
        v-model="form.confirmPassword"
        type="password"
        name="confirmPassword"
        label="确认密码"
        placeholder="请再次输入新密码"
        :rules="[
          { required: true, message: '请再次输入新密码' },
          { validator: validateConfirm, message: '两次输入的密码不一致' },
        ]"
        maxlength="64"
      />

      <div class="submit-section">
        <van-button
          round
          block
          type="primary"
          :loading="submitting"
          loading-text="修改中..."
          native-type="submit"
        >
          确认修改
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import { changePassword } from '../api/voucher';

const router = useRouter();
const formRef = ref(null);

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const submitting = ref(false);

function validatePassword(val) {
  return val.length >= 6;
}

function validateConfirm(val) {
  return val === form.newPassword;
}

async function handleSubmit() {
  if (!formRef.value) return;

  try {
    await formRef.value.validate();
  } catch {
    return;
  }

  submitting.value = true;
  try {
    await changePassword({
      oldPassword: form.oldPassword,
      newPassword: form.newPassword,
    });
    showSuccessToast('密码修改成功');
    router.back();
  } catch {
    // Error handled by request interceptor
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.change-password-page {
  min-height: 100vh;
  background: var(--color-bg);
  padding-bottom: 60px;
}

.password-form {
  margin-top: var(--spacing-sm);
}

.submit-section {
  padding: var(--spacing-lg) var(--spacing-md);
}
</style>
