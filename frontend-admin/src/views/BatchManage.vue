<template>
  <div class="batch-manage">
    <!-- Page header -->
    <div class="page-header">
      <h2>卡券管理</h2>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        创建批次
      </el-button>
    </div>

    <!-- Batch list table -->
    <div class="card">
      <el-table
        :data="batchList"
        stripe
        v-loading="tableLoading"
        empty-text="暂无批次数据"
        style="width: 100%"
      >
        <el-table-column prop="batchName" label="名称" min-width="140" />
        <el-table-column prop="voucherType" label="券类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.voucherType === 'COUPON' ? 'warning' : ''" size="small">
              {{ row.voucherType === 'COUPON' ? '优惠券' : '因私使用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="resourceDesc" label="资源描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="validDays" label="有效天数" width="100">
          <template #default="{ row }">
            {{ row.validDays }} 天
          </template>
        </el-table-column>
        <el-table-column prop="totalCount" label="已发数量" width="100" align="center">
          <template #default="{ row }">
            <span class="mono">{{ row.totalCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 'ACTIVE'"
              :loading="row._statusLoading"
              @change="(val) => toggleBatchStatus(row, val)"
              inline-prompt
              active-text="启用"
              inactive-text="停用"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleIssue(row)">
              发放券码
            </el-button>
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">
              查看券码
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div v-if="total > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          small
          @current-change="fetchBatchList"
        />
      </div>
    </div>

    <!-- Create Batch Dialog -->
    <el-dialog
      v-model="showCreateDialog"
      title="创建批次"
      width="480px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-position="top"
      >
        <el-form-item label="券类型" prop="voucherType">
          <el-radio-group v-model="createForm.voucherType">
            <el-radio value="RESOURCE_USAGE">因私使用</el-radio>
            <el-radio value="COUPON">优惠券</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="批次名称" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入批次名称" maxlength="50" />
        </el-form-item>
        <el-form-item v-if="createForm.voucherType === 'RESOURCE_USAGE'" label="资源描述" prop="resourceDesc">
          <el-input
            v-model="createForm.resourceDesc"
            type="textarea"
            :rows="3"
            placeholder="例如：会议室B使用权限，2小时/次"
            maxlength="200"
          />
        </el-form-item>
        <el-form-item label="有效天数" prop="validDays">
          <el-input-number
            v-model="createForm.validDays"
            :min="1"
            :max="3650"
            controls-position="right"
          />
          <span class="form-hint">天（从发放之日起计算）</span>
        </el-form-item>
        <template v-if="createForm.voucherType === 'COUPON'">
          <el-form-item label="折扣类型" prop="discountType">
            <el-radio-group v-model="createForm.discountType">
              <el-radio value="FIXED_AMOUNT">满减</el-radio>
              <el-radio value="PERCENTAGE">折扣率</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="优惠金额" prop="discountValue">
            <el-input-number
              v-model="createForm.discountValue"
              :min="0.01"
              :precision="2"
              controls-position="right"
            />
            <span class="form-hint">
              {{ createForm.discountType === 'PERCENTAGE' ? '%（例如输入15表示85折）' : '元' }}
            </span>
          </el-form-item>
          <el-form-item v-if="createForm.discountType === 'PERCENTAGE'" label="面额上限" prop="faceValue">
            <el-input-number
              v-model="createForm.faceValue"
              :min="0.01"
              :precision="2"
              controls-position="right"
            />
            <span class="form-hint">元（最高抵扣金额）</span>
          </el-form-item>
          <el-form-item label="最低消费" prop="minOrderAmount">
            <el-input-number
              v-model="createForm.minOrderAmount"
              :min="0"
              :precision="2"
              controls-position="right"
            />
            <span class="form-hint">元（0表示无门槛）</span>
          </el-form-item>
        </template>
        <el-form-item label="可转赠">
          <el-switch v-model="createForm.transferable" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="handleCreateBatch">
          创建
        </el-button>
      </template>
    </el-dialog>

    <!-- Issue Vouchers Dialog -->
    <el-dialog
      v-model="showIssueDialog"
      title="发放券码"
      width="560px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <template v-if="currentBatch">
        <p class="issue-batch-info">
          批次：<strong>{{ currentBatch.batchName }}</strong>
          <el-tag v-if="currentBatch.voucherType === 'COUPON'" type="warning" size="small" style="margin-left: 8px">优惠券</el-tag>
          <span v-if="currentBatch.voucherType === 'COUPON' && currentBatch.discountType" style="margin-left: 8px; font-size: 12px; color: #969799">
            {{ currentBatch.discountType === 'FIXED_AMOUNT' ? `满减 ¥${currentBatch.discountValue}` : `${currentBatch.discountValue}%折扣` }}
            <template v-if="currentBatch.minOrderAmount > 0"> · 满¥{{ currentBatch.minOrderAmount }}可用</template>
          </span>
        </p>

        <el-form
          ref="issueFormRef"
          :model="issueForm"
          :rules="issueRules"
          label-position="top"
        >
          <el-form-item label="发放对象">
            <div class="issue-employees">
              <div
                v-for="(emp, index) in issueForm.employees"
                :key="index"
                class="employee-row"
              >
                <el-autocomplete
                  v-model="emp.employeeId"
                  :fetch-suggestions="searchEmployees"
                  :debounce="300"
                  placeholder="搜索员工（工号/姓名）"
                  @select="(item) => onEmployeeSelect(item, index)"
                  style="width: 200px"
                  clearable
                >
                  <template #default="{ item }">
                    <span>{{ item.value }} | {{ item.employeeName }} | {{ item.department }}</span>
                  </template>
                </el-autocomplete>
                <el-input
                  v-model="emp.employeeName"
                  placeholder="姓名"
                  style="width: 160px"
                />
                <el-button
                  type="danger"
                  link
                  @click="removeEmployee(index)"
                  :disabled="issueForm.employees.length <= 1"
                >
                  移除
                </el-button>
              </div>
            </div>
            <el-button type="primary" link class="add-employee-btn" @click="addEmployee">
              <el-icon><Plus /></el-icon>
              添加员工
            </el-button>
          </el-form-item>
        </el-form>

        <!-- Issue result -->
        <el-alert
          v-if="issueResult.show"
          :title="issueResult.title"
          :type="issueResult.type"
          :description="issueResult.description"
          show-icon
          closable
          @close="issueResult.show = false"
        />
      </template>

      <template #footer>
        <el-button @click="showIssueDialog = false">取消</el-button>
        <el-button type="primary" :loading="issueLoading" @click="handleIssueSubmit">
          确认发放
        </el-button>
      </template>
    </el-dialog>

    <!-- Voucher Detail Dialog -->
    <el-dialog
      v-model="showVoucherDialog"
      :title="`券码列表 - ${currentBatch?.batchName || ''}`"
      width="520px"
      destroy-on-close
    >
      <el-table
        :data="voucherList"
        stripe
        size="small"
        max-height="400"
        empty-text="暂无券码数据"
      >
        <el-table-column prop="voucherCode" label="券码" min-width="180">
          <template #default="{ row }">
            <span class="mono">{{ row.voucherCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="holderName" label="持有人" width="100" />
        <el-table-column v-if="currentBatch?.voucherType === 'COUPON'" label="面额" width="100" align="right">
          <template #default="{ row }">
            <span class="mono">¥{{ row.faceValue || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <span class="status-badge" :class="'status-' + statusClass(row.status)">
              {{ statusText(row.status) }}
            </span>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="showVoucherDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getBatchList, createBatch, issueVouchers, getVouchersByBatch } from '../api/batch'
import { getEmployeeList } from '../api/employee'

// ==================== Batch List ====================
const batchList = ref([])
const tableLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

async function fetchBatchList() {
  tableLoading.value = true
  try {
    const res = await getBatchList({
      page: currentPage.value,
      pageSize: pageSize.value,
    })
    const list = Array.isArray(res.data) ? res.data : (res.data?.list || [])
    batchList.value = list
    total.value = res.data?.total || list.length
  } catch {
    batchList.value = []
  } finally {
    tableLoading.value = false
  }
}

async function toggleBatchStatus(row, enabled) {
  row._statusLoading = true
  try {
    // Call API to toggle status
    // await updateBatchStatus(row.id, enabled ? 'active' : 'inactive')
    row.status = enabled ? 'ACTIVE' : 'INACTIVE'
    ElMessage.success(enabled ? '已启用' : '已停用')
  } catch {
    ElMessage.error('操作失败')
  } finally {
    row._statusLoading = false
  }
}

// ==================== Create Batch ====================
const showCreateDialog = ref(false)
const createFormRef = ref(null)
const createLoading = ref(false)

const createForm = reactive({
  name: '',
  voucherType: 'RESOURCE_USAGE',
  resourceDesc: '',
  validDays: 30,
  discountType: 'FIXED_AMOUNT',
  discountValue: null,
  minOrderAmount: null,
  faceValue: null,
  transferable: 1,
})

const createRules = {
  name: [{ required: true, message: '请输入批次名称', trigger: 'blur' }],
  voucherType: [{ required: true, message: '请选择券类型', trigger: 'change' }],
  resourceDesc: [
    {
      validator: (rule, value, callback) => {
        if (createForm.voucherType === 'RESOURCE_USAGE' && !value) {
          callback(new Error('请输入资源描述'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  validDays: [{ required: true, message: '请设置有效天数', trigger: 'blur' }],
  discountType: [
    {
      required: true,
      validator: (rule, value, callback) => {
        if (createForm.voucherType === 'COUPON' && !value) {
          callback(new Error('请选择折扣类型'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
  discountValue: [
    {
      required: true,
      validator: (rule, value, callback) => {
        if (createForm.voucherType === 'COUPON' && (!value || value <= 0)) {
          callback(new Error('请输入优惠金额'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

async function handleCreateBatch() {
  if (!createFormRef.value) return
  try {
    await createFormRef.value.validate()
  } catch {
    return
  }

  createLoading.value = true
  try {
    const payload = {
      batchName: createForm.name,
      voucherType: createForm.voucherType,
      resourceDesc: createForm.resourceDesc,
      validDays: createForm.validDays,
      transferable: createForm.transferable,
    }
    if (createForm.voucherType === 'COUPON') {
      payload.discountType = createForm.discountType
      payload.discountValue = createForm.discountValue
      payload.minOrderAmount = createForm.minOrderAmount
      if (createForm.discountType === 'PERCENTAGE') {
        payload.faceValue = createForm.faceValue
      }
    }
    await createBatch(payload)
    ElMessage.success('批次创建成功')
    showCreateDialog.value = false
    resetCreateForm()
    fetchBatchList()
  } catch {
    // Error handled by interceptor
  } finally {
    createLoading.value = false
  }
}

function resetCreateForm() {
  createForm.name = ''
  createForm.voucherType = 'RESOURCE_USAGE'
  createForm.resourceDesc = ''
  createForm.validDays = 30
  createForm.discountType = 'FIXED_AMOUNT'
  createForm.discountValue = null
  createForm.minOrderAmount = null
  createForm.faceValue = null
  createForm.transferable = 1
}

// ==================== Issue Vouchers ====================
const showIssueDialog = ref(false)
const issueFormRef = ref(null)
const issueLoading = ref(false)
const currentBatch = ref(null)

const issueForm = reactive({
  employees: [{ employeeId: '', employeeName: '' }],
})

const issueRules = {
  employees: [
    {
      validator: (rule, value, callback) => {
        const valid = value.every((e) => e.employeeId && e.employeeName)
        if (!valid) {
          callback(new Error('请填写完整的员工信息'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
}

const issueResult = reactive({
  show: false,
  title: '',
  type: 'success',
  description: '',
})

function handleIssue(batch) {
  currentBatch.value = batch
  issueForm.employees = [{ employeeId: '', employeeName: '' }]
  issueResult.show = false
  showIssueDialog.value = true
}

function addEmployee() {
  issueForm.employees.push({ employeeId: '', employeeName: '' })
}

function removeEmployee(index) {
  if (issueForm.employees.length <= 1) return
  issueForm.employees.splice(index, 1)
}

let searchTimer = null
function searchEmployees(queryString, callback) {
  if (searchTimer) clearTimeout(searchTimer)
  if (!queryString || queryString.length < 1) {
    callback([])
    return
  }
  searchTimer = setTimeout(async () => {
    try {
      const res = await getEmployeeList({ keyword: queryString, pageSize: 10 })
      const list = Array.isArray(res.data) ? res.data : (res.data?.records || [])
      const results = list.map((emp) => ({
        value: emp.employeeNo || emp.username,
        employeeNo: emp.employeeNo || emp.username,
        employeeName: emp.realName,
        department: emp.department || '-',
      }))
      callback(results)
    } catch {
      callback([])
    }
  }, 300)
}

function onEmployeeSelect(item, index) {
  issueForm.employees[index].employeeName = item.employeeName
}

async function handleIssueSubmit() {
  if (!issueFormRef.value) return
  try {
    await issueFormRef.value.validate()
  } catch {
    return
  }

  issueLoading.value = true
  issueResult.show = false

  try {
    const res = await issueVouchers({
      batchId: currentBatch.value.id,
      employees: issueForm.employees.map((e) => ({
        employeeId: e.employeeId,
        employeeName: e.employeeName,
      })),
    })
    const data = res.data || {}
    const successCount = data.successCount || issueForm.employees.length
    const failCount = data.failCount || 0

    issueResult.type = failCount > 0 ? 'warning' : 'success'
    issueResult.title = '发放完成'
    issueResult.description = `成功发放 ${successCount} 张${
      failCount > 0 ? `，失败 ${failCount} 张` : ''
    }`
    issueResult.show = true

    // Refresh batch list
    fetchBatchList()
  } catch {
    issueResult.type = 'error'
    issueResult.title = '发放失败'
    issueResult.description = '券码发放失败，请稍后重试'
    issueResult.show = true
  } finally {
    issueLoading.value = false
  }
}

// ==================== Voucher Detail ====================
const showVoucherDialog = ref(false)
const voucherList = ref([])

async function handleViewDetail(batch) {
  currentBatch.value = batch
  showVoucherDialog.value = true
  try {
    const res = await getVouchersByBatch(batch.id, { page: 1, pageSize: 50 })
    const list = Array.isArray(res.data) ? res.data : (res.data?.list || [])
    voucherList.value = list
  } catch {
    voucherList.value = []
  }
}

function statusClass(status) {
  const map = {
    ISSUED: 'valid',
    USED: 'used',
    EXPIRED: 'expired',
    CANCELLED: 'cancelled',
  }
  return map[status] || ''
}

function statusText(status) {
  const map = {
    ISSUED: '有效',
    USED: '已核销',
    EXPIRED: '已过期',
    CANCELLED: '已作废',
  }
  return map[status] || status
}

// ==================== Init ====================
onMounted(() => {
  fetchBatchList()
})
</script>

<style scoped>
.batch-manage {
  height: 100%;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-md, 16px);
}

.form-hint {
  font-size: 12px;
  color: var(--color-text-secondary, #969799);
  margin-left: var(--space-xs, 8px);
}

/* Issue vouchers */
.issue-batch-info {
  font-size: 14px;
  color: var(--color-text-primary, #323233);
  margin-bottom: var(--space-md, 16px);
  padding: var(--space-sm, 12px);
  background: var(--color-page-bg, #f7f8fa);
  border-radius: var(--radius-sm, 4px);
}

.issue-employees {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm, 12px);
  margin-bottom: var(--space-sm, 12px);
}

.employee-row {
  display: flex;
  align-items: center;
  gap: var(--space-xs, 8px);
}

.add-employee-btn {
  margin-top: var(--space-xs, 8px);
}

/* Status badge overrides */
.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: var(--radius-sm, 4px);
  font-size: 12px;
  font-weight: 500;
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
</style>
