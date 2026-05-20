<template>
  <div class="usage-records-page">
    <van-nav-bar
      title="使用记录"
      left-text="返回"
      left-arrow
      fixed
      placeholder
      @click-left="$router.back()"
    />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <template v-if="loading">
        <div class="skeleton-list">
          <van-skeleton title row="3" v-for="n in 4" :key="n" class="skeleton-item" />
        </div>
      </template>

      <template v-else-if="error">
        <div class="error-state">
          <van-icon name="warn-o" size="48" color="#c8c9cc" />
          <p class="error-text">加载失败</p>
          <van-button type="primary" size="small" @click="fetchRecords">重新加载</van-button>
        </div>
      </template>

      <template v-else-if="records.length === 0">
        <div class="empty-state">
          <van-icon name="notes-o" size="64" color="#c8c9cc" />
          <p class="empty-title">暂无使用记录</p>
        </div>
      </template>

      <template v-else>
        <div class="record-list">
          <div
            v-for="item in records"
            :key="item.id"
            class="record-card card"
          >
            <div class="record-header">
              <span class="record-code voucher-code">{{ item.voucherCode }}</span>
              <van-tag :type="item.type === 'CONSUMPTION' ? 'warning' : 'primary'" size="small">
                {{ item.type === 'CONSUMPTION' ? '消费' : '核销' }}
              </van-tag>
            </div>
            <div class="record-body">
              <div v-if="item.amount != null" class="record-row">
                <span class="record-label">金额</span>
                <span class="record-value amount">¥{{ item.amount }}</span>
              </div>
              <div class="record-row">
                <span class="record-label">操作人</span>
                <span class="record-value">{{ item.operatorName || '--' }}</span>
              </div>
              <div v-if="item.remark" class="record-row">
                <span class="record-label">备注</span>
                <span class="record-value">{{ item.remark }}</span>
              </div>
            </div>
            <div class="record-footer">
              <span class="record-time">{{ formatTime(item.createdAt) }}</span>
            </div>
          </div>
        </div>
      </template>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getUsageRecords } from '../api/voucher';

const records = ref([]);
const loading = ref(false);
const error = ref(false);
const refreshing = ref(false);

function formatTime(dateStr) {
  if (!dateStr) return '--';
  const date = new Date(dateStr);
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  const h = String(date.getHours()).padStart(2, '0');
  const min = String(date.getMinutes()).padStart(2, '0');
  return `${y}-${m}-${d} ${h}:${min}`;
}

async function fetchRecords() {
  loading.value = true;
  error.value = false;
  try {
    const res = await getUsageRecords();
    records.value = res?.data || [];
  } catch {
    error.value = true;
  } finally {
    loading.value = false;
    refreshing.value = false;
  }
}

function onRefresh() {
  fetchRecords();
}

onMounted(() => {
  fetchRecords();
});
</script>

<style scoped>
.usage-records-page {
  min-height: 100vh;
  background: var(--color-bg);
  padding-bottom: 60px;
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

.error-state, .empty-state {
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

.error-text {
  margin-top: var(--spacing-md);
  font-size: var(--font-size-body);
  color: var(--color-text-secondary);
}

.record-list {
  padding: var(--spacing-sm) var(--spacing-md);
}

.record-card {
  margin-bottom: var(--spacing-sm);
}

.record-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-sm);
}

.record-code {
  font-size: var(--font-size-body);
}

.record-body {
  margin-bottom: var(--spacing-xs);
}

.record-row {
  display: flex;
  align-items: center;
  margin-bottom: var(--spacing-2xs);
  font-size: var(--font-size-small);
}

.record-label {
  color: var(--color-text-secondary);
  width: 48px;
  flex-shrink: 0;
}

.record-value {
  color: var(--color-text-primary);
}

.record-value.amount {
  font-family: var(--font-family-mono);
  color: var(--color-primary);
  font-weight: 500;
}

.record-footer {
  text-align: right;
}

.record-time {
  font-size: var(--font-size-small);
  color: var(--color-text-placeholder);
}
</style>
