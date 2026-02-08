<template>
  <header class="ml-64 h-16 bg-[#050505]/90 backdrop-blur-xl border-b border-neutral-900 flex items-center justify-between px-10 sticky top-0 z-40">
    <div class="flex items-center space-x-4">
      <div id="breadcrumb" class="flex items-center text-xs space-x-2 text-neutral-500 font-medium">
        <span class="hover:text-amber-500 cursor-pointer transition-colors">根目录</span>
        <i class="fas fa-chevron-right text-[8px] opacity-30"></i>
        <span id="current-page-tag" class="text-neutral-200 tracking-widest uppercase">{{ currentTitle }}</span>
      </div>
    </div>

    <div class="flex items-center space-x-6">
      <div class="relative group hidden md:block">
        <i class="fas fa-search absolute left-3 top-1/2 -translate-y-1/2 text-neutral-600 text-xs"></i>
        <input type="text" placeholder="搜索架构、表名、SQL片段..." class="w-64 bg-neutral-900 border border-neutral-800 rounded-full py-1.5 pl-9 pr-4 text-xs outline-none focus:border-amber-600 transition-all text-neutral-300">
      </div>
      <div class="flex items-center space-x-3 border-l border-neutral-800 pl-6">
        <div class="text-right">
          <div class="text-sm font-bold text-white">{{ userStore.displayName || 'User' }}</div>
          <div class="text-[10px] text-neutral-500 font-mono cursor-pointer hover:text-amber-500 transition-colors" @click="handleLogout">切换账号</div>
        </div>
        <div @click="showProfileModal = true" class="w-10 h-10 rounded-xl bg-amber-600 p-[2px] cursor-pointer hover:rotate-3 transition-transform">
          <div class="w-full h-full rounded-[10px] bg-black overflow-hidden">
            <img :src="userStore.userInfo?.avatarUrl || `https://api.dicebear.com/7.x/pixel-art/svg?seed=${userStore.displayName || 'User'}`" alt="avatar">
          </div>
        </div>
      </div>
    </div>

    <Teleport to="body">
      <ProfileModal v-if="showProfileModal" @close="showProfileModal = false" />
    </Teleport>
  </header>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import ProfileModal from '@/components/ProfileModal.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const showProfileModal = ref(false)

const currentTitle = computed(() => {
  const name = route.name
  const map = {
    'dashboard': '仪表盘',
    'sql-import': 'SQL 导入',
    'db-connect': '数据库连接',
    'ai-generate': 'AI 生成',
    'projects': '项目管理',
    'settings': '系统设置',
    'subscribe': '订阅计划'
  }
  return map[name] || route.meta?.title || '未知页面'
})

const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}
</script>
