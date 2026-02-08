<template>
  <section class="project-detail p-10 fade-in">
    <!-- Loading State -->
    <div v-if="loading" class="flex justify-center items-center py-20">
      <div class="flex flex-col items-center">
        <i class="fas fa-circle-notch fa-spin text-4xl text-amber-600 mb-4"></i>
        <p class="text-neutral-500 text-sm">加载项目中...</p>
      </div>
    </div>

    <!-- Project Content -->
    <div v-else-if="project" class="max-w-7xl mx-auto">
      <!-- Header -->
      <div class="flex items-center justify-between mb-8">
        <div class="flex items-center space-x-4">
          <button @click="$router.back()" class="w-10 h-10 rounded-xl bg-neutral-900 border border-neutral-800 flex items-center justify-center text-neutral-400 hover:text-white hover:border-amber-600 transition-all">
            <i class="fas fa-arrow-left"></i>
          </button>
          <div>
            <h1 class="text-3xl font-black text-white">{{ project.projectName }}</h1>
            <p class="text-neutral-500 text-sm mt-1">{{ project.description || '暂无描述' }}</p>
          </div>
        </div>
        <div class="flex items-center space-x-3">
          <button @click="handleExport" class="px-5 py-2.5 bg-neutral-900 border border-neutral-800 rounded-xl text-sm font-bold hover:bg-neutral-800 transition-all text-neutral-300">
            <i class="fas fa-download mr-2"></i>导出
          </button>
          <button @click="handleEdit" class="px-5 py-2.5 bg-amber-600 hover:bg-amber-500 rounded-xl text-sm font-bold text-white transition-all">
            <i class="fas fa-edit mr-2"></i>编辑
          </button>
        </div>
      </div>

      <!-- Project Info Cards -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
        <div class="glass-card p-4 rounded-xl">
          <div class="text-xs text-neutral-500 mb-1">来源类型</div>
          <div class="text-lg font-bold text-white">{{ project.sourceType || 'SQL' }}</div>
        </div>
        <div class="glass-card p-4 rounded-xl">
          <div class="text-xs text-neutral-500 mb-1">数据库类型</div>
          <div class="text-lg font-bold text-white">{{ project.dbType || 'MySQL' }}</div>
        </div>
        <div class="glass-card p-4 rounded-xl">
          <div class="text-xs text-neutral-500 mb-1">表数量</div>
          <div class="text-lg font-bold text-amber-500">{{ project.tableCount || 0 }}</div>
        </div>
        <div class="glass-card p-4 rounded-xl">
          <div class="text-xs text-neutral-500 mb-1">最后更新</div>
          <div class="text-sm font-mono text-white">{{ formatDate(project.updateTime) }}</div>
        </div>
      </div>

      <!-- Diagram Viewer -->
      <div class="glass-card rounded-2xl p-6">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-xl font-bold text-white">架构图</h2>
          <div class="flex items-center space-x-2">
            <button @click="handleZoomOut" class="w-8 h-8 rounded-lg bg-neutral-900 border border-neutral-800 flex items-center justify-center text-neutral-400 hover:text-white transition-all">
              <i class="fas fa-minus text-xs"></i>
            </button>
            <span class="text-xs text-neutral-500 font-mono w-12 text-center">{{ Math.round(scale * 100) }}%</span>
            <button @click="handleZoomIn" class="w-8 h-8 rounded-lg bg-neutral-900 border border-neutral-800 flex items-center justify-center text-neutral-400 hover:text-white transition-all">
              <i class="fas fa-plus text-xs"></i>
            </button>
            <button @click="handleFullscreen" class="w-8 h-8 rounded-lg bg-neutral-900 border border-neutral-800 flex items-center justify-center text-neutral-400 hover:text-white transition-all ml-2">
              <i class="fas fa-expand text-xs"></i>
            </button>
          </div>
        </div>
        
        <div ref="diagramContainer" class="bg-[#0a0a0a] border border-neutral-800 rounded-xl overflow-auto" style="height: 600px;">
          <!-- Display MinIO PNG image if available -->
          <div v-if="project.imageUrl" class="p-8 flex items-center justify-center h-full">
            <img :src="project.imageUrl" :alt="project.projectName" class="max-w-full max-h-full object-contain" :style="{ transform: `scale(${scale})`, transformOrigin: 'center' }" />
          </div>
          <!-- Fallback to SVG content -->
          <div v-else-if="project.svgContent" v-html="project.svgContent" class="p-8" :style="{ transform: `scale(${scale})`, transformOrigin: 'top left' }"></div>
          <!-- Fallback to Mermaid code only -->
          <div v-else-if="project.mermaidCode" class="flex items-center justify-center h-full">
            <div class="text-center">
              <i class="fas fa-code text-4xl text-neutral-700 mb-4"></i>
              <p class="text-neutral-500">仅保存了 Mermaid 源码</p>
              <button @click="handleViewCode" class="mt-4 px-4 py-2 bg-neutral-900 border border-neutral-800 rounded-lg text-sm text-neutral-300 hover:text-white transition-all">
                查看源码
              </button>
            </div>
          </div>
          <!-- No data -->
          <div v-else class="flex items-center justify-center h-full">
            <div class="text-center">
              <i class="fas fa-exclamation-circle text-4xl text-neutral-700 mb-4"></i>
              <p class="text-neutral-500">暂无架构图数据</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Mermaid Code Section -->
      <div v-if="showCode && project.mermaidCode" class="glass-card rounded-2xl p-6 mt-6">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-xl font-bold text-white">Mermaid 源码</h2>
          <button @click="handleCopyCode" class="px-4 py-2 bg-neutral-900 border border-neutral-800 rounded-lg text-sm text-neutral-300 hover:text-white transition-all">
            <i class="fas fa-copy mr-2"></i>复制
          </button>
        </div>
        <pre class="bg-black border border-neutral-800 rounded-xl p-4 overflow-auto text-sm text-amber-100 font-mono" style="max-height: 400px;">{{ project.mermaidCode }}</pre>
      </div>
    </div>

    <!-- Error State -->
    <div v-else class="flex flex-col items-center justify-center py-20">
      <i class="fas fa-exclamation-triangle text-4xl text-red-500 mb-4"></i>
      <p class="text-neutral-500">项目加载失败</p>
      <button @click="$router.back()" class="mt-4 px-6 py-2 bg-neutral-900 border border-neutral-800 rounded-xl text-sm text-neutral-300 hover:text-white transition-all">
        返回列表
      </button>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useProjectStore } from '@/store/project'
import { useMessage } from 'naive-ui'

const route = useRoute()
const router = useRouter()
const projectStore = useProjectStore()
const message = useMessage()

const loading = ref(true)
const project = ref(null)
const scale = ref(0.8)
const showCode = ref(false)
const diagramContainer = ref(null)

onMounted(async () => {
  const projectId = route.params.id
  if (!projectId) {
    message.error('项目 ID 无效')
    router.push('/projects')
    return
  }

  try {
    const res = await projectStore.getProjectDetail(projectId)
    project.value = res.data.project
  } catch (error) {
    message.error('加载项目失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
})

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

const handleZoomIn = () => {
  if (scale.value < 2) {
    scale.value = Math.min(2, scale.value + 0.1)
  }
}

const handleZoomOut = () => {
  if (scale.value > 0.3) {
    scale.value = Math.max(0.3, scale.value - 0.1)
  }
}

const handleFullscreen = () => {
  if (diagramContainer.value) {
    if (!document.fullscreenElement) {
      diagramContainer.value.requestFullscreen()
    } else {
      document.exitFullscreen()
    }
  }
}

const handleViewCode = () => {
  showCode.value = !showCode.value
}

const handleCopyCode = () => {
  if (project.value?.mermaidCode) {
    navigator.clipboard.writeText(project.value.mermaidCode)
    message.success('已复制到剪贴板')
  }
}

const handleExport = async () => {
  try {
    const res = await projectStore.exportProject(project.value.id)
    
    // 下载 Mermaid 源码
    const blob = new Blob([res.data], { type: 'text/plain' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${project.value.projectName || 'project'}.mmd`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    
    message.success('导出成功')
  } catch (error) {
    message.error('导出失败: ' + (error.message || '未知错误'))
  }
}

const handleEdit = () => {
  message.info('编辑功能开发中...')
}
</script>

<style scoped>
.fade-in {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>
