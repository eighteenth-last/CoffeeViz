<template>
  <section id="page-sql-import" class="page-view active">
    <div class="flex flex-col h-[calc(100vh-180px)]">
      <div class="flex items-center justify-between mb-8">
        <div>
          <h2 class="text-2xl font-black text-white">SQL 智能解析</h2>
          <p class="text-neutral-500 text-sm italic font-mono">输入 DDL 脚本以生成 Mermaid 架构图。</p>
        </div>
        <div class="flex items-center space-x-3">
          <button 
            @click="clearWorkspace" 
            class="px-5 py-2.5 bg-neutral-900 border border-neutral-800 rounded-xl text-sm font-bold hover:bg-neutral-800 hover:border-red-600/50 hover:text-red-500 transition-all text-neutral-400 flex items-center">
            <i class="fas fa-trash-alt mr-2"></i> 清空
          </button>
          <button @click="handleParse" :disabled="projectStore.loading" class="btn-amber text-white px-8 py-2.5 rounded-xl text-sm font-black flex items-center disabled:opacity-50 disabled:cursor-not-allowed">
            <i class="fas fa-play mr-2" :class="{'fa-spin': projectStore.loading}"></i> {{ projectStore.loading ? '解析中...' : '执行解析' }}
          </button>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 flex-1 min-h-0">
        <!-- Left Column: Editor & Console -->
        <div class="flex flex-col space-y-4 min-h-0">
          <div class="flex-1 bg-black border border-neutral-800 rounded-2xl overflow-hidden focus-within:border-amber-600 transition-all flex flex-col min-h-0">
            <div class="h-10 bg-neutral-900 border-b border-neutral-800 flex items-center px-4 justify-between shrink-0">
              <span class="text-[10px] font-bold text-neutral-500 uppercase font-mono tracking-widest">编辑器: dialect_auto.sql</span>
              <div class="flex space-x-2">
                <div class="w-2.5 h-2.5 rounded-full bg-red-500/20 border border-red-500/50"></div>
                <div class="w-2.5 h-2.5 rounded-full bg-yellow-500/20 border border-yellow-500/50"></div>
                <div class="w-2.5 h-2.5 rounded-full bg-green-500/20 border border-green-500/50"></div>
              </div>
            </div>
            <textarea 
              v-model="sqlText"
              id="sql-editor" 
              class="flex-1 bg-transparent p-6 code-font text-sm text-amber-100 outline-none resize-none custom-scrollbar" 
              placeholder="-- 请粘贴您的 DDL 脚本
CREATE TABLE orders (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2)
);">
            </textarea>
          </div>

          <!-- Console Output -->
          <div class="h-40 bg-[#080808] border border-neutral-900 rounded-2xl p-4 code-font text-[11px] overflow-y-auto custom-scrollbar shadow-inner shrink-0">
            <div class="flex items-center text-neutral-500 mb-2 border-b border-neutral-900 pb-2">
              <i class="fas fa-terminal mr-2"></i> OUTPUT CONSOLE
            </div>
            <div id="parse-console" class="space-y-1">
              <div v-if="logs.length === 0" class="text-neutral-600">> Ready for input...</div>
              <div v-for="(log, idx) in logs" :key="idx" :class="getLogClass(log.type)">
                <span class="text-neutral-500">[{{ log.time }}]</span> {{ log.message }}
              </div>
            </div>
          </div>
        </div>

        <!-- Right Column: Diagram View -->
        <div class="flex flex-col min-h-0">
          <div class="flex-1 bg-[#0a0a0a] border border-neutral-800 rounded-2xl canvas-dots relative overflow-hidden group flex flex-col min-h-0">
            <!-- Canvas Controls -->
            <div class="absolute top-4 right-4 z-10 flex flex-col space-y-2 opacity-0 group-hover:opacity-100 transition-opacity">
              <button @click="handleZoomIn" title="放大" class="w-10 h-10 bg-neutral-900 border border-neutral-800 rounded-xl text-neutral-400 hover:text-white hover:border-amber-500 flex items-center justify-center transition-all"><i class="fas fa-plus"></i></button>
              <button @click="handleZoomOut" title="缩小" class="w-10 h-10 bg-neutral-900 border border-neutral-800 rounded-xl text-neutral-400 hover:text-white hover:border-amber-500 flex items-center justify-center transition-all"><i class="fas fa-minus"></i></button>
              <button @click="handleFullscreen" title="全屏显示" class="w-10 h-10 bg-neutral-900 border border-neutral-800 rounded-xl text-neutral-400 hover:text-white hover:border-amber-500 flex items-center justify-center transition-all"><i class="fas fa-expand"></i></button>
              <div class="h-[1px] bg-neutral-800"></div>
              <button @click="handleSaveProject" title="保存到归档库" class="w-10 h-10 bg-green-600 rounded-xl text-white flex items-center justify-center shadow-lg hover:bg-green-700 transition-all"><i class="fas fa-save"></i></button>
              <button @click="handleDownload" title="下载到本地" class="w-10 h-10 bg-amber-600 rounded-xl text-white flex items-center justify-center shadow-lg hover:bg-amber-700 transition-all"><i class="fas fa-download"></i></button>
            </div>

            <div id="diagram-view" class="w-full h-full flex items-center justify-center p-10 overflow-auto">
              <div v-if="!projectStore.diagramData.svgContent" class="text-center">
                <div class="w-20 h-20 bg-neutral-900 border border-neutral-800 rounded-3xl flex items-center justify-center mx-auto mb-6">
                  <i class="fas fa-image text-neutral-700 text-3xl"></i>
                </div>
                <h4 class="text-white font-bold mb-2">架构画布已就绪</h4>
                <p class="text-neutral-600 text-xs max-w-[200px] mx-auto">点击上方按钮执行脚本解析，画布将展示生成的 Mermaid ER 图</p>
              </div>
              <div v-else v-html="projectStore.diagramData.svgContent" class="w-full h-full flex items-center justify-center"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Save Project Modal -->
    <div v-if="showSaveModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-black/80 backdrop-blur-sm" @click="showSaveModal = false"></div>
      <div class="glass-card w-full max-w-2xl rounded-3xl p-8 relative z-10 border border-neutral-800 shadow-2xl animate-fade-in-up max-h-[90vh] overflow-y-auto">
        <div class="flex items-center justify-between mb-8">
          <h2 class="text-2xl font-black text-white">保存到归档库</h2>
          <button @click="showSaveModal = false" class="w-8 h-8 rounded-full bg-neutral-900 flex items-center justify-center text-neutral-500 hover:text-white transition-colors">
            <i class="fas fa-times"></i>
          </button>
        </div>
        
        <div class="space-y-6">
          <!-- 选择架构库 -->
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">选择架构库</label>
            
            <!-- 加载中 -->
            <div v-if="loadingRepositories" class="flex items-center justify-center py-8">
              <i class="fas fa-spinner fa-spin text-2xl text-amber-600"></i>
            </div>
            
            <!-- 架构库列表 -->
            <div v-else class="space-y-2 max-h-48 overflow-y-auto mb-4">
              <!-- 新建架构库选项 -->
              <div 
                @click="selectedRepositoryId = 'new'"
                :class="['p-4 rounded-xl border cursor-pointer transition-all', selectedRepositoryId === 'new' ? 'bg-green-600/20 border-green-600' : 'bg-neutral-900/50 border-neutral-800 hover:border-green-600/50']"
              >
                <div class="flex items-center justify-between">
                  <div class="flex items-center">
                    <div class="w-10 h-10 rounded-lg bg-green-600/20 border border-green-600 flex items-center justify-center mr-3">
                      <i class="fas fa-plus text-green-600"></i>
                    </div>
                    <div>
                      <h3 class="text-white font-bold">新建架构库</h3>
                      <p class="text-sm text-neutral-500">创建一个新的架构库来保存此图</p>
                    </div>
                  </div>
                  <div v-if="selectedRepositoryId === 'new'">
                    <i class="fas fa-check-circle text-green-600 text-xl"></i>
                  </div>
                </div>
              </div>
              
              <!-- 已有架构库 -->
              <div 
                v-for="repo in existingRepositories" 
                :key="repo.id"
                @click="selectedRepositoryId = repo.id"
                :class="['p-4 rounded-xl border cursor-pointer transition-all', selectedRepositoryId === repo.id ? 'bg-amber-600/20 border-amber-600' : 'bg-neutral-900/50 border-neutral-800 hover:border-amber-600/50']"
              >
                <div class="flex items-start justify-between">
                  <div class="flex-1">
                    <h3 class="text-white font-bold mb-1">{{ repo.repositoryName }}</h3>
                    <p class="text-sm text-neutral-500 line-clamp-1">{{ repo.description || '暂无描述' }}</p>
                    <div class="flex items-center space-x-4 mt-2 text-xs text-neutral-600">
                      <span><i class="fas fa-folder mr-1"></i>{{ repo.diagramCount || 0 }} 个架构图</span>
                      <span><i class="fas fa-clock mr-1"></i>{{ formatDate(repo.updateTime) }}</span>
                    </div>
                  </div>
                  <div v-if="selectedRepositoryId === repo.id" class="ml-4">
                    <i class="fas fa-check-circle text-amber-600 text-xl"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 新建架构库表单 -->
          <div v-if="selectedRepositoryId === 'new'" class="space-y-4 p-4 bg-green-600/10 border border-green-600/30 rounded-xl">
            <div>
              <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">架构库名称</label>
              <input 
                v-model="saveFormData.repositoryName"
                type="text" 
                class="w-full bg-black/50 border border-neutral-800 rounded-xl px-4 py-3 text-white outline-none focus:border-green-600 transition-all placeholder-neutral-700"
                placeholder="例如：电商系统架构库"
              >
            </div>
            
            <div>
              <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">架构库描述 <span class="text-neutral-700 font-normal normal-case">(可选)</span></label>
              <textarea 
                v-model="saveFormData.repositoryDescription"
                rows="2" 
                class="w-full bg-black/50 border border-neutral-800 rounded-xl px-4 py-3 text-white outline-none focus:border-green-600 transition-all resize-none placeholder-neutral-700"
                placeholder="简要描述该架构库的用途..."
              ></textarea>
            </div>
          </div>

          <!-- 架构图信息 -->
          <div class="space-y-4">
            <div>
              <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">架构图名称</label>
              <input 
                v-model="saveFormData.diagramName"
                type="text" 
                class="w-full bg-black/50 border border-neutral-800 rounded-xl px-4 py-3 text-white outline-none focus:border-amber-600 transition-all placeholder-neutral-700"
                placeholder="例如：用户订单模块 v1.0"
              >
            </div>
            
            <div>
              <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">架构图描述 <span class="text-neutral-700 font-normal normal-case">(可选)</span></label>
              <textarea 
                v-model="saveFormData.diagramDescription"
                rows="3" 
                class="w-full bg-black/50 border border-neutral-800 rounded-xl px-4 py-3 text-white outline-none focus:border-amber-600 transition-all resize-none placeholder-neutral-700"
                placeholder="简要描述该架构图的业务背景或技术栈..."
              ></textarea>
            </div>
          </div>

          <div class="bg-neutral-900/50 border border-neutral-800 rounded-xl p-4">
            <div class="flex items-center justify-between text-sm mb-2">
              <span class="text-neutral-500">表数量</span>
              <span class="text-white font-bold">{{ projectStore.diagramData.tableCount || 0 }}</span>
            </div>
            <div class="flex items-center justify-between text-sm">
              <span class="text-neutral-500">关系数量</span>
              <span class="text-white font-bold">{{ projectStore.diagramData.relationCount || 0 }}</span>
            </div>
          </div>
          
          <div class="pt-4 flex space-x-4">
            <button @click="showSaveModal = false" class="flex-1 py-3 rounded-xl border border-neutral-800 text-neutral-400 hover:text-white hover:bg-neutral-800 transition-all font-bold">取消</button>
            <button 
              @click="handleConfirmSave" 
              :disabled="!selectedRepositoryId || !saveFormData.diagramName || (selectedRepositoryId === 'new' && !saveFormData.repositoryName) || saving"
              class="flex-1 py-3 bg-green-600 hover:bg-green-500 rounded-xl text-white font-bold shadow-lg shadow-green-900/20 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
            >
              <i v-if="saving" class="fas fa-spinner fa-spin mr-2"></i>
              <span>{{ saving ? '保存中...' : '保存架构图' }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useProjectStore } from '@/store/project'
import { useMessage, useDialog } from 'naive-ui'

const projectStore = useProjectStore()
const message = useMessage()
const dialog = useDialog()

const sqlText = ref('')
const logs = ref([])
const diagramScale = ref(1)
const diagramTransform = ref({ x: 0, y: 0 })
const showSaveModal = ref(false)
const saving = ref(false)
const existingRepositories = ref([])
const loadingRepositories = ref(false)
const selectedRepositoryId = ref(null)

const parseOptions = reactive({
  viewMode: 'PHYSICAL',
  inferRelations: true,
  relationDepth: 1
})

const saveFormData = reactive({
  repositoryName: '',
  repositoryDescription: '',
  diagramName: '',
  diagramDescription: ''
})

// 模拟日志功能
const addLog = (type, messageText) => {
  const time = new Date().toLocaleTimeString()
  logs.value.push({
    type,
    time,
    message: messageText
  })
  // Auto scroll to bottom
  setTimeout(() => {
    const consoleEl = document.querySelector('#parse-console')?.parentElement
    if (consoleEl) consoleEl.scrollTop = consoleEl.scrollHeight
  }, 10)
}

const getLogClass = (type) => {
  switch (type) {
    case 'error': return 'text-red-500'
    case 'success': return 'text-green-500'
    case 'warning': return 'text-yellow-500'
    default: return 'text-neutral-400'
  }
}

const handleParse = async () => {
  if (!sqlText.value.trim()) {
    message.warning('请先输入 SQL 脚本')
    return
  }

  addLog('info', 'Starting SQL parsing...')
  try {
    // 调用 Store action
    await projectStore.generateFromSql(sqlText.value, parseOptions)
    
    addLog('success', `Parsing completed successfully (${projectStore.diagramData.tableCount} tables, ${projectStore.diagramData.relationCount} relations)`)
    
    if (projectStore.diagramData.warnings?.length) {
      projectStore.diagramData.warnings.forEach(w => addLog('warning', w))
    }
    
    // 重置缩放
    diagramScale.value = 1
    message.success(`解析成功！共 ${projectStore.diagramData.tableCount} 张表`)
  } catch (error) {
    addLog('error', `Parsing failed: ${error.message}`)
    message.error(error.message || '解析失败')
  }
}

// 放大
const handleZoomIn = () => {
  if (diagramScale.value < 3) {
    diagramScale.value = Math.min(3, diagramScale.value + 0.2)
    applyTransform()
    addLog('info', `Zoom in: ${Math.round(diagramScale.value * 100)}%`)
  } else {
    message.info('已达到最大缩放比例')
  }
}

// 缩小
const handleZoomOut = () => {
  if (diagramScale.value > 0.3) {
    diagramScale.value = Math.max(0.3, diagramScale.value - 0.2)
    applyTransform()
    addLog('info', `Zoom out: ${Math.round(diagramScale.value * 100)}%`)
  } else {
    message.info('已达到最小缩放比例')
  }
}

// 全屏显示
const handleFullscreen = () => {
  const diagramContainer = document.querySelector('#diagram-view')
  if (!diagramContainer) return

  if (!document.fullscreenElement) {
    diagramContainer.requestFullscreen().then(() => {
      addLog('info', 'Entered fullscreen mode')
      message.success('已进入全屏模式，按 ESC 退出')
    }).catch(err => {
      addLog('error', `Fullscreen failed: ${err.message}`)
      message.error('全屏失败')
    })
  } else {
    document.exitFullscreen().then(() => {
      addLog('info', 'Exited fullscreen mode')
    })
  }
}

// 应用变换
const applyTransform = () => {
  const diagramView = document.querySelector('#diagram-view > div:last-child')
  if (diagramView) {
    diagramView.style.transform = `scale(${diagramScale.value}) translate(${diagramTransform.value.x}px, ${diagramTransform.value.y}px)`
    diagramView.style.transformOrigin = 'center center'
    diagramView.style.transition = 'transform 0.3s ease'
  }
}

// 下载到本地
const handleDownload = () => {
  if (!projectStore.diagramData.mermaidCode) {
    message.warning('请先生成图表')
    return
  }

  // 创建下载选项对话框
  const downloadType = prompt('请选择下载格式：\n1. Mermaid 源码 (.mmd)\n2. SVG 图片 (.svg)\n3. PNG 图片 (.png)\n\n请输入数字 1-3：', '1')
  
  if (!downloadType) return

  const timestamp = new Date().toISOString().slice(0, 19).replace(/:/g, '-')
  const filename = `coffeeviz-diagram-${timestamp}`

  try {
    switch (downloadType) {
      case '1':
        // 下载 Mermaid 源码
        downloadFile(projectStore.diagramData.mermaidCode, `${filename}.mmd`, 'text/plain')
        addLog('success', `Downloaded: ${filename}.mmd`)
        message.success('Mermaid 源码下载成功')
        break
      
      case '2':
        // 下载 SVG
        if (projectStore.diagramData.svgContent) {
          downloadFile(projectStore.diagramData.svgContent, `${filename}.svg`, 'image/svg+xml')
          addLog('success', `Downloaded: ${filename}.svg`)
          message.success('SVG 图片下载成功')
        } else {
          message.warning('SVG 内容不可用')
        }
        break
      
      case '3':
        // 下载 PNG
        if (projectStore.diagramData.pngBase64) {
          // 检查是否包含 data URL 前缀
          const base64Data = projectStore.diagramData.pngBase64.includes('base64,')
            ? projectStore.diagramData.pngBase64.split('base64,')[1]
            : projectStore.diagramData.pngBase64
          
          downloadBase64File(base64Data, `${filename}.png`, 'image/png')
          addLog('success', `Downloaded: ${filename}.png`)
          message.success('PNG 图片下载成功')
        } else {
          message.warning('PNG 内容不可用，可能后端未安装 Mermaid CLI')
        }
        break
      
      default:
        message.warning('无效的选项')
    }
  } catch (error) {
    addLog('error', `Download failed: ${error.message}`)
    message.error('下载失败')
  }
}

// 下载文件辅助函数
const downloadFile = (content, filename, mimeType) => {
  const blob = new Blob([content], { type: mimeType })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

// 下载 Base64 文件
const downloadBase64File = (base64Data, filename, mimeType) => {
  try {
    // Base64 数据已经是纯净的（前端已经处理过前缀）
    const byteCharacters = atob(base64Data)
    const byteNumbers = new Array(byteCharacters.length)
    for (let i = 0; i < byteCharacters.length; i++) {
      byteNumbers[i] = byteCharacters.charCodeAt(i)
    }
    const byteArray = new Uint8Array(byteNumbers)
    const blob = new Blob([byteArray], { type: mimeType })
    
    // 创建下载链接
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  } catch (error) {
    console.error('Base64 decode error:', error)
    throw new Error('PNG 数据格式错误，可能后端未正确生成')
  }
}

// 保存项目到归档库
const handleSaveProject = async () => {
  if (!projectStore.diagramData.mermaidCode) {
    message.warning('请先生成 ER 图')
    return
  }
  
  // 加载已有架构库列表
  loadingRepositories.value = true
  try {
    const response = await fetch('/api/repository/list?page=1&size=100', {
      headers: {
        'Authorization': localStorage.getItem('token') || ''
      }
    })
    const result = await response.json()
    if (result.code === 200) {
      existingRepositories.value = result.data.list || []
    }
  } catch (error) {
    console.error('加载架构库列表失败:', error)
  } finally {
    loadingRepositories.value = false
  }
  
  // 打开保存弹窗
  showSaveModal.value = true
  selectedRepositoryId.value = null
  addLog('info', 'Opening save diagram dialog...')
}

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

// 确认保存架构图
const handleConfirmSave = async () => {
  if (!selectedRepositoryId.value) {
    message.warning('请选择架构库')
    return
  }
  
  if (!saveFormData.diagramName) {
    message.warning('请输入架构图名称')
    return
  }
  
  if (selectedRepositoryId.value === 'new' && !saveFormData.repositoryName) {
    message.warning('请输入架构库名称')
    return
  }
  
  try {
    saving.value = true
    let repositoryId = selectedRepositoryId.value
    
    // 如果选择新建架构库，先创建架构库
    if (selectedRepositoryId.value === 'new') {
      addLog('info', `Creating repository: ${saveFormData.repositoryName}`)
      
      const repoResponse = await fetch('/api/repository/create', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': localStorage.getItem('token') || ''
        },
        body: JSON.stringify({
          repositoryName: saveFormData.repositoryName,
          description: saveFormData.repositoryDescription
        })
      })
      
      const repoResult = await repoResponse.json()
      if (repoResult.code !== 200) {
        throw new Error(repoResult.message || '创建架构库失败')
      }
      
      repositoryId = repoResult.data
      addLog('success', `Repository created: ${saveFormData.repositoryName} (ID: ${repositoryId})`)
    }
    
    // 创建架构图
    addLog('info', `Saving diagram: ${saveFormData.diagramName}`)
    
    const diagramResponse = await fetch('/api/diagram/create', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': localStorage.getItem('token') || ''
      },
      body: JSON.stringify({
        repositoryId: repositoryId,
        diagramName: saveFormData.diagramName,
        description: saveFormData.diagramDescription,
        sourceType: 'SQL',
        dbType: 'mysql',
        mermaidCode: projectStore.diagramData.mermaidCode,
        pngBase64: projectStore.diagramData.pngBase64,
        tableCount: projectStore.diagramData.tableCount,
        relationCount: projectStore.diagramData.relationCount
      })
    })
    
    const diagramResult = await diagramResponse.json()
    if (diagramResult.code !== 200) {
      throw new Error(diagramResult.message || '保存架构图失败')
    }
    
    addLog('success', `Diagram saved successfully: ${saveFormData.diagramName} (ID: ${diagramResult.data})`)
    message.success('架构图已保存到归档库')
    
    // 关闭弹窗并清空表单
    showSaveModal.value = false
    saveFormData.repositoryName = ''
    saveFormData.repositoryDescription = ''
    saveFormData.diagramName = ''
    saveFormData.diagramDescription = ''
    selectedRepositoryId.value = null
    
    // 清空编辑器和画布
    clearWorkspace()
    
  } catch (error) {
    addLog('error', `Save failed: ${error.message}`)
    message.error('保存失败: ' + (error.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

// 清空工作区
const clearWorkspace = () => {
  // 清空 SQL 编辑器
  sqlText.value = ''
  
  // 清空控制台日志
  logs.value = []
  
  // 清空图表数据
  projectStore.clearDiagram()
  
  // 重置缩放
  diagramScale.value = 1
  diagramTransform.value = { x: 0, y: 0 }
  
  addLog('info', 'Workspace cleared. Ready for new input...')
  
  message.success('工作区已清空，可以开始新的解析')
}
</script>

<style scoped>
/* Custom scrollbar for this component */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: #000;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #333;
  border-radius: 3px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #555;
}
</style>