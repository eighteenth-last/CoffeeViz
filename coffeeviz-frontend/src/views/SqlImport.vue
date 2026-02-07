<template>
  <section id="page-sql-import" class="page-view active">
    <div class="flex flex-col h-[calc(100vh-180px)]">
      <div class="flex items-center justify-between mb-8">
        <div>
          <h2 class="text-2xl font-black text-white">SQL 智能解析</h2>
          <p class="text-neutral-500 text-sm italic font-mono">输入 DDL 脚本以生成 Mermaid 架构图。</p>
        </div>
        <div class="flex items-center space-x-3">
          <!-- <div class="flex bg-neutral-900 p-1 rounded-xl border border-neutral-800">
            <button @click="loadExample('mysql')" class="px-4 py-1.5 rounded-lg text-xs font-bold hover:text-white text-neutral-500 hover:bg-neutral-800 transition-all">MySQL 示例</button>
            <button @click="loadExample('postgres')" class="px-4 py-1.5 rounded-lg text-xs font-bold hover:text-white text-neutral-500 hover:bg-neutral-800 transition-all">Postgres 示例</button>
          </div> -->
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
              <button title="放大" class="w-10 h-10 bg-neutral-900 border border-neutral-800 rounded-xl text-neutral-400 hover:text-white hover:border-amber-500 flex items-center justify-center"><i class="fas fa-plus"></i></button>
              <button title="缩小" class="w-10 h-10 bg-neutral-900 border border-neutral-800 rounded-xl text-neutral-400 hover:text-white hover:border-amber-500 flex items-center justify-center"><i class="fas fa-minus"></i></button>
              <button title="自适应" class="w-10 h-10 bg-neutral-900 border border-neutral-800 rounded-xl text-neutral-400 hover:text-white hover:border-amber-500 flex items-center justify-center"><i class="fas fa-expand"></i></button>
              <div class="h-[1px] bg-neutral-800"></div>
              <button @click="handleSaveProject" title="保存" class="w-10 h-10 bg-amber-600 rounded-xl text-white flex items-center justify-center shadow-lg"><i class="fas fa-save"></i></button>
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
  </section>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useProjectStore } from '@/store/project'
import { useMessage } from 'naive-ui'

const projectStore = useProjectStore()
const message = useMessage()

const sqlText = ref('')
const logs = ref([])

const parseOptions = reactive({
  viewMode: 'PHYSICAL',
  inferRelations: true,
  relationDepth: 1
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
    const consoleEl = document.querySelector('#parse-console').parentElement
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
    
    addLog('success', 'Parsing completed successfully')
    
    if (projectStore.diagramData.warnings?.length) {
      projectStore.diagramData.warnings.forEach(w => addLog('warning', w))
    }
  } catch (error) {
    addLog('error', `Parsing failed: ${error.message}`)
    message.error(error.message || '解析失败')
  }
}

const handleSaveProject = () => {
  // Logic to save project
  message.success('项目保存成功')
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