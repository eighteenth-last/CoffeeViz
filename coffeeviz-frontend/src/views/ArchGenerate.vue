<template>
  <div class="p-8 max-w-7xl mx-auto">
    <!-- 页面标题 -->
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-white mb-2">系统功能结构图</h1>
      <p class="text-neutral-500 text-sm">从 DDL 或项目文档中提取功能模块层级，生成树状功能结构图</p>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- 左侧：输入区 -->
      <div class="space-y-4">
        <!-- 模式选择 -->
        <n-card class="bg-neutral-900/50 border-neutral-800" :bordered="true">
          <div class="mb-4">
            <n-radio-group v-model:value="mode" name="mode">
              <n-space>
                <n-radio value="ddl">
                  <span class="text-neutral-300">DDL 解析</span>
                </n-radio>
                <n-radio value="document">
                  <span class="text-neutral-300">项目文档</span>
                </n-radio>
                <n-radio value="hybrid">
                  <span class="text-neutral-300">混合模式</span>
                </n-radio>
              </n-space>
            </n-radio-group>
          </div>
          <n-tag :type="modeTagType" size="small">{{ modeDescription }}</n-tag>
        </n-card>

        <!-- DDL 输入 -->
        <n-card
          v-if="mode === 'ddl' || mode === 'hybrid'"
          title="SQL DDL"
          class="bg-neutral-900/50 border-neutral-800"
          :bordered="true" size="small"
        >
          <n-input
            v-model:value="sqlText" type="textarea"
            placeholder="粘贴 CREATE TABLE 语句..."
            :rows="mode === 'hybrid' ? 8 : 14" class="font-mono"
          />
        </n-card>

        <!-- 文档输入 -->
        <n-card
          v-if="mode === 'document' || mode === 'hybrid'"
          title="项目文档 (Markdown)"
          class="bg-neutral-900/50 border-neutral-800"
          :bordered="true" size="small"
        >
          <template #header-extra>
            <n-checkbox v-model:checked="forceAi" size="small">
              <span class="text-neutral-400 text-xs">强制 AI</span>
            </n-checkbox>
          </template>
          <n-input
            v-model:value="docContent" type="textarea"
            :placeholder="docPlaceholder"
            :rows="mode === 'hybrid' ? 8 : 14"
          />
        </n-card>

        <!-- 生成按钮 -->
        <n-button
          type="primary" block :loading="loading"
          :disabled="!canGenerate" @click="handleGenerate"
          class="h-12"
        >
          <template #icon><n-icon><FlashOutline /></n-icon></template>
          生成功能结构图
        </n-button>
      </div>

      <!-- 右侧：结果区 -->
      <div class="space-y-4">
        <!-- 结构图预览 -->
        <n-card
          title="功能结构图"
          class="bg-neutral-900/50 border-neutral-800" :bordered="true"
        >
          <template #header-extra>
            <n-space v-if="result">
              <n-button-group>
                <n-button :type="viewType === 'diagram' ? 'primary' : 'default'" size="small" @click="viewType = 'diagram'">图表</n-button>
                <n-button :type="viewType === 'code' ? 'primary' : 'default'" size="small" @click="viewType = 'code'">Mermaid</n-button>
                <n-button :type="viewType === 'tree' ? 'primary' : 'default'" size="small" @click="viewType = 'tree'">树形</n-button>
              </n-button-group>
              <n-button size="small" @click="copyMermaid">
                <template #icon><n-icon><CopyOutline /></n-icon></template>
                复制
              </n-button>
              <n-button size="small" type="success" @click="handleSaveProject">
                <template #icon><n-icon><SaveOutline /></n-icon></template>
                保存到归档库
              </n-button>
            </n-space>
          </template>

          <div v-if="!result" class="py-16">
            <n-empty description="输入 DDL 或项目文档后点击生成" />
          </div>

          <div v-else>
            <!-- Mermaid 渲染 -->
            <div v-if="viewType === 'diagram'" class="bg-white rounded-lg p-6 min-h-[400px] overflow-auto">
              <div ref="mermaidContainer" class="mermaid-render"></div>
            </div>

            <!-- Mermaid 源码 -->
            <div v-else-if="viewType === 'code'">
              <n-code :code="result.mermaidCode" language="mermaid" class="max-h-[500px] overflow-auto" />
            </div>

            <!-- 树形视图 -->
            <div v-else-if="viewType === 'tree'" class="max-h-[500px] overflow-auto">
              <n-tree
                :data="treeData"
                block-line
                default-expand-all
                :selectable="false"
              />
            </div>
          </div>
        </n-card>

        <!-- 统计信息 -->
        <n-card
          v-if="result"
          title="提取结果"
          class="bg-neutral-900/50 border-neutral-800"
          :bordered="true" size="small"
        >
          <n-space class="mb-3">
            <n-tag type="info">节点数: {{ result.nodeCount }}</n-tag>
            <n-tag :type="result.extractMethod === 'ai' ? 'warning' : 'success'">
              {{ extractMethodLabel }}
            </n-tag>
          </n-space>

          <!-- 层级概览 -->
          <div v-if="result.tree" class="text-xs text-neutral-500">
            <span class="text-neutral-300">{{ result.tree.name }}</span>
            → {{ result.tree.children?.length || 0 }} 个子系统
            → {{ leafCount }} 个功能模块
          </div>

          <!-- 处理日志 -->
          <div v-if="result.warnings && result.warnings.length > 0" class="mt-3">
            <n-collapse>
              <n-collapse-item title="处理日志" name="warnings">
                <div v-for="(w, i) in result.warnings" :key="i" class="text-xs text-neutral-500">{{ w }}</div>
              </n-collapse-item>
            </n-collapse>
          </div>
        </n-card>
      </div>
    </div>

    <!-- 保存到归档库弹窗 -->
    <n-modal v-model:show="showSaveModal" preset="card" title="保存到归档库" style="width: 600px;" :bordered="true" class="bg-neutral-900">
      <n-spin :show="loadingRepositories">
        <div class="space-y-5">
          <!-- 选择架构库 -->
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">选择架构库</label>
            <n-space vertical>
              <!-- 新建 -->
              <div
                @click="selectedRepositoryId = 'new'"
                :class="['p-3 rounded-lg border cursor-pointer transition-all', selectedRepositoryId === 'new' ? 'bg-green-600/20 border-green-600' : 'bg-neutral-800/50 border-neutral-700 hover:border-green-600/50']"
              >
                <div class="flex items-center">
                  <n-icon size="18" class="mr-2 text-green-500"><AddCircleOutline /></n-icon>
                  <span class="text-white font-bold">新建架构库</span>
                </div>
              </div>
              <!-- 已有 -->
              <div
                v-for="repo in existingRepositories" :key="repo.id"
                @click="selectedRepositoryId = repo.id"
                :class="['p-3 rounded-lg border cursor-pointer transition-all', selectedRepositoryId === repo.id ? 'bg-amber-600/20 border-amber-600' : 'bg-neutral-800/50 border-neutral-700 hover:border-amber-600/50']"
              >
                <div class="flex items-center justify-between">
                  <div>
                    <span class="text-white font-bold">{{ repo.repositoryName }}</span>
                    <span class="text-neutral-500 text-xs ml-2">{{ repo.diagramCount || 0 }} 个架构图</span>
                  </div>
                  <n-icon v-if="selectedRepositoryId === repo.id" size="18" class="text-amber-500"><CheckmarkCircleOutline /></n-icon>
                </div>
              </div>
            </n-space>
          </div>

          <!-- 新建架构库表单 -->
          <div v-if="selectedRepositoryId === 'new'" class="space-y-3 p-4 bg-green-600/10 border border-green-600/30 rounded-lg">
            <n-input v-model:value="saveFormData.repositoryName" placeholder="架构库名称" />
            <n-input v-model:value="saveFormData.repositoryDescription" type="textarea" placeholder="架构库描述（可选）" :rows="2" />
          </div>

          <!-- 架构图信息 -->
          <div class="space-y-3">
            <n-input v-model:value="saveFormData.diagramName" placeholder="架构图名称，例如：电商系统功能结构 v1.0" />
            <n-input v-model:value="saveFormData.diagramDescription" type="textarea" placeholder="架构图描述（可选）" :rows="2" />
          </div>

          <!-- 统计 -->
          <div class="flex items-center space-x-4 text-sm text-neutral-400">
            <span>节点数: {{ result?.nodeCount || 0 }}</span>
            <span>提取方式: {{ extractMethodLabel }}</span>
          </div>

          <!-- 操作按钮 -->
          <div class="flex justify-end space-x-3 pt-2">
            <n-button @click="showSaveModal = false">取消</n-button>
            <n-button
              type="success" :loading="saving"
              :disabled="!selectedRepositoryId || !saveFormData.diagramName || (selectedRepositoryId === 'new' && !saveFormData.repositoryName)"
              @click="handleConfirmSave"
            >
              保存架构图
            </n-button>
          </div>
        </div>
      </n-spin>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, reactive } from 'vue'
import { useMessage } from 'naive-ui'
import { FlashOutline, CopyOutline, SaveOutline, AddCircleOutline, CheckmarkCircleOutline } from '@vicons/ionicons5'
import api from '@/api'

const message = useMessage()

const mode = ref('document')
const sqlText = ref('')
const docContent = ref('')
const forceAi = ref(false)
const loading = ref(false)
const result = ref(null)
const viewType = ref('diagram')
const mermaidContainer = ref(null)

// 保存到归档库相关
const showSaveModal = ref(false)
const saving = ref(false)
const loadingRepositories = ref(false)
const existingRepositories = ref([])
const selectedRepositoryId = ref(null)
const saveFormData = reactive({
  repositoryName: '',
  repositoryDescription: '',
  diagramName: '',
  diagramDescription: ''
})

const docPlaceholder = `粘贴 Markdown 项目文档，支持的格式：

# 系统名称
## 子系统A
### 功能模块1
### 功能模块2
## 子系统B
- 功能模块3
- 功能模块4`

const canGenerate = computed(() => {
  if (mode.value === 'ddl') return sqlText.value.trim().length > 0
  if (mode.value === 'document') return docContent.value.trim().length > 0
  return sqlText.value.trim().length > 0 || docContent.value.trim().length > 0
})

const modeTagType = computed(() => ({ ddl: 'success', document: 'info', hybrid: 'warning' }[mode.value]))

const modeDescription = computed(() => ({
  ddl: '零 AI · 按表名前缀自动聚类为子系统 → 功能模块',
  document: '规则优先 · 从标题层级提取结构，不足时降级 AI',
  hybrid: '混合 · DDL + 文档合并生成'
}[mode.value]))

const extractMethodLabel = computed(() => {
  if (!result.value) return ''
  return { rule: '规则提取（零 AI）', ai: 'AI 智能提取', hybrid: '混合提取' }[result.value.extractMethod] || ''
})

// 计算叶子节点数
const leafCount = computed(() => {
  if (!result.value?.tree) return 0
  let count = 0
  const walk = (node) => {
    if (!node.children || node.children.length === 0) { count++; return }
    node.children.forEach(walk)
  }
  result.value.tree.children?.forEach(walk)
  return count
})

// 转换为 Naive UI Tree 数据格式
const treeData = computed(() => {
  if (!result.value?.tree) return []
  const convert = (node) => ({
    key: node.id,
    label: node.name,
    children: node.children?.map(convert) || []
  })
  return [convert(result.value.tree)]
})

const handleGenerate = async () => {
  loading.value = true
  try {
    const res = await api.post('/api/architecture/generate', {
      mode: mode.value,
      sqlText: sqlText.value || null,
      docContent: docContent.value || null,
      forceAi: forceAi.value
    })
    result.value = res.data
    message.success(`生成成功，共 ${res.data.nodeCount} 个节点`)
    await nextTick()
    renderMermaid()
  } catch (error) {
    message.error(error.message || '生成失败')
  } finally {
    loading.value = false
  }
}

const renderMermaid = async () => {
  if (!result.value?.mermaidCode || !mermaidContainer.value) return
  try {
    const mermaid = (await import('https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.esm.min.mjs')).default
    mermaid.initialize({
      startOnLoad: false,
      theme: 'base',
      themeVariables: {
        primaryColor: '#ffffff',
        primaryBorderColor: '#333333',
        primaryTextColor: '#333333',
        lineColor: '#666666',
        fontSize: '14px'
      },
      flowchart: {
        useMaxWidth: true,
        htmlLabels: true,
        curve: 'linear',
        rankSpacing: 60,
        nodeSpacing: 30
      }
    })
    // 每次渲染需要唯一 ID
    const id = 'arch-' + Date.now()
    const { svg } = await mermaid.render(id, result.value.mermaidCode)
    mermaidContainer.value.innerHTML = svg
  } catch (e) {
    console.error('Mermaid 渲染失败:', e)
    if (mermaidContainer.value) {
      mermaidContainer.value.innerHTML = `<pre style="color:#999;font-size:12px;white-space:pre-wrap">${result.value.mermaidCode}</pre>`
    }
  }
}

const copyMermaid = () => {
  if (!result.value?.mermaidCode) return
  navigator.clipboard.writeText(result.value.mermaidCode)
  message.success('已复制 Mermaid 代码')
}

// ==================== SVG → PNG 转换 ====================

const getDiagramPngBase64 = () => {
  if (!mermaidContainer.value) return Promise.resolve(null)
  const svgEl = mermaidContainer.value.querySelector('svg')
  if (!svgEl) return Promise.resolve(null)

  // 克隆 SVG 并内联所有样式，避免跨域污染
  const cloned = svgEl.cloneNode(true)

  // 确保 SVG 有 xmlns 属性
  cloned.setAttribute('xmlns', 'http://www.w3.org/2000/svg')
  cloned.setAttribute('xmlns:xlink', 'http://www.w3.org/1999/xlink')

  // 移除可能导致跨域的外部引用
  cloned.querySelectorAll('image, use[href^="http"]').forEach(el => el.remove())

  const svgData = new XMLSerializer().serializeToString(cloned)

  // 获取尺寸
  const rect = svgEl.getBoundingClientRect()
  const viewBoxAttr = svgEl.getAttribute('viewBox')
  let width = rect.width || 800
  let height = rect.height || 600
  if (viewBoxAttr) {
    const vb = viewBoxAttr.split(/\s+|,/).map(Number)
    if (vb.length === 4) { width = vb[2]; height = vb[3] }
  }

  const scale = 2
  const canvas = document.createElement('canvas')
  canvas.width = width * scale
  canvas.height = height * scale
  const ctx = canvas.getContext('2d')
  const img = new Image()

  // 关键：使用 data URI 而非 Blob URL，避免 tainted canvas
  const svgBase64 = btoa(unescape(encodeURIComponent(svgData)))
  const dataUrl = 'data:image/svg+xml;base64,' + svgBase64

  return new Promise((resolve) => {
    img.onload = () => {
      ctx.fillStyle = '#ffffff'
      ctx.fillRect(0, 0, canvas.width, canvas.height)
      ctx.drawImage(img, 0, 0, canvas.width, canvas.height)
      try {
        const base64 = canvas.toDataURL('image/png')
        resolve(base64)
      } catch (e) {
        console.error('Canvas toDataURL 失败:', e)
        resolve(null)
      }
    }
    img.onerror = (e) => {
      console.error('SVG 图片加载失败:', e)
      resolve(null)
    }
    img.src = dataUrl
  })
}

// ==================== 保存到归档库 ====================

const handleSaveProject = async () => {
  if (!result.value?.mermaidCode) {
    message.warning('请先生成功能结构图')
    return
  }

  // 确保当前是图表视图，这样 SVG 才存在
  if (viewType.value !== 'diagram') {
    viewType.value = 'diagram'
    await nextTick()
    await renderMermaid()
    // 等待渲染完成
    await new Promise(r => setTimeout(r, 500))
  }

  // 加载架构库列表
  loadingRepositories.value = true
  showSaveModal.value = true
  selectedRepositoryId.value = null
  saveFormData.repositoryName = ''
  saveFormData.repositoryDescription = ''
  saveFormData.diagramName = ''
  saveFormData.diagramDescription = ''

  try {
    const res = await api.get('/api/repository/list', { params: { page: 1, size: 100 } })
    existingRepositories.value = res.data?.list || res.data || []
  } catch (e) {
    console.error('加载架构库列表失败:', e)
  } finally {
    loadingRepositories.value = false
  }
}

const handleConfirmSave = async () => {
  if (!selectedRepositoryId.value || !saveFormData.diagramName) return

  saving.value = true
  try {
    let repositoryId = selectedRepositoryId.value

    // 新建架构库
    if (repositoryId === 'new') {
      const repoRes = await api.post('/api/repository/create', {
        repositoryName: saveFormData.repositoryName,
        description: saveFormData.repositoryDescription
      })
      repositoryId = repoRes.data
    }

    // 生成 PNG Base64 用于上传到 MinIO
    const pngBase64 = await getDiagramPngBase64()
    if (!pngBase64) {
      message.warning('PNG 图片生成失败，将仅保存 Mermaid 代码')
    }

    // 创建架构图，sourceType 为 ARCH
    await api.post('/api/diagram/create', {
      repositoryId: repositoryId,
      diagramName: saveFormData.diagramName,
      description: saveFormData.diagramDescription,
      sourceType: 'ARCH',
      mermaidCode: result.value.mermaidCode,
      pngBase64: pngBase64 || null,
      tableCount: result.value.nodeCount || 0,
      relationCount: 0
    })

    message.success('功能结构图已保存到归档库')
    showSaveModal.value = false
  } catch (e) {
    message.error('保存失败: ' + (e.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

watch(viewType, async (val) => {
  if (val === 'diagram' && result.value) {
    await nextTick()
    renderMermaid()
  }
})
</script>

<style scoped>
.mermaid-render :deep(svg) {
  max-width: 100%;
  height: auto;
}
</style>
