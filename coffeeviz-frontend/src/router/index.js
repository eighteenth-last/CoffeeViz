import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'sql-import',
        name: 'SqlImport',
        component: () => import('@/views/SqlImport.vue'),
        meta: { title: 'SQL 导入' }
      },
      {
        path: 'db-connect',
        name: 'DbConnect',
        component: () => import('@/views/DbConnect.vue'),
        meta: { title: '数据库连接' }
      },
      {
        path: 'ai-generate',
        name: 'AiGenerate',
        component: () => import('@/views/AiGenerate.vue'),
        meta: { title: 'AI 生成' }
      },
      {
        path: 'projects',
        name: 'Projects',
        component: () => import('@/views/Projects.vue'),
        meta: { title: '项目管理' }
      },
      {
        path: 'repository/:id',
        name: 'RepositoryDetail',
        component: () => import('@/views/RepositoryDetail.vue'),
        meta: { title: '架构库详情' }
      },
      {
        path: 'project/:id',
        name: 'ProjectDetail',
        component: () => import('@/views/ProjectDetail.vue'),
        meta: { title: '项目详情' }
      },
      {
        path: 'subscribe',
        name: 'Subscribe',
        component: () => import('@/views/Subscribe.vue'),
        meta: { title: '订阅计划' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue'),
        meta: { title: '设置' }
      }
    ]
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: () => import('@/views/Checkout.vue'),
    meta: { title: '收银台', requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.name === 'Login' && userStore.isLoggedIn) {
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router
