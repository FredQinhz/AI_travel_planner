import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/pages/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/pages/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/trips/create',
    name: 'TripCreate',
    component: () => import('@/pages/TripCreate.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/trips/:id',
    name: 'TripDetail',
    component: () => import('@/pages/TripDetail.vue'),
    meta: { requiresAuth: true },
    props: true
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 路由守卫
router.beforeEach((to, _, next) => {
  const authStore = useAuthStore();
  
  // 先从本地加载认证状态
  authStore.loadFromLocal();
  
  const requiresAuth = to.meta.requiresAuth !== false;
  
  if (requiresAuth && !authStore.isAuthenticated) {
    // 需要认证但未登录，重定向到登录页
    next('/login');
  } else if (!requiresAuth && authStore.isAuthenticated && to.path === '/login') {
    // 已登录且访问登录页，重定向到仪表盘
    next('/dashboard');
  } else {
    next();
  }
});

export default router;