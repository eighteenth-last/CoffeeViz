<template>
  <div class="diagram-viewer">
    <n-card title="ER 图预览" :bordered="false">
      <template #header-extra>
        <n-space>
          <n-button-group>
            <n-button 
              :type="viewType === 'svg' ? 'primary' : 'default'"
              @click="viewType = 'svg'"
            >
              SVG
            </n-button>
            <n-button 
              :type="viewType === 'mermaid' ? 'primary' : 'default'"
              @click="viewType = 'mermaid'"
            >
              Mermaid
            </n-button>
          </n-button-group>
          
          <!-- 缩放控制 -->
          <n-button-group v-if="viewType === 'svg'">
            <n-button @click="zoomOut" :disabled="zoomLevel <= 20">
              <template #icon>
                <n-icon><RemoveOutline /></n-icon>
              </template>
            </n-button>
            <n-button disabled>
              {{ zoomLevel }}%
            </n-button>
            <n-button @click="zoomIn" :disabled="zoomLevel >= 200">
              <template #icon>
                <n-icon><AddOutline /></n-icon>
              </template>
            </n-button>
            <n-button @click="resetZoom">
              <template #icon>
                <n-icon><RefreshOutline /></n-icon>
              </template>
            </n-button>
          </n-button-group>
          
          <n-button @click="handleDownload('svg')" :disabled="!hasDiagram">
            <template #icon>
              <n-icon><DownloadOutline /></n-icon>
            </template>
            下载 SVG
          </n-button>
          
          <n-button @click="handleDownload('png')" :disabled="!hasDiagram">
            <template #icon>
              <n-icon><DownloadOutline /></n-icon>
            </template>
            下载 PNG
          </n-button>
        </n-space>
      </template>
      
      <div v-if="!hasDiagram" class="empty-state">
        <n-empty description="暂无图表数据" />
      </div>
      
      <div v-else class="diagram-content">
        <!-- SVG 视图 -->
        <div v-if="viewType === 'svg'" class="svg-wrapper">
          <div ref="svgContainerRef" class="svg-container">
            <div 
              class="svg-content"
              :style="{ transform: `scale(${zoomLevel / 100})`, transformOrigin: 'top center' }"
              v-html="svgContent"
            ></div>
          </div>
          
          <!-- 回到顶部按钮 -->
          <n-button
            v-show="showBackToTop"
            class="back-to-top"
            circle
            type="primary"
            @click="scrollToTop"
          >
            <template #icon>
              <n-icon><ArrowUpOutline /></n-icon>
            </template>
          </n-button>
        </div>
        
        <!-- Mermaid 代码视图 -->
        <div v-else class="mermaid-container">
          <n-code :code="mermaidCode" language="mermaid" />
        </div>
        
        <!-- 统计信息 -->
        <div class="diagram-stats">
          <n-space>
            <n-tag type="info">表数量: {{ tableCount }}</n-tag>
            <n-tag type="success">关系数量: {{ relationCount }}</n-tag>
            <n-tag v-if="viewType === 'svg'" type="warning">缩放: {{ zoomLevel }}%</n-tag>
          </n-space>
        </div>
        
        <!-- 警告信息 -->
        <div v-if="warnings && warnings.length > 0" class="diagram-warnings">
          <n-alert type="warning" title="解析警告">
            <ul>
              <li v-for="(warning, index) in warnings" :key="index">{{ warning }}</li>
            </ul>
          </n-alert>
        </div>
      </div>
    </n-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { NIcon } from 'naive-ui'
import { DownloadOutline, ArrowUpOutline, AddOutline, RemoveOutline, RefreshOutline } from '@vicons/ionicons5'
import { downloadFile } from '@/utils/download'

const props = defineProps({
  mermaidCode: {
    type: String,
    default: ''
  },
  svgContent: {
    type: String,
    default: ''
  },
  pngBase64: {
    type: String,
    default: ''
  },
  warnings: {
    type: Array,
    default: () => []
  },
  tableCount: {
    type: Number,
    default: 0
  },
  relationCount: {
    type: Number,
    default: 0
  }
})

const viewType = ref('svg')
const svgContainerRef = ref(null)
const showBackToTop = ref(false)
const zoomLevel = ref(80) // 默认 80% 缩放

const hasDiagram = computed(() => {
  return !!props.mermaidCode || !!props.svgContent
})

// 缩放控制
const zoomIn = () => {
  if (zoomLevel.value < 200) {
    zoomLevel.value += 10
  }
}

const zoomOut = () => {
  if (zoomLevel.value > 20) {
    zoomLevel.value -= 10
  }
}

const resetZoom = () => {
  zoomLevel.value = 80
}

// 监听滚动事件
const handleScroll = () => {
  if (svgContainerRef.value) {
    showBackToTop.value = svgContainerRef.value.scrollTop > 200
  }
}

// 滚动到顶部
const scrollToTop = () => {
  if (svgContainerRef.value) {
    svgContainerRef.value.scrollTo({
      top: 0,
      behavior: 'smooth'
    })
  }
}

// 初始化滚动位置
const initScrollPosition = () => {
  nextTick(() => {
    if (svgContainerRef.value) {
      // 确保从顶部开始显示
      svgContainerRef.value.scrollTop = 0
      svgContainerRef.value.scrollLeft = 0
    }
  })
}

// 下载文件
const handleDownload = (type) => {
  if (type === 'svg') {
    downloadFile(props.svgContent, 'er-diagram.svg', 'image/svg+xml')
  } else if (type === 'png') {
    // 将 Base64 转换为 Blob 并下载
    const base64Data = props.pngBase64.replace(/^data:image\/png;base64,/, '')
    const byteCharacters = atob(base64Data)
    const byteNumbers = new Array(byteCharacters.length)
    for (let i = 0; i < byteCharacters.length; i++) {
      byteNumbers[i] = byteCharacters.charCodeAt(i)
    }
    const byteArray = new Uint8Array(byteNumbers)
    const blob = new Blob([byteArray], { type: 'image/png' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'er-diagram.png'
    link.click()
    URL.revokeObjectURL(url)
  }
}

// 监听 SVG 内容变化，重置滚动位置和缩放
watch(() => props.svgContent, () => {
  zoomLevel.value = 80 // 重置为 80%
  initScrollPosition()
})

onMounted(() => {
  if (svgContainerRef.value) {
    svgContainerRef.value.addEventListener('scroll', handleScroll)
  }
  initScrollPosition()
})

onUnmounted(() => {
  if (svgContainerRef.value) {
    svgContainerRef.value.removeEventListener('scroll', handleScroll)
  }
})
</script>

<style scoped>
.diagram-viewer {
  width: 100%;
  height: 100%;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.diagram-content {
  position: relative;
  width: 100%;
  height: 100%;
}

.svg-wrapper {
  position: relative;
  width: 100%;
}

.svg-container {
  width: 100%;
  height: calc(100vh - 300px); /* 减去头部和其他元素的高度 */
  min-height: 500px;
  max-height: 800px;
  overflow: auto;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  padding: 24px; /* 增加内边距，确保顶部有足够空间 */
  background: #fff;
  /* 确保可以滚动到顶部 */
  scroll-behavior: smooth;
  position: relative;
}

.svg-container::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.svg-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.svg-container::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 4px;
}

.svg-container::-webkit-scrollbar-thumb:hover {
  background: #555;
}

/* SVG 内容包装器 - 应用缩放变换 */
.svg-content {
  width: 100%;
  min-height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  /* 缩放时保持居中 */
  transition: transform 0.2s ease;
}

/* 确保 SVG 内容从顶部开始显示，并有足够的上边距 */
.svg-content :deep(svg) {
  display: block;
  margin: 0 auto;
  max-width: 100%;
  height: auto;
  vertical-align: top;
  /* 添加上边距，确保顶部内容可见 */
  padding-top: 20px;
}

/* 确保 SVG 容器内的所有内容都可见 */
.svg-content :deep(*) {
  overflow: visible !important;
}

/* 回到顶部按钮 */
.back-to-top {
  position: fixed;
  right: 40px;
  bottom: 40px;
  z-index: 1000;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.back-to-top:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

.mermaid-container {
  width: 100%;
  max-height: 600px;
  overflow: auto;
}

.diagram-stats {
  margin-top: 16px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 4px;
}

.diagram-warnings {
  margin-top: 16px;
}

.diagram-warnings ul {
  margin: 8px 0 0 0;
  padding-left: 20px;
}

.diagram-warnings li {
  margin: 4px 0;
}
</style>
