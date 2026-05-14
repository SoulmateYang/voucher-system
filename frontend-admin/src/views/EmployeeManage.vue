<template>
  <div class="employee-manage">
    <div class="page-header">
      <h2>员工管理</h2>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        新增员工
      </el-button>
    </div>

    <!-- Search bar -->
    <div class="card search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索姓名、工号、手机号、部门"
        clearable
        style="width: 320px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <!-- Employee table -->
    <div class="card">
      <el-table
        :data="employeeList"
        stripe
        v-loading="tableLoading"
        empty-text="暂无员工数据"
        style="width: 100%"
      >
        <el-table-column prop="employeeNo" label="工号" width="120" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="mobile" label="手机号" width="140">
          <template #default="{ row }">
            {{ row.mobile || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="department" label="部门" min-width="160">
          <template #default="{ row }">
            {{ row.department || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEditDialog(row)">
              编辑
            </el-button>
            <el-button type="warning" link size="small" @click="handleResetPassword(row)">
              重置密码
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              删除
            </el-button>
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
          @current-change="fetchEmployeeList"
        />
      </div>
    </div>

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="showFormDialog"
      :title="isEdit ? '编辑员工' : '新增员工'"
      width="480px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-position="top"
      >
        <el-form-item label="工号" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入工号（即登录账号）"
            :disabled="isEdit"
            maxlength="32"
          />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" maxlength="20" />
        </el-form-item>
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="form.mobile" placeholder="请输入手机号（选填）" maxlength="16" />
        </el-form-item>
        <el-form-item label="部门" prop="department">
          <el-input v-model="form.department" placeholder="请输入部门（选填）" maxlength="64" />
        </el-form-item>

        <!-- Initial password display for new employees -->
        <el-alert
          v-if="initialPassword"
          title="员工创建成功"
          type="success"
          :closable="false"
          show-icon
          class="password-alert"
        >
          <template #default>
            <p>初始密码：<strong class="password-text">{{ initialPassword }}</strong></p>
            <p class="password-hint">请将该密码线下告知员工，此密码仅显示一次</p>
          </template>
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="showFormDialog = false">取消</el-button>
        <el-button v-if="!initialPassword" type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- Reset Password Result Dialog -->
    <el-dialog
      v-model="showPasswordResult"
      title="密码重置成功"
      width="420px"
      :close-on-click-modal="false"
    >
      <el-alert type="success" :closable="false" show-icon>
        <template #default>
          <p>员工 <strong>{{ resetTarget?.realName }}</strong> 的密码已重置</p>
          <p>新密码：<strong class="password-text">{{ newPassword }}</strong></p>
          <p class="password-hint">请将该密码线下告知员工，此密码仅显示一次</p>
        </template>
      </el-alert>
      <template #footer>
        <el-button type="primary" @click="showPasswordResult = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getEmployeeList,
  createEmployee,
  updateEmployee,
  deleteEmployee,
  resetPassword,
} from '../api/employee'

// ==================== Employee List ====================
const employeeList = ref([])
const tableLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')

async function fetchEmployeeList() {
  tableLoading.value = true
  try {
    const res = await getEmployeeList({
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
    })
    const data = res.data
    const list = data?.records || data?.list || []
    employeeList.value = list
    total.value = data?.total || 0
  } catch {
    employeeList.value = []
  } finally {
    tableLoading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchEmployeeList()
}

// ==================== Create / Edit ====================
const showFormDialog = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const editId = ref(null)
const submitLoading = ref(false)
const initialPassword = ref('')

const form = reactive({
  username: '',
  realName: '',
  mobile: '',
  department: '',
})

const formRules = {
  username: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
}

function openCreateDialog() {
  isEdit.value = false
  editId.value = null
  initialPassword.value = ''
  form.username = ''
  form.realName = ''
  form.mobile = ''
  form.department = ''
  showFormDialog.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  editId.value = row.id
  initialPassword.value = ''
  form.username = row.employeeNo || row.username || ''
  form.realName = row.realName || ''
  form.mobile = row.mobile || ''
  form.department = row.department || ''
  showFormDialog.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateEmployee(editId.value, {
        realName: form.realName,
        mobile: form.mobile || undefined,
        department: form.department || undefined,
      })
      ElMessage.success('保存成功')
      showFormDialog.value = false
      fetchEmployeeList()
    } else {
      const res = await createEmployee({
        username: form.username,
        realName: form.realName,
        mobile: form.mobile || undefined,
        department: form.department || undefined,
      })
      initialPassword.value = res.data?.initialPassword || ''
      ElMessage.success('员工创建成功')
      fetchEmployeeList()
    }
  } catch {
    // Error handled by interceptor
  } finally {
    submitLoading.value = false
  }
}

// ==================== Delete ====================
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确认删除员工「${row.realName}」吗？删除后该员工将无法登录系统。`,
      '删除确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }

  try {
    await deleteEmployee(row.id)
    ElMessage.success('已删除')
    fetchEmployeeList()
  } catch {
    // Error handled by interceptor
  }
}

// ==================== Reset Password ====================
const showPasswordResult = ref(false)
const newPassword = ref('')
const resetTarget = ref(null)

async function handleResetPassword(row) {
  try {
    await ElMessageBox.confirm(
      `确认重置员工「${row.realName}」的密码？重置后原密码将立即失效。`,
      '重置密码',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }

  try {
    const res = await resetPassword(row.id)
    resetTarget.value = row
    newPassword.value = res.data || ''
    showPasswordResult.value = true
  } catch {
    // Error handled by interceptor
  }
}

// ==================== Utils ====================
function formatDate(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

// ==================== Init ====================
onMounted(() => {
  fetchEmployeeList()
})
</script>

<style scoped>
.employee-manage {
  height: 100%;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: var(--space-sm, 12px);
  margin-bottom: var(--space-md, 16px);
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-md, 16px);
}

.password-alert {
  margin-top: var(--space-sm, 12px);
}

.password-text {
  font-size: 18px;
  letter-spacing: 2px;
  font-family: 'Courier New', monospace;
  color: #323233;
}

.password-hint {
  margin-top: var(--space-2xs, 4px);
  font-size: 12px;
  color: var(--color-text-secondary, #969799);
}
</style>
