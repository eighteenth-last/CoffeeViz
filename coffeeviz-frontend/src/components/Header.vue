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
        <!-- 通知铃铛 -->
        <div class="relative" ref="bellRef">
          <div @click="toggleDropdown" class="relative p-2 text-neutral-500 hover:text-amber-500 transition-colors cursor-pointer">
            <i class="fas fa-bell text-lg"></i>
            <span v-if="unreadCount > 0" class="absolute -top-0.5 -right-0.5 w-4 h-4 bg-amber-500 rounded-full text-[9px] font-bold text-black flex items-center justify-center">
              {{ unreadCount > 9 ? '9+' : unreadCount }}
            </span>
          </div>

          <!-- 下拉通知面板 -->
          <transition name="fade">
            <div v-if="showDropdown" class="absolute right-0 top-12 w-80 bg-[#111] border border-neutral-800 rounded-xl shadow-2xl overflow-hidden z-50">
              <div class="flex items-center justify-between px-4 py-3 border-b border-neutral-800">
                <span class="text-sm font-bold text-white">通知</span>
                <span v-if="unreadCount > 0" @click="markAllRead" class="text-[10px] text-amber-500 cursor-pointer hover:text-amber-400 transition-colors">全部已读</span>
              </div>
              <div v-if="notifyLoading" class="py-8 text-center text-neutral-600">
                <i class="fas fa-spinner fa-spin"></i>
              </div>
              <div v-else-if="notifications.length === 0" class="py-8 text-center text-neutral-600 text-xs">
                <i class="fas fa-bell-slash text-lg mb-2 block"></i>暂无通知
              </div>
              <div v-else class="max-h-80 overflow-y-auto custom-scrollbar">
                <div v-for="item in notifications" :key="item.id"
                  @click="openDetail(item)"
                  :class="['px-4 py-3 cursor-pointer transition-all border-b border-neutral-800/50 last:border-0',
                    item.read ? 'hover:bg-neutral-900/50' : 'bg-amber-500/5 hover:bg-amber-500/10']">
                  <div class="flex items-start space-x-2.5">
                    <div :class="['w-1.5 h-1.5 rounded-full mt-1.5 flex-shrink-0', item.read ? 'bg-neutral-700' : 'bg-amber-500']"></div>
                    <div class="flex-1 min-w-0">
                      <div class="flex items-center justify-between">
                        <span :class="['text-xs font-semibold truncate', item.read ? 'text-neutral-400' : 'text-white']">{{ item.title }}</span>
                        <span class="text-[9px] text-neutral-600 flex-shrink-0 ml-2">{{ formatTime(item.time) }}</span>
                      </div>
                      <p class="text-[11px] text-neutral-500 mt-0.5 truncate">{{ item.content }}</p>
                    </div>
                  </div>
                </div>
              </div>
              <div v-if="notifications.length > 0" class="border-t border-neutral-800 px-4 py-2.5 text-center">
                <span v-if="unreadCount > 0" @click="markAllRead" class="text-[10px] text-neutral-500 hover:text-amber-500 transition-colors cursor-pointer">全部标为已读</span>
              </div>
            </div>
          </transition>
        </div>

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

    <!-- 通知详情弹窗 -->
    <Teleport to="body">
      <transition name="fade">
        <div v-if="detailItem" class="fixed inset-0 bg-black/60 backdrop-blur-sm z-[100] flex items-center justify-center" @click.self="detailItem = null">
          <div class="bg-[#111] border border-neutral-800 rounded-2xl w-[480px] max-h-[80vh] overflow-hidden shadow-2xl">
            <div class="flex items-center justify-between px-6 py-4 border-b border-neutral-800">
              <h3 class="text-sm font-bold text-white truncate pr-4">{{ detailItem.title }}</h3>
              <button @click="detailItem = null" class="text-neutral-500 hover:text-white transition-colors flex-shrink-0">
                <i class="fas fa-times"></i>
              </button>
            </div>
            <div class="px-6 py-5 overflow-y-auto max-h-[60vh]">
              <p class="text-sm text-neutral-300 leading-relaxed whitespace-pre-wrap">{{ detailItem.content }}</p>
            </div>
            <div class="px-6 py-3 border-t border-neutral-800 flex items-center justify-between">
              <span class="text-[10px] text-neutral-600">{{ formatTime(detailItem.time) }}</span>
              <button @click="detailItem = null" class="px-4 py-1.5 bg-amber-600/10 border border-amber-600/30 rounded-lg text-[11px] font-bold text-amber-500 hover:bg-amber-600 hover:text-white transition-all">
                关闭
              </button>
            </div>
          </div>
        </div>
      </transition>
      <ProfileModal v-if="showProfileModal" @close="showProfileModal = false" />
    </Teleport>
  </header>
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import ProfileModal from '@/components/ProfileModal.vue'
import api from '@/api/index.js'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const showProfileModal = ref(false)
const unreadCount = ref(0)
const showDropdown = ref(false)
const notifications = ref([])
const notifyLoading = ref(false)
const detailItem = ref(null)
const bellRef = ref(null)

const currentTitle = computed(() => {
  const name = route.name
  const map = {
    'dashboard': '仪表盘',
    'sql-import': 'SQL 导入',
    'db-connect': '数据库连接',
    'ai-generate': 'AI 生成',
    'projects': '项目管理',
    'settings': '系统设置',
    'subscribe': '订阅计划',
    'notifications': '站内通知'
  }
  return map[name] || route.meta?.title || '未知页面'
})

const loadUnreadCount = async () => {
  try {
    const res = await api.get('/api/notifications/unread-count')
    unreadCount.value = res.data
  } catch (e) { /* 静默 */ }
}

const loadNotifications = async () => {
  notifyLoading.value = true
  try {
    const res = await api.get('/api/notifications', { params: { page: 1, size: 10 } })
    notifications.value = res.data.records
  } catch (e) {
    console.error('加载通知失败', e)
  } finally {
    notifyLoading.value = false
  }
}

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
  if (showDropdown.value) {
    loadNotifications()
  }
}

const openDetail = async (item) => {
  detailItem.value = item
  showDropdown.value = false
  if (!item.read) {
    try {
      await api.post(`/api/notifications/${item.id}/read`)
      item.read = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (e) { /* 静默 */ }
  }
}

const markAllRead = async () => {
  try {
    await api.post('/api/notifications/read-all')
    notifications.value.forEach(n => n.read = true)
    unreadCount.value = 0
  } catch (e) { /* 静默 */ }
}

const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return d.toLocaleDateString('zh-CN')
}

// 点击外部关闭下拉
const handleClickOutside = (e) => {
  if (bellRef.value && !bellRef.value.contains(e.target)) {
    showDropdown.value = false
  }
}

const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}

let timer = null
onMounted(() => {
  loadUnreadCount()
  timer = setInterval(loadUnreadCount, 60000)
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.15s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
