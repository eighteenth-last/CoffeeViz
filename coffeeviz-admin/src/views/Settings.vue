<template>
  <div class="space-y-6">
    <div class="bg-bg-card rounded-xl border border-white/5 overflow-hidden">
      <div class="border-b border-white/5 px-6 py-4">
        <h3 class="text-lg font-medium text-white">大模型接入配置</h3>
        <p class="text-gray-500 text-xs mt-1">配置系统支持的 AI 模型 API 密钥及参数。</p>
      </div>

      <div class="p-6 space-y-6">
        <!-- Model Items -->
        <div v-for="model in models" :key="model.key"
          class="flex items-center justify-between p-4 bg-bg-input rounded-lg border border-white/5"
        >
          <div class="flex items-center gap-4">
            <div :class="['w-10 h-10 rounded flex items-center justify-center', model.iconBg]">
              <n-icon :size="20" :class="model.iconColor"><component :is="model.icon" /></n-icon>
            </div>
            <div>
              <h4 class="text-white font-medium">{{ model.name }}</h4>
              <p class="text-gray-500 text-xs">{{ model.desc }}</p>
            </div>
          </div>
          <n-switch v-model:value="model.enabled" />
        </div>

        <!-- OpenAI Config -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <n-form-item label="API Endpoint" label-placement="top">
            <n-input v-model:value="config.apiEndpoint" placeholder="https://api.openai.com/v1" />
          </n-form-item>
          <n-form-item label="API Key" label-placement="top">
            <n-input v-model:value="config.apiKey" type="password" show-password-on="click" placeholder="sk-xxxxxxxx" />
          </n-form-item>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <n-form-item label="默认模型" label-placement="top">
            <n-input v-model:value="config.modelName" placeholder="gpt-4" />
          </n-form-item>
          <n-form-item label="Max Tokens" label-placement="top">
            <n-input-number v-model:value="config.maxTokens" :min="100" :max="128000" class="w-full" />
          </n-form-item>
        </div>
      </div>

      <div class="bg-bg-input/30 px-6 py-4 border-t border-white/5 flex justify-end">
        <n-button type="primary" :loading="saving" @click="saveConfig">保存更改</n-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { LogoAndroid, SparklesOutline } from '@vicons/ionicons5'
import api from '@/api'

const message = useMessage()
const saving = ref(false)

const models = ref([
  { key: 'openai', name: 'OpenAI (GPT-4/3.5)', desc: '默认模型提供商', enabled: true, icon: SparklesOutline, iconBg: 'bg-green-500/10', iconColor: 'text-green-500' },
  { key: 'claude', name: 'Anthropic (Claude 3)', desc: '擅长长文本处理', enabled: false, icon: LogoAndroid, iconBg: 'bg-purple-500/10', iconColor: 'text-purple-500' }
])

const config = reactive({
  apiEndpoint: 'https://api.openai.com/v1',
  apiKey: '',
  modelName: 'gpt-4',
  maxTokens: 4096
})

onMounted(async () => {
  try {
    const res = await api.get('/api/admin/settings/ai')
    if (res.data) {
      config.apiEndpoint = res.data.apiEndpoint || config.apiEndpoint
      config.apiKey = res.data.apiKey || ''
      config.modelName = res.data.modelName || config.modelName
      config.maxTokens = res.data.maxTokens || config.maxTokens
    }
  } catch { /* 接口未实现，使用默认值 */ }
})

const saveConfig = async () => {
  saving.value = true
  try {
    await api.post('/api/admin/settings/ai', config)
    message.success('配置已保存')
  } catch (e) {
    message.error('保存失败: ' + (e.message || e))
  } finally {
    saving.value = false
  }
}
</script>
