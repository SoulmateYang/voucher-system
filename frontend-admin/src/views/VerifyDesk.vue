<template>
  <div class="verify-desk">
    <!-- Alert banner -->
    <transition name="fade">
      <el-alert
        v-if="alertVisible"
        :title="alertTitle"
        :description="alertDescription"
        :type="alertType"
        show-icon
        closable
        class="alert-banner"
        @close="alertVisible = false"
      />
    </transition>

    <div class="verify-body">
      <!-- LEFT: Scan area -->
      <div class="verify-left">
        <div class="scan-input-wrapper">
          <el-input
            ref="scanInputRef"
            v-model="scanCode"
            placeholder="扫描或输入券码，按回车查询"
            class="scan-input"
            @keydown.enter="handleScan"
          />
        </div>

        <!-- Recent scans -->
        <div class="recent-section">
          <div class="section-header">
            <h3>最近扫描</h3>
            <span v-if="recentScans.length > 0" class="scan-count">
              共 {{ recentScans.length }} 条
            </span>
          </div>

          <div v-if="recentScans.length === 0" class="empty-state">
            <el-icon :size="48" class="empty-icon"><Search /></el-icon>
            <p>扫描券码后将在此显示记录</p>
          </div>

          <div v-else class="scan-list">
            <div
              v-for="(item, index) in recentScans"
              :key="index"
              class="scan-item"
              :class="{ 'is-active': index === activeScanIndex }"
              @click="selectScan(item, index)"
            >
              <div class="scan-item-left">
                <span class="scan-code mono">{{ item.voucherCode }}</span>
                <span class="scan-time">{{ item.scannedAt }}</span>
              </div>
              <span
                class="status-badge"
                :class="'status-' + item.status"
              >
                {{ item.statusText }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- RIGHT: Voucher detail panel -->
      <div class="verify-right">
        <template v-if="voucherDetail">
          <div class="detail-panel card">
            <h3 class="detail-title">卡券详情</h3>

            <div class="detail-grid">
              <div class="detail-row">
                <label>持有人</label>
                <span class="detail-value">{{ voucherDetail.holderName }}</span>
              </div>
              <div class="detail-row">
                <label>券类型</label>
                <span class="detail-value">
                  <span class="status-badge" :class="typeBadgeClass(voucherDetail.voucherType)">
                    {{ typeLabel(voucherDetail.voucherType) }}
                  </span>
                </span>
              </div>
              <div v-if="voucherDetail.voucherType === 'COUPON'" class="detail-row">
                <label>面额</label>
                <span class="detail-value" style="font-size: 20px; font-weight: 600; color: #ee0a24">
                  ¥{{ voucherDetail.faceValue }}
                </span>
              </div>
              <div v-if="voucherDetail.voucherType === 'STORED_VALUE'" class="detail-row">
                <label>剩余余额</label>
                <span class="detail-value" style="font-size: 20px; font-weight: 600; color: #07c160">
                  ¥{{ voucherDetail.remainingBalance || 0 }}
                </span>
              </div>
              <div v-if="voucherDetail.voucherType === 'RESOURCE_USAGE'" class="detail-row">
                <label>资源类型</label>
                <span class="detail-value">{{ voucherDetail.resourceType }}</span>
              </div>
              <div class="detail-row">
                <label>有效期</label>
                <span class="detail-value">{{ voucherDetail.validity }}</span>
              </div>
              <div class="detail-row">
                <label>状态</label>
                <span
                  class="status-badge"
                  :class="'status-' + voucherDetail.status"
                >
                  {{ voucherDetail.statusText }}
                </span>
              </div>
              <div class="detail-row">
                <label>券码</label>
                <span class="detail-value mono voucher-code-text">
                  {{ voucherDetail.voucherCode }}
                </span>
              </div>
              <div v-if="verifyResult.show && verifyResult.isStoredValue" class="detail-row">
                <label>扣减金额</label>
                <span class="detail-value" style="font-size: 18px; font-weight: 600; color: #07c160">
                  ¥{{ verifyResult.deductAmount }}
                </span>
              </div>
              <div v-if="verifyResult.show && verifyResult.isStoredValue" class="detail-row">
                <label>剩余余额</label>
                <span class="detail-value" style="font-size: 16px; font-weight: 600; color: #323233">
                  ¥{{ verifyResult.remainingBalance }}
                </span>
              </div>
              <div v-if="verifyResult.show && !verifyResult.isStoredValue" class="detail-row">
                <label>抵扣金额</label>
                <span class="detail-value" style="font-size: 18px; font-weight: 600; color: #07c160">
                  ¥{{ verifyResult.discountAmount }}
                </span>
              </div>
            </div>

            <el-input-number
              v-if="showOrderAmountInput && voucherDetail.status === 'valid'"
              v-model="orderAmount"
              :min="0"
              :precision="2"
              :controls="false"
              :placeholder="orderAmountPlaceholder"
              style="width: 100%; margin-bottom: 12px"
            />

            <el-button
              type="primary"
              class="confirm-btn"
              :disabled="voucherDetail.status !== 'valid'"
              @click="handleConfirmVerify"
            >
              确认核销
            </el-button>

            <p class="hint-text">核销后不可撤销 · 请确认持有人身份</p>
          </div>

          <!-- Today's records -->
          <div class="today-section">
            <h3>今日核销记录</h3>
            <el-table
              :data="todayRecords"
              stripe
              size="small"
              max-height="280"
              empty-text="暂无今日核销记录"
            >
              <el-table-column prop="voucherCode" label="券码" min-width="140">
                <template #default="{ row }">
                  <span class="mono">{{ row.voucherCode }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="holderName" label="持有人" width="100" />
              <el-table-column prop="verifiedAt" label="核销时间" width="160" />
            </el-table>
          </div>
        </template>

        <!-- Empty state: no detail -->
        <template v-else>
          <div class="detail-empty">
            <el-icon :size="48" class="empty-icon"><Ticket /></el-icon>
            <p>扫描券码后将在此显示卡券详情</p>
            <p class="empty-hint">使用扫码枪扫描或手动输入券码</p>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { Search, Ticket } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { verifyVoucher, confirmVerify, getTodayRecords } from '../api/voucher'

// Error code mapping
const ERROR_MAP = {
  4001: { text: '该券已被核销', type: 'warning' },
  4002: { text: '该券已过期', type: 'error' },
  4003: { text: '该券已作废', type: 'error' },
  4004: { text: '券码不存在，请确认后重试', type: 'error' },
}

const STATUS_MAP = {
  valid: { text: '有效', type: 'success' },
  used: { text: '已核销', type: 'primary' },
  expired: { text: '已过期', type: 'warning' },
  cancelled: { text: '已作废', type: 'danger' },
}

// State
const scanInputRef = ref(null)
const scanCode = ref('')
const orderAmount = ref(null)
const alertVisible = ref(false)
const alertTitle = ref('')
const alertDescription = ref('')
const alertType = ref('success')
const voucherDetail = ref(null)
const todayRecords = ref([])
const activeScanIndex = ref(-1)
const verifyResult = reactive({ show: false, discountAmount: 0, deductAmount: 0, remainingBalance: 0, isStoredValue: false })

const showOrderAmountInput = computed(() => {
  if (!voucherDetail.value) return false
  const vt = voucherDetail.value.voucherType
  if (vt === 'STORED_VALUE') return true
  if (vt === 'COUPON' && voucherDetail.value.discountType === 'PERCENTAGE') return true
  return false
})

const orderAmountPlaceholder = computed(() => {
  if (voucherDetail.value?.voucherType === 'STORED_VALUE') return '请输入消费金额'
  return '请输入订单金额'
})

function typeBadgeClass(voucherType) {
  const map = { COUPON: 'status-warning', RESOURCE_USAGE: 'status-used', STORED_VALUE: 'status-valid' }
  return map[voucherType] || 'status-used'
}

function typeLabel(voucherType) {
  const map = { COUPON: '优惠券', RESOURCE_USAGE: '因私使用', STORED_VALUE: '储值卡' }
  return map[voucherType] || voucherType
}

const recentScans = reactive([])

let debounceTimer = null

// Auto-focus input on mount
onMounted(() => {
  nextTick(() => {
    scanInputRef.value?.focus()
  })
  fetchTodayRecords()
})

// Watch for input changes with 200ms debounce
// This handles scan guns that may not send Enter
watch(scanCode, (newVal) => {
  if (debounceTimer) {
    clearTimeout(debounceTimer)
    debounceTimer = null
  }
  if (!newVal) return
  debounceTimer = setTimeout(() => {
    if (scanCode.value.trim()) {
      triggerLookup()
    }
  }, 200)
})

// Handle Enter key from scan gun or manual input
function handleScan() {
  if (debounceTimer) {
    clearTimeout(debounceTimer)
    debounceTimer = null
  }
  if (!scanCode.value.trim()) return
  triggerLookup()
}

// Core lookup function
async function triggerLookup() {
  const code = scanCode.value.trim()
  if (!code) return

  try {
    const res = await verifyVoucher(code)
    const data = res.data

    // Map status
    const statusInfo = STATUS_MAP[data.status] || { text: data.status, type: 'info' }

    // Update voucher detail
    voucherDetail.value = {
      holderName: data.holderName || '-',
      resourceType: data.resourceType || '-',
      validity: data.validity || '-',
      status: data.status,
      statusText: statusInfo.text,
      voucherCode: code,
      voucherType: data.voucherType || 'RESOURCE_USAGE',
      faceValue: data.faceValue,
      remainingBalance: data.remainingBalance,
      initialBalance: data.initialBalance,
      discountType: data.discountType,
      discountValue: data.discountValue,
      minOrderAmount: data.minOrderAmount,
    }
    verifyResult.show = false
    orderAmount.value = null

    // Add to recent scans
    const now = new Date()
    const timeStr = now.toLocaleTimeString('zh-CN', { hour12: false })
    recentScans.unshift({
      voucherCode: code,
      scannedAt: timeStr,
      status: data.status,
      statusText: statusInfo.text,
    })
    activeScanIndex.value = 0

    // Show success alert
    showAlert('success', '查询成功', `券码 ${code} 查询成功`)
    alertType.value = 'success'
  } catch (err) {
    const errCode = err.response?.data?.code
    const errInfo = ERROR_MAP[errCode] || { text: err.message || '查询失败', type: 'error' }

    // Still add to recent scans with error status
    const now = new Date()
    const timeStr = now.toLocaleTimeString('zh-CN', { hour12: false })
    recentScans.unshift({
      voucherCode: code,
      scannedAt: timeStr,
      status: 'error',
      statusText: '查询失败',
    })
    activeScanIndex.value = 0

    // Clear detail if failed
    voucherDetail.value = null

    showAlert(errInfo.type === 'warning' ? 'warning' : 'error', '查询失败', errInfo.text)
  } finally {
    scanCode.value = ''
    nextTick(() => {
      scanInputRef.value?.focus()
    })
  }
}

// Select from recent scans
function selectScan(item, index) {
  activeScanIndex.value = index
  // Re-trigger lookup for this code
  scanCode.value = item.voucherCode
  nextTick(() => {
    triggerLookup()
  })
}

// Confirm verification
async function handleConfirmVerify() {
  if (!voucherDetail.value) return

  const code = voucherDetail.value.voucherCode

  try {
    await ElMessageBox.confirm(
      '确认核销该卡券？核销后不可撤销。',
      '确认核销',
      {
        confirmButtonText: '确认核销',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger',
      }
    )
  } catch {
    // User cancelled
    return
  }

  try {
    const res = await confirmVerify(code, orderAmount.value)
    const data = res.data || {}
    verifyResult.show = true
    const isStored = data.voucherType === 'STORED_VALUE'
    verifyResult.isStoredValue = isStored
    if (isStored) {
      verifyResult.deductAmount = data.deductAmount || 0
      verifyResult.remainingBalance = data.remainingBalance || 0
      verifyResult.discountAmount = 0
    } else {
      verifyResult.discountAmount = data.discountAmount || 0
      verifyResult.deductAmount = 0
      verifyResult.remainingBalance = 0
    }

    let successMsg
    if (isStored) {
      successMsg = `券码 ${code} 已成功核销，扣减 ¥${data.deductAmount}，剩余 ¥${data.remainingBalance}`
    } else {
      successMsg = data.discountAmount > 0
        ? `券码 ${code} 已成功核销，抵扣 ¥${data.discountAmount}`
        : `券码 ${code} 已成功核销`
    }
    showAlert('success', '核销成功', successMsg)
    alertType.value = 'success'

    // Update voucher detail status
    if (voucherDetail.value) {
      voucherDetail.value.status = data.status || 'used'
      voucherDetail.value.statusText = data.status === 'EXHAUSTED' ? '已用完' : '已核销'
      if (isStored) {
        voucherDetail.value.remainingBalance = data.remainingBalance || 0
      }
    }

    // Update recent scan item
    const idx = recentScans.findIndex((s) => s.voucherCode === code)
    if (idx !== -1) {
      if (data.status === 'EXHAUSTED') {
        recentScans[idx].status = 'used'
        recentScans[idx].statusText = '已用完'
      } else {
        recentScans[idx].status = data.status || 'used'
        recentScans[idx].statusText = data.status === 'ISSUED' ? '有效' : '已核销'
      }
    }

    // Refresh today's records
    fetchTodayRecords()
  } catch (err) {
    showAlert('error', '核销失败', err.message || '核销失败，请重试')
    alertType.value = 'error'
  }
}

// Fetch today's records
async function fetchTodayRecords() {
  try {
    const res = await getTodayRecords()
    todayRecords.value = res.data || []
  } catch {
    todayRecords.value = []
  }
}

// Alert helper
function showAlert(type, title, description) {
  alertType.value = type
  alertTitle.value = title
  alertDescription.value = description
  alertVisible.value = true

  // Auto-hide after 5 seconds
  setTimeout(() => {
    alertVisible.value = false
  }, 5000)
}
</script>

<style scoped>
.verify-desk {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.alert-banner {
  margin-bottom: var(--space-md, 16px);
}

.verify-body {
  display: flex;
  gap: var(--space-lg, 24px);
  flex: 1;
  min-height: 0;
}

/* LEFT */
.verify-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.scan-input-wrapper {
  margin-bottom: var(--space-md, 16px);
}

.scan-input {
  height: 52px;
}

.scan-input :deep(.el-input__wrapper) {
  border: 2px solid #1989fa;
  border-radius: var(--radius-sm, 4px);
  box-shadow: none;
  height: 52px;
  background: #ffffff;
}

.scan-input :deep(.el-input__wrapper):hover,
.scan-input :deep(.el-input__wrapper).is-focus {
  border-color: #1989fa;
  box-shadow: 0 0 0 2px rgba(25, 137, 250, 0.15);
}

.scan-input :deep(.el-input__inner) {
  font-family: 'JetBrains Mono', 'SF Mono', 'Consolas', 'Monaco', monospace;
  font-size: 20px;
  letter-spacing: 2px;
  height: 48px;
}

.scan-input :deep(.el-input__inner)::placeholder {
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 14px;
  letter-spacing: normal;
}

/* Recent scans */
.recent-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-sm, 12px);
}

.section-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, #323233);
}

.scan-count {
  font-size: 12px;
  color: var(--color-text-secondary, #969799);
}

.scan-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-2xs, 4px);
}

.scan-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-sm, 12px) var(--space-md, 16px);
  background: #ffffff;
  border: 1px solid var(--color-border, #ebedf0);
  border-radius: var(--radius-sm, 4px);
  cursor: pointer;
  transition: background-color 0.15s, border-color 0.15s;
}

.scan-item:hover {
  border-color: var(--color-primary, #1989fa);
}

.scan-item.is-active {
  border-color: var(--color-primary, #1989fa);
  background-color: rgba(25, 137, 250, 0.04);
}

.scan-item-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.scan-code {
  font-family: 'JetBrains Mono', 'SF Mono', 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  color: var(--color-text-primary, #323233);
  letter-spacing: 0.5px;
}

.scan-time {
  font-size: 11px;
  color: var(--color-text-secondary, #969799);
}

/* RIGHT */
.verify-right {
  width: 420px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-md, 16px);
}

.detail-panel {
  flex-shrink: 0;
}

.detail-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, #323233);
  margin-bottom: var(--space-md, 16px);
  padding-bottom: var(--space-sm, 12px);
  border-bottom: 1px solid var(--color-border, #ebedf0);
}

.detail-grid {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm, 12px);
  margin-bottom: var(--space-lg, 24px);
}

.detail-row {
  display: flex;
  align-items: flex-start;
}

.detail-row label {
  width: 80px;
  font-size: 13px;
  color: var(--color-text-secondary, #969799);
  flex-shrink: 0;
}

.detail-value {
  font-size: 14px;
  color: var(--color-text-primary, #323233);
  word-break: break-all;
}

.voucher-code-text {
  font-size: 13px;
  letter-spacing: 1px;
}

.confirm-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}

.confirm-btn.is-disabled {
  opacity: 0.5;
}

.hint-text {
  text-align: center;
  font-size: 12px;
  color: var(--color-text-secondary, #969799);
  margin-top: var(--space-xs, 8px);
}

/* Today's records */
.today-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.today-section h3 {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary, #323233);
  margin-bottom: var(--space-sm, 12px);
}

/* Empty states */
.detail-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--color-text-secondary, #969799);
  text-align: center;
  background: #ffffff;
  border: 1px solid var(--color-border, #ebedf0);
  border-radius: var(--radius-md, 8px);
  padding: var(--space-2xl, 48px);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  color: var(--color-text-secondary, #969799);
  text-align: center;
  padding: var(--space-3xl, 64px);
}

.empty-icon {
  color: var(--color-text-placeholder, #c8c9cc);
  margin-bottom: var(--space-md, 16px);
}

.empty-hint {
  font-size: 12px;
  margin-top: var(--space-xs, 8px);
  color: var(--color-text-placeholder, #c8c9cc);
}

/* Status badge overrides */
.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: var(--radius-sm, 4px);
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.status-valid {
  background-color: #e8f8e8;
  color: #07c160;
}

.status-used {
  background-color: #e8f4ff;
  color: #1989fa;
}

.status-expired {
  background-color: #fff7e6;
  color: #fa8c16;
}

.status-cancelled {
  background-color: #fef0f0;
  color: #ee0a24;
}

.status-error {
  background-color: #fef0f0;
  color: #ee0a24;
}
</style>
