<template>
  <section id="page-ai-generate" class="page-view active">
    <div class="max-w-5xl mx-auto h-[calc(100vh-200px)] flex flex-col">
      <!-- 初始状态 -->
      <div v-if="!showResult" class="flex-1 flex flex-col items-center justify-center text-center">
        <div class="w-24 h-24 bg-gradient-to-tr from-amber-600/20 to-yellow-500/20 rounded-3xl flex items-center justify-center mb-8 relative">
          <div class="absolute inset-0 bg-amber-500 animate-ping opacity-10 rounded-3xl"></div>
          <i class="fas fa-atom text-amber-500 text-4xl"></i>
        </div>
        <h2 class="text-5xl font-black text-white mb-6 tracking-tight">AI 架构对话引擎</h2>
        <p class="text-neutral-500 max-w-2xl mb-12 text-lg">无需编写任何 SQL，只需描述你的业务流。AI 将为您生成最优的规范化模型，并自动补全索引建议。</p>

        <div class="w-full max-w-3xl relative group">
          <div class="absolute -inset-1 bg-gradient-to-r from-amber-600/30 to-blue-500/30 rounded-[2rem] blur-xl opacity-0 group-hover:opacity-100 transition duration-500"></div>
          <div class="relative bg-[#0a0a0a] border border-neutral-800 rounded-[2rem] p-3 flex flex-col">
            <textarea 
              v-model="formData.prompt"
              rows="4" 
              class="w-full bg-transparent border-none outline-none text-white px-6 py-4 resize-none text-lg placeholder-neutral-700" 
              placeholder="描述你想要构建的系统，例如：创建一个带有等级、经验值和任务系统的游戏后端模型..."
              @keydown.enter.ctrl="handleGenerate"
            ></textarea>
            <div class="flex items-center justify-between px-4 pb-2">
              <div class="flex space-x-2">
                <button @click="showOptions = !showOptions" class="p-2 text-neutral-500 hover:text-amber-500 transition-colors" title="生成选项">
                  <i class="fas fa-cog"></i>
                </button>
              </div>
              <button @click="handleGenerate" :disabled="loading || !formData.prompt.trim()" class="btn-amber w-12 h-12 rounded-2xl flex items-center justify-center text-white text-lg shadow-xl disabled:opacity-50 disabled:cursor-not-allowed">
                <i v-if="!loading" class="fas fa-bolt"></i>
                <i v-else class="fas fa-spinner fa-spin"></i>
              </button>
            </div>
            
            <!-- 生成选项 -->
            <div v-if="showOptions" class="px-6 pb-4 space-y-3 border-t border-neutral-800 pt-4 mt-2">
              <div class="flex items-center justify-between">
                <span class="text-sm text-neutral-400">数据库类型</span>
                <select v-model="formData.dbType" class="bg-neutral-900 border border-neutral-800 rounded-lg px-3 py-1 text-sm text-white">
                  <option value="mysql">MySQL</option>
                  <option value="postgres">PostgreSQL</option>
                </select>
              </div>
              <div class="flex items-center justify-between">
                <span class="text-sm text-neutral-400">命名风格</span>
                <select v-model="formData.namingStyle" class="bg-neutral-900 border border-neutral-800 rounded-lg px-3 py-1 text-sm text-white">
                  <option value="snake_case">snake_case</option>
                  <option value="camelCase">camelCase</option>
                </select>
              </div>
              <div class="flex items-center justify-between">
                <span class="text-sm text-neutral-400">生成中间表</span>
                <input type="checkbox" v-model="formData.generateJunctionTables" class="w-4 h-4">
              </div>
              <div class="flex items-center justify-between">
                <span class="text-sm text-neutral-400">生成索引建议</span>
                <input type="checkbox" v-model="formData.generateIndexes" class="w-4 h-4">
              </div>
              <div class="flex items-center justify-between">
                <span class="text-sm text-neutral-400">生成注释</span>
                <input type="checkbox" v-model="formData.generateComments" class="w-4 h-4">
              </div>
            </div>
          </div>
        </div>

        <div v-if="!loading" id="ai-suggestion-pills" class="mt-10 flex flex-wrap justify-center gap-3">
          <button @click="fillAIInput('多租户商城系统模型，包含秒杀抢购逻辑')" class="px-4 py-2 bg-neutral-900 border border-neutral-800 rounded-full text-[11px] font-bold text-neutral-500 hover:border-amber-600 hover:text-amber-500 transition-all"># 商城系统</button>
          <button @click="fillAIInput('设计一个基于 RBAC 的用户权限模型，并支持审计日志')" class="px-4 py-2 bg-neutral-900 border border-neutral-800 rounded-full text-[11px] font-bold text-neutral-500 hover:border-amber-600 hover:text-amber-500 transition-all"># 权限模型</button>
          <button @click="fillAIInput('一个支持多端协作的任务管理系统看板模型')" class="px-4 py-2 bg-neutral-900 border border-neutral-800 rounded-full text-[11px] font-bold text-neutral-500 hover:border-amber-600 hover:text-amber-500 transition-all"># 协同办公</button>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="flex-1 flex flex-col items-center justify-center">
        <div class="flex space-x-2 mb-4">
          <div class="w-3 h-3 bg-amber-600 rounded-full animate-bounce"></div>
          <div class="w-3 h-3 bg-amber-600 rounded-full animate-bounce" style="animation-delay: 0.1s"></div>
          <div class="w-3 h-3 bg-amber-600 rounded-full animate-bounce" style="animation-delay: 0.2s"></div>
        </div>
        <div class="text-[10px] font-bold text-neutral-600 uppercase tracking-widest">Coffee AI 正在思考并推导关系模型...</div>
      </div>

      <!-- 结果展示 -->
      <div v-if="showResult && !loading" class="flex-1 flex flex-col overflow-hidden">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-2xl font-black text-white">AI 生成结果</h3>
          <button @click="resetForm" class="px-4 py-2 bg-neutral-900 border border-neutral-800 rounded-xl text-sm font-bold hover:bg-neutral-800 transition-all text-neutral-400">
            <i class="fas fa-arrow-left mr-2"></i> 重新生成
          </button>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 flex-1 min-h-0">
          <!-- 左侧：业务说明和 SQL -->
          <div class="flex flex-col space-y-4 overflow-auto">
            <!-- 业务说明 -->
            <div v-if="result.aiExplanation" class="glass-card p-6 rounded-2xl">
              <h4 class="text-sm font-bold text-amber-500 mb-3 flex items-center">
                <i class="fas fa-lightbulb mr-2"></i> 业务说明
              </h4>
              <p class="text-sm text-neutral-300 leading-relaxed">{{ result.aiExplanation }}</p>
            </div>

            <!-- AI 建议 -->
            <div v-if="result.aiSuggestions && result.aiSuggestions.length > 0" class="glass-card p-6 rounded-2xl">
              <h4 class="text-sm font-bold text-blue-500 mb-3 flex items-center">
                <i class="fas fa-magic mr-2"></i> AI 建议
              </h4>
              <ul class="space-y-2">
                <li v-for="(suggestion, idx) in result.aiSuggestions" :key="idx" class="text-sm text-neutral-400 flex items-start">
                  <i class="fas fa-check-circle text-green-500 mr-2 mt-0.5"></i>
                  <span>{{ suggestion }}</span>
                </li>
              </ul>
            </div>

            <!-- SQL DDL -->
            <div class="glass-card p-6 rounded-2xl flex-1 flex flex-col min-h-0">
              <div class="flex items-center justify-between mb-3">
                <h4 class="text-sm font-bold text-purple-500 flex items-center">
                  <i class="fas fa-code mr-2"></i> 生成的 SQL DDL
                </h4>
                <button @click="copySql" class="text-xs text-neutral-500 hover:text-white transition-colors">
                  <i class="fas fa-copy mr-1"></i> 复制
                </button>
              </div>
              <pre class="flex-1 bg-black/50 rounded-xl p-4 text-xs text-amber-100 overflow-auto code-font">{{ result.sqlDdl }}</pre>
            </div>
          </div>

          <!-- 右侧：ER 图 -->
          <div class="flex flex-col min-h-0">
            <div class="glass-card p-6 rounded-2xl flex-1 flex flex-col min-h-0">
              <div class="flex items-center justify-between mb-3">
                <h4 class="text-sm font-bold text-amber-500 flex items-center">
                  <i class="fas fa-diagram-project mr-2"></i> ER 图预览
                </h4>
                <div class="flex space-x-2">
                  <button @click="handleSave" class="text-xs px-3 py-1.5 bg-green-600 rounded-lg text-white hover:bg-green-700 transition-colors">
                    <i class="fas fa-save mr-1"></i> 保存
                  </button>
                  <button @click="handleDownload" class="text-xs px-3 py-1.5 bg-amber-600 rounded-lg text-white hover:bg-amber-700 transition-colors">
                    <i class="fas fa-download mr-1"></i> 下载
                  </button>
                </div>
              </div>
              <div class="flex-1 bg-black/50 rounded-xl p-4 overflow-auto" v-html="result.svgContent"></div>
              <div class="mt-3 flex items-center justify-between text-xs text-neutral-500">
                <span><i class="fas fa-table mr-1"></i> {{ result.tableCount }} 张表</span>
                <span><i class="fas fa-link mr-1"></i> {{ result.relationCount }} 个关系</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useMessage } from 'naive-ui'
import { useRouter } from 'vue-router'
import api from '@/api/index.js'

const message = useMessage()
const router = useRouter()
const loading = ref(false)
const showResult = ref(false)
const showOptions = ref(false)

const formData = reactive({
  prompt: '',
  dbType: 'mysql',
  namingStyle: 'snake_case',
  generateJunctionTables: true,
  generateIndexes: true,
  generateComments: true
})

const result = reactive({
  mermaidCode: '',
  svgContent: '',
  pngBase64: '',
  sqlDdl: '',
  aiExplanation: '',
  aiSuggestions: [],
  tableCount: 0,
  relationCount: 0
})

const fillAIInput = (text) => {
  formData.prompt = text
}

const handleGenerate = async () => {
  if (!formData.prompt.trim()) {
    message.warning('请输入业务需求描述')
    return
  }
  
  loading.value = true
  try {
    const res = await api.post('/api/ai/generate', formData)
    
    if (res.code === 200) {
      Object.assign(result, res.data)
      showResult.value = true
      message.success('AI 生成成功')
    } else {
      message.error(res.message || 'AI 生成失败')
    }
    
  } catch (error) {
    console.error('AI 生成失败:', error)
    message.error(error.response?.data?.message || 'AI 生成失败，请重试')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  showResult.value = false
  formData.prompt = ''
  Object.assign(result, {
    mermaidCode: '',
    svgContent: '',
    pngBase64: '',
    sqlDdl: '',
    aiExplanation: '',
    aiSuggestions: [],
    tableCount: 0,
    relationCount: 0
  })
}

const copySql = () => {
  navigator.clipboard.writeText(result.sqlDdl)
  message.success('SQL 已复制到剪贴板')
}

const handleSave = () => {
  // 跳转到 SQL 导入页面，并填充 SQL
  router.push({
    path: '/sql-import',
    query: { sql: result.sqlDdl }
  })
}

const handleDownload = () => {
  const timestamp = new Date().toISOString().slice(0, 19).replace(/:/g, '-')
  const filename = `ai-generated-${timestamp}.sql`
  
  const blob = new Blob([result.sqlDdl], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  
  message.success('SQL 文件下载成功')
}
</script>

<style scoped>
.code-font {
  font-family: 'Fira Code', 'Consolas', 'Monaco', monospace;
}
</style>
