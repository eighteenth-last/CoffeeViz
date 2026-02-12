<template>
  <div class="min-h-screen bg-[#050505] text-white font-sans">
    <!-- Navbar -->
    <nav class="border-b border-neutral-800 px-6 py-4 flex items-center bg-[#050505]">
      <div class="text-xl font-bold flex items-center cursor-pointer" @click="router.push('/subscribe')">
        <i class="fas fa-arrow-left mr-4 text-neutral-400 hover:text-white transition-colors"></i>
        CoffeeViz
      </div>
    </nav>

    <div class="flex flex-col md:flex-row h-[calc(100vh-65px)]">
      <!-- Left: Order Summary -->
      <div class="w-full md:w-1/2 p-8 md:p-16 border-b md:border-b-0 md:border-r border-neutral-800 flex flex-col">
        <h2 class="text-3xl font-bold mb-2">订单摘要</h2>
        <p class="text-neutral-500 mb-12">以人民币计价</p>

        <div v-if="plan" class="flex-1 animate-in fade-in duration-300">
          <div class="flex justify-between items-center py-4 border-b border-neutral-800">
            <span class="text-xl font-medium">{{ plan.planName }} Plan</span>
            <span class="text-xl font-bold">¥{{ finalPrice }}</span>
          </div>
          <div class="py-2 text-neutral-500 text-sm">{{ cycleText }}</div>

          <div class="flex justify-between items-center py-4 border-b border-neutral-800 mt-4 text-green-500">
            <span>限时折扣</span>
            <span>-¥0.00</span>
          </div>
          <div class="py-2 text-neutral-500 text-sm">暂无优惠</div>

          <div class="mt-auto pt-8">
            <div class="flex justify-between items-center py-2">
              <span class="text-neutral-400">税前总计</span>
              <span>¥{{ finalPrice }}</span>
            </div>
            <div class="flex justify-between items-center py-2">
              <span class="text-neutral-400">税费</span>
              <span>¥0.00</span>
            </div>
            <div class="flex justify-between items-center py-6 text-2xl font-bold border-t border-neutral-800 mt-4">
              <span>今日应付总额</span>
              <span>¥{{ finalPrice }}</span>
            </div>
          </div>
        </div>
        <div v-else class="flex-1 animate-pulse">
          <div class="flex justify-between items-center py-4 border-b border-neutral-800">
            <div class="h-7 bg-neutral-800 rounded w-32"></div>
            <div class="h-7 bg-neutral-800 rounded w-20"></div>
          </div>
          <div class="py-2 mt-1">
            <div class="h-4 bg-neutral-800 rounded w-16"></div>
          </div>

          <div class="flex justify-between items-center py-4 border-b border-neutral-800 mt-4">
            <div class="h-6 bg-neutral-800 rounded w-24"></div>
            <div class="h-6 bg-neutral-800 rounded w-16"></div>
          </div>
          
          <div class="mt-auto pt-20">
             <div class="space-y-4">
                <div class="flex justify-between"><div class="h-5 bg-neutral-800 rounded w-20"></div><div class="h-5 bg-neutral-800 rounded w-16"></div></div>
                <div class="flex justify-between"><div class="h-5 bg-neutral-800 rounded w-16"></div><div class="h-5 bg-neutral-800 rounded w-16"></div></div>
                <div class="flex justify-between pt-6 border-t border-neutral-800 mt-4"><div class="h-8 bg-neutral-800 rounded w-32"></div><div class="h-8 bg-neutral-800 rounded w-24"></div></div>
             </div>
          </div>
        </div>
      </div>

      <!-- Right: Payment Method -->
      <div class="w-full md:w-1/2 p-8 md:p-16 bg-neutral-900/50">
        <h2 class="text-2xl font-bold mb-8">支付方式</h2>

        <div class="mb-6">
          <div class="text-sm font-bold text-neutral-400 mb-4">当前账号</div>
          <div class="bg-neutral-800 p-4 rounded-lg flex items-center text-neutral-300">
            <i class="fas fa-user-circle text-2xl mr-3"></i>
            {{ userStore.username || userStore.email || 'User' }}
          </div>
        </div>

        <div class="mb-8">
          <div class="text-sm font-bold text-neutral-400 mb-4">选择支付方式</div>
          <div v-if="loadingMethods" class="space-y-3">
            <div class="h-16 bg-neutral-800 rounded-xl animate-pulse"></div>
            <div class="h-16 bg-neutral-800 rounded-xl animate-pulse"></div>
          </div>
          <div v-else class="space-y-3">
            <label 
              v-for="method in paymentMethods" :key="method.code"
              class="flex items-center justify-between p-4 border rounded-xl cursor-pointer transition-all group"
              :class="selectedMethod === method.code 
                ? getMethodActiveClass(method.color)
                : 'border-neutral-700 hover:border-neutral-500'"
            >
              <div class="flex items-center">
                <input type="radio" v-model="selectedMethod" :value="method.code" class="hidden">
                <i :class="[method.icon, getMethodIconClass(method.color), 'text-2xl mr-4']"></i>
                <span class="font-bold">{{ method.name }}</span>
              </div>
              <i v-if="selectedMethod === method.code" :class="['fas fa-check-circle', getMethodIconClass(method.color)]"></i>
            </label>
          </div>
        </div>

        <button 
          v-if="plan"
          @click="confirmPayment"
          :disabled="processing"
          class="w-full py-4 bg-green-600 hover:bg-green-500 text-white rounded-lg font-bold text-lg transition-colors flex items-center justify-center disabled:opacity-50 disabled:cursor-not-allowed animate-in fade-in duration-300"
        >
          <i v-if="processing" class="fas fa-spinner fa-spin mr-2"></i>
          {{ processing ? '正在创建订单...' : `支付 ¥${finalPrice}` }}
        </button>
        <div v-else class="w-full h-[60px] bg-neutral-800 rounded-lg animate-pulse"></div>

        <div class="mt-6 text-xs text-neutral-500 space-y-2">
          <p>• 继续操作即表示你确认已阅读、理解并同意我们的 <a href="#" class="underline hover:text-neutral-300">服务条款</a> 和 <a href="#" class="underline hover:text-neutral-300">隐私政策</a>。</p>
          <p>• 本服务由 CoffeeViz 提供技术支持，你的支付方式将被安全保存以便于未来使用。</p>
        </div>
      </div>
    </div>

    <!-- Payment QR Code Modal -->
    <n-modal v-model:show="showPaymentModal" title="支付二维码" preset="card" class="w-[350px] dark-modal" :style="{ backgroundColor: '#171717', color: 'white', border: '1px solid #262626' }" :mask-closable="false">
      <template #header-extra>
        <i class="fas fa-times cursor-pointer text-neutral-400 hover:text-white" @click="handleCancelPayment"></i>
      </template>
      <div class="flex flex-col items-center p-2">
        <div class="bg-white p-2 rounded-lg mb-6 shadow-lg">
            <img :src="qrCodeUrl" alt="支付二维码" class="w-48 h-48 block" />
        </div>
        <p class="text-sm text-neutral-300 mb-2">请使用<span class="font-bold text-white mx-1">{{ selectedMethodName }}</span>扫描二维码支付</p>
        <p class="text-xs text-neutral-500 mb-6 font-mono bg-neutral-800 px-2 py-1 rounded">订单号: {{ currentOrderNo }}</p>
        <div v-if="pollingStatus === 'polling'" class="mb-4 flex items-center text-amber-400 text-xs">
          <i class="fas fa-spinner fa-spin mr-2"></i>
          等待支付中，支付完成后将自动跳转...
        </div>
        <div v-else-if="pollingStatus === 'paid'" class="mb-4 flex items-center text-green-400 text-xs">
          <i class="fas fa-check-circle mr-2"></i>
          支付成功，正在跳转...
        </div>
        <button @click="handlePaymentComplete" :disabled="processing" class="w-full py-3 bg-green-600 hover:bg-green-500 text-white rounded-lg font-bold transition-colors shadow-lg shadow-green-900/20 disabled:opacity-50 disabled:cursor-not-allowed">
          <i v-if="processing" class="fas fa-spinner fa-spin mr-2"></i>
          我已完成支付
        </button>
        <button @click="handleCancelPayment" :disabled="cancelling" class="w-full py-2 mt-3 text-neutral-400 hover:text-white text-sm transition-colors">
          <i v-if="cancelling" class="fas fa-spinner fa-spin mr-1"></i>
          取消支付
        </button>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useMessage, NModal } from 'naive-ui'
import api from '@/api'
import QRCode from 'qrcode'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const message = useMessage()

const plan = ref(null)
const selectedMethod = ref('')
const processing = ref(false)
const showPaymentModal = ref(false)
const qrCodeUrl = ref('')
const currentOrderNo = ref('')
const billingCycle = ref('monthly')
const paymentMethods = ref([])
const loadingMethods = ref(true)
const pollingStatus = ref('') // '', 'polling', 'paid'
const cancelling = ref(false)
let pollTimer = null

const finalPrice = computed(() => {
  if (!plan.value) return 0
  if (billingCycle.value === 'monthly') return plan.value.priceMonthly
  // Yearly price logic (matches Subscribe.vue)
  return plan.value.priceMonthly * 10
})

const cycleText = computed(() => {
  return billingCycle.value === 'monthly' ? '按月计费' : '按年计费'
})

const selectedMethodName = computed(() => {
  const m = paymentMethods.value.find(m => m.code === selectedMethod.value)
  return m ? m.name : selectedMethod.value
})

const colorMap = {
  blue: { active: 'border-blue-500 bg-blue-500/10', icon: 'text-blue-500' },
  green: { active: 'border-green-500 bg-green-500/10', icon: 'text-green-500' },
  amber: { active: 'border-amber-500 bg-amber-500/10', icon: 'text-amber-500' },
  red: { active: 'border-red-500 bg-red-500/10', icon: 'text-red-500' },
  purple: { active: 'border-purple-500 bg-purple-500/10', icon: 'text-purple-500' }
}
const getMethodActiveClass = (color) => colorMap[color]?.active || colorMap.blue.active
const getMethodIconClass = (color) => colorMap[color]?.icon || colorMap.blue.icon

onMounted(async () => {
  // 加载可用支付方式
  try {
    const methodsRes = await api.get('/api/payment/methods')
    paymentMethods.value = methodsRes.data || []
    if (paymentMethods.value.length > 0) {
      selectedMethod.value = paymentMethods.value[0].code
    }
  } catch (e) {
    // 降级：使用默认支付方式
    paymentMethods.value = [
      { code: 'ALIPAY', name: '支付宝', icon: 'fab fa-alipay', color: 'blue' },
      { code: 'WECHAT', name: '微信支付', icon: 'fab fa-weixin', color: 'green' }
    ]
    selectedMethod.value = 'ALIPAY'
  } finally {
    loadingMethods.value = false
  }

  // If plan details are passed via state (from router.push)
  if (history.state && history.state.plan) {
    plan.value = history.state.plan
    if (history.state.billingCycle) {
      billingCycle.value = history.state.billingCycle
    }
  } else {
    // If refreshed, maybe try to fetch plan by ID from query param if we implemented that
    const planId = route.query.planId
    if (route.query.billingCycle) {
      billingCycle.value = route.query.billingCycle
    }
    if (planId) {
      try {
        const res = await api.get('/api/subscription/plans')
        const plans = res.data || []
        // Ensure type compatibility (planId from query is string, p.id might be number)
        plan.value = plans.find(p => String(p.id) === String(planId))
      } catch (e) {
        console.error('Failed to fetch plan details:', e)
      }
    }
  }

  if (!plan.value) {
    message.error('无法加载订单信息，请重新选择订阅计划')
    // Delay redirect slightly so user sees the message
    setTimeout(() => {
        router.push('/subscribe')
    }, 1500)
  }
})

const confirmPayment = async () => {
  if (!plan.value) return
  
  processing.value = true
  try {
    const res = await api.post('/api/payment/create', {
      planId: plan.value.id,
      billingCycle: billingCycle.value,
      paymentMethod: selectedMethod.value
    })
    
    const paymentData = res.data
    currentOrderNo.value = paymentData.orderNo

    // 测试支付模式：直接确认支付，跳过二维码
    if (selectedMethod.value === 'TEST') {
      await api.post(`/api/payment/confirm/${paymentData.orderNo}`)
      message.success('测试支付成功，订阅已生效')
      router.push('/dashboard')
      return
    }
    
    // Generate QR Code
    const targetContent = paymentData.qrCode || paymentData.paymentUrl || `mock_payment_${paymentData.orderNo}`
    
    try {
      qrCodeUrl.value = await QRCode.toDataURL(targetContent, {
        width: 200,
        margin: 2,
        color: {
          dark: '#000000',
          light: '#ffffff'
        }
      })
      showPaymentModal.value = true
    } catch (err) {
      console.error('QR Code generation failed', err)
      message.error('生成支付二维码失败')
    }
    
  } catch (error) {
    console.error(error)
    message.error(error.message || '创建支付订单失败')
  } finally {
    processing.value = false
  }
}

const handlePaymentComplete = async () => {
  if (!currentOrderNo.value) {
    message.error('订单信息异常')
    return
  }
  
  processing.value = true
  try {
    await api.post(`/api/payment/confirm/${currentOrderNo.value}`)
    stopPolling()
    showPaymentModal.value = false
    message.success('支付成功，订阅已生效')
    router.push('/dashboard')
  } catch (error) {
    console.error(error)
    // 402 表示支付尚未完成
    if (error?.response?.status === 402 || error?.code === 402) {
      message.warning('支付尚未完成，请先完成支付')
    } else {
      message.error(error.message || '支付确认失败，请稍后重试')
    }
  } finally {
    processing.value = false
  }
}

const handleCancelPayment = async () => {
  stopPolling()
  if (!currentOrderNo.value) {
    showPaymentModal.value = false
    return
  }
  cancelling.value = true
  try {
    await api.post(`/api/payment/cancel/${currentOrderNo.value}`)
    message.info('支付已取消')
  } catch {
    // 取消失败也关闭弹窗
  } finally {
    cancelling.value = false
    showPaymentModal.value = false
    currentOrderNo.value = ''
  }
}

// 轮询支付状态
const startPolling = () => {
  if (pollTimer) return
  pollingStatus.value = 'polling'
  pollTimer = setInterval(async () => {
    if (!currentOrderNo.value) return
    try {
      const res = await api.get(`/api/payment/status/${currentOrderNo.value}`)
      if (res.data && res.data.paid) {
        pollingStatus.value = 'paid'
        stopPolling()
        // 自动确认并跳转
        setTimeout(async () => {
          try {
            await api.post(`/api/payment/confirm/${currentOrderNo.value}`)
          } catch { /* 可能已经处理过了 */ }
          showPaymentModal.value = false
          message.success('支付成功，订阅已生效')
          router.push('/dashboard')
        }, 1000)
      } else if (res.data && res.data.status === 'cancelled') {
        // 订单已取消，停止轮询
        stopPolling()
        showPaymentModal.value = false
        message.info('订单已取消')
      }
    } catch { /* 忽略轮询错误 */ }
  }, 3000) // 每3秒轮询一次
}

const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
  pollingStatus.value = ''
}

// 弹窗打开时开始轮询
watch(showPaymentModal, (val) => {
  if (val) {
    startPolling()
  }
  // 关闭时由 handleCancelPayment 或支付成功逻辑处理，不在这里 stopPolling
})

onUnmounted(() => {
  stopPolling()
  // 页面卸载时如果还有未完成的订单，尝试取消
  if (currentOrderNo.value && pollingStatus.value !== 'paid') {
    api.post(`/api/payment/cancel/${currentOrderNo.value}`).catch(() => {})
  }
})
</script>