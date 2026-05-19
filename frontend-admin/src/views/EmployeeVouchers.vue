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
            <el-option label="储值卡" value="STORED_VALUE" />
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
        <el-form-item label="分类">
          <el-select v-model="filters.categoryId" placeholder="全部" clearable style="width: 130px">
            <el-option label="全部" :value="null" />
            <el-option v-for="cat in categoryList" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
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
            <el-tag :type="voucherTypeTag(row.voucherType)" size="small">
              {{ voucherTypeLabel(row.voucherType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="面额/余额" width="110" align="right">
          <template #default="{ row }">
            <span v-if="row.voucherType === 'COUPON'" class="mono">¥{{ row.faceValue || 0 }}</span>
            <span v-else-if="row.voucherType === 'STORED_VALUE'" class="mono">¥{{ row.remainingBalance || 0 }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="有效期" width="110" align="center">
          <template #default="{ row }">
            {{ formatDate(row.expireAt) }}
          </template>
        </el-table-column>
        <el-table-column label="分类" width="100" align="center">
          <template #default="{ row }">
            <span>{{ categoryName(row) || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <span class="status-badge" :class="'status-' + statusClass(row.status)">
              {{ statusText(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
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

    <!-- Edit dialog -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑卡券"
      width="440px"
      :close-on-click-modal="false"
    >
      <el-form v-if="editingRow" label-width="80px">
        <el-form-item label="券码">
          <el-input :model-value="editingRow.voucherCode" disabled />
        </el-form-item>
        <el-form-item label="有效期">
          <el-date-picker
            v-model="editForm.expireAt"
            type="datetime"
            placeholder="选择有效期"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="editForm.remark"
            type="textarea"
            :rows="3"
            placeholder="修改备注信息"
            maxlength="255"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSubmitting" @click="handleEditSubmit">保存</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import QRCode from 'qrcode'
import { getVoucherList, updateVoucher } from '../api/voucher'
import { getCategoryList } from '../api/category'

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
  categoryId: null,
})

const categoryList = ref([])

// Edit state
const editDialogVisible = ref(false)
const editSubmitting = ref(false)
const editingRow = ref(null)
const editForm = reactive({ expireAt: '', remark: '' })

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
    if (filters.categoryId) params.categoryId = filters.categoryId

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
  filters.categoryId = null
  currentPage.value = 1
  fetchData()
}

// Debounced auto-search on filter changes
watch(
  () => [filters.holderId, filters.holderName, filters.keyword, filters.voucherType, filters.expireStart, filters.expireEnd, filters.categoryId],
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

function voucherTypeTag(type) {
  const map = { COUPON: 'warning', RESOURCE_USAGE: '', STORED_VALUE: 'success' }
  return map[type] || ''
}

function voucherTypeLabel(type) {
  const map = { COUPON: '优惠券', RESOURCE_USAGE: '因私使用', STORED_VALUE: '储值卡' }
  return map[type] || type
}

function statusClass(status) {
  const map = { ISSUED: 'valid', USED: 'used', EXHAUSTED: 'expired', EXPIRED: 'expired', CANCELLED: 'cancelled' }
  return map[status] || ''
}

function statusText(status) {
  const map = { ISSUED: '有效', USED: '已核销', EXHAUSTED: '已用完', EXPIRED: '已过期', CANCELLED: '已作废' }
  return map[status] || status
}

async function loadCategories() {
  try {
    const res = await getCategoryList()
    categoryList.value = res.data || []
  } catch {
    // handled by interceptor
  }
}

function categoryName(row) {
  if (!row.voucherType) return null
  const cat = categoryList.value.find(c => c.voucherType === row.voucherType)
  return cat ? cat.name : null
}

// ---- Edit ----
function showEditDialog(row) {
  editingRow.value = row
  editForm.expireAt = row.expireAt ? row.expireAt.replace(' ', 'T') : ''
  editForm.remark = row.remark || ''
  editDialogVisible.value = true
}

async function handleEditSubmit() {
  editSubmitting.value = true
  try {
    const payload = {}
    if (editForm.expireAt) payload.expireAt = editForm.expireAt
    if (editForm.remark !== (editingRow.value?.remark || '')) payload.remark = editForm.remark
    await updateVoucher(editingRow.value.id, payload)
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    fetchData()
  } catch {
    // handled by interceptor
  } finally {
    editSubmitting.value = false
  }
}

onMounted(() => {
  loadCategories()
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
