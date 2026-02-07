<template>
  <section id="page-ai-generate" class="page-view active">
    <div class="max-w-5xl mx-auto h-[calc(100vh-200px)] flex flex-col">
      <div class="flex-1 flex flex-col items-center justify-center text-center">
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
                <button class="p-2 text-neutral-500 hover:text-amber-500 transition-colors"><i class="fas fa-paperclip"></i></button>
                <button class="p-2 text-neutral-500 hover:text-amber-500 transition-colors"><i class="fas fa-microphone"></i></button>
              </div>
              <button @click="handleGenerate" :disabled="loading || !formData.prompt.trim()" class="btn-amber w-12 h-12 rounded-2xl flex items-center justify-center text-white text-lg shadow-xl disabled:opacity-50 disabled:cursor-not-allowed">
                <i v-if="!loading" class="fas fa-bolt"></i>
                <i v-else class="fas fa-spinner fa-spin"></i>
              </button>
            </div>
          </div>
        </div>

        <div v-if="!loading" id="ai-suggestion-pills" class="mt-10 flex flex-wrap justify-center gap-3">
          <button @click="fillAIInput('多租户商城系统模型，包含秒杀抢购逻辑')" class="px-4 py-2 bg-neutral-900 border border-neutral-800 rounded-full text-[11px] font-bold text-neutral-500 hover:border-amber-600 hover:text-amber-500 transition-all"># 商城系统</button>
          <button @click="fillAIInput('设计一个基于 RBAC 的用户权限模型，并支持审计日志')" class="px-4 py-2 bg-neutral-900 border border-neutral-800 rounded-full text-[11px] font-bold text-neutral-500 hover:border-amber-600 hover:text-amber-500 transition-all"># 权限模型</button>
          <button @click="fillAIInput('一个支持多端协作的任务管理系统看板模型')" class="px-4 py-2 bg-neutral-900 border border-neutral-800 rounded-full text-[11px] font-bold text-neutral-500 hover:border-amber-600 hover:text-amber-500 transition-all"># 协同办公</button>
        </div>
      </div>

      <!-- Chat History / Loading State -->
      <div v-if="loading" class="flex flex-col items-center py-10">
        <div class="flex space-x-2 mb-4">
          <div class="w-3 h-3 bg-amber-600 rounded-full animate-bounce"></div>
          <div class="w-3 h-3 bg-amber-600 rounded-full animate-bounce" style="animation-delay: 0.1s"></div>
          <div class="w-3 h-3 bg-amber-600 rounded-full animate-bounce" style="animation-delay: 0.2s"></div>
        </div>
        <div class="text-[10px] font-bold text-neutral-600 uppercase tracking-widest">Coffee AI 正在思考并推导关系模型...</div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useMessage } from 'naive-ui'
import { useRouter } from 'vue-router'

const message = useMessage()
const router = useRouter()
const loading = ref(false)

const formData = reactive({
  prompt: '',
  dbType: 'mysql',
  namingStyle: 'snake_case',
  generateJunctionTables: true,
  generateIndexes: true,
  generateComments: true
})

const fillAIInput = (text) => {
  formData.prompt = text
}

const handleGenerate = async () => {
  if (!formData.prompt.trim()) return
  
  loading.value = true
  try {
    // 暂未实现后端接口
    // await projectStore.generateFromAI(formData)
    await new Promise(resolve => setTimeout(resolve, 1000))
    message.warning('AI 生成功能正在开发中，敬请期待')
    
  } catch (error) {
    message.error('生成失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>
