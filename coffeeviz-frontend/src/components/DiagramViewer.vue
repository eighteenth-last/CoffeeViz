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
        <div v-if="viewType === 'svg'" class="svg-container" v-html="svgContent"></div>
        
        <!-- Mermaid 代码视图 -->
        <div v-else class="mermaid-container">
          <n-code :code="mermaidCode" language="mermaid" />
        </div>
        
        <!-- 统计信息 -->
        <div class="diagram-stats">
          <n-space>
            <n-tag type="info">表数量: {{ tableCount }}</n-tag>
            <n-tag type="success">关系数量: {{ relationCount }}</n-tag>
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
import { ref, computed } from 'vue'
import { NIcon } from 'naive-ui'
import { DownloadOutline } from '@vicons/ionicons5'
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

const hasDiagram = computed(() => {
  return !!props.mermaidCode || !!props.svgContent
})

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
</script>

<style scoped>
.diagram-viewer {
  width: 100%;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.diagram-content {
  position: relative;
}

.svg-container {
  width: 100%;
  overflow: auto;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  padding: 16px;
  background: #fff;
}

.mermaid-container {
  width: 100%;
  max-height: 600px;
  overflow: auto;
}

.diagram-stats {
  margin-top: 16px;
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
