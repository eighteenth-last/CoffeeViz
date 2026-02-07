<template>
  <div class="h-full flex items-center justify-center min-h-[600px]">
    <div class="glass-card w-full max-w-md p-8 rounded-3xl border border-neutral-800 shadow-2xl relative overflow-hidden">
      <!-- Decoration -->
      <div class="absolute top-0 right-0 w-32 h-32 bg-amber-600/10 rounded-full blur-3xl -mr-16 -mt-16"></div>
      <div class="absolute bottom-0 left-0 w-32 h-32 bg-blue-600/10 rounded-full blur-3xl -ml-16 -mb-16"></div>

      <div class="text-center mb-8 relative z-10">
        <div class="w-16 h-16 bg-amber-600 rounded-2xl flex items-center justify-center shadow-lg shadow-amber-900/40 mx-auto mb-4">
          <i class="fas fa-mug-hot text-white text-3xl"></i>
        </div>
        <h2 class="text-2xl font-black text-white mb-1">欢迎回来</h2>
        <p class="text-xs text-neutral-500">登录以继续您的架构设计之旅</p>
      </div>

      <!-- Tabs -->
      <div class="flex border-b border-neutral-800 mb-6 relative z-10">
        <button 
          @click="activeTab = 'phone'" 
          :class="['flex-1 pb-3 text-xs font-bold transition-all border-b-2', activeTab === 'phone' ? 'text-amber-500 border-amber-500' : 'text-neutral-500 border-transparent hover:text-neutral-300']"
        >
          手机验证码
        </button>
        <button 
          @click="activeTab = 'qr'" 
          :class="['flex-1 pb-3 text-xs font-bold transition-all border-b-2', activeTab === 'qr' ? 'text-amber-500 border-amber-500' : 'text-neutral-500 border-transparent hover:text-neutral-300']"
        >
          扫码登录
        </button>
        <button 
          @click="activeTab = 'account'" 
          :class="['flex-1 pb-3 text-xs font-bold transition-all border-b-2', activeTab === 'account' ? 'text-amber-500 border-amber-500' : 'text-neutral-500 border-transparent hover:text-neutral-300']"
        >
          账号登录
        </button>
      </div>

      <!-- Form: Phone -->
      <div v-if="activeTab === 'phone'" class="space-y-4 relative z-10 animate-fade-in">
        <div class="space-y-1">
          <label class="text-[10px] font-bold text-neutral-500 uppercase tracking-widest">手机号</label>
          <div class="flex bg-black border border-neutral-800 rounded-xl overflow-hidden focus-within:border-amber-600 transition-all">
            <span class="px-3 py-3 text-neutral-500 text-sm border-r border-neutral-900">+86</span>
            <input 
              v-model="phoneFormData.phone"
              type="tel" 
              class="flex-1 bg-transparent px-3 py-3 text-sm text-white outline-none" 
              placeholder="138 0000 0000"
              maxlength="11"
              @keyup.enter="handlePhoneLogin"
            >
          </div>
        </div>
        <div class="space-y-1">
          <label class="text-[10px] font-bold text-neutral-500 uppercase tracking-widest">验证码</label>
          <div class="flex space-x-3">
            <input 
              v-model="phoneFormData.code"
              type="text" 
              class="flex-1 bg-black border border-neutral-800 rounded-xl px-4 py-3 text-sm text-white outline-none focus:border-amber-600 transition-all" 
              placeholder="6位验证码"
              maxlength="6"
              @keyup.enter="handlePhoneLogin"
            >
            <button 
              @click="handleSendCode" 
              :disabled="sendingCode || codeCountdown > 0"
              class="px-4 py-2 bg-amber-600 hover:bg-amber-700 border border-amber-700 rounded-xl text-xs font-bold text-white transition-all whitespace-nowrap disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
            </button>
          </div>
        </div>
        <button 
          @click="handlePhoneLogin" 
          :disabled="phoneLoading"
          class="w-full py-3 rounded-xl text-white font-bold text-sm shadow-lg mt-4 flex items-center justify-center bg-gradient-to-r from-amber-600 to-amber-800 hover:from-amber-700 hover:to-amber-900 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <i v-if="phoneLoading" class="fas fa-spinner fa-spin mr-2"></i>
          登录 / 注册
        </button>
      </div>

      <!-- Form: QR (WeChat Login) -->
      <div v-if="activeTab === 'qr'" class="flex flex-col items-center justify-center py-4 relative z-10 animate-fade-in">
        <div v-if="qrCodeLoading" class="w-48 h-48 bg-neutral-900 rounded-2xl flex items-center justify-center mb-4">
          <i class="fas fa-spinner fa-spin text-amber-500 text-3xl"></i>
        </div>
        <div v-else-if="qrCodeError" class="w-48 h-48 bg-neutral-900 rounded-2xl flex flex-col items-center justify-center mb-4 p-4">
          <i class="fas fa-exclamation-circle text-red-500 text-3xl mb-2"></i>
          <p class="text-xs text-neutral-500 text-center">{{ qrCodeError }}</p>
          <button @click="generateQrCode" class="mt-3 px-4 py-2 bg-amber-600 rounded-lg text-xs text-white hover:bg-amber-700">重新生成</button>
        </div>
        <div v-else-if="qrCodeExpired" class="w-48 h-48 bg-neutral-900 rounded-2xl flex flex-col items-center justify-center mb-4 p-4">
          <i class="fas fa-clock text-neutral-600 text-3xl mb-2"></i>
          <p class="text-xs text-neutral-500 text-center">二维码已过期</p>
          <button @click="generateQrCode" class="mt-3 px-4 py-2 bg-amber-600 rounded-lg text-xs text-white hover:bg-amber-700">重新生成</button>
        </div>
        <div v-else class="w-48 h-48 bg-white rounded-2xl p-3 mb-4">
          <canvas ref="qrCanvas" class="w-full h-full"></canvas>
        </div>
        <p v-if="qrCodeStatus === 'scanned'" class="text-xs text-green-500 mb-2 font-bold">
          <i class="fas fa-check-circle mr-1"></i>已扫描，请在手机上确认
        </p>
        <p v-else class="text-xs text-neutral-500 mb-2">请使用 <span class="text-amber-500 font-bold">微信</span> 扫码登录</p>
        <p class="text-[10px] text-neutral-600">二维码将在 <span class="font-mono">{{ qrCodeCountdown }}s</span> 后过期</p>
      </div>

      <!-- Form: Account (Active) -->
      <div v-if="activeTab === 'account'" class="space-y-4 relative z-10 animate-fade-in">
        <div class="space-y-1">
          <label class="text-[10px] font-bold text-neutral-500 uppercase tracking-widest">用户名 / 邮箱</label>
          <input 
            v-model="formData.username"
            type="text" 
            class="w-full bg-black border border-neutral-800 rounded-xl px-4 py-3 text-sm text-white outline-none focus:border-amber-600 transition-all" 
            placeholder="请输入用户名"
            @keyup.enter="handleLogin"
          >
        </div>
        <div class="space-y-1">
          <label class="text-[10px] font-bold text-neutral-500 uppercase tracking-widest">密码</label>
          <input 
            v-model="formData.password"
            type="password" 
            class="w-full bg-black border border-neutral-800 rounded-xl px-4 py-3 text-sm text-white outline-none focus:border-amber-600 transition-all" 
            placeholder="••••••••"
            @keyup.enter="handleLogin"
          >
        </div>
        <div class="flex items-center justify-between text-xs">
          <label class="flex items-center space-x-2 cursor-pointer">
            <input type="checkbox" class="rounded bg-neutral-900 border-neutral-800 text-amber-600">
            <span class="text-neutral-500">记住我</span>
          </label>
          <a href="#" class="text-amber-500 hover:text-amber-400">忘记密码?</a>
        </div>
        <button 
          @click="handleLogin" 
          :disabled="loading"
          class="w-full py-3 rounded-xl text-white font-bold text-sm shadow-lg mt-4 flex items-center justify-center bg-gradient-to-r from-amber-600 to-amber-800 hover:from-amber-700 hover:to-amber-900 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <i v-if="loading" class="fas fa-spinner fa-spin mr-2"></i>
          {{ isRegisterMode ? '注册并登录' : '登录' }}
        </button>
        <button 
          @click="toggleMode" 
          class="w-full py-3 rounded-xl text-neutral-500 font-bold text-xs hover:text-white transition-all"
        >
          {{ isRegisterMode ? '已有账号？返回登录' : '没有账号？创建新账号' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onUnmounted } from 'vue'
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
  password: ''
})

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
}

const handleLogin = async () => {
  if (!formData.username || !formData.password) {
    message.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    if (isRegisterMode.value) {
      // 注册逻辑
      await userStore.register(formData)
      message.success('注册成功，已自动登录')
    } else {
      // 登录逻辑
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
    qrCodeError.value = error.message || '生成二维码失败，请重试'
  } finally {
    qrCodeLoading.value = false
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
