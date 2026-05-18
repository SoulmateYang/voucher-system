<template>
  <div class="gift-records-page">
    <van-nav-bar
      title="转赠记录"
      left-arrow
      @click-left="goBack"
      fixed
      placeholder
    />

    <template v-if="loading">
      <div class="skeleton-list">
        <van-skeleton title round row="3" v-for="i in 2" :key="i" />
      </div>
    </template>

    <template v-else-if="error">
      <div class="error-state">
        <van-icon name="warn-o" size="48" color="#c8c9cc" />
        <p class="error-text">加载失败</p>
        <van-button type="primary" size="small" @click="fetchRecords" class="retry-btn">
          重新加载
        </van-button>
      </div>
    </template>

    <template v-else-if="records.length === 0">
      <div class="empty-state">
        <van-empty description="暂无转赠记录" />
      </div>
    </template>

    <template v-else>
      <div class="records-list">
        <div
          v-for="record in records"
          :key="record.giftId"
          class="record-card card"
        >
          <!-- Status badge and time -->
          <div class="record-header">
            <span class="record-time">{{ formatDateTime(record.giftAt) }}</span>
            <van-tag
              :type="statusType(record.status)"
              size="small"
              class="record-status"
            >
              {{ statusLabel(record.status) }}
            </van-tag>
          </div>

          <!-- Transfer flow -->
          <div class="transfer-flow">
            <div class="transfer-person">
              <span class="transfer-label">转出</span>
              <span class="transfer-name">{{ record.fromUserName }}</span>
              <span class="transfer-id">{{ record.fromUserId }}</span>
            </div>
            <div class="transfer-arrow">
              <van-icon name="arrow" />
            </div>
            <div class="transfer-person">
              <span class="transfer-label">接收</span>
              <span class="transfer-name">{{ record.toUserName }}</span>
              <span class="transfer-id">{{ record.toUserId }}</span>
            </div>
          </div>

          <!-- Message -->
          <div v-if="record.message" class="record-message">
            <span class="message-text">"{{ record.message }}"</span>
          </div>

          <!-- Voucher info -->
          <div class="record-voucher">
            <span class="voucher-code mono">{{ record.voucherCode }}</span>
          </div>
        </div>
      </div>
    </template>

    <!-- Bottom tab bar -->
    <van-tabbar fixed route>
      <van-tabbar-item icon="coupon-o" :to="{ name: 'VoucherList' }">
        我的卡券
      </van-tabbar-item>
      <van-tabbar-item icon="records" :to="{ name: 'GiftInbox' }">
        转赠消息
      </van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getGiftRecords } from '../api/voucher'

const route = useRoute()
const router = useRouter()

const records = ref([])
const loading = ref(false)
const error = ref(false)

const STATUS_MAP = {
  PENDING: { label: '待处理', type: 'warning' },
  ACCEPTED: { label: '已领取', type: 'success' },
  REJECTED: { label: '已拒绝', type: 'danger' },
  CANCELLED: { label: '已撤销', type: '' },
  EXPIRED: { label: '已超时', type: '' },
}

function statusLabel(status) {
  return STATUS_MAP[status]?.label || status
}

function statusType(status) {
  return STATUS_MAP[status]?.type || ''
}

function formatDateTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}`
}

async function fetchRecords() {
  const id = route.params.id
  if (!id) return

  loading.value = true
  error.value = false
  try {
    const res = await getGiftRecords(id)
    records.value = res.data || []
  } catch {
    error.value = true
    records.value = []
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.back()
}

onMounted(() => {
  fetchRecords()
})
</script>

<style scoped>
.gift-records-page {
  min-height: 100vh;
  padding-bottom: 60px;
  background: var(--color-bg);
}

.skeleton-list {
  padding: var(--spacing-md);
}
.skeleton-list > * {
  margin-bottom: var(--spacing-sm);
}

.error-state, .empty-state {
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
.retry-btn {
  margin-top: var(--spacing-md);
}

.records-list {
  padding: var(--spacing-sm) var(--spacing-md);
}

.record-card {
  margin-bottom: var(--spacing-sm);
  padding: var(--spacing-md);
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-sm);
}
.record-time {
  font-size: var(--font-size-small);
  color: var(--color-text-secondary);
}

.transfer-flow {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) 0;
}
.transfer-person {
  flex: 1;
  text-align: center;
}
.transfer-label {
  display: block;
  font-size: 11px;
  color: var(--color-text-secondary);
  margin-bottom: 2px;
}
.transfer-name {
  display: block;
  font-size: var(--font-size-body);
  font-weight: 500;
  color: var(--color-text-primary);
}
.transfer-id {
  display: block;
  font-size: 11px;
  color: var(--color-text-secondary);
}
.transfer-arrow {
  color: var(--color-text-secondary);
}

.record-message {
  text-align: center;
  padding: var(--spacing-xs) 0;
}
.message-text {
  font-size: 13px;
  color: var(--color-text-secondary);
  font-style: italic;
}

.record-voucher {
  text-align: center;
  margin-top: var(--spacing-sm);
  padding-top: var(--spacing-sm);
  border-top: 1px solid var(--color-border-light);
}
.voucher-code {
  font-size: var(--font-size-small);
  color: var(--color-text-secondary);
  letter-spacing: 1px;
}
</style>
