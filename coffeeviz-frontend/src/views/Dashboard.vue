<template>
  <section id="page-dashboard" class="page-view active p-10">
    <div class="flex justify-between items-end mb-10">
      <div>
        <h1 class="text-4xl font-black text-white mb-2 tracking-tight">你好，<span class="text-amber-500">{{ userStore.displayName || '架构师' }}</span></h1>
        <p class="text-neutral-500">今日有 {{ statistics.newTables || 0 }} 个数据库变更待可视化同步。</p>
      </div>
      <div class="flex space-x-3">
        <button @click="$router.push('/sql-import')" class="px-5 py-2.5 bg-neutral-900 border border-neutral-800 rounded-xl text-sm font-bold hover:bg-neutral-800 transition-all text-neutral-300">快速导入</button>
        <button @click="$router.push('/ai-generate')" class="btn-amber px-6 py-2.5 rounded-xl text-sm font-black text-white flex items-center">
          <i class="fas fa-magic mr-2"></i> 智能建模
        </button>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-12">
      <div class="glass-card p-6 rounded-2xl">
        <div class="flex justify-between items-start mb-4">
          <div class="w-10 h-10 rounded-lg bg-blue-500/10 flex items-center justify-center text-blue-500"><i class="fas fa-table"></i></div>
          <span class="text-xs text-green-500">+12%</span>
        </div>
        <div class="text-2xl font-black text-white">{{ statistics.totalTables || 0 }}</div>
        <div class="text-xs text-neutral-500 font-medium">累计解析数据表</div>
      </div>
      <div class="glass-card p-6 rounded-2xl">
        <div class="flex justify-between items-start mb-4">
          <div class="w-10 h-10 rounded-lg bg-amber-500/10 flex items-center justify-center text-amber-500"><i class="fas fa-diagram-project"></i></div>
          <span class="text-xs text-neutral-500">正常</span>
        </div>
        <div class="text-2xl font-black text-white">{{ statistics.totalProjects || 0 }}</div>
        <div class="text-xs text-neutral-500 font-medium">已构建架构视图</div>
      </div>
      <div class="glass-card p-6 rounded-2xl">
        <div class="flex justify-between items-start mb-4">
          <div class="w-10 h-10 rounded-lg bg-purple-500/10 flex items-center justify-center text-purple-500"><i class="fas fa-bolt"></i></div>
          <span class="text-xs text-amber-500">极速</span>
        </div>
        <div class="text-2xl font-black text-white">2.4s</div>
        <div class="text-xs text-neutral-500 font-medium">平均解析耗时</div>
      </div>
      <div 
        @click="handleUpgradeClick"
        class="glass-card p-6 rounded-2xl cursor-pointer transition-all hover:border-amber-600/40"
        :class="subscriptionInfo.color === 'amber' ? 'border-amber-600/20 bg-amber-600/[0.02]' : subscriptionInfo.color === 'purple' ? 'border-purple-600/20 bg-purple-600/[0.02]' : ''">
        <div class="flex justify-between items-start mb-4">
          <div 
            class="w-10 h-10 rounded-lg flex items-center justify-center"
            :class="subscriptionInfo.color === 'amber' ? 'bg-amber-600/20 text-amber-600' : subscriptionInfo.color === 'purple' ? 'bg-purple-600/20 text-purple-600' : 'bg-neutral-800 text-neutral-400'">
            <i class="fas" :class="subscriptionInfo.icon"></i>
          </div>
          <span v-if="subscriptionStore.isExpired" class="text-xs text-red-500">已过期</span>
        </div>
        <div class="text-lg font-black text-white">{{ subscriptionInfo.name }}</div>
        <div class="text-xs text-neutral-500 font-medium">{{ expiresText }}</div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <div class="lg:col-span-2">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-lg font-bold text-white flex items-center">
            <i class="fas fa-history text-amber-500 mr-2 text-sm"></i> 最近的工作
          </h3>
          <a @click="$router.push('/projects')" class="text-xs text-neutral-500 hover:text-white transition-colors cursor-pointer">管理所有归档</a>
        </div>
        
        <div class="space-y-4">
          <div v-if="loading" class="text-neutral-500 text-center py-4">加载中...</div>
          <div v-else-if="recentProjects.length === 0" class="text-neutral-500 text-center py-4">暂无项目，快去创建吧！</div>
          
          <div v-for="project in recentProjects" :key="project.id" @click="handleProjectClick(project)" 
               class="glass-card p-4 rounded-xl flex items-center justify-between group cursor-pointer hover:border-amber-600/30 transition-all">
            <div class="flex items-center space-x-4">
              <div class="w-10 h-10 rounded-lg bg-neutral-800 flex items-center justify-center text-neutral-400 group-hover:bg-amber-600/10 group-hover:text-amber-500 transition-colors">
                <i class="fas fa-database"></i>
              </div>
              <div>
                <div class="text-sm font-bold text-white group-hover:text-amber-500 transition-colors">{{ project.projectName }}</div>
                <div class="text-[10px] text-neutral-500">{{ project.description || '暂无描述' }}</div>
              </div>
            </div>
            <div class="flex items-center space-x-6">
              <div class="text-right hidden sm:block">
                <div class="text-[10px] text-neutral-500">最后更新</div>
                <div class="text-xs font-mono text-neutral-300">{{ formatDate(project.updateTime) }}</div>
              </div>
              <div class="w-8 h-8 rounded-full bg-neutral-800 flex items-center justify-center text-neutral-500 group-hover:bg-amber-600 group-hover:text-white transition-all transform group-hover:rotate-45">
                <i class="fas fa-arrow-up text-xs"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 配额使用情况 -->
      <div class="lg:col-span-1">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-lg font-bold text-white flex items-center">
            <i class="fas fa-chart-pie text-amber-500 mr-2 text-sm"></i> 配额使用
          </h3>
        </div>
        
        <div class="space-y-4">
          <!-- 架构库配额 -->
          <div class="glass-card p-4 rounded-xl">
            <div class="flex justify-between items-center mb-2">
              <div class="flex items-center space-x-2">
                <i class="fas fa-folder text-blue-500 text-sm"></i>
                <span class="text-sm font-medium text-white">架构库</span>
              </div>
              <span class="text-xs font-mono text-neutral-400">{{ quotaStats.repository }}</span>
            </div>
            <div class="w-full bg-neutral-800 rounded-full h-1.5">
              <div 
                class="bg-blue-500 h-1.5 rounded-full transition-all"
                :style="{ width: subscriptionStore.repositoryUsagePercent + '%' }">
              </div>
            </div>
          </div>
          
          <!-- 架构图配额 -->
          <div class="glass-card p-4 rounded-xl">
            <div class="flex justify-between items-center mb-2">
              <div class="flex items-center space-x-2">
                <i class="fas fa-diagram-project text-amber-500 text-sm"></i>
                <span class="text-sm font-medium text-white">架构图</span>
              </div>
              <span class="text-xs font-mono text-neutral-400">{{ quotaStats.diagram }}</span>
            </div>
            <div class="w-full bg-neutral-800 rounded-full h-1.5">
              <div 
                class="bg-amber-500 h-1.5 rounded-full transition-all"
                :style="{ width: subscriptionStore.diagramUsagePercent + '%' }">
              </div>
            </div>
            <div class="text-[10px] text-neutral-500 mt-1">每月重置</div>
          </div>
          
          <!-- SQL解析配额 -->
          <div class="glass-card p-4 rounded-xl">
            <div class="flex justify-between items-center mb-2">
              <div class="flex items-center space-x-2">
                <i class="fas fa-code text-purple-500 text-sm"></i>
                <span class="text-sm font-medium text-white">SQL 解析</span>
              </div>
              <span class="text-xs font-mono text-neutral-400">{{ quotaStats.sqlParse }}</span>
            </div>
            <div class="w-full bg-neutral-800 rounded-full h-1.5">
              <div 
                class="bg-purple-500 h-1.5 rounded-full transition-all"
                :style="{ width: subscriptionStore.sqlParseUsagePercent + '%' }">
              </div>
            </div>
            <div class="text-[10px] text-neutral-500 mt-1">每月重置</div>
          </div>
          
          <!-- AI生成配额 -->
          <div class="glass-card p-4 rounded-xl">
            <div class="flex justify-between items-center mb-2">
              <div class="flex items-center space-x-2">
                <i class="fas fa-magic text-green-500 text-sm"></i>
                <span class="text-sm font-medium text-white">AI 生成</span>
              </div>
              <span class="text-xs font-mono text-neutral-400">{{ quotaStats.aiGenerate }}</span>
            </div>
            <div class="w-full bg-neutral-800 rounded-full h-1.5">
              <div 
                class="bg-green-500 h-1.5 rounded-full transition-all"
                :style="{ width: subscriptionStore.aiGenerateUsagePercent + '%' }">
              </div>
            </div>
            <div class="text-[10px] text-neutral-500 mt-1">每月重置</div>
          </div>
          
          <!-- 升级按钮 -->
          <button 
            v-if="subscriptionStore.planCode === 'FREE'"
            @click="handleUpgradeClick"
            class="w-full btn-amber px-4 py-3 rounded-xl text-sm font-bold text-white flex items-center justify-center mt-4">
            <i class="fas fa-crown mr-2"></i> 升级到 Pro
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useProjectStore } from '@/store/project'
import { useSubscriptionStore } from '@/store/subscription'

const router = useRouter()
const userStore = useUserStore()
const projectStore = useProjectStore()
const subscriptionStore = useSubscriptionStore()

const loading = ref(false)
const statistics = ref({
  totalProjects: 0,
  monthlyNew: 0,
  totalTables: 0,
  newTables: 0
})
const recentProjects = ref([])
let refreshTimer = null

// 订阅信息计算属性
const subscriptionInfo = computed(() => {
  const sub = subscriptionStore.currentSubscription
  if (!sub) return { name: 'Free', icon: 'fa-coffee', color: 'neutral' }
  
  const planMap = {
    'FREE': { name: 'Coffee Free', icon: 'fa-coffee', color: 'neutral' },
    'PRO': { name: 'Coffee Pro', icon: 'fa-crown', color: 'amber' },
    'TEAM': { name: 'Coffee Team', icon: 'fa-users', color: 'purple' }
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
  
  return {
    repository: repo ? `${repo.quotaUsed}/${repo.quotaLimit === -1 ? '∞' : repo.quotaLimit}` : '0/0',
    diagram: diagram ? `${diagram.quotaUsed}/${diagram.quotaLimit === -1 ? '∞' : diagram.quotaLimit}` : '0/0',
    sqlParse: sqlParse ? `${sqlParse.quotaUsed}/${sqlParse.quotaLimit === -1 ? '∞' : sqlParse.quotaLimit}` : '0/0',
    aiGenerate: aiGen ? `${aiGen.quotaUsed}/${aiGen.quotaLimit === -1 ? '∞' : aiGen.quotaLimit}` : '0/0'
  }
})

onMounted(async () => {
  loading.value = true
  try {
    // 并行获取数据
    await Promise.all([
      loadProjects(),
      loadStatistics(),
      loadSubscription()
    ])
    
    // 设置定时刷新配额（每30秒）
    refreshTimer = setInterval(() => {
      subscriptionStore.refreshQuotas()
    }, 30000)
  } catch (error) {
    console.error('Failed to load dashboard data', error)
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  // 清除定时器
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})

const loadProjects = async () => {
  try {
    const res = await projectStore.fetchProjects({
      page: 1,
      size: 5,
      sort: 'updateTime,desc'
    })
    
    recentProjects.value = res.data.list || []
    
    // 计算统计数据 - 只统计项目数量
    const total = res.data.total || 0
    
    statistics.value = {
      totalProjects: total,
      monthlyNew: 0,
      totalTables: 0,  // 将通过 loadStatistics 获取
      newTables: 0
    }
  } catch (error) {
    console.error('Failed to load projects', error)
  }
}

const loadStatistics = async () => {
  try {
    const api = (await import('@/api/index.js')).default
    const res = await api.get('/api/diagram/statistics')
    
    if (res.code === 200 && res.data) {
      statistics.value.totalTables = res.data.totalTables || 0
      statistics.value.totalRelations = res.data.totalRelations || 0
    }
  } catch (error) {
    console.error('Failed to load statistics', error)
  }
}

const loadSubscription = async () => {
  try {
    await Promise.all([
      subscriptionStore.fetchCurrentSubscription(),
      subscriptionStore.fetchQuotas()
    ])
  } catch (error) {
    console.error('Failed to load subscription', error)
  }
}

const handleProjectClick = (project) => {
  router.push('/projects')
}

const handleUpgradeClick = () => {
  router.push('/subscribe')
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}
</script>
