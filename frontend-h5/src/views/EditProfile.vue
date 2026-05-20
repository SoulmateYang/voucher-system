<template>
  <div class="edit-profile-page">
    <van-nav-bar
      title="编辑资料"
      left-text="返回"
      left-arrow
      fixed
      placeholder
      @click-left="$router.back()"
    />

    <van-form ref="formRef" class="edit-form" @submit="handleSubmit">
      <van-field
        :model-value="user.username || user.employeeNo || '--'"
        label="工号"
        readonly
        disabled
      />
      <van-field
        :model-value="user.realName || '--'"
        label="姓名"
        readonly
        disabled
      />
      <van-field
        v-model="form.mobile"
        name="mobile"
        label="手机号"
        placeholder="请输入手机号"
        maxlength="11"
      />
      <van-field
        v-model="form.department"
        name="department"
        label="部门"
        placeholder="请输入部门"
        maxlength="50"
      />

      <div class="submit-section">
        <van-button
          round
          block
          type="primary"
          :loading="submitting"
          loading-text="保存中..."
          native-type="submit"
        >
          保存
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import { getProfile, updateProfile } from '../api/voucher';

const router = useRouter();
const formRef = ref(null);

const user = reactive({
  username: '',
  employeeNo: '',
  realName: '',
});

const form = reactive({
  mobile: '',
  department: '',
});

const submitting = ref(false);

async function fetchProfile() {
  try {
    const res = await getProfile();
    const data = res?.data || {};
    Object.assign(user, { username: data.username || data.employeeNo, employeeNo: data.employeeNo, realName: data.realName });
    form.mobile = data.mobile || '';
    form.department = data.department || '';
  } catch {
    // Error handled by request interceptor
  }
}

async function handleSubmit() {
  submitting.value = true;
  try {
    await updateProfile({ mobile: form.mobile, department: form.department });
    showSuccessToast('修改成功');
    router.back();
  } catch {
    // Error handled by request interceptor
  } finally {
    submitting.value = false;
  }
}

onMounted(() => {
  fetchProfile();
});
</script>

<style scoped>
.edit-profile-page {
  min-height: 100vh;
  background: var(--color-bg);
  padding-bottom: 60px;
}

.edit-form {
  margin-top: var(--spacing-sm);
}

.submit-section {
  padding: var(--spacing-lg) var(--spacing-md);
}
</style>
