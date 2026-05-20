<template>
  <div class="profile-page">
    <van-nav-bar title="我的" fixed placeholder />

    <!-- Loading skeleton -->
    <template v-if="loading">
      <div class="profile-skeleton">
        <van-skeleton row="5" />
      </div>
    </template>

    <!-- Error state -->
    <template v-else-if="error">
      <div class="error-state">
        <van-icon name="warn-o" size="48" color="#c8c9cc" />
        <p class="error-text">加载失败</p>
        <van-button type="primary" size="small" @click="fetchProfile">重新加载</van-button>
      </div>
    </template>

    <!-- Profile content -->
    <template v-else>
      <div class="profile-header">
        <div class="avatar-placeholder">
          <van-icon name="manager-o" size="32" color="#fff" />
        </div>
        <div class="profile-name">{{ user.realName || '--' }}</div>
      </div>

      <van-cell-group title="基本信息">
        <van-cell title="工号" :value="user.employeeNo || user.username || '--'" />
        <van-cell title="姓名" :value="user.realName || '--'" />
        <van-cell title="部门" :value="user.department || '未设置'" />
        <van-cell title="手机号" :value="user.mobile || '未设置'" />
      </van-cell-group>

      <van-cell-group title="功能">
        <van-cell title="编辑资料" icon="edit" is-link to="/edit-profile" />
        <van-cell title="修改密码" icon="lock" is-link to="/change-password" />
        <van-cell title="使用记录" icon="notes-o" is-link to="/usage-records" />
        <van-cell title="我的收件箱" icon="envelop-o" is-link to="/gift-inbox" />
        <van-cell title="我的发件箱" icon="envelop-o" is-link to="/gift-outbox" />
      </van-cell-group>

      <div class="logout-section">
        <van-button
          round
          block
          type="default"
          class="logout-button"
          @click="handleLogout"
        >
          退出登录
        </van-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showDialog, showSuccessToast } from 'vant';
import { getProfile } from '../api/voucher';
import { removeToken } from '../api/request';

const router = useRouter();

const loading = ref(false);
const error = ref(false);
const user = reactive({
  username: '',
  employeeNo: '',
  realName: '',
  department: '',
  mobile: '',
});

async function fetchProfile() {
  loading.value = true;
  error.value = false;
  try {
    const res = await getProfile();
    const data = res?.data || {};
    Object.assign(user, data);
  } catch {
    error.value = true;
  } finally {
    loading.value = false;
  }
}

function handleLogout() {
  showDialog({
    title: '退出登录',
    message: '确定要退出登录吗？',
    showCancelButton: true,
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  }).then(() => {
    removeToken();
    showSuccessToast('已退出登录');
    router.replace({ name: 'Login' });
  }).catch(() => {});
}

onMounted(() => {
  fetchProfile();
});
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: var(--color-bg);
  padding-bottom: 80px;
}

.profile-skeleton {
  padding: var(--spacing-md);
}

.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px var(--spacing-md);
}

.error-text {
  margin-top: var(--spacing-md);
  font-size: var(--font-size-body);
  color: var(--color-text-secondary);
}

.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-lg) var(--spacing-md);
  background: var(--color-card);
  margin-bottom: var(--spacing-sm);
}

.avatar-placeholder {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--spacing-sm);
}

.profile-name {
  font-size: var(--font-size-heading);
  font-weight: 600;
  color: var(--color-text-primary);
}

.logout-section {
  padding: var(--spacing-lg) var(--spacing-md);
}

.logout-button {
  height: 48px;
  font-size: var(--font-size-card-title);
  color: var(--color-text-secondary);
}
</style>
