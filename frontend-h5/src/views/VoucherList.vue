<template>
  <div class="voucher-list-page">
    <van-nav-bar title="我的卡券" fixed placeholder>
      <template #right>
        <van-icon name="envelop-o" size="20" @click="goToInbox" />
      </template>
    </van-nav-bar>

    <!-- Search and filter bar -->
    <div class="search-bar">
      <van-search
        v-model="keyword"
        placeholder="搜索卡券"
        shape="round"
        @search="onSearch"
      />
      <div class="filter-row">
        <van-dropdown-menu>
          <van-dropdown-item v-model="statusFilter" :options="statusOptions" @change="onFilterChange" />
          <van-dropdown-item v-model="typeFilter" :options="typeOptions" @change="onFilterChange" />
        </van-dropdown-menu>
      </div>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <template v-if="loading && vouchers.length === 0">
        <div class="skeleton-list">
          <van-skeleton title round row="4" v-for="n in 4" :key="n" class="skeleton-item" />
        </div>
      </template>

      <template v-else-if="error && vouchers.length === 0">
        <div class="error-state">
          <van-icon name="warn-o" size="48" color="#c8c9cc" />
          <p class="error-text">加载失败</p>
          <van-button type="primary" size="small" @click="fetchVouchers" class="retry-btn">重新加载</van-button>
        </div>
      </template>

      <template v-else-if="!loading && vouchers.length === 0">
        <div class="empty-state">
          <van-icon name="coupon-o" size="64" color="#c8c9cc" />
          <p class="empty-title">还没有卡券</p>
          <p class="empty-desc">如有需要请联系管理员申请</p>
        </div>
      </template>

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
              :class="{ 'is-coupon': item.voucherType === 'COUPON', 'is-pinned': item.isPinned }"
              @click="goToDetail(item.id)"
            >
              <div class="voucher-card-header">
                <div class="voucher-card-left">
                  <div v-if="item.voucherType === 'COUPON'" class="coupon-value">
                    <span class="coupon-symbol">¥</span>
                    <span class="coupon-amount">{{ item.faceValue }}</span>
                  </div>
                  <span class="voucher-card-title">
                    <van-icon v-if="item.isPinned" name="star" size="14" color="#fa8c16" class="pin-icon" />
                    {{ item.remark || '卡券' }}
                  </span>
                </div>
                <van-tag :class="statusTagClass(item.status)" size="small">
                  {{ statusLabel(item.status) }}
                </van-tag>
              </div>
              <div class="voucher-card-meta">
                <span class="meta-label">有效期至</span>
                <span class="meta-value">{{ formatDate(item.expireAt) }}</span>
              </div>
              <div class="voucher-card-footer">
                <van-icon
                  :name="item.isFavorite ? 'like' : 'like-o'"
                  :color="item.isFavorite ? '#ee0a24' : '#c8c9cc'"
                  size="18"
                  @click.stop="onToggleFavorite(item)"
                />
                <van-icon
                  :name="item.isPinned ? 'star' : 'star-o'"
                  :color="item.isPinned ? '#fa8c16' : '#c8c9cc'"
                  size="18"
                  @click.stop="onTogglePin(item)"
                />
              </div>
            </div>
          </div>
        </van-list>
      </template>
    </van-pull-refresh>

    <div class="float-actions">
      <van-button icon="add-o" type="primary" round class="float-btn" @click="goToManualAdd" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import { getMyVouchers, toggleFavorite, togglePin } from '../api/voucher';

const router = useRouter();

const vouchers = ref([]);
const loading = ref(false);
const error = ref(false);
const refreshing = ref(false);
const listLoading = ref(false);
const listFinished = ref(false);

const keyword = ref('');
const statusFilter = ref('');
const typeFilter = ref('');

const queryParams = ref({ page: 1, size: 20 });

const statusOptions = [
  { text: '全部状态', value: '' },
  { text: '有效', value: 'ISSUED' },
  { text: '已使用', value: 'USED' },
  { text: '已过期', value: 'EXPIRED' },
  { text: '已作废', value: 'CANCELLED' },
];

const typeOptions = [
  { text: '全部类型', value: '' },
  { text: '资源使用', value: 'RESOURCE_USAGE' },
  { text: '优惠券', value: 'COUPON' },
];

const STATUS_MAP = {
  ISSUED: { label: '有效', class: 'tag-valid' },
  USED: { label: '已使用', class: 'tag-used' },
  EXPIRED: { label: '已过期', class: 'tag-expired' },
  CANCELLED: { label: '已作废', class: 'tag-revoked' },
};

function statusLabel(s) { return STATUS_MAP[s]?.label || s; }
function statusTagClass(s) { return STATUS_MAP[s]?.class || ''; }

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
    const params = {
      page: queryParams.value.page,
      size: queryParams.value.size,
    };
    if (keyword.value) params.keyword = keyword.value;
    if (statusFilter.value) params.status = statusFilter.value;
    if (typeFilter.value) params.voucherType = typeFilter.value;

    const res = await getMyVouchers(params);
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
  } finally {
    loading.value = false;
    listLoading.value = false;
    refreshing.value = false;
  }
}

function onSearch() { fetchVouchers(true); }
function onFilterChange() { fetchVouchers(true); }
function onRefresh() { fetchVouchers(true); }

function onLoadMore() {
  queryParams.value.page += 1;
  listLoading.value = true;
  fetchVouchers(false);
}

async function onToggleFavorite(item) {
  try {
    await toggleFavorite(item.id);
    item.isFavorite = item.isFavorite ? 0 : 1;
    showSuccessToast(item.isFavorite ? '已收藏' : '已取消收藏');
  } catch { /* handled */ }
}

async function onTogglePin(item) {
  try {
    await togglePin(item.id);
    item.isPinned = item.isPinned ? 0 : 1;
    showSuccessToast(item.isPinned ? '已置顶' : '已取消置顶');
  } catch { /* handled */ }
}

function goToDetail(id) { router.push({ name: 'VoucherDetail', params: { id } }); }
function goToInbox() { router.push({ name: 'GiftInbox' }); }
function goToManualAdd() { router.push({ name: 'ManualAdd' }); }

onMounted(() => { fetchVouchers(true); });
</script>

<style scoped>
.voucher-list-page { min-height: 100vh; background: var(--color-bg); }

.search-bar {
  background: var(--color-card);
  padding-bottom: var(--spacing-xs);
  border-bottom: 1px solid var(--color-border-light);
}

.filter-row { padding: 0 var(--spacing-md); }

.skeleton-list { padding: var(--spacing-md); }
.skeleton-item {
  margin-bottom: var(--spacing-sm);
  padding: var(--spacing-md);
  background: var(--color-card);
  border-radius: var(--radius-md);
}

.error-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px var(--spacing-md);
}

.empty-title { margin-top: var(--spacing-md); font-size: var(--font-size-card-title); color: var(--color-text-primary); }
.empty-desc { margin-top: var(--spacing-xs); font-size: var(--font-size-small); color: var(--color-text-secondary); }
.error-text { margin-top: var(--spacing-md); font-size: var(--font-size-body); color: var(--color-text-secondary); }
.retry-btn { margin-top: var(--spacing-md); min-width: 120px; }

.voucher-cards { padding: var(--spacing-sm) var(--spacing-md); }

.voucher-card {
  margin-bottom: var(--spacing-sm);
  cursor: pointer;
  transition: box-shadow 0.2s ease;
}

.voucher-card:active { box-shadow: var(--shadow-md); }

.voucher-card.is-pinned {
  border-color: #ffd666;
}

.pin-icon {
  margin-right: 4px;
  vertical-align: middle;
}

.voucher-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: var(--spacing-sm);
}

.voucher-card-left { flex: 1; min-width: 0; }

.coupon-value { display: flex; align-items: baseline; margin-bottom: 4px; }
.coupon-symbol { font-size: 14px; font-weight: 700; color: #ee0a24; margin-right: 2px; }
.coupon-amount { font-size: 24px; font-weight: 700; color: #ee0a24; font-family: 'JetBrains Mono', 'SF Mono', monospace; line-height: 1; }

.voucher-card.is-coupon { background: #fff7e6; border-color: #ffd666; }

.voucher-card-title {
  font-size: var(--font-size-card-title);
  font-weight: 600;
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.voucher-card-meta {
  display: flex;
  align-items: center;
  font-size: var(--font-size-small);
  margin-bottom: var(--spacing-xs);
}

.meta-label { color: var(--color-text-secondary); margin-right: var(--spacing-xs); }
.meta-value { color: var(--color-text-primary); }

.voucher-card-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-md);
  padding-top: var(--spacing-xs);
}

.float-actions {
  position: fixed;
  right: 16px;
  bottom: 80px;
  z-index: 10;
}

.float-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  box-shadow: var(--shadow-lg);
}
</style>
