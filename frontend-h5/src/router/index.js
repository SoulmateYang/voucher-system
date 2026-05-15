import { createRouter, createWebHashHistory } from 'vue-router';
import { getToken } from '../api/request';

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/',
    redirect: '/voucher-list',
  },
  {
    path: '/voucher-list',
    name: 'VoucherList',
    component: () => import('../views/VoucherList.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/voucher-detail/:id',
    name: 'VoucherDetail',
    component: () => import('../views/VoucherDetail.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/gift-send/:id',
    name: 'GiftSend',
    component: () => import('../views/GiftSend.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/gift-inbox',
    name: 'GiftInbox',
    component: () => import('../views/GiftInbox.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/gift-outbox',
    name: 'GiftOutbox',
    component: () => import('../views/GiftInbox.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/manual-add',
    name: 'ManualAdd',
    component: () => import('../views/ManualAdd.vue'),
    meta: { requiresAuth: true },
  },
];

const router = createRouter({
  history: createWebHashHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const token = getToken();
  if (to.meta.requiresAuth && !token) {
    next({ name: 'Login' });
  } else if (to.name === 'Login' && token) {
    next({ name: 'VoucherList' });
  } else {
    next();
  }
});

export default router;
