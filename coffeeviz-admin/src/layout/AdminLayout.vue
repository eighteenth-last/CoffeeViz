<template>
  <div class="h-screen flex overflow-hidden text-sm">
    <!-- Sidebar -->
    <aside class="w-64 bg-bg-card border-r border-white/5 flex flex-col flex-shrink-0">
      <!-- Logo -->
      <div class="h-16 flex items-center px-6 border-b border-white/5">
        <div class="flex items-center gap-3">
          <div class="w-8 h-8 rounded bg-gradient-to-br from-blue-500 to-cyan-400 flex items-center justify-center text-white font-bold">C</div>
          <span class="text-lg font-bold tracking-tight text-white">CoffeeViz <span class="text-xs font-normal text-gray-500 ml-1">Admin</span></span>
        </div>
      </div>

      <!-- Navigation -->
      <nav class="flex-1 overflow-y-auto py-4 space-y-1">
        <div class="px-4 mb-2 text-xs font-semibold text-gray-500 uppercase tracking-wider">概览</div>
        <router-link
          v-for="item in overviewMenus" :key="item.path"
          :to="item.path"
          :class="['group flex items-center px-6 py-3 transition-all', isActive(item.path) ? 'bg-bg-input border-l-[3px] border-primary-500 text-white' : 'text-gray-300 hover:bg-bg-hover hover:text-white border-l-[3px] border-transparent']"
        >
          <n-icon :size="16" class="mr-3"><component :is="item.icon" /></n-icon>
          {{ item.label }}
        </router-link>

        <div class="px-4 mt-6 mb-2 text-xs font-semibold text-gray-500 uppercase tracking-wider">业务管理</div>
        <router-link
          v-for="item in businessMenus" :key="item.path"
          :to="item.path"
          :class="['group flex items-center px-6 py-3 transition-all', isActive(item.path) ? 'bg-bg-input border-l-[3px] border-primary-500 text-white' : 'text-gray-300 hover:bg-bg-hover hover:text-white border-l-[3px] border-transparent']"
        >
          <n-icon :size="16" class="mr-3"><component :is="item.icon" /></n-icon>
          {{ item.label }}
        </router-link>

        <div class="px-4 mt-6 mb-2 text-xs font-semibold text-gray-500 uppercase tracking-wider">系统</div>
        <router-link
          v-for="item in systemMenus" :key="item.path"
          :to="item.path"
          :class="['group flex items-center px-6 py-3 transition-all', isActive(item.path) ? 'bg-bg-input border-l-[3px] border-primary-500 text-white' : 'text-gray-300 hover:bg-bg-hover hover:text-white border-l-[3px] border-transparent']"
        >
          <n-icon :size="16" class="mr-3"><component :is="item.icon" /></n-icon>
          {{ item.label }}
        </router-link>
      </nav>

      <!-- User Profile -->
      <div class="border-t border-white/5 p-4">
        <div class="flex items-center gap-3">
          <n-avatar :size="36" round src="https://ui-avatars.com/api/?name=Admin&background=3b82f6&color=fff" />
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-white truncate">{{ authStore.userInfo?.username || 'Admin' }}</p>
            <p class="text-xs text-gray-500 truncate">{{ authStore.userInfo?.email || 'admin@coffeeviz.com' }}</p>
          </div>
          <n-button text @click="handleLogout">
            <n-icon :size="16" class="text-gray-400 hover:text-white"><LogOutOutline /></n-icon>
          </n-button>
        </div>
      </div>
    </aside>

    <!-- Main Content -->
    <main class="flex-1 flex flex-col bg-bg overflow-hidden">
      <!-- Top Header -->
      <header class="h-16 bg-bg-card/50 backdrop-blur-md border-b border-white/5 flex items-center justify-between px-8 sticky top-0 z-10">
        <h2 class="text-xl font-semibold text-white">{{ currentTitle }}</h2>
        <div class="flex items-center gap-4">
          <n-input placeholder="全局搜索..." round size="small" class="w-64">
            <template #prefix><n-icon><SearchOutline /></n-icon></template>
          </n-input>
          <n-badge :value="3" :max="99">
            <n-button text><n-icon :size="18" class="text-gray-400"><NotificationsOutline /></n-icon></n-button>
          </n-badge>
        </div>
      </header>

      <!-- Content -->
      <div class="flex-1 overflow-y-auto p-8">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import {
  StatsChartOutline, CubeOutline, PeopleOutline,
  PersonOutline, NotificationsOutline, SettingsOutline,
  SearchOutline, LogOutOutline
} from '@vicons/ionicons5'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const overviewMenus = [
  { path: '/dashboard', label: '数据可视化', icon: StatsChartOutline }
]
const businessMenus = [
  { path: '/plans', label: '订阅与配额', icon: CubeOutline },
  { path: '/teams', label: '团队管理', icon: PeopleOutline },
  { path: '/users', label: '用户管理', icon: PersonOutline }
]
const systemMenus = [
  { path: '/notifications', label: '消息通知', icon: NotificationsOutline },
  { path: '/settings', label: '系统设置', icon: SettingsOutline }
]

const currentTitle = computed(() => route.meta.title || 'Dashboard')
const isActive = (path) => route.path === path

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>
