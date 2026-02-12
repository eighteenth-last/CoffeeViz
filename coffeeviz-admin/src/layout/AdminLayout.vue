<template>
  <div class="h-screen flex overflow-hidden text-sm">
    <!-- Sidebar -->
    <aside class="w-64 bg-bg-card border-r border-white/5 flex flex-col flex-shrink-0">
      <!-- Logo -->
      <div class="h-16 flex items-center px-6 border-b border-white/5">
        <div class="flex items-center gap-3">
          <img src="/logo.png" alt="CoffeeViz" class="w-8 h-8 object-contain" />
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
        <template v-for="item in systemMenus" :key="item.path || item.label">
          <!-- Leaf Node -->
          <router-link
            v-if="!item.children"
            :to="item.path"
            :class="['group flex items-center px-6 py-3 transition-all', isActive(item.path) ? 'bg-bg-input border-l-[3px] border-primary-500 text-white' : 'text-gray-300 hover:bg-bg-hover hover:text-white border-l-[3px] border-transparent']"
          >
            <n-icon :size="16" class="mr-3"><component :is="item.icon" /></n-icon>
            {{ item.label }}
          </router-link>

          <!-- Parent Node -->
          <div v-else>
            <div
              @click="toggleMenu(item.label)"
              class="group flex items-center px-6 py-3 cursor-pointer text-gray-300 hover:bg-bg-hover hover:text-white border-l-[3px] border-transparent transition-all"
            >
              <n-icon :size="16" class="mr-3"><component :is="item.icon" /></n-icon>
              <span class="flex-1">{{ item.label }}</span>
              <n-icon :size="14" class="text-gray-500">
                <component :is="expandedMenus.includes(item.label) ? ChevronDownOutline : ChevronForwardOutline" />
              </n-icon>
            </div>
            <!-- Children -->
            <div v-show="expandedMenus.includes(item.label)" class="bg-black/20">
              <router-link
                v-for="child in item.children" :key="child.path"
                :to="child.path"
                :class="['group flex items-center pl-14 pr-6 py-2.5 text-xs transition-all', isActive(child.path) ? 'text-primary-400 font-medium' : 'text-gray-400 hover:text-white']"
              >
                {{ child.label }}
              </router-link>
            </div>
          </div>
        </template>
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
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import {
  StatsChartOutline, CubeOutline, PeopleOutline,
  PersonOutline, NotificationsOutline, SettingsOutline,
  SearchOutline, LogOutOutline, CardOutline, ChevronDownOutline, ChevronForwardOutline,
  HardwareChipOutline
} from '@vicons/ionicons5'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const expandedMenus = ref(['系统设置']) // Default expand system settings

const toggleMenu = (label) => {
  const index = expandedMenus.value.indexOf(label)
  if (index > -1) {
    expandedMenus.value.splice(index, 1)
  } else {
    expandedMenus.value.push(label)
  }
}

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
  {
    label: '系统设置',
    icon: SettingsOutline,
    children: [
      { path: '/payment-settings', label: '支付配置' },
      { path: '/email-settings', label: '邮件配置' },
      { path: '/settings', label: 'AI 模型配置' }
    ]
  }
]

const currentTitle = computed(() => route.meta.title || 'Dashboard')
const isActive = (path) => route.path === path

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>
