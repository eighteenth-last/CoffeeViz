<template>
  <section id="page-dashboard" class="page-view active">
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
      <div class="glass-card p-6 rounded-2xl border-amber-600/20 bg-amber-600/[0.02]">
        <div class="flex justify-between items-start mb-4">
          <div class="w-10 h-10 rounded-lg bg-amber-600/20 flex items-center justify-center text-amber-600"><i class="fas fa-crown"></i></div>
        </div>
        <div class="text-lg font-black text-white">Coffee Pro</div>
        <div class="text-xs text-neutral-500 font-medium">当前订阅计划</div>
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
                <div class="text-sm font-bold text-white group-hover:text-amber-500 transition-colors">{{ project.name }}</div>
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
      
      <!-- Right Column (could be added later or kept empty for now as per ff.html implies a layout but content might be truncated in my read) -->
      <!-- Adding a placeholder or keeping it simple based on what I saw in ff.html which had a grid -->
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useProjectStore } from '@/store/project'

const router = useRouter()
const userStore = useUserStore()
const projectStore = useProjectStore()

const loading = ref(false)
const statistics = ref({
  totalProjects: 0,
  monthlyNew: 0,
  totalTables: 0,
  newTables: 0
})
const recentProjects = ref([])

onMounted(async () => {
  loading.value = true
  try {
    // 获取最近的项目
    const res = await projectStore.fetchProjects({
      page: 1,
      size: 5,
      sort: 'updateTime,desc'
    })
    
    recentProjects.value = res.data.list || []
    
    // 计算统计数据
    const total = res.data.total || 0
    // 简单的统计计算，实际应由后端接口提供
    let tableCount = 0
    recentProjects.value.forEach(p => {
      if (p.tableCount) tableCount += p.tableCount
    })
    
    statistics.value = {
      totalProjects: total,
      monthlyNew: 0, // 暂无数据
      totalTables: tableCount, // 仅统计最近项目的表数量作为示例，或者显示0
      newTables: 0
    }
  } catch (error) {
    console.error('Failed to load dashboard data', error)
  } finally {
    loading.value = false
  }
})

const handleProjectClick = (project) => {
  // Navigate to project details or editor
  // router.push(`/project/${project.id}`)
  // 暂时跳转到项目列表，或者如果有详情页则跳转详情页
  router.push('/projects')
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}
</script>
