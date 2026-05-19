<template>
  <div class="gift-inbox-page">
    <van-nav-bar
      :title="isOutbox ? '发出的赠送' : '收到的赠送'"
      left-arrow
      @click-left="goBack"
      fixed
      placeholder
    />

    <van-tabs v-model:active="activeTab" @change="onTabChange">
      <van-tab title="收到的" />
      <van-tab title="发出的" />
    </van-tabs>

    <div class="gift-content">
      <template v-if="loading">
        <van-skeleton title round row="3" v-for="n in 3" :key="n" class="skeleton-item" />
      </template>

      <template v-else-if="list.length === 0">
        <div class="empty-state">
          <van-icon name="envelop-o" size="48" color="#c8c9cc" />
          <p class="empty-text">{{ isOutbox ? '暂无发出的赠送' : '暂无收到的赠送' }}</p>
        </div>
      </template>

      <template v-else>
        <div
          v-for="item in list"
          :key="item.giftId"
          class="gift-card card"
        >
          <div class="gift-card-body">
            <div class="gift-voucher-info">
              <div v-if="item.faceValue" class="gift-face-value">¥{{ item.faceValue }}</div>
              <div class="gift-voucher-name">{{ item.remark || '卡券' }}</div>
              <div class="gift-voucher-code mono">{{ item.voucherCode }}</div>
            </div>
            <div class="gift-meta">
              <span v-if="isOutbox">接收人：{{ item.toUserName }}</span>
              <span v-else>赠送人：{{ item.fromUserName }}</span>
              <span class="gift-time">{{ formatDate(item.giftAt) }}</span>
            </div>
            <div v-if="item.message" class="gift-message">留言：{{ item.message }}</div>
            <van-tag :type="statusType(item.status)" size="small">{{ statusLabel(item.status) }}</van-tag>
          </div>
          <div v-if="item.status === 'PENDING' && !isOutbox" class="gift-card-actions">
            <van-button size="small" plain type="danger" @click="onReject(item)">拒绝</van-button>
            <van-button size="small" type="primary" @click="onAccept(item)">领取</van-button>
          </div>
          <div v-if="item.status === 'PENDING' && isOutbox" class="gift-card-actions">
            <van-button size="small" plain type="danger" @click="onCancel(item)">撤销</van-button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast, showConfirmDialog } from 'vant';
import { getGiftInbox, getGiftOutbox, acceptGift, rejectGift, cancelGift } from '../api/voucher';

const router = useRouter();
const activeTab = ref(0);
const list = ref([]);
const loading = ref(false);

const isOutbox = computed(() => activeTab.value === 1);

const STATUS_MAP = {
  PENDING: { label: '待处理', type: 'warning' },
  ACCEPTED: { label: '已领取', type: 'success' },
  REJECTED: { label: '已拒绝', type: 'danger' },
  CANCELLED: { label: '已撤销', type: '' },
  EXPIRED: { label: '已超时', type: '' },
};

function statusLabel(s) { return STATUS_MAP[s]?.label || s; }
function statusType(s) { return STATUS_MAP[s]?.type || ''; }

function formatDate(dateStr) {
  if (!dateStr) return '--';
  const date = new Date(dateStr);
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  const h = String(date.getHours()).padStart(2, '0');
  const min = String(date.getMinutes()).padStart(2, '0');
  return `${y}-${m}-${d} ${h}:${min}`;
}

async function fetchList() {
  loading.value = true;
  try {
    const fn = isOutbox.value ? getGiftOutbox : getGiftInbox;
    const res = await fn();
    list.value = res?.data || [];
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false;
  }
}

async function onAccept(item) {
  try {
    await showConfirmDialog({ title: '确认领取', message: '领取后卡券将归入你的卡券库' });
  } catch { return; }
  try {
    await acceptGift(item.giftId);
    showSuccessToast('领取成功');
    fetchList();
  } catch { /* handled */ }
}

async function onReject(item) {
  try {
    await showConfirmDialog({ title: '确认拒绝', message: '拒绝后卡券将退回赠送方' });
  } catch { return; }
  try {
    await rejectGift(item.giftId);
    showSuccessToast('已拒绝');
    fetchList();
  } catch { /* handled */ }
}

async function onCancel(item) {
  try {
    await showConfirmDialog({ title: '确认撤销', message: '撤销后卡券将回到你的卡券库' });
  } catch { return; }
  try {
    await cancelGift(item.giftId);
    showSuccessToast('已撤销');
    fetchList();
  } catch { /* handled */ }
}

function onTabChange() {
  fetchList();
}

function goBack() {
  router.back();
}

onMounted(() => {
  const fromRoute = router.currentRoute.value;
  if (fromRoute.name === 'GiftOutbox') {
    activeTab.value = 1;
  }
  fetchList();
});
</script>

<style scoped>
.gift-inbox-page {
  min-height: 100vh;
  background: var(--color-bg);
}

.gift-content {
  padding: var(--spacing-md);
}

.skeleton-item {
  margin-bottom: var(--spacing-sm);
  padding: var(--spacing-md);
  background: var(--color-card);
  border-radius: var(--radius-md);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px var(--spacing-md);
}

.empty-text {
  margin-top: var(--spacing-md);
  font-size: var(--font-size-body);
  color: var(--color-text-secondary);
}

.gift-card {
  margin-bottom: var(--spacing-sm);
}

.gift-card-body {
  position: relative;
}

.gift-voucher-info {
  margin-bottom: var(--spacing-xs);
}

.gift-face-value {
  font-size: 20px;
  font-weight: 700;
  color: #ee0a24;
  font-family: 'JetBrains Mono', 'SF Mono', monospace;
}

.gift-voucher-name {
  font-size: var(--font-size-card-title);
  font-weight: 600;
  color: var(--color-text-primary);
}

.gift-voucher-code {
  font-size: var(--font-size-small);
  color: var(--color-text-secondary);
  word-break: break-all;
}

.gift-meta {
  font-size: var(--font-size-small);
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-2xs);
  display: flex;
  justify-content: space-between;
}

.gift-time {
  color: var(--color-text-placeholder);
}

.gift-message {
  font-size: var(--font-size-small);
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-xs);
}

.gift-card-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-sm);
  padding-top: var(--spacing-sm);
  border-top: 1px solid var(--color-border-light);
}
</style>
