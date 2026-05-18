<template>
  <div class="employee-vouchers">
    <div class="page-header">
      <h2>员工卡券</h2>
    </div>

    <!-- Filter bar -->
    <div class="card filter-card">
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="工号">
          <el-input v-model="filters.holderId" placeholder="工号" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="filters.holderName" placeholder="姓名" clearable style="width: 120px" />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" placeholder="券码/卡券名称" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="券类型">
          <el-select v-model="filters.voucherType" placeholder="全部" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="优惠券" value="COUPON" />
            <el-option label="因私使用" value="RESOURCE_USAGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="到期起始">
          <el-date-picker
            v-model="filters.expireStart"
            type="date"
            placeholder="起始日期"
            value-format="YYYY-MM-DD"
            style="width: 140px"
          />
        </el-form-item>
        <el-form-item label="到期截止">
          <el-date-picker
            v-model="filters.expireEnd"
            type="date"
            placeholder="截止日期"
            value-format="YYYY-MM-DD"
            style="width: 140px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Voucher table -->
    <div class="card">
      <el-table
        :data="tableData"
        stripe
        v-loading="tableLoading"
        empty-text="暂无卡券数据"
        style="width: 100%"
      >
        <el-table-column prop="voucherCode" label="券码" min-width="160">
          <template #default="{ row }">
            <span class="mono">{{ row.voucherCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="holderName" label="持有人" width="100" />
        <el-table-column prop="holderId" label="工号" width="110" />
        <el-table-column label="卡券名称" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.remark || row.batchName || (row.voucherType === 'COUPON' ? '优惠券' : '因私使用') }}
          </template>
        </el-table-column>
        <el-table-column label="二维码" width="80" align="center">
          <template #default="{ row }">
            <template v-if="row.status === 'ISSUED' && row._qrDataUrl">
              <el-popover placement="left" trigger="click" :width="220" :z-index="2100">
                <template #reference>
                  <img :src="row._qrDataUrl" class="qr-thumb" />
                </template>
                <div class="qr-preview">
                  <img :src="row._qrDataUrl" class="qr-large" />
                  <p class="qr-preview-code">{{ row.voucherCode }}</p>
                </div>
              </el-popover>
            </template>
            <template v-else>
              <span class="text-muted">—</span>
            </template>
          </template>
        </el-table-column>
        <el-table-column prop="voucherType" label="券类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.voucherType === 'COUPON' ? 'warning' : ''" size="small">
              {{ row.voucherType === 'COUPON' ? '优惠券' : '因私使用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="面额" width="100" align="right">
          <template #default="{ row }">
            <span v-if="row.voucherType === 'COUPON'" class="mono">¥{{ row.faceValue || 0 }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="有效期" width="110" align="center">
          <template #default="{ row }">
            {{ formatDate(row.expireAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <span class="status-badge" :class="'status-' + statusClass(row.status)">
              {{ statusText(row.status) }}
            </span>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="total > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          small
          @current-change="fetchData"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import QRCode from 'qrcode'
import { getVoucherList } from '../api/voucher'

const tableData = ref([])
const tableLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const filters = reactive({
  holderId: '',
  holderName: '',
  keyword: '',
  voucherType: '',
  expireStart: '',
  expireEnd: '',
})

let searchTimer = null

async function generateQRDataUrl(voucherCode) {
  try {
    const svg = await QRCode.toString(voucherCode, {
      type: 'svg',
      width: 200,
      margin: 1,
      errorCorrectionLevel: 'M',
      color: { dark: '#323233', light: '#ffffff' },
    })
    return 'data:image/svg+xml;base64,' + btoa(svg)
  } catch {
    return ''
  }
}

async function fetchData() {
  tableLoading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
    }
    if (filters.holderId) params.holderId = filters.holderId
    if (filters.holderName) params.holderName = filters.holderName
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.voucherType) params.voucherType = filters.voucherType
    if (filters.expireStart) params.expireStart = filters.expireStart
    if (filters.expireEnd) params.expireEnd = filters.expireEnd

    const res = await getVoucherList(params)
    const data = res.data || {}
    tableData.value = data.records || []
    total.value = data.total || 0

    // Pre-generate QR codes for ISSUED vouchers
    for (const voucher of tableData.value) {
      if (voucher.status === 'ISSUED') {
        voucher._qrDataUrl = await generateQRDataUrl(voucher.voucherCode)
      }
    }
  } catch {
    tableData.value = []
    total.value = 0
  } finally {
    tableLoading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  filters.holderId = ''
  filters.holderName = ''
  filters.keyword = ''
  filters.voucherType = ''
  filters.expireStart = ''
  filters.expireEnd = ''
  currentPage.value = 1
  fetchData()
}

// Debounced auto-search on filter changes
watch(
  () => [filters.holderId, filters.holderName, filters.keyword, filters.voucherType, filters.expireStart, filters.expireEnd],
  () => {
    if (searchTimer) clearTimeout(searchTimer)
    searchTimer = setTimeout(() => {
      currentPage.value = 1
      fetchData()
    }, 400)
  },
  { deep: true }
)

function formatDate(dateStr) {
  if (!dateStr) return '—'
  return dateStr.substring(0, 10)
}

function statusClass(status) {
  const map = { ISSUED: 'valid', USED: 'used', EXPIRED: 'expired', CANCELLED: 'cancelled' }
  return map[status] || ''
}

function statusText(status) {
  const map = { ISSUED: '有效', USED: '已核销', EXPIRED: '已过期', CANCELLED: '已作废' }
  return map[status] || status
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.employee-vouchers {
  height: 100%;
}

.filter-card {
  margin-bottom: var(--space-md, 16px);
  padding: var(--space-sm, 12px) var(--space-md, 16px) 0;
}

.filter-form .el-form-item {
  margin-bottom: var(--space-sm, 12px);
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-md, 16px);
}

.text-muted {
  color: #c8c9cc;
}

/* QR code in voucher table */
.qr-thumb {
  width: 36px;
  height: 36px;
  cursor: pointer;
  display: block;
  margin: 0 auto;
}

.qr-preview {
  text-align: center;
}

.qr-large {
  width: 200px;
  height: 200px;
  display: block;
  margin: 0 auto;
}

.qr-preview-code {
  margin-top: 8px;
  font-family: 'JetBrains Mono', 'SF Mono', Consolas, monospace;
  font-size: 11px;
  color: #969799;
  word-break: break-all;
}
</style>
