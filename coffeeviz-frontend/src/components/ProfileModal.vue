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
                <h1 class="text-2xl md:text-3xl font-black text-white tracking-tight mb-1 break-words">{{ userStore.displayName || 'User' }}</h1>
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
import { onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const emit = defineEmits(['close'])

// 跳转到设置页面
const goToSettings = () => {
  emit('close')
  router.push('/settings')
}

// 退出登录
const handleLogout = async () => {
  emit('close')
  await userStore.logout()
  router.push('/login')
}

// Prevent body scroll when modal is open
onMounted(() => {
  document.body.style.overflow = 'hidden'
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