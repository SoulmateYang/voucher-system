<template>
  <el-container class="layout-container">
    <!-- Sidebar -->
    <el-aside width="200px" class="sidebar">
      <div class="sidebar-logo">
        <div class="logo-icon">
          <el-icon :size="22"><Coin /></el-icon>
        </div>
        <span class="logo-text">因私卡券</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :router="true"
        background-color="#0a1628"
        text-color="#ffffff"
        active-text-color="#1989fa"
        class="sidebar-menu"
      >
        <el-menu-item index="/batch">
          <el-icon><Wallet /></el-icon>
          <template #title>卡券管理</template>
        </el-menu-item>
        <el-menu-item index="/verify">
          <el-icon><Select /></el-icon>
          <template #title>核销台</template>
        </el-menu-item>
        <el-menu-item index="/reports">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>统计报表</template>
        </el-menu-item>
        <el-menu-item index="/employee-vouchers">
          <el-icon><Collection /></el-icon>
          <template #title>员工卡券</template>
        </el-menu-item>
        <el-menu-item index="/employees">
          <el-icon><User /></el-icon>
          <template #title>员工管理</template>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <el-menu
          :router="true"
          background-color="#0a1628"
          text-color="#ffffff"
          active-text-color="#1989fa"
        >
          <el-menu-item index="/settings" disabled>
            <el-icon><Setting /></el-icon>
          </el-menu-item>
        </el-menu>
      </div>
    </el-aside>

    <!-- Main area -->
    <el-container>
      <!-- Top bar -->
      <el-header class="top-bar">
        <span class="product-name">因私卡券管理系统</span>
        <div class="user-area">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-trigger">
              <el-icon :size="16"><UserFilled /></el-icon>
              <span class="user-name">{{ userDisplayName }}</span>
              <el-icon :size="12"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- Content -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Coin,
  Wallet,
  Select,
  DataAnalysis,
  Collection,
  Setting,
  UserFilled,
  User,
  ArrowDown,
  SwitchButton,
} from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const activeMenu = computed(() => {
  // Match the first segment of the path
  const path = route.path
  if (path.startsWith('/batch')) return '/batch'
  if (path.startsWith('/verify')) return '/verify'
  if (path.startsWith('/reports')) return '/reports'
  if (path.startsWith('/employee-vouchers')) return '/employee-vouchers'
  if (path.startsWith('/employees')) return '/employees'
  return '/batch'
})

const userDisplayName = computed(() => {
  return authStore.state.user?.username || '管理员'
})

function handleCommand(command) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定退出登录吗？', '退出确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    })
      .then(() => {
        authStore.logout()
        router.push('/login')
      })
      .catch(() => {
        // cancelled
      })
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  display: flex;
}

/* Sidebar */
.sidebar {
  background-color: #0a1628;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: var(--space-xs, 8px);
  padding: var(--space-md, 16px);
  color: #ffffff;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  min-height: 56px;
}

.logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: rgba(25, 137, 250, 0.15);
  border-radius: var(--radius-sm, 4px);
  color: #1989fa;
}

.logo-text {
  font-size: 16px;
  letter-spacing: 1px;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  padding-top: var(--space-xs, 8px);
}

.sidebar-menu .el-menu-item {
  height: 44px;
  line-height: 44px;
  margin: 2px var(--space-xs, 8px);
  border-radius: var(--radius-sm, 4px);
}

.sidebar-menu .el-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.06) !important;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: rgba(25, 137, 250, 0.12) !important;
}

.sidebar-footer {
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.sidebar-footer .el-menu {
  border-right: none;
}

.sidebar-footer .el-menu-item {
  justify-content: center;
  height: 44px;
}

/* Top bar */
.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  background: #ffffff;
  border-bottom: 1px solid var(--color-border, #ebedf0);
  padding: 0 var(--space-lg, 24px);
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, #323233);
}

.user-area {
  display: flex;
  align-items: center;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: var(--space-xs, 8px);
  cursor: pointer;
  padding: var(--space-2xs, 4px) var(--space-xs, 8px);
  border-radius: var(--radius-sm, 4px);
  color: var(--color-text-secondary, #969799);
  font-size: 14px;
  transition: background-color 0.15s;
}

.user-trigger:hover {
  background-color: var(--color-border-light, #f5f5f5);
  color: var(--color-text-primary, #323233);
}

.user-name {
  color: var(--color-text-primary, #323233);
  font-weight: 500;
}

/* Main content */
.main-content {
  background-color: var(--color-page-bg, #f7f8fa);
  padding: var(--space-lg, 24px);
  overflow-y: auto;
}
</style>
