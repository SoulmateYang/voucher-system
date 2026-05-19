import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/batch',
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('../views/Layout.vue'),
    redirect: '/batch',
    children: [
      {
        path: 'batch',
        name: 'BatchManage',
        component: () => import('../views/BatchManage.vue'),
        meta: { title: '卡券管理' },
      },
      {
        path: 'verify',
        name: 'VerifyDesk',
        component: () => import('../views/VerifyDesk.vue'),
        meta: { title: '核销台' },
      },
      {
        path: 'reports',
        name: 'Reports',
        component: () => import('../views/Reports.vue'),
        meta: { title: '统计报表' },
      },
      {
        path: 'employee-vouchers',
        name: 'EmployeeVouchers',
        component: () => import('../views/EmployeeVouchers.vue'),
        meta: { title: '员工卡券' },
      },
      {
        path: 'employees',
        name: 'EmployeeManage',
        component: () => import('../views/EmployeeManage.vue'),
        meta: { title: '员工管理' },
      },
      {
        path: 'categories',
        name: 'CategoryManage',
        component: () => import('../views/CategoryManage.vue'),
        meta: { title: '分类管理' },
      },
      {
        path: 'consumption/:voucherId',
        name: 'ConsumptionHistory',
        component: () => import('../views/ConsumptionHistory.vue'),
        meta: { title: '消费明细' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Navigation guard — protect routes
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 因私卡券管理系统` : '因私卡券管理系统'

  const token = localStorage.getItem('admin_token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
