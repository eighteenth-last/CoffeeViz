<template>
  <div class="repository-detail px-10 pb-10 fade-in">
    <!-- Header Section -->
    <div class="flex justify-between items-end mb-8">
      <div>
        <button @click="$router.back()" class="text-neutral-500 hover:text-white mb-4 flex items-center transition-colors">
          <i class="fas fa-arrow-left mr-2"></i> 返回架构库列表
        </button>
        <h1 class="text-4xl font-black text-white mb-2 tracking-tight">
          <i class="fas fa-folder-open text-amber-500 mr-3"></i>{{ repository?.repositoryName || '加载中...' }}
        </h1>
        <p class="text-neutral-500">{{ repository?.description || '暂无描述' }}</p>
      </div>
    </div>

    <!-- Stats Bar -->
    <div class="glass-card p-6 rounded-2xl mb-8 grid grid-cols-3 gap-6">
      <div class="text-center">
        <div class="text-3xl font-black text-amber-500 mb-2">{{ diagrams.length }}</div>
        <div class="text-sm text-neutral-500">架构图数量</div>
      </div>
      <div class="text-center">
        <div class="text-3xl font-black text-green-500 mb-2">{{ totalTables }}</div>
        <div class="text-sm text-neutral-500">总表数量</div>
      </div>
      <div class="text-center">
        <div class="text-3xl font-black text-blue-500 mb-2">{{ totalRelations }}</div>
        <div class="text-sm text-neutral-500">总关系数量</div>
      </div>
    </div>

    <!-- Diagrams Grid -->
    <div v-if="loading" class="flex justify-center items-center py-20">
      <div class="flex flex-col items-center">
        <i class="fas fa-circle-notch fa-spin text-4xl text-amber-600 mb-4"></i>
        <p class="text-neutral-500 text-sm">加载架构图中...</p>
      </div>
    </div>
    
    <div v-else-if="diagrams.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div 
        v-for="diagram in diagrams" 
        :key="diagram.id" 
        class="glass-card group p-6 rounded-2xl relative overflow-hidden hover:-translate-y-1 transition-all duration-300 cursor-pointer border border-neutral-800 hover:border-amber-600/30"
        @click="handleDiagramClick(diagram)"
      >
        <!-- Image Preview -->
        <div v-if="diagram.imageUrl" class="mb-4 rounded-xl overflow-hidden bg-neutral-900 border border-neutral-800">
          <img :src="diagram.imageUrl" :alt="diagram.diagramName" class="w-full h-40 object-cover object-top opacity-80 group-hover:opacity-100 transition-opacity" />
        </div>
        <div v-else class="mb-4 rounded-xl bg-neutral-900 border border-neutral-800 h-40 flex items-center justify-center">
          <i class="fas fa-image text-neutral-700 text-4xl"></i>
        </div>

        <div class="flex items-start justify-between mb-4">
          <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-neutral-800 to-black border border-neutral-800 flex items-center justify-center text-2xl group-hover:scale-110 transition-transform duration-500 shadow-lg">
            <i class="fas fa-file-alt text-amber-600"></i>
          </div>
          <div class="flex flex-col items-end">
            <span class="px-2 py-1 rounded text-[10px] font-bold uppercase tracking-wider border bg-blue-500/10 text-blue-500 border-blue-500/20">
              {{ diagram.sourceType || 'SQL' }}
            </span>
          </div>
        </div>

        <h3 class="text-lg font-bold text-white mb-2 line-clamp-1 group-hover:text-amber-500 transition-colors">{{ diagram.diagramName }}</h3>
        <p class="text-sm text-neutral-500 mb-6 line-clamp-2 h-10">{{ diagram.description || '暂无描述信息...' }}</p>

        <div class="pt-4 border-t border-neutral-800 flex items-center justify-between text-xs text-neutral-500 font-mono">
          <div class="flex items-center space-x-3">
            <span><i class="fas fa-table mr-1 text-neutral-600"></i>{{ diagram.tableCount || 0 }}</span>
            <span><i class="fas fa-link mr-1 text-neutral-600"></i>{{ diagram.relationCount || 0 }}</span>
          </div>
          <div class="flex items-center space-x-2">
            <button 
              @click.stop="handleCopyMermaid(diagram)" 
              class="w-8 h-8 rounded-lg bg-neutral-900 border border-neutral-800 flex items-center justify-center text-neutral-400 hover:text-amber-500 hover:border-amber-600 transition-all"
              title="复制 Mermaid 代码"
            >
              <i class="fas fa-code text-xs"></i>
            </button>
            <span>{{ formatDate(diagram.updateTime) }}</span>
          </div>
        </div>
        
        <!-- Hover Gradient -->
        <div class="absolute inset-0 bg-gradient-to-br from-amber-600/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none"></div>
      </div>
    </div>

    <div v-else class="flex flex-col items-center justify-center py-20 text-neutral-500">
      <div class="w-20 h-20 bg-neutral-900 border border-neutral-800 rounded-3xl flex items-center justify-center mb-6">
        <i class="fas fa-inbox text-3xl opacity-50"></i>
      </div>
      <p class="text-lg mb-2">此架构库中还没有架构图</p>
      <p class="text-sm">前往 SQL 导入或数据库连接页面创建架构图</p>
    </div>

    <!-- Diagram Detail Modal -->
    <div v-if="showDiagramModal && selectedDiagram" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-black/80 backdrop-blur-sm" @click="showDiagramModal = false"></div>
      <div class="glass-card w-full max-w-6xl rounded-3xl p-8 relative z-10 border border-neutral-800 shadow-2xl animate-fade-in-up max-h-[90vh] overflow-auto">
        <div class="flex items-center justify-between mb-6">
          <div>
            <h2 class="text-2xl font-black text-white">{{ selectedDiagram.diagramName }}</h2>
            <p class="text-sm text-neutral-500 mt-1">{{ selectedDiagram.description || '暂无描述' }}</p>
          </div>
          <button @click="showDiagramModal = false" class="w-10 h-10 rounded-full bg-neutral-900 flex items-center justify-center text-neutral-500 hover:text-white transition-colors">
            <i class="fas fa-times"></i>
          </button>
        </div>

        <!-- Diagram Info -->
        <div class="grid grid-cols-4 gap-4 mb-6">
          <div class="glass-card p-4 rounded-xl">
            <div class="text-xs text-neutral-500 mb-1">来源类型</div>
            <div class="text-lg font-bold text-white">{{ selectedDiagram.sourceType || 'SQL' }}</div>
          </div>
          <div class="glass-card p-4 rounded-xl">
            <div class="text-xs text-neutral-500 mb-1">数据库类型</div>
            <div class="text-lg font-bold text-white">{{ selectedDiagram.dbType || 'MySQL' }}</div>
          </div>
          <div class="glass-card p-4 rounded-xl">
            <div class="text-xs text-neutral-500 mb-1">表数量</div>
            <div class="text-lg font-bold text-amber-500">{{ selectedDiagram.tableCount || 0 }}</div>
          </div>
          <div class="glass-card p-4 rounded-xl">
            <div class="text-xs text-neutral-500 mb-1">关系数量</div>
            <div class="text-lg font-bold text-green-500">{{ selectedDiagram.relationCount || 0 }}</div>
          </div>
        </div>

        <!-- Diagram Image -->
        <div class="bg-[#0a0a0a] border border-neutral-800 rounded-xl overflow-auto p-8" style="max-height: 600px;">
          <div v-if="selectedDiagram.imageUrl" class="flex items-center justify-center">
            <img 
              :src="selectedDiagram.imageUrl" 
              :alt="selectedDiagram.diagramName" 
              class="max-w-full max-h-full object-contain" 
              :style="{ transform: `scale(${diagramScale})`, transformOrigin: 'center' }" 
            />
          </div>
          <div v-else class="flex items-center justify-center h-64">
            <div class="text-center">
              <i class="fas fa-image text-4xl text-neutral-700 mb-4"></i>
              <p class="text-neutral-500">暂无图片</p>
            </div>
          </div>
        </div>

        <!-- Zoom Controls -->
        <div class="flex items-center justify-center space-x-4 mt-6">
          <button @click="diagramScale = Math.max(0.3, diagramScale - 0.1)" class="w-10 h-10 rounded-lg bg-neutral-900 border border-neutral-800 flex items-center justify-center text-neutral-400 hover:text-white transition-all">
            <i class="fas fa-minus text-xs"></i>
          </button>
          <span class="text-sm text-neutral-500 font-mono w-16 text-center">{{ Math.round(diagramScale * 100) }}%</span>
          <button @click="diagramScale = Math.min(2, diagramScale + 0.1)" class="w-10 h-10 rounded-lg bg-neutral-900 border border-neutral-800 flex items-center justify-center text-neutral-400 hover:text-white transition-all">
            <i class="fas fa-plus text-xs"></i>
          </button>
          <div class="w-px h-8 bg-neutral-800 mx-2"></div>
          <button @click="showMermaidCode = !showMermaidCode" class="px-4 py-2 rounded-lg bg-neutral-900 border border-neutral-800 text-sm text-neutral-400 hover:text-white hover:border-amber-600 transition-all">
            <i class="fas fa-code mr-2"></i>{{ showMermaidCode ? '隐藏代码' : '查看代码' }}
          </button>
        </div>

        <!-- Mermaid Code Section -->
        <div v-if="showMermaidCode && selectedDiagram.mermaidCode" class="mt-6">
          <div class="flex items-center justify-between mb-3">
            <h3 class="text-sm font-bold text-white">Mermaid 源码</h3>
            <button @click="handleCopyMermaid(selectedDiagram)" class="px-3 py-1.5 rounded-lg bg-neutral-900 border border-neutral-800 text-xs text-neutral-400 hover:text-white hover:border-amber-600 transition-all">
              <i class="fas fa-copy mr-1"></i>复制代码
            </button>
          </div>
          <pre class="bg-black border border-neutral-800 rounded-xl p-4 overflow-auto text-xs text-amber-100 font-mono max-h-64">{{ selectedDiagram.mermaidCode }}</pre>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const loading = ref(false)
const repository = ref(null)
const diagrams = ref([])
const selectedDiagram = ref(null)
const showDiagramModal = ref(false)
const diagramScale = ref(0.8)
const showMermaidCode = ref(false)

// 计算总表数量和总关系数量
const totalTables = computed(() => {
  return diagrams.value.reduce((sum, d) => sum + (d.tableCount || 0), 0)
})

const totalRelations = computed(() => {
  return diagrams.value.reduce((sum, d) => sum + (d.relationCount || 0), 0)
})

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  
  return date.toLocaleDateString('zh-CN')
}

// 加载架构库详情
const loadRepository = async () => {
  try {
    const response = await fetch(`/api/repository/detail/${route.params.id}`, {
      headers: {
        'Authorization': localStorage.getItem('token') || ''
      }
    })
    const result = await response.json()
    if (result.code === 200) {
      repository.value = result.data
    } else {
      throw new Error(result.message || '加载失败')
    }
  } catch (error) {
    message.error('加载架构库失败：' + (error.message || error))
  }
}

// 加载架构图列表
const loadDiagrams = async () => {
  loading.value = true
  try {
    const response = await fetch(`/api/diagram/list/${route.params.id}`, {
      headers: {
        'Authorization': localStorage.getItem('token') || ''
      }
    })
    const result = await response.json()
    if (result.code === 200) {
      diagrams.value = result.data || []
    } else {
      throw new Error(result.message || '加载失败')
    }
  } catch (error) {
    message.error('加载架构图失败：' + (error.message || error))
  } finally {
    loading.value = false
  }
}

// 点击架构图
const handleDiagramClick = (diagram) => {
  // 在当前页面显示架构图详情（使用模态框或展开视图）
  // 暂时使用 message 提示，后续可以实现模态框查看
  selectedDiagram.value = diagram
  showDiagramModal.value = true
}

// 复制 Mermaid 代码
const handleCopyMermaid = async (diagram) => {
  if (!diagram.mermaidCode) {
    message.warning('该架构图没有 Mermaid 代码')
    return
  }
  
  try {
    await navigator.clipboard.writeText(diagram.mermaidCode)
    message.success('Mermaid 代码已复制到剪贴板')
  } catch (error) {
    message.error('复制失败：' + (error.message || error))
  }
}

onMounted(async () => {
  await loadRepository()
  await loadDiagrams()
})
</script>

<style scoped>
.fade-in {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.animate-fade-in-up {
  animation: fadeInUp 0.3s ease-out forwards;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
