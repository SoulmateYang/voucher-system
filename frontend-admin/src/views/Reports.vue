<template>
  <div class="reports">
    <!-- Page header -->
    <div class="page-header">
      <h2>统计报表</h2>
      <div class="header-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          :default-value="defaultStart"
          @change="fetchReports"
        />
        <el-button type="primary" :loading="exportLoading" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出报表
        </el-button>
      </div>
    </div>

    <!-- Statistics cards -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-value" style="color: #1989fa">{{ stats.totalIssued }}</div>
        <div class="stat-label">已发放</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #07c160">{{ stats.totalUsed }}</div>
        <div class="stat-label">已核销</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #fa8c16">{{ stats.totalExpired }}</div>
        <div class="stat-label">已过期</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #ee0a24">{{ stats.totalCancelled }}</div>
        <div class="stat-label">已作废</div>
      </div>
    </div>

    <!-- Daily trend -->
    <div class="card trend-section">
      <h3 class="section-title">每日趋势</h3>
      <el-table
        :data="dailyTrend"
        stripe
        v-loading="trendLoading"
        empty-text="暂无数据"
        style="width: 100%"
      >
        <el-table-column prop="date" label="日期" width="140" />
        <el-table-column prop="issuedCount" label="发放数量" width="120" align="center">
          <template #default="{ row }">
            <span class="mono">{{ row.issuedCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="usedCount" label="核销数量" width="120" align="center">
          <template #default="{ row }">
            <span class="mono">{{ row.usedCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="expiredCount" label="过期数量" width="120" align="center">
          <template #default="{ row }">
            <span class="mono">{{ row.expiredCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="cancelledCount" label="作废数量" width="120" align="center">
          <template #default="{ row }">
            <span class="mono">{{ row.cancelledCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalValid" label="有效存量" width="120" align="center">
          <template #default="{ row }">
            <span class="mono">{{ row.totalValid }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getReports, getDailyTrend } from '../api/voucher'

// Date range
const today = new Date()
const defaultStart = new Date(today.getFullYear(), today.getMonth(), 1)

const dateRange = ref([
  formatDate(defaultStart),
  formatDate(today),
])

// Stats
const stats = reactive({
  totalIssued: 0,
  totalUsed: 0,
  totalExpired: 0,
  totalCancelled: 0,
})

// Daily trend
const dailyTrend = ref([])
const trendLoading = ref(false)
const exportLoading = ref(false)

// Format date to YYYY-MM-DD
function formatDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

// Fetch reports
async function fetchReports() {
  if (!dateRange.value || dateRange.value.length !== 2) return

  const params = {
    startDate: dateRange.value[0],
    endDate: dateRange.value[1],
  }

  try {
    const res = await getReports(params)
    const data = res.data || {}
    stats.totalIssued = data.totalIssued || 0
    stats.totalUsed = data.totalUsed || 0
    stats.totalExpired = data.totalExpired || 0
    stats.totalCancelled = data.totalCancelled || 0
  } catch {
    // Reset stats on error
    stats.totalIssued = 0
    stats.totalUsed = 0
    stats.totalExpired = 0
    stats.totalCancelled = 0
  }

  // Fetch daily trend
  trendLoading.value = true
  try {
    const res = await getDailyTrend(params)
    dailyTrend.value = res.data || []
  } catch {
    dailyTrend.value = []
  } finally {
    trendLoading.value = false
  }
}

// Export to CSV
function handleExport() {
  exportLoading.value = true

  try {
    const rows = dailyTrend.value
    if (!rows || rows.length === 0) {
      ElMessage.warning('暂无数据可导出')
      exportLoading.value = false
      return
    }

    // Build CSV
    const headers = ['日期', '发放数量', '核销数量', '过期数量', '作废数量', '有效存量']
    const csvRows = [headers.join(',')]

    for (const row of rows) {
      csvRows.push([
        row.date,
        row.issuedCount || 0,
        row.usedCount || 0,
        row.expiredCount || 0,
        row.cancelledCount || 0,
        row.totalValid || 0,
      ].join(','))
    }

    const csvContent = '﻿' + csvRows.join('\n') // BOM for Excel UTF-8
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `卡券报表_${dateRange.value[0]}_${dateRange.value[1]}.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)

    ElMessage.success('报表导出成功')
  } catch {
    ElMessage.error('导出失败，请重试')
  } finally {
    exportLoading.value = false
  }
}

onMounted(() => {
  fetchReports()
})
</script>

<style scoped>
.reports {
  height: 100%;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--space-sm, 12px);
}

/* Stat cards */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-md, 16px);
  margin-bottom: var(--space-lg, 24px);
}

.stat-card {
  background: var(--color-card-bg, #ffffff);
  border: 1px solid var(--color-border, #ebedf0);
  border-radius: var(--radius-md, 8px);
  padding: var(--space-lg, 24px);
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  font-family: 'JetBrains Mono', 'SF Mono', 'Consolas', 'Monaco', monospace;
}

.stat-label {
  font-size: 13px;
  color: var(--color-text-secondary, #969799);
  margin-top: var(--space-xs, 8px);
}

/* Trend section */
.trend-section {
  margin-top: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, #323233);
  margin-bottom: var(--space-md, 16px);
}
</style>
