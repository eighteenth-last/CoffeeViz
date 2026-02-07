<template>
  <div class="fixed inset-0 z-[100] flex items-center justify-center p-4">
    <!-- Backdrop -->
    <div class="absolute inset-0 bg-black/80 backdrop-blur-sm transition-opacity" @click="$emit('close')"></div>
    
    <!-- Modal Content -->
    <div class="relative z-10 w-full max-w-4xl max-h-[90vh] overflow-y-auto custom-scrollbar glass-card rounded-2xl border border-neutral-800 shadow-2xl animate-in fade-in zoom-in duration-200 bg-[#050505]">
      
      <!-- Close Button -->
      <button @click="$emit('close')" class="absolute top-4 right-4 z-20 w-8 h-8 flex items-center justify-center rounded-full bg-black/50 hover:bg-neutral-800 text-neutral-400 hover:text-white transition-all border border-neutral-700">
        <i class="fas fa-times"></i>
      </button>

      <div class="p-8">
        <!-- Profile Header -->
        <div class="glass-card p-8 rounded-2xl mb-8 flex flex-col md:flex-row items-center md:items-start gap-8 relative overflow-hidden">
          <div class="absolute top-0 left-0 w-full h-32 bg-gradient-to-r from-amber-600/20 to-purple-600/20"></div>
          
          <div class="relative z-10 w-32 h-32 rounded-2xl bg-[#0f0f0f] p-1 border-2 border-amber-600/30 shadow-2xl">
            <img :src="userStore.userInfo?.avatarUrl || `https://api.dicebear.com/7.x/pixel-art/svg?seed=${userStore.displayName || 'User'}`" 
                 alt="avatar" class="w-full h-full rounded-xl object-cover bg-neutral-900">
            <div class="absolute -bottom-2 -right-2 w-8 h-8 bg-green-500 rounded-full border-4 border-[#0f0f0f] flex items-center justify-center" title="Online">
              <div class="w-2 h-2 bg-white rounded-full animate-pulse"></div>
            </div>
          </div>

          <div class="relative z-10 flex-1 text-center md:text-left mt-4 md:mt-8">
            <div class="flex flex-col md:flex-row justify-between items-center gap-4">
              <div>
                <h1 class="text-3xl font-black text-white tracking-tight mb-1">{{ userStore.displayName || 'User' }}</h1>
                <p class="text-neutral-500 font-mono text-sm">@{{ userStore.username || 'user' }}</p>
              </div>
              <!-- <button class="px-5 py-2 bg-neutral-800 hover:bg-neutral-700 border border-neutral-700 rounded-lg text-white text-sm font-bold transition-all flex items-center">
                <i class="fas fa-pen mr-2 text-xs"></i> 编辑资料
              </button> -->
            </div>
            
            <div class="mt-6 flex flex-wrap justify-center md:justify-start gap-6">
              <!-- <div class="flex items-center text-neutral-400 text-sm">
                <i class="fas fa-briefcase mr-2 text-amber-500"></i>
                <span>高级架构师</span>
              </div>
              <div class="flex items-center text-neutral-400 text-sm">
                <i class="fas fa-map-marker-alt mr-2 text-amber-500"></i>
                <span>上海, 中国</span>
              </div> -->
              <div class="flex items-center text-neutral-400 text-sm">
                <i class="fas fa-envelope mr-2 text-amber-500"></i>
                <span>{{ userStore.userInfo?.email || '暂无邮箱' }}</span>
              </div>
              <!-- <div class="flex items-center text-neutral-400 text-sm">
                <i class="fas fa-calendar-alt mr-2 text-amber-500"></i>
                <span>加入于 2023年</span>
              </div> -->
            </div>
          </div>
        </div>

        <!-- <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
           ... Stats and Recent Activity Removed ...
        </div> -->

      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

defineEmits(['close'])

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
</style>