<template>
  <div class="settings p-10 fade-in">
    <!-- Header Section -->
    <div class="flex justify-between items-end mb-10">
      <div>
        <h1 class="text-4xl font-black text-white mb-2 tracking-tight">系统<span class="text-amber-500">参数</span></h1>
        <p class="text-neutral-500">管理您的个人资料、安全设置与系统集成配置。</p>
      </div>
      <div class="flex space-x-3">
        <button @click="handleInitDefaults" class="px-5 py-2.5 bg-neutral-900 border border-neutral-800 rounded-xl text-sm font-bold text-neutral-400 hover:text-white hover:bg-neutral-800 transition-all flex items-center">
          <i class="fas fa-undo-alt mr-2"></i> 重置默认配置
        </button>
      </div>
    </div>

    <div v-if="loading" class="flex justify-center items-center py-20">
      <div class="flex flex-col items-center">
        <i class="fas fa-circle-notch fa-spin text-4xl text-amber-600 mb-4"></i>
        <p class="text-neutral-500 text-sm">加载配置中...</p>
      </div>
    </div>

    <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- Left Column: Profile & Security -->
      <div class="space-y-8">
        <!-- Profile Card -->
        <div class="glass-card p-8 rounded-3xl relative overflow-hidden">
          <div class="flex items-center space-x-4 mb-8">
            <div class="w-12 h-12 rounded-xl bg-blue-500/10 flex items-center justify-center text-blue-500 border border-blue-500/20">
              <i class="fas fa-user-circle text-2xl"></i>
            </div>
            <div>
              <h3 class="text-xl font-bold text-white">个人资料</h3>
              <p class="text-xs text-neutral-500">更新您的基本联系信息</p>
            </div>
          </div>

          <div class="space-y-5">
            <div>
              <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">显示名称</label>
              <input 
                v-model="profileForm.displayName"
                type="text" 
                class="w-full bg-black/40 border border-neutral-800 rounded-xl px-4 py-3 text-white outline-none focus:border-blue-500 transition-all placeholder-neutral-700"
                placeholder="神阁绘XXXXXXXXXX"
              >
            </div>
            
            <div>
              <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">电子邮箱</label>
              <input 
                v-model="profileForm.email"
                type="email" 
                class="w-full bg-black/40 border border-neutral-800 rounded-xl px-4 py-3 text-white outline-none focus:border-blue-500 transition-all placeholder-neutral-700"
                placeholder="example@domain.com"
              >
            </div>
            
            <div>
              <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">手机号码</label>
              <input 
                v-model="profileForm.phone"
                type="tel" 
                class="w-full bg-black/40 border border-neutral-800 rounded-xl px-4 py-3 text-white outline-none focus:border-blue-500 transition-all placeholder-neutral-700"
                placeholder="+86 1xx xxxx xxxx"
              >
            </div>

            <button 
              @click="handleSaveProfile" 
              :disabled="profileLoading"
              class="w-full py-3 bg-blue-600 hover:bg-blue-500 rounded-xl text-white font-bold shadow-lg shadow-blue-900/20 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center mt-4"
            >
              <i v-if="profileLoading" class="fas fa-spinner fa-spin mr-2"></i>
              <span>保存资料</span>
            </button>
          </div>
        </div>

        <!-- Security Card -->
        <div class="glass-card p-8 rounded-3xl relative overflow-hidden">
          <div class="flex items-center space-x-4 mb-8">
            <div class="w-12 h-12 rounded-xl bg-red-500/10 flex items-center justify-center text-red-500 border border-red-500/20">
              <i class="fas fa-shield-alt text-2xl"></i>
            </div>
            <div>
              <h3 class="text-xl font-bold text-white">安全设置</h3>
              <p class="text-xs text-neutral-500">修改您的登录密码</p>
            </div>
          </div>

          <div class="space-y-5">
            <div>
              <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">当前密码</label>
              <input 
                v-model="passwordForm.oldPassword"
                type="password" 
                class="w-full bg-black/40 border border-neutral-800 rounded-xl px-4 py-3 text-white outline-none focus:border-red-500 transition-all placeholder-neutral-700"
                placeholder="••••••••"
              >
            </div>
            
            <div>
              <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">新密码</label>
              <input 
                v-model="passwordForm.newPassword"
                type="password" 
                class="w-full bg-black/40 border border-neutral-800 rounded-xl px-4 py-3 text-white outline-none focus:border-red-500 transition-all placeholder-neutral-700"
                placeholder="至少 6 位字符"
              >
            </div>
            
            <div>
              <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">确认新密码</label>
              <input 
                v-model="passwordForm.confirmPassword"
                type="password" 
                class="w-full bg-black/40 border border-neutral-800 rounded-xl px-4 py-3 text-white outline-none focus:border-red-500 transition-all placeholder-neutral-700"
                placeholder="再次输入新密码"
              >
            </div>

            <button 
              @click="handleChangePassword" 
              :disabled="passwordLoading"
              class="w-full py-3 bg-red-600 hover:bg-red-500 rounded-xl text-white font-bold shadow-lg shadow-red-900/20 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center mt-4"
            >
              <i v-if="passwordLoading" class="fas fa-spinner fa-spin mr-2"></i>
              <span>修改密码</span>
            </button>
          </div>
        </div>
      </div>

      <!-- Right Column: System Config -->
      <div class="lg:col-span-2 space-y-8">
        <div class="glass-card p-8 rounded-3xl relative overflow-hidden border-amber-600/30">
          <!-- Glow effect -->
          <div class="absolute -top-24 -right-24 w-64 h-64 bg-amber-600/10 rounded-full blur-3xl pointer-events-none"></div>

          <div class="flex items-center justify-between mb-8 relative z-10">
            <div class="flex items-center space-x-4">
              <div class="w-12 h-12 rounded-xl bg-amber-600/10 flex items-center justify-center text-amber-500 border border-amber-600/20">
                <i class="fas fa-cogs text-2xl"></i>
              </div>
              <div>
                <h3 class="text-xl font-bold text-white">系统集成配置</h3>
                <p class="text-xs text-neutral-500">配置 AI 模型接口与第三方工具路径</p>
              </div>
            </div>
            <div class="px-3 py-1 bg-amber-600/20 border border-amber-600/30 rounded text-[10px] font-bold text-amber-500 uppercase tracking-wider">
              Advanced
            </div>
          </div>

          <div class="p-4 bg-amber-900/10 border border-amber-600/20 rounded-xl mb-8 flex items-start space-x-3">
            <i class="fas fa-info-circle text-amber-500 mt-0.5"></i>
            <div class="text-xs text-amber-200/80 leading-relaxed">
              API 密钥将以加密形式存储在数据库中。LLM 功能为可选功能，如果暂不使用 AI 生成功能，可以留空此配置。配置更改后建议重启应用以确保生效。
            </div>
          </div>

          <div class="space-y-8 relative z-10">
            <!-- OpenAI Config Section -->
            <div>
              <h4 class="text-sm font-bold text-white flex items-center mb-6">
                <i class="fas fa-robot mr-2 text-neutral-500"></i> OpenAI / LLM 配置
              </h4>
              
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div class="md:col-span-2">
                  <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">API Key</label>
                  <div class="relative">
                    <input 
                      v-model="apiForm.openaiApiKey"
                      :type="showApiKey ? 'text' : 'password'"
                      class="w-full bg-black/40 border border-neutral-800 rounded-xl py-3 pl-4 pr-12 text-white outline-none focus:border-amber-600 transition-all placeholder-neutral-700 font-mono text-sm"
                      placeholder="sk-..."
                    >
                    <button @click="showApiKey = !showApiKey" class="absolute right-4 top-1/2 -translate-y-1/2 text-neutral-500 hover:text-white transition-colors">
                      <i :class="['fas', showApiKey ? 'fa-eye-slash' : 'fa-eye']"></i>
                    </button>
                  </div>
                </div>

                <div>
                  <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">API Base URL</label>
                  <input 
                    v-model="apiForm.openaiBaseUrl"
                    type="text" 
                    class="w-full bg-black/40 border border-neutral-800 rounded-xl px-4 py-3 text-white outline-none focus:border-amber-600 transition-all placeholder-neutral-700 font-mono text-sm"
                    placeholder="https://api.openai.com/v1"
                  >
                </div>

                <div>
                  <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">Model Name</label>
                  <input 
                    v-model="apiForm.modelName"
                    type="text" 
                    class="w-full bg-black/40 border border-neutral-800 rounded-xl px-4 py-3 text-white outline-none focus:border-amber-600 transition-all placeholder-neutral-700 font-mono text-sm"
                    placeholder="gpt-4"
                  >
                </div>
              </div>
            </div>

            <div class="h-px bg-neutral-800"></div>

            <!-- Mermaid Config Section -->
            <div>
              <h4 class="text-sm font-bold text-white flex items-center mb-6">
                <i class="fas fa-project-diagram mr-2 text-neutral-500"></i> 可视化引擎配置
              </h4>
              
              <div>
                <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">Mermaid CLI Path</label>
                <div class="flex space-x-2">
                  <input 
                    v-model="apiForm.mermaidCliPath"
                    type="text" 
                    class="w-full bg-black/40 border border-neutral-800 rounded-xl px-4 py-3 text-white outline-none focus:border-amber-600 transition-all placeholder-neutral-700 font-mono text-sm"
                    placeholder="/usr/local/bin/mmdc"
                  >
                  <button class="px-4 py-2 bg-neutral-900 border border-neutral-800 rounded-xl text-neutral-400 hover:text-white hover:border-amber-600 transition-all">
                    <i class="fas fa-folder-open"></i>
                  </button>
                </div>
                <p class="text-[10px] text-neutral-600 mt-2">用于生成高质量 PDF/PNG 导出。若未安装 CLI，系统将使用浏览器渲染。</p>
              </div>
            </div>

            <div class="flex justify-end pt-4">
              <button 
                @click="handleSaveApi" 
                :disabled="apiLoading"
                class="btn-amber px-8 py-3 rounded-xl text-white font-bold shadow-lg shadow-amber-900/20 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center"
              >
                <i v-if="apiLoading" class="fas fa-spinner fa-spin mr-2"></i>
                <span>保存系统配置</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { useSettingsStore } from '@/store/settings'
import { useMessage } from 'naive-ui'

const userStore = useUserStore()
const settingsStore = useSettingsStore()
const message = useMessage()

const loading = ref(false)
const profileLoading = ref(false)
const passwordLoading = ref(false)
const apiLoading = ref(false)
const showApiKey = ref(false)

const profileForm = reactive({
  displayName: '',
  email: '',
  phone: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const apiForm = reactive({
  openaiApiKey: '',
  openaiBaseUrl: '',
  modelName: '',
  mermaidCliPath: ''
})

// 加载用户信息
const loadUserInfo = async () => {
  try {
    await userStore.fetchUserInfo()
    profileForm.displayName = userStore.userInfo?.displayName || ''
    profileForm.email = userStore.userInfo?.email || ''
    profileForm.phone = userStore.userInfo?.phone || ''
  } catch (error) {
    message.error('加载用户信息失败')
  }
}

// 加载系统配置
const loadSystemConfig = async () => {
  loading.value = true
  try {
    const res = await settingsStore.fetchSystemConfig()
    const configs = res.data || {}
    
    apiForm.openaiApiKey = configs['openai.api.key'] || ''
    apiForm.openaiBaseUrl = configs['openai.api.base_url'] || 'https://api.openai.com/v1'
    apiForm.modelName = configs['openai.model.name'] || 'gpt-4'
    apiForm.mermaidCliPath = configs['mermaid.cli.path'] || ''
  } catch (error) {
    message.error('加载系统配置失败')
  } finally {
    loading.value = false
  }
}

// 保存个人资料
const handleSaveProfile = async () => {
  profileLoading.value = true
  try {
    await userStore.updateUserInfo({
      displayName: profileForm.displayName,
      email: profileForm.email,
      phone: profileForm.phone
    })
    message.success('个人资料保存成功')
  } catch (error) {
    message.error(error.message || '保存失败')
  } finally {
    profileLoading.value = false
  }
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    message.warning('请填写完整的密码信息')
    return
  }
  
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    message.warning('两次输入的新密码不一致')
    return
  }
  
  if (passwordForm.newPassword.length < 6) {
    message.warning('新密码至少6位')
    return
  }
  
  passwordLoading.value = true
  try {
    await userStore.updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    message.success('密码修改成功')
    
    // 清空表单
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    message.error(error.message || '修改失败')
  } finally {
    passwordLoading.value = false
  }
}

// 保存 API 配置
const handleSaveApi = async () => {
  apiLoading.value = true
  try {
    const configs = {
      'openai.api.key': apiForm.openaiApiKey,
      'openai.api.base_url': apiForm.openaiBaseUrl,
      'openai.model.name': apiForm.modelName,
      'mermaid.cli.path': apiForm.mermaidCliPath
    }
    
    await settingsStore.updateSystemConfig(configs)
    message.success('API 配置保存成功')
  } catch (error) {
    message.error(error.message || '保存失败')
  } finally {
    apiLoading.value = false
  }
}

// 初始化默认配置
const handleInitDefaults = async () => {
  try {
    await settingsStore.initDefaultConfigs()
    message.success('默认配置初始化成功')
    await loadSystemConfig()
  } catch (error) {
    message.error(error.message || '初始化失败')
  }
}

onMounted(() => {
  loadUserInfo()
  loadSystemConfig()
})
</script>

<style scoped>
.settings {
  min-height: calc(100vh - 80px);
}

.fade-in {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>
