<template>
  <div class="h-full min-h-screen w-full flex bg-black overflow-hidden relative">
    <!-- Background Gradient Orbs (Global) -->
    <div class="absolute top-0 left-0 w-full h-full overflow-hidden pointer-events-none z-0">
      <div class="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-amber-600/10 rounded-full blur-[100px] animate-pulse-slow"></div>
      <div class="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-blue-600/10 rounded-full blur-[100px] animate-pulse-slow delay-1000"></div>
    </div>

    <!-- Left Side: Branding (60%) -->
    <div class="hidden lg:flex w-[60%] h-full flex-col justify-center px-12 relative z-10 border-r border-neutral-800/50 bg-neutral-900/30 backdrop-blur-sm">
      <div class="absolute inset-0 bg-gradient-to-br from-neutral-900/50 to-black/50 pointer-events-none"></div>
      
      <div class="relative z-10 max-w-2xl mx-auto">
        <div class="w-20 h-20 rounded-2xl flex items-center justify-center mb-8 overflow-hidden bg-gradient-to-br from-amber-500/20 to-transparent border border-amber-500/20 shadow-[0_0_30px_rgba(245,158,11,0.2)]">
          <img src="/logo.png" class="w-full h-full object-cover" alt="Logo">
        </div>
        
        <h1 class="text-5xl font-black text-white mb-6 leading-tight tracking-tight">
          架构设计的<br/>
          <span class="text-transparent bg-clip-text bg-gradient-to-r from-amber-500 to-amber-200">未来已来</span>
        </h1>
        
        <p class="text-neutral-400 text-lg leading-relaxed mb-12">
          ShenGeDraw 让架构可视化变得前所未有的简单。通过 AI 驱动的设计工具，将您的想法瞬间转化为专业的架构图表。
        </p>

        <div class="grid grid-cols-2 gap-8">
          <div class="flex items-start space-x-4">
            <div class="w-10 h-10 rounded-lg bg-amber-500/10 flex items-center justify-center flex-shrink-0 mt-1">
              <i class="fas fa-magic text-amber-500 text-lg"></i>
            </div>
            <div>
              <h3 class="text-white font-bold text-lg mb-1">AI 智能生成</h3>
              <p class="text-neutral-500 text-sm">输入自然语言描述，自动生成完整的架构图表，支持实时修改与优化。</p>
            </div>
          </div>
          
          <div class="flex items-start space-x-4">
            <div class="w-10 h-10 rounded-lg bg-blue-500/10 flex items-center justify-center flex-shrink-0 mt-1">
              <i class="fas fa-project-diagram text-blue-500 text-lg"></i>
            </div>
            <div>
              <h3 class="text-white font-bold text-lg mb-1">SQL 可视化</h3>
              <p class="text-neutral-500 text-sm">一键导入 SQL 脚本，自动解析并生成实体关系图 (ERD)。</p>
            </div>
          </div>
          
          <div class="flex items-start space-x-4">
            <div class="w-10 h-10 rounded-lg bg-green-500/10 flex items-center justify-center flex-shrink-0 mt-1">
              <i class="fas fa-users text-green-500 text-lg"></i>
            </div>
            <div>
              <h3 class="text-white font-bold text-lg mb-1">团队实时协作</h3>
              <p class="text-neutral-500 text-sm">邀请团队成员加入项目，多人实时编辑，版本控制与权限管理一应俱全。</p>
            </div>
          </div>
        </div>
        
        <div class="mt-12 flex items-center space-x-2 text-neutral-600 text-xs">
          <span>&copy; 2026 ShenGeDraw. All rights reserved.</span>
          <span>•</span>
          <a href="#" class="hover:text-amber-500 transition-colors">隐私政策</a>
          <span>•</span>
          <a href="#" class="hover:text-amber-500 transition-colors">服务条款</a>
        </div>
      </div>
    </div>

    <!-- Right Side: Login Form (40%) -->
    <div class="w-full lg:w-[40%] h-full flex items-center justify-center p-4 relative z-10">
      <div class="w-full max-w-md">
        <!-- Mobile Header (Visible only on mobile) -->
        <div class="lg:hidden text-center mb-6">
          <div class="w-12 h-12 rounded-xl flex items-center justify-center mx-auto mb-3 overflow-hidden bg-gradient-to-br from-amber-500/20 to-transparent border border-amber-500/20">
            <img src="/logo.png" class="w-full h-full object-cover" alt="Logo">
          </div>
          <h2 class="text-xl font-black text-white">ShenGeDraw</h2>
        </div>

        <div class="glass-card w-full p-6 lg:p-8 rounded-3xl border border-neutral-800/50 shadow-2xl relative overflow-hidden backdrop-blur-xl bg-black/40">
          <!-- Decoration inside card -->
          <div class="absolute top-0 right-0 w-32 h-32 bg-amber-600/5 rounded-full blur-3xl -mr-16 -mt-16 pointer-events-none"></div>
          <div class="absolute bottom-0 left-0 w-32 h-32 bg-blue-600/5 rounded-full blur-3xl -ml-16 -mb-16 pointer-events-none"></div>

          <div class="text-center mb-6 relative z-10">
            <h2 class="text-2xl font-black text-white mb-2">欢迎回来</h2>
            <p class="text-sm text-neutral-500">登录以继续您的架构设计之旅</p>
          </div>

          <!-- Tabs -->
          <div class="flex border-b border-neutral-800 mb-6 relative z-10">
            <button 
              @click="activeTab = 'phone'" 
              :class="['flex-1 pb-2 text-sm font-bold transition-all border-b-2', activeTab === 'phone' ? 'text-amber-500 border-amber-500' : 'text-neutral-500 border-transparent hover:text-neutral-300']"
            >
              手机验证码
            </button>
            <button 
              @click="activeTab = 'qr'" 
              :class="['flex-1 pb-2 text-sm font-bold transition-all border-b-2', activeTab === 'qr' ? 'text-amber-500 border-amber-500' : 'text-neutral-500 border-transparent hover:text-neutral-300']"
            >
              扫码登录
            </button>
            <button 
              @click="activeTab = 'account'" 
              :class="['flex-1 pb-2 text-sm font-bold transition-all border-b-2', activeTab === 'account' ? 'text-amber-500 border-amber-500' : 'text-neutral-500 border-transparent hover:text-neutral-300']"
            >
              账号登录
            </button>
          </div>

          <!-- Form: Phone -->
          <div v-if="activeTab === 'phone'" class="space-y-4 relative z-10 animate-fade-in">
            <div class="space-y-1">
              <label class="text-[11px] font-bold text-neutral-500 uppercase tracking-widest ml-1">手机号</label>
              <div class="flex bg-black/50 border border-neutral-800 rounded-xl overflow-hidden focus-within:border-amber-600 focus-within:ring-1 focus-within:ring-amber-600/50 transition-all group hover:border-neutral-700">
                <span class="px-4 py-2.5 text-neutral-500 text-sm border-r border-neutral-800 bg-neutral-900/30 flex items-center justify-center min-w-[60px]">+86</span>
                <input 
                  v-model="phoneFormData.phone"
                  type="tel" 
                  class="flex-1 bg-transparent px-4 py-2.5 text-sm text-white outline-none placeholder-neutral-600" 
                  placeholder="请输入手机号"
                  maxlength="11"
                  @keyup.enter="handlePhoneLogin"
                >
              </div>
            </div>
            <div class="space-y-1">
              <label class="text-[11px] font-bold text-neutral-500 uppercase tracking-widest ml-1">验证码</label>
              <div class="flex space-x-3">
                <input 
                  v-model="phoneFormData.code"
                  type="text" 
                  class="flex-1 bg-black/50 border border-neutral-800 rounded-xl px-4 py-2.5 text-sm text-white outline-none focus:border-amber-600 focus:ring-1 focus:ring-amber-600/50 transition-all placeholder-neutral-600 hover:border-neutral-700" 
                  placeholder="6位验证码"
                  maxlength="6"
                  @keyup.enter="handlePhoneLogin"
                >
                <button 
                  @click="handleSendCode" 
                  :disabled="sendingCode || codeCountdown > 0"
                  class="px-5 py-2 bg-amber-600/10 hover:bg-amber-600/20 border border-amber-600/30 hover:border-amber-600/50 rounded-xl text-xs font-bold text-amber-500 transition-all whitespace-nowrap disabled:opacity-50 disabled:cursor-not-allowed min-w-[100px]"
                >
                  {{ codeCountdown > 0 ? `${codeCountdown}s 后重发` : '获取验证码' }}
                </button>
              </div>
            </div>
            <button 
              @click="handlePhoneLogin" 
              :disabled="phoneLoading"
              class="w-full py-3 rounded-xl text-white font-bold text-sm shadow-[0_4px_14px_0_rgba(217,119,6,0.39)] mt-4 flex items-center justify-center bg-gradient-to-r from-amber-600 to-amber-700 hover:from-amber-500 hover:to-amber-600 active:scale-[0.98] transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
            >
              <i v-if="phoneLoading" class="fas fa-spinner fa-spin mr-2"></i>
              登录 / 注册
            </button>
          </div>

          <!-- Form: QR (WeChat Login) -->
          <div v-if="activeTab === 'qr'" class="flex flex-col items-center justify-center py-4 relative z-10 animate-fade-in min-h-[300px]">
            <div v-if="qrCodeLoading" class="w-52 h-52 bg-neutral-900/50 rounded-2xl flex items-center justify-center mb-6 border border-neutral-800">
              <i class="fas fa-spinner fa-spin text-amber-500 text-4xl"></i>
            </div>
            <div v-else-if="qrCodeError" class="w-52 h-52 bg-neutral-900/50 rounded-2xl flex flex-col items-center justify-center mb-6 p-6 border border-red-900/30">
              <i class="fas fa-exclamation-circle text-red-500 text-4xl mb-3"></i>
              <p class="text-xs text-neutral-400 text-center mb-4 leading-relaxed">{{ qrCodeError }}</p>
              <button @click="generateQrCode" class="px-4 py-2 bg-neutral-800 hover:bg-neutral-700 rounded-lg text-xs text-white transition-colors border border-neutral-700">重新生成</button>
            </div>
            <div v-else-if="qrCodeExpired" class="w-52 h-52 bg-neutral-900/50 rounded-2xl flex flex-col items-center justify-center mb-6 p-6 border border-neutral-800">
              <i class="fas fa-clock text-neutral-600 text-4xl mb-3"></i>
              <p class="text-xs text-neutral-500 text-center mb-4">二维码已过期</p>
              <button @click="generateQrCode" class="px-4 py-2 bg-amber-600/20 hover:bg-amber-600/30 text-amber-500 rounded-lg text-xs transition-colors border border-amber-600/30">刷新二维码</button>
            </div>
            <div v-else class="w-52 h-52 bg-white rounded-2xl p-2 mb-6 shadow-lg transform transition-transform hover:scale-105 duration-300">
              <canvas ref="qrCanvas" class="w-full h-full"></canvas>
            </div>
            
            <div class="text-center">
              <p v-if="qrCodeStatus === 'scanned'" class="text-sm text-green-500 mb-2 font-bold animate-bounce">
                <i class="fas fa-check-circle mr-1"></i> 已扫描，请在手机上确认
              </p>
              <p v-else class="text-sm text-neutral-400 mb-2">请使用 <span class="text-amber-500 font-bold">微信</span> 扫码登录</p>
              <p class="text-[10px] text-neutral-600 uppercase tracking-wider">二维码有效期 <span class="font-mono text-neutral-500">{{ qrCodeCountdown }}s</span></p>
            </div>
          </div>

          <!-- Form: Account (Active) -->
          <div v-if="activeTab === 'account'" class="space-y-3 relative z-10 animate-fade-in">
            <div class="space-y-1">
              <label class="text-[11px] font-bold text-neutral-500 uppercase tracking-widest ml-1">用户名 / 邮箱</label>
              <div class="relative group">
                <i class="fas fa-user absolute left-4 top-1/2 transform -translate-y-1/2 text-neutral-600 group-focus-within:text-amber-500 transition-colors z-10"></i>
                <input 
                  v-model="formData.username"
                  type="text" 
                  class="w-full bg-black/50 border border-neutral-800 rounded-xl pl-10 pr-4 py-2.5 text-sm text-white outline-none focus:border-amber-600 focus:ring-1 focus:ring-amber-600/50 transition-all placeholder-neutral-600 hover:border-neutral-700" 
                  :placeholder="isRegisterMode ? '请输入用户名' : '请输入用户名或邮箱'"
                  @keyup.enter="handleLogin"
                >
              </div>
            </div>
            
            <!-- 注册模式：邮箱 + 验证码 -->
            <template v-if="isRegisterMode">
              <div class="space-y-1">
                <label class="text-[11px] font-bold text-neutral-500 uppercase tracking-widest ml-1">邮箱</label>
                <div class="relative group">
                  <i class="fas fa-envelope absolute left-4 top-1/2 transform -translate-y-1/2 text-neutral-600 group-focus-within:text-amber-500 transition-colors z-10"></i>
                  <input 
                    v-model="formData.email"
                    type="email" 
                    class="w-full bg-black/50 border border-neutral-800 rounded-xl pl-10 pr-4 py-2.5 text-sm text-white outline-none focus:border-amber-600 focus:ring-1 focus:ring-amber-600/50 transition-all placeholder-neutral-600 hover:border-neutral-700" 
                    placeholder="请输入邮箱"
                  >
                </div>
              </div>
              <div class="space-y-1">
                <label class="text-[11px] font-bold text-neutral-500 uppercase tracking-widest ml-1">邮箱验证码</label>
                <div class="flex space-x-3">
                  <div class="relative flex-1 group">
                    <i class="fas fa-key absolute left-4 top-1/2 transform -translate-y-1/2 text-neutral-600 group-focus-within:text-amber-500 transition-colors z-10"></i>
                    <input 
                      v-model="formData.emailCode"
                      type="text" 
                      class="w-full bg-black/50 border border-neutral-800 rounded-xl pl-10 pr-4 py-2.5 text-sm text-white outline-none focus:border-amber-600 focus:ring-1 focus:ring-amber-600/50 transition-all placeholder-neutral-600 hover:border-neutral-700" 
                      placeholder="6位验证码"
                      maxlength="6"
                    >
                  </div>
                  <button 
                    @click="handleSendEmailCode" 
                    :disabled="sendingEmailCode || emailCodeCountdown > 0"
                    class="px-5 py-2 bg-amber-600/10 hover:bg-amber-600/20 border border-amber-600/30 hover:border-amber-600/50 rounded-xl text-xs font-bold text-amber-500 transition-all whitespace-nowrap disabled:opacity-50 disabled:cursor-not-allowed min-w-[100px]"
                  >
                    {{ emailCodeCountdown > 0 ? `${emailCodeCountdown}s 后重发` : '获取验证码' }}
                  </button>
                </div>
              </div>
            </template>
            
            <div class="space-y-1">
              <label class="text-[11px] font-bold text-neutral-500 uppercase tracking-widest ml-1">密码</label>
              <div class="relative group">
                <i class="fas fa-lock absolute left-4 top-1/2 transform -translate-y-1/2 text-neutral-600 group-focus-within:text-amber-500 transition-colors z-10"></i>
                <input 
                  v-model="formData.password"
                  type="password" 
                  class="w-full bg-black/50 border border-neutral-800 rounded-xl pl-10 pr-4 py-2.5 text-sm text-white outline-none focus:border-amber-600 focus:ring-1 focus:ring-amber-600/50 transition-all placeholder-neutral-600 hover:border-neutral-700" 
                  placeholder="••••••••"
                  @keyup.enter="handleLogin"
                >
              </div>
            </div>
            
            <div v-if="!isRegisterMode" class="flex items-center justify-between text-xs pt-1">
              <label class="flex items-center space-x-2 cursor-pointer group">
                <div class="relative">
                  <input type="checkbox" class="peer sr-only">
                  <div class="w-4 h-4 border border-neutral-700 rounded bg-neutral-900 peer-checked:bg-amber-600 peer-checked:border-amber-600 transition-all"></div>
                  <i class="fas fa-check text-white text-[10px] absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 opacity-0 peer-checked:opacity-100 transition-opacity"></i>
                </div>
                <span class="text-neutral-500 group-hover:text-neutral-300 transition-colors">记住我</span>
              </label>
              <a href="#" class="text-amber-500 hover:text-amber-400 transition-colors font-medium">忘记密码?</a>
            </div>
            <button 
              @click="handleLogin" 
              :disabled="loading"
              class="w-full py-3 rounded-xl text-white font-bold text-sm shadow-[0_4px_14px_0_rgba(217,119,6,0.39)] mt-4 flex items-center justify-center bg-gradient-to-r from-amber-600 to-amber-700 hover:from-amber-500 hover:to-amber-600 active:scale-[0.98] transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
            >
              <i v-if="loading" class="fas fa-spinner fa-spin mr-2"></i>
              {{ isRegisterMode ? '注册并登录' : '登录' }}
            </button>
            <button 
              @click="toggleMode" 
              class="w-full py-2 rounded-xl text-neutral-500 font-bold text-xs hover:text-white transition-all hover:bg-white/5"
            >
              {{ isRegisterMode ? '已有账号？返回登录' : '没有账号？创建新账号' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onUnmounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useMessage } from 'naive-ui'
import QRCode from 'qrcode'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const message = useMessage()

const activeTab = ref('account')
const isRegisterMode = ref(false)
const loading = ref(false)

const formData = reactive({
  username: '',
  password: '',
  email: '',
  emailCode: ''
})

// Email verification code state
const sendingEmailCode = ref(false)
const emailCodeCountdown = ref(0)
let emailCodeCountdownInterval = null

// Phone verification state
const phoneFormData = reactive({
  phone: '',
  code: ''
})
const phoneLoading = ref(false)
const sendingCode = ref(false)
const codeCountdown = ref(0)
let codeCountdownInterval = null

// WeChat QR Code state
const qrCanvas = ref(null)
const qrCodeLoading = ref(false)
const qrCodeError = ref('')
const qrCodeExpired = ref(false)
const qrCodeId = ref('')
const qrCodeUrl = ref('')
const qrCodeStatus = ref('pending')
const qrCodeCountdown = ref(300)
let qrCheckInterval = null
let qrCountdownInterval = null

const toggleMode = () => {
  isRegisterMode.value = !isRegisterMode.value
  formData.username = ''
  formData.password = ''
  formData.email = ''
  formData.emailCode = ''
}

const handleLogin = async () => {
  if (!formData.username || !formData.password) {
    message.warning('请输入用户名和密码')
    return
  }

  if (isRegisterMode.value) {
    // 注册校验
    if (!formData.email) {
      message.warning('请输入邮箱')
      return
    }
    if (!formData.emailCode) {
      message.warning('请输入邮箱验证码')
      return
    }
  }

  loading.value = true
  try {
    if (isRegisterMode.value) {
      await userStore.register(formData)
      message.success('注册成功，已自动登录')
    } else {
      await userStore.login(formData)
      message.success('登录成功')
    }
    
    const redirect = route.query.redirect || '/dashboard'
    router.push(redirect)
  } catch (error) {
    message.error(error.message || '操作失败')
  } finally {
    loading.value = false
  }
}

// Send email verification code
const handleSendEmailCode = async () => {
  console.log('[邮箱验证] 点击获取验证码, email=', formData.email)
  
  if (!formData.email) {
    message.warning('请先输入邮箱')
    return
  }
  
  const emailRegex = /^[\w.-]+@[\w.-]+\.[a-zA-Z]{2,}$/
  if (!emailRegex.test(formData.email)) {
    message.warning('请输入有效的邮箱地址')
    return
  }
  
  sendingEmailCode.value = true
  try {
    const res = await userStore.sendEmailCode(formData.email)
    console.log('[邮箱验证] 发送成功, res=', res)
    message.success('验证码已发送，请查收邮件')
    
    emailCodeCountdown.value = 60
    startEmailCodeCountdown()
  } catch (error) {
    console.error('[邮箱验证] 发送失败:', error)
    const msg = typeof error === 'string' ? error : (error?.message || '发送验证码失败')
    message.error(msg)
  } finally {
    sendingEmailCode.value = false
  }
}

const startEmailCodeCountdown = () => {
  stopEmailCodeCountdown()
  emailCodeCountdownInterval = setInterval(() => {
    emailCodeCountdown.value--
    if (emailCodeCountdown.value <= 0) {
      stopEmailCodeCountdown()
    }
  }, 1000)
}

const stopEmailCodeCountdown = () => {
  if (emailCodeCountdownInterval) {
    clearInterval(emailCodeCountdownInterval)
    emailCodeCountdownInterval = null
  }
}

// Generate WeChat QR Code
const generateQrCode = async () => {
  qrCodeLoading.value = true
  qrCodeError.value = ''
  qrCodeExpired.value = false
  qrCodeStatus.value = 'pending'
  qrCodeCountdown.value = 300
  
  try {
    const res = await userStore.generateWechatQrCode()
    qrCodeId.value = res.data.qrCodeId
    qrCodeUrl.value = res.data.qrCodeUrl
    
    // Stop loading to render canvas
    qrCodeLoading.value = false
    await nextTick()
    
    // Generate QR code on canvas
    if (qrCanvas.value) {
      await QRCode.toCanvas(qrCanvas.value, qrCodeUrl.value, {
        width: 180,
        margin: 1,
        color: {
          dark: '#000000',
          light: '#FFFFFF'
        }
      })
    }
    
    // Start polling for status
    startQrCodePolling()
    startCountdown()
  } catch (error) {
    qrCodeLoading.value = false
    qrCodeError.value = error.message || '生成二维码失败，请重试'
  }
}

// Poll QR code status
const startQrCodePolling = () => {
  stopQrCodePolling()
  
  qrCheckInterval = setInterval(async () => {
    try {
      const res = await userStore.checkWechatQrCode(qrCodeId.value)
      qrCodeStatus.value = res.data.status
      
      if (res.data.status === 'confirmed') {
        stopQrCodePolling()
        stopCountdown()
        message.success('登录成功')
        const redirect = route.query.redirect || '/dashboard'
        router.push(redirect)
      } else if (res.data.status === 'expired') {
        stopQrCodePolling()
        stopCountdown()
        qrCodeExpired.value = true
      }
    } catch (error) {
      console.error('检查二维码状态失败:', error)
    }
  }, 2000)
}

const stopQrCodePolling = () => {
  if (qrCheckInterval) {
    clearInterval(qrCheckInterval)
    qrCheckInterval = null
  }
}

// Countdown timer
const startCountdown = () => {
  stopCountdown()
  
  qrCountdownInterval = setInterval(() => {
    qrCodeCountdown.value--
    if (qrCodeCountdown.value <= 0) {
      stopCountdown()
      stopQrCodePolling()
      qrCodeExpired.value = true
    }
  }, 1000)
}

const stopCountdown = () => {
  if (qrCountdownInterval) {
    clearInterval(qrCountdownInterval)
    qrCountdownInterval = null
  }
}

// Watch tab changes
watch(activeTab, (newTab) => {
  if (newTab === 'qr' && !qrCodeId.value) {
    generateQrCode()
  } else if (newTab !== 'qr') {
    stopQrCodePolling()
    stopCountdown()
  }
})

// Phone verification: Send code
const handleSendCode = async () => {
  // Validate phone number
  if (!phoneFormData.phone) {
    message.warning('请输入手机号')
    return
  }
  
  // Simple Chinese mobile number validation
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(phoneFormData.phone)) {
    message.warning('请输入有效的手机号')
    return
  }
  
  sendingCode.value = true
  try {
    await userStore.sendSmsCode(phoneFormData.phone)
    message.success('验证码已发送，请查收短信')
    
    // Start countdown
    codeCountdown.value = 60
    startCodeCountdown()
  } catch (error) {
    message.error(error.message || '发送验证码失败')
  } finally {
    sendingCode.value = false
  }
}

// Phone verification: Login
const handlePhoneLogin = async () => {
  if (!phoneFormData.phone) {
    message.warning('请输入手机号')
    return
  }
  
  if (!phoneFormData.code) {
    message.warning('请输入验证码')
    return
  }
  
  // Validate phone number
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(phoneFormData.phone)) {
    message.warning('请输入有效的手机号')
    return
  }
  
  phoneLoading.value = true
  try {
    await userStore.loginWithSms(phoneFormData.phone, phoneFormData.code)
    message.success('登录成功')
    
    const redirect = route.query.redirect || '/dashboard'
    router.push(redirect)
  } catch (error) {
    message.error(error.message || '登录失败')
  } finally {
    phoneLoading.value = false
  }
}

// Code countdown timer
const startCodeCountdown = () => {
  stopCodeCountdown()
  
  codeCountdownInterval = setInterval(() => {
    codeCountdown.value--
    if (codeCountdown.value <= 0) {
      stopCodeCountdown()
    }
  }, 1000)
}

const stopCodeCountdown = () => {
  if (codeCountdownInterval) {
    clearInterval(codeCountdownInterval)
    codeCountdownInterval = null
  }
}

// Cleanup on unmount
onUnmounted(() => {
  stopQrCodePolling()
  stopCountdown()
  stopCodeCountdown()
  stopEmailCodeCountdown()
})
</script>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(5px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
