<template>
  <div class="voucher-list-page">
    <!-- Navbar -->
    <van-nav-bar title="我的卡券" fixed placeholder />

    <!-- Pull-to-refresh content -->
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <!-- Loading skeleton -->
      <template v-if="loading && vouchers.length === 0">
        <div class="skeleton-list">
          <van-skeleton title round row="4" v-for="n in 4" :key="n" class="skeleton-item" />
        </div>
      </template>

      <!-- Error state -->
      <template v-else-if="error && vouchers.length === 0">
        <div class="error-state">
          <van-icon name="warn-o" size="48" color="#c8c9cc" />
          <p class="error-text">加载失败</p>
          <van-button type="primary" size="small" @click="fetchVouchers" class="retry-btn">
            重新加载
          </van-button>
        </div>
      </template>

      <!-- Empty state -->
      <template v-else-if="!loading && vouchers.length === 0">
        <div class="empty-state">
          <van-icon name="coupon-o" size="64" color="#c8c9cc" />
          <p class="empty-title">还没有卡券</p>
          <p class="empty-desc">如有需要请联系管理员申请</p>
        </div>
      </template>

      <!-- Voucher list -->
      <template v-else>
        <van-list
          v-model:loading="listLoading"
          :finished="listFinished"
          finished-text="没有更多了"
          @load="onLoadMore"
        >
          <div class="voucher-cards">
            <div
              v-for="item in vouchers"
              :key="item.id"
              class="voucher-card card"
              @click="goToDetail(item.id)"
            >
              <div class="voucher-card-header">
                <span class="voucher-card-title">{{ item.remark || '卡券' }}</span>
                <van-tag
                  :class="statusTagClass(item.status)"
                  size="small"
                >
                  {{ statusLabel(item.status) }}
                </van-tag>
              </div>
              <div class="voucher-card-meta">
                <span class="meta-label">有效期至</span>
                <span class="meta-value">{{ formatDate(item.expireAt) }}</span>
              </div>
            </div>
          </div>
        </van-list>
      </template>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Toast } from 'vant';
import { getMyVouchers } from '../api/voucher';

const router = useRouter();

const vouchers = ref([]);
const loading = ref(false);
const error = ref(false);
const refreshing = ref(false);
const listLoading = ref(false);
const listFinished = ref(false);

const queryParams = ref({
  page: 1,
  size: 20,
});

const STATUS_MAP = {
  ISSUED: { label: '有效', class: 'tag-valid' },
  USED: { label: '已使用', class: 'tag-used' },
  EXPIRED: { label: '已过期', class: 'tag-expired' },
  CANCELLED: { label: '已作废', class: 'tag-revoked' },
};

function statusLabel(status) {
  return STATUS_MAP[status]?.label || status;
}

function statusTagClass(status) {
  return STATUS_MAP[status]?.class || '';
}

function formatDate(dateStr) {
  if (!dateStr) return '--';
  const date = new Date(dateStr);
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

async function fetchVouchers(reset = false) {
  if (reset) {
    queryParams.value.page = 1;
    listFinished.value = false;
    vouchers.value = [];
  }

  loading.value = true;
  error.value = false;

  try {
    const res = await getMyVouchers({
      page: queryParams.value.page,
      size: queryParams.value.size,
    });
    const records = res?.data?.records || res?.data || [];
    const total = res?.data?.total || records.length;

    if (reset) {
      vouchers.value = records;
    } else {
      vouchers.value = [...vouchers.value, ...records];
    }

    if (vouchers.value.length >= total) {
      listFinished.value = true;
    }
  } catch (err) {
    error.value = true;
    if (!err.response) {
      Toast.fail('网络异常，请稍后重试');
    }
  } finally {
    loading.value = false;
    listLoading.value = false;
    refreshing.value = false;
  }
}

function onRefresh() {
  fetchVouchers(true);
}

function onLoadMore() {
  queryParams.value.page += 1;
  listLoading.value = true;
  fetchVouchers(false);
}

function goToDetail(id) {
  router.push({ name: 'VoucherDetail', params: { id } });
}

onMounted(() => {
  fetchVouchers(true);
});
</script>

<style scoped>
.voucher-list-page {
  min-height: 100vh;
  background: var(--color-bg);
}

.skeleton-list {
  padding: var(--spacing-md);
}

.skeleton-item {
  margin-bottom: var(--spacing-sm);
  padding: var(--spacing-md);
  background: var(--color-card);
  border-radius: var(--radius-md);
}

.error-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px var(--spacing-md);
}

.empty-title {
  margin-top: var(--spacing-md);
  font-size: var(--font-size-card-title);
  color: var(--color-text-primary);
}

.empty-desc {
  margin-top: var(--spacing-xs);
  font-size: var(--font-size-small);
  color: var(--color-text-secondary);
}

.error-text {
  margin-top: var(--spacing-md);
  font-size: var(--font-size-body);
  color: var(--color-text-secondary);
}

.retry-btn {
  margin-top: var(--spacing-md);
  min-width: 120px;
}

.voucher-cards {
  padding: var(--spacing-sm) var(--spacing-md);
}

.voucher-card {
  margin-bottom: var(--spacing-sm);
  cursor: pointer;
  transition: box-shadow 0.2s ease;
}

.voucher-card:active {
  box-shadow: var(--shadow-md);
}

.voucher-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-sm);
}

.voucher-card-title {
  font-size: var(--font-size-card-title);
  font-weight: 600;
  color: var(--color-text-primary);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: var(--spacing-xs);
}

.voucher-card-meta {
  display: flex;
  align-items: center;
  font-size: var(--font-size-small);
}

.meta-label {
  color: var(--color-text-secondary);
  margin-right: var(--spacing-xs);
}

.meta-value {
  color: var(--color-text-primary);
}
</style>
