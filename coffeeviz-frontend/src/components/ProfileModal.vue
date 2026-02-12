<template>
  <div class="fixed inset-0 z-[100] flex items-center justify-center p-4">
    <!-- Backdrop -->
    <div class="absolute inset-0 bg-black/80 backdrop-blur-sm transition-opacity" @click="$emit('close')"></div>
    
    <!-- Modal Content -->
    <div class="relative z-10 w-full max-w-4xl max-h-[85vh] overflow-y-auto custom-scrollbar glass-card rounded-2xl border border-neutral-800 shadow-2xl animate-in fade-in zoom-in duration-200 bg-[#050505]">
      
      <!-- Close Button -->
      <button @click="$emit('close')" class="absolute top-4 right-4 z-20 w-8 h-8 flex items-center justify-center rounded-full bg-black/50 hover:bg-neutral-800 text-neutral-400 hover:text-white transition-all border border-neutral-700">
        <i class="fas fa-times"></i>
      </button>

      <div class="p-6 md:p-8">
        <!-- Profile Header -->
        <div class="glass-card p-6 md:p-8 rounded-2xl mb-6 flex flex-col md:flex-row items-center md:items-start gap-6 md:gap-8 relative overflow-hidden">
          <div class="absolute top-0 left-0 w-full h-32 bg-gradient-to-r from-amber-600/20 to-purple-600/20"></div>
          
          <div class="relative z-10 w-28 h-28 md:w-32 md:h-32 rounded-2xl bg-[#0f0f0f] p-1 border-2 border-amber-600/30 shadow-2xl flex-shrink-0">
            <img :src="userStore.userInfo?.avatarUrl || `https://api.dicebear.com/7.x/pixel-art/svg?seed=${userStore.displayName || 'User'}`" 
                 alt="avatar" class="w-full h-full rounded-xl object-cover bg-neutral-900">
            <div class="absolute -bottom-2 -right-2 w-7 h-7 md:w-8 md:h-8 bg-green-500 rounded-full border-4 border-[#0f0f0f] flex items-center justify-center" title="Online">
              <div class="w-2 h-2 bg-white rounded-full animate-pulse"></div>
            </div>
          </div>

          <div class="relative z-10 flex-1 text-center md:text-left mt-2 md:mt-6">
            <div class="flex flex-col md:flex-row justify-between items-center gap-4">
              <div class="w-full md:w-auto">
                <div class="flex items-center justify-center md:justify-start gap-2 mb-1">
                  <h1 class="text-2xl md:text-3xl font-black text-white tracking-tight break-words">{{ userStore.displayName || 'User' }}</h1>
                  <span 
                    class="px-2 py-0.5 rounded-md text-[10px] font-black tracking-wider"
                    :class="{
                      'bg-amber-600/20 text-amber-500 border border-amber-600/30': subscriptionInfo.color === 'amber',
                      'bg-purple-600/20 text-purple-500 border border-purple-600/30': subscriptionInfo.color === 'purple',
                      'bg-neutral-800 text-neutral-400 border border-neutral-700': subscriptionInfo.color === 'neutral'
                    }">
                    {{ subscriptionInfo.badge }}
                  </span>
                </div>
                <p class="text-neutral-500 font-mono text-xs md:text-sm break-all">@{{ userStore.username || 'user' }}</p>
              </div>
            </div>
            
            <div class="mt-4 md:mt-6 flex flex-col md:flex-row flex-wrap justify-center md:justify-start gap-3 md:gap-6">
              <div v-if="userStore.userInfo?.email" class="flex items-center text-neutral-400 text-xs md:text-sm">
                <i class="fas fa-envelope mr-2 text-amber-500 flex-shrink-0"></i>
                <span class="break-all">{{ userStore.userInfo.email }}</span>
              </div>
              <div v-if="userStore.userInfo?.phone" class="flex items-center text-neutral-400 text-xs md:text-sm">
                <i class="fas fa-phone mr-2 text-amber-500 flex-shrink-0"></i>
                <span>{{ userStore.userInfo.phone }}</span>
              </div>
              <div v-if="!userStore.userInfo?.email && !userStore.userInfo?.phone" class="flex items-center text-neutral-500 text-xs md:text-sm italic">
                <i class="fas fa-info-circle mr-2 flex-shrink-0"></i>
                <span>暂无联系方式</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Subscription Info -->
        <div class="glass-card p-6 rounded-2xl mb-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-bold text-white flex items-center">
              <i class="fas fa-crown text-amber-500 mr-2"></i> 订阅信息
            </h3>
            <button 
              v-if="subscriptionStore.planCode === 'FREE'"
              @click="goToSubscribe"
              class="px-3 py-1.5 bg-amber-600 hover:bg-amber-700 rounded-lg text-xs font-bold text-white transition-all">
              升级
            </button>
          </div>
          
          <div class="flex items-center justify-between mb-6 p-4 rounded-xl"
               :class="{
                 'bg-amber-600/10 border border-amber-600/20': subscriptionInfo.color === 'amber',
                 'bg-purple-600/10 border border-purple-600/20': subscriptionInfo.color === 'purple',
                 'bg-neutral-800/50 border border-neutral-700': subscriptionInfo.color === 'neutral'
               }">
            <div class="flex items-center gap-3">
              <div 
                class="w-12 h-12 rounded-xl flex items-center justify-center"
                :class="{
                  'bg-amber-600/20 text-amber-500': subscriptionInfo.color === 'amber',
                  'bg-purple-600/20 text-purple-500': subscriptionInfo.color === 'purple',
                  'bg-neutral-700 text-neutral-400': subscriptionInfo.color === 'neutral'
                }">
                <i class="fas text-lg" :class="subscriptionInfo.icon"></i>
              </div>
              <div>
                <div class="text-base font-bold text-white">{{ subscriptionInfo.name }}</div>
                <div class="text-xs text-neutral-400">{{ expiresText }}</div>
              </div>
            </div>
            <div v-if="subscriptionStore.isExpired" class="px-2 py-1 bg-red-600/20 border border-red-600/30 rounded-md">
              <span class="text-xs font-bold text-red-500">已过期</span>
            </div>
          </div>
          
          <!-- Quota Stats -->
          <div class="space-y-3">
            <div v-for="quota in quotaStats" :key="quota.name" class="flex items-center justify-between">
              <div class="flex items-center gap-2 flex-1">
                <i class="fas text-sm w-4" :class="[`fa-${quota.icon}`, `text-${quota.color}-500`]"></i>
                <span class="text-sm text-neutral-300">{{ quota.name }}</span>
              </div>
              <div class="flex items-center gap-3 flex-1 justify-end">
                <div class="w-24 bg-neutral-800 rounded-full h-1.5">
                  <div 
                    class="h-1.5 rounded-full transition-all"
                    :class="`bg-${quota.color}-500`"
                    :style="{ width: quota.percent + '%' }">
                  </div>
                </div>
                <span class="text-xs font-mono text-neutral-400 w-16 text-right">
                  {{ quota.used }}/{{ quota.limit === -1 ? '∞' : quota.limit }}
                </span>
              </div>
            </div>
          </div>
          
          <div class="mt-4 pt-4 border-t border-neutral-800">
            <div class="text-[10px] text-neutral-500 space-y-1">
              <div class="flex items-center gap-2">
                <i class="fas fa-info-circle"></i>
                <span>配额重置周期：架构库永不重置，架构图和AI生成每月重置，SQL解析每月重置</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Quick Actions -->
        <div class="grid grid-cols-2 gap-4">
          <button @click="goToSettings" class="glass-card p-4 rounded-xl hover:border-amber-600/50 transition-all group">
            <div class="flex items-center justify-center md:justify-start gap-3">
              <div class="w-10 h-10 rounded-lg bg-amber-600/10 flex items-center justify-center text-amber-500 group-hover:bg-amber-600/20 transition-all">
                <i class="fas fa-cog"></i>
              </div>
              <div class="hidden md:block text-left">
                <div class="text-sm font-bold text-white">设置</div>
                <div class="text-xs text-neutral-500">个人资料</div>
              </div>
            </div>
          </button>
          
          <button @click="handleLogout" class="glass-card p-4 rounded-xl hover:border-red-600/50 transition-all group">
            <div class="flex items-center justify-center md:justify-start gap-3">
              <div class="w-10 h-10 rounded-lg bg-red-600/10 flex items-center justify-center text-red-500 group-hover:bg-red-600/20 transition-all">
                <i class="fas fa-sign-out-alt"></i>
              </div>
              <div class="hidden md:block text-left">
                <div class="text-sm font-bold text-white">退出</div>
                <div class="text-xs text-neutral-500">登出账号</div>
              </div>
            </div>
          </button>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useSubscriptionStore } from '@/store/subscription'

const router = useRouter()
const userStore = useUserStore()
const subscriptionStore = useSubscriptionStore()

const emit = defineEmits(['close'])

// 订阅信息计算属性
const subscriptionInfo = computed(() => {
  const sub = subscriptionStore.currentSubscription
  if (!sub) return { name: 'Free', icon: 'fa-coffee', color: 'neutral', badge: 'FREE' }
  
  const planMap = {
    'FREE': { name: 'Coffee Free', icon: 'fa-coffee', color: 'neutral', badge: 'FREE' },
    'PRO': { name: 'Coffee Pro', icon: 'fa-crown', color: 'amber', badge: 'PRO' },
    'TEAM': { name: 'Coffee Team', icon: 'fa-users', color: 'purple', badge: 'TEAM' }
  }
  
  return planMap[sub.planCode] || planMap['FREE']
})

const expiresText = computed(() => {
  const expiresAt = subscriptionStore.expiresAt
  if (!expiresAt) return '永久有效'
  
  const date = new Date(expiresAt)
  const now = new Date()
  const diffDays = Math.ceil((date - now) / (1000 * 60 * 60 * 24))
  
  if (diffDays < 0) return '已过期'
  if (diffDays === 0) return '今天到期'
  if (diffDays <= 30) return `${diffDays} 天后到期`
  
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
})

// 配额信息
const quotaStats = computed(() => {
  const repo = subscriptionStore.repositoryQuota
  const diagram = subscriptionStore.diagramQuota
  const sqlParse = subscriptionStore.sqlParseQuota
  const aiGen = subscriptionStore.aiGenerateQuota
  
  return [
    {
      name: '架构库',
      icon: 'fa-folder',
      color: 'blue',
      used: repo?.quotaUsed || 0,
      limit: repo?.quotaLimit || 0,
      percent: subscriptionStore.repositoryUsagePercent,
      resetCycle: '永不重置'
    },
    {
      name: '架构图',
      icon: 'fa-diagram-project',
      color: 'amber',
      used: diagram?.quotaUsed || 0,
      limit: diagram?.quotaLimit || 0,
      percent: subscriptionStore.diagramUsagePercent,
      resetCycle: '每月重置'
    },
    {
      name: 'SQL解析',
      icon: 'fa-code',
      color: 'purple',
      used: sqlParse?.quotaUsed || 0,
      limit: sqlParse?.quotaLimit || 0,
      percent: subscriptionStore.sqlParseUsagePercent,
      resetCycle: '每日重置'
    },
    {
      name: 'AI生成',
      icon: 'fa-magic',
      color: 'green',
      used: aiGen?.quotaUsed || 0,
      limit: aiGen?.quotaLimit || 0,
      percent: subscriptionStore.aiGenerateUsagePercent,
      resetCycle: '每月重置'
    }
  ]
})

// 跳转到设置页面
const goToSettings = () => {
  emit('close')
  router.push('/settings')
}

// 跳转到订阅页面
const goToSubscribe = () => {
  emit('close')
  router.push('/subscribe')
}

// 退出登录
const handleLogout = async () => {
  emit('close')
  await userStore.logout()
  router.push('/login')
}

// Prevent body scroll when modal is open
onMounted(async () => {
  document.body.style.overflow = 'hidden'
  
  // 加载订阅信息
  try {
    await Promise.all([
      subscriptionStore.fetchCurrentSubscription(),
      subscriptionStore.fetchQuotas()
    ])
  } catch (error) {
    console.error('Failed to load subscription info', error)
  }
})

onUnmounted(() => {
  document.body.style.overflow = ''
})
</script>

<style scoped>
/* Ensure the modal content inherits the correct styles */
.glass-card {
  background: linear-gradient(145deg, rgba(25, 25, 25, 0.9) 0%, rgba(10, 10, 10, 0.9) 100%);
  border: 1px solid var(--border-muted, rgba(255, 255, 255, 0.08));
  backdrop-filter: blur(12px);
}

/* Custom scrollbar */
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(245, 158, 11, 0.3);
  border-radius: 4px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(245, 158, 11, 0.5);
}

/* Animation */
@keyframes fade-in {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes zoom-in {
  from {
    transform: scale(0.95);
  }
  to {
    transform: scale(1);
  }
}

.animate-in {
  animation: fade-in 0.2s ease-out, zoom-in 0.2s ease-out;
}
</style>