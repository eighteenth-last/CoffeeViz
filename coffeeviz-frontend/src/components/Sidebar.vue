<template>
  <aside class="fixed left-0 top-0 h-full w-64 bg-[#080808] border-r border-neutral-900 z-50 flex flex-col">
    <div class="p-8 flex items-center space-x-3">
      <div class="w-10 h-10 rounded-xl flex items-center justify-center overflow-hidden">
        <img src="/logo.png" class="w-full h-full object-cover" alt="Logo">
      </div>
      <span class="text-xl font-black tracking-tight text-white uppercase italic">Coffee<span class="text-amber-500">Viz</span></span>
    </div>

    <nav class="flex-1 px-4 space-y-1">
      <div class="text-[10px] font-bold text-neutral-600 uppercase tracking-widest px-4 mb-2">主菜单</div>
      
      <router-link to="/dashboard" custom v-slot="{ navigate, isActive }">
        <a @click="navigate" :class="['sidebar-item flex items-center px-4 py-2 rounded-lg text-sm font-medium transition-all group cursor-pointer', isActive ? 'active' : 'text-neutral-500 hover:text-neutral-200 hover:bg-neutral-900/50']">
          <i :class="['fas fa-layer-group w-8 text-lg transition-transform', isActive ? '' : 'group-hover:scale-110']"></i>
          <span>总控制台</span>
        </a>
      </router-link>

      <router-link to="/sql-import" custom v-slot="{ navigate, isActive }">
        <a @click="navigate" :class="['sidebar-item flex items-center px-4 py-2 rounded-lg text-sm font-medium transition-all group cursor-pointer', isActive ? 'active' : 'text-neutral-500 hover:text-neutral-200 hover:bg-neutral-900/50']">
          <i :class="['fas fa-terminal w-8 text-lg transition-transform', isActive ? '' : 'group-hover:rotate-12']"></i>
          <span>SQL 智能导入</span>
        </a>
      </router-link>

      <router-link to="/db-connect" custom v-slot="{ navigate, isActive }">
        <a @click="navigate" :class="['sidebar-item flex items-center px-4 py-2 rounded-lg text-sm font-medium transition-all group cursor-pointer', isActive ? 'active' : 'text-neutral-500 hover:text-neutral-200 hover:bg-neutral-900/50']">
          <i :class="['fas fa-link w-8 text-lg transition-transform', isActive ? '' : 'group-hover:scale-110']"></i>
          <span>实时数据库连接</span>
        </a>
      </router-link>

      <router-link to="/ai-generate" custom v-slot="{ navigate, isActive }">
        <a @click="navigate" :class="['sidebar-item flex items-center px-4 py-2 rounded-lg text-sm font-medium transition-all group cursor-pointer', isActive ? 'active' : 'text-neutral-500 hover:text-neutral-200 hover:bg-neutral-900/50']">
          <i :class="['fas fa-microchip w-8 text-lg transition-colors', isActive ? '' : 'group-hover:text-amber-500']"></i>
          <span>AI 自然语言建模</span>
        </a>
      </router-link>

      <div class="pt-6 text-[10px] font-bold text-neutral-600 uppercase tracking-widest px-4 mb-2">资源管理</div>
      
      <router-link to="/projects" custom v-slot="{ navigate, isActive }">
        <a @click="navigate" :class="['sidebar-item flex items-center px-4 py-3 rounded-lg text-sm font-medium transition-all group cursor-pointer', isActive ? 'active' : 'text-neutral-500 hover:text-neutral-200 hover:bg-neutral-900/50']">
          <i class="fas fa-archive w-8 text-lg"></i>
          <span>架构归档库</span>
        </a>
      </router-link>

      <router-link to="/team" custom v-slot="{ navigate, isActive }">
        <a @click="navigate" :class="['sidebar-item flex items-center px-4 py-3 rounded-lg text-sm font-medium transition-all group cursor-pointer', isActive ? 'active' : 'text-neutral-500 hover:text-neutral-200 hover:bg-neutral-900/50']">
          <i class="fas fa-users w-8 text-lg"></i>
          <span>团队协作</span>
        </a>
      </router-link>

      <router-link to="/subscribe" custom v-slot="{ navigate, isActive }">
        <a @click="navigate" :class="['sidebar-item flex items-center px-4 py-3 rounded-lg text-sm font-medium transition-all group cursor-pointer', isActive ? 'active' : 'text-neutral-500 hover:text-neutral-200 hover:bg-neutral-900/50']">
          <i class="fas fa-credit-card w-8 text-lg"></i>
          <span>订阅计划</span>
        </a>
      </router-link>

      <router-link to="/settings" custom v-slot="{ navigate, isActive }">
        <a @click="navigate" :class="['sidebar-item flex items-center px-4 py-3 rounded-lg text-sm font-medium transition-all group cursor-pointer', isActive ? 'active' : 'text-neutral-500 hover:text-neutral-200 hover:bg-neutral-900/50']">
          <i class="fas fa-sliders w-8 text-lg"></i>
          <span>系统设置</span>
        </a>
      </router-link>
    </nav>

    <div class="p-4" v-if="subscriptionStore.planCode === 'FREE'">
      <div class="bg-gradient-to-br from-neutral-900 to-black border border-neutral-800 rounded-2xl p-4 relative overflow-hidden group">
        <div class="text-xs text-amber-500 font-bold mb-1">升级到 Pro</div>
        <p class="text-[10px] text-neutral-500 mb-3">解锁多方言 SQL 高级解析与云端同步功能</p>
        <button @click="handleUpgradeClick" class="w-full py-2 bg-amber-600/10 border border-amber-600/30 rounded-lg text-[10px] font-bold text-amber-500 hover:bg-amber-600 hover:text-white transition-all">立即升级</button>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useSubscriptionStore } from '@/store/subscription'

const router = useRouter()
const subscriptionStore = useSubscriptionStore()

const handleUpgradeClick = () => {
  router.push('/subscribe')
}
</script>
