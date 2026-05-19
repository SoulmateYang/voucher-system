<template>
  <div class="consumption-history">
    <div class="page-header">
      <el-button link @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2>储值卡消费明细</h2>
    </div>

    <!-- Card info -->
    <div class="card info-card" v-if="voucherDetail">
      <div class="info-row">
        <div class="info-item">
          <span class="info-label">券码</span>
          <span class="info-value mono">{{ voucherDetail.voucherCode }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">持有人</span>
          <span class="info-value">{{ voucherDetail.holderName }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">初始余额</span>
          <span class="info-value mono">¥{{ voucherDetail.initialBalance || 0 }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">剩余余额</span>
          <span class="info-value mono highlight">¥{{ voucherDetail.remainingBalance || 0 }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">状态</span>
          <el-tag :type="statusTag(voucherDetail.status)" size="small">{{ statusLabel(voucherDetail.status) }}</el-tag>
        </div>
      </div>
    </div>

    <!-- Consumption table -->
    <div class="card">
      <el-table :data="tableData" stripe v-loading="loading" empty-text="暂无消费记录" style="width: 100%">
        <el-table-column label="时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="消费金额" width="120" align="right">
          <template #default="{ row }">
            <span class="mono">¥{{ row.consumeAmount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="消费前余额" width="120" align="right">
          <template #default="{ row }">
            <span class="mono">¥{{ row.balanceBefore || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="消费后余额" width="120" align="right">
          <template #default="{ row }">
            <span class="mono">¥{{ row.balanceAfter || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="核销员" width="100" />
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
      </el-table>

      <div v-if="tableData.length === 0 && !loading" class="empty-hint">该储值卡暂无消费记录</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getConsumptionHistory } from '../api/consumption'
import { getVoucherDetail } from '../api/voucher'

const route = useRoute()
const voucherId = route.params.voucherId

const voucherDetail = ref(null)
const tableData = ref([])
const loading = ref(false)

function formatDateTime(dateStr) {
  if (!dateStr) return '—'
  return dateStr.replace('T', ' ').substring(0, 19)
}

function statusTag(status) {
  const map = { ISSUED: 'success', EXHAUSTED: 'info', EXPIRED: 'warning', CANCELLED: 'danger' }
  return map[status] || ''
}

function statusLabel(status) {
  const map = { ISSUED: '使用中', EXHAUSTED: '已用完', EXPIRED: '已过期', CANCELLED: '已作废' }
  return map[status] || status
}

async function fetchData() {
  loading.value = true
  try {
    const [detailRes, consumptionRes] = await Promise.all([
      getVoucherDetail(voucherId),
      getConsumptionHistory(voucherId),
    ])
    voucherDetail.value = detailRes.data
    tableData.value = consumptionRes.data || []
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.consumption-history {
  height: 100%;
}

.page-header {
  display: flex;
  align-items: center;
  gap: var(--space-sm, 12px);
  margin-bottom: var(--space-md, 16px);
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.info-card {
  margin-bottom: var(--space-md, 16px);
  padding: var(--space-md, 16px);
}

.info-row {
  display: flex;
  gap: var(--space-lg, 24px);
  flex-wrap: wrap;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  color: var(--color-text-secondary, #969799);
}

.info-value {
  font-size: 14px;
  color: var(--color-text-primary, #323233);
}

.highlight {
  color: #07c160;
  font-weight: 600;
}

.empty-hint {
  text-align: center;
  padding: var(--space-xl, 40px) 0;
  color: var(--color-text-secondary, #969799);
  font-size: 14px;
}
</style>
