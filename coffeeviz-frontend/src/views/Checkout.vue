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
          <div class="space-y-3">
            <label 
              class="flex items-center justify-between p-4 border rounded-xl cursor-pointer transition-all group"
              :class="selectedMethod === 'ALIPAY' ? 'border-blue-500 bg-blue-500/10' : 'border-neutral-700 hover:border-neutral-500'"
            >
              <div class="flex items-center">
                <input type="radio" v-model="selectedMethod" value="ALIPAY" class="hidden">
                <i class="fab fa-alipay text-blue-500 text-2xl mr-4"></i>
                <span class="font-bold">支付宝</span>
              </div>
              <i v-if="selectedMethod === 'ALIPAY'" class="fas fa-check-circle text-blue-500"></i>
            </label>

            <label 
              class="flex items-center justify-between p-4 border rounded-xl cursor-pointer transition-all group"
              :class="selectedMethod === 'WECHAT' ? 'border-green-500 bg-green-500/10' : 'border-neutral-700 hover:border-neutral-500'"
            >
              <div class="flex items-center">
                <input type="radio" v-model="selectedMethod" value="WECHAT" class="hidden">
                <i class="fab fa-weixin text-green-500 text-2xl mr-4"></i>
                <span class="font-bold">微信支付</span>
              </div>
              <i v-if="selectedMethod === 'WECHAT'" class="fas fa-check-circle text-green-500"></i>
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
          支付 ¥{{ finalPrice }}
        </button>
        <div v-else class="w-full h-[60px] bg-neutral-800 rounded-lg animate-pulse"></div>

        <div class="mt-6 text-xs text-neutral-500 space-y-2">
          <p>• 继续操作即表示你确认已阅读、理解并同意我们的 <a href="#" class="underline hover:text-neutral-300">服务条款</a> 和 <a href="#" class="underline hover:text-neutral-300">隐私政策</a>。</p>
          <p>• 本服务由 CoffeeViz 提供技术支持，你的支付方式将被安全保存以便于未来使用。</p>
        </div>
      </div>
    </div>

    <!-- Payment QR Code Modal -->
    <n-modal v-model:show="showPaymentModal" title="支付二维码" preset="card" class="w-[350px] dark-modal" :style="{ backgroundColor: '#171717', color: 'white', border: '1px solid #262626' }">
      <template #header-extra>
        <i class="fas fa-times cursor-pointer text-neutral-400 hover:text-white" @click="showPaymentModal = false"></i>
      </template>
      <div class="flex flex-col items-center p-2">
        <div class="bg-white p-2 rounded-lg mb-6 shadow-lg">
            <img :src="qrCodeUrl" alt="支付二维码" class="w-48 h-48 block" />
        </div>
        <p class="text-sm text-neutral-300 mb-2">请使用<span class="font-bold text-white mx-1">{{ selectedMethod === 'ALIPAY' ? '支付宝' : '微信' }}</span>扫描二维码支付</p>
        <p class="text-xs text-neutral-500 mb-6 font-mono bg-neutral-800 px-2 py-1 rounded">订单号: {{ currentOrderNo }}</p>
        <button @click="handlePaymentComplete" class="w-full py-3 bg-green-600 hover:bg-green-500 text-white rounded-lg font-bold transition-colors shadow-lg shadow-green-900/20">
          我已完成支付
        </button>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
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
const selectedMethod = ref('ALIPAY')
const processing = ref(false)
const showPaymentModal = ref(false)
const qrCodeUrl = ref('')
const currentOrderNo = ref('')
const billingCycle = ref('monthly')

const finalPrice = computed(() => {
  if (!plan.value) return 0
  if (billingCycle.value === 'monthly') return plan.value.priceMonthly
  // Yearly price logic (matches Subscribe.vue)
  return plan.value.priceMonthly * 10
})

const cycleText = computed(() => {
  return billingCycle.value === 'monthly' ? '按月计费' : '按年计费'
})

onMounted(async () => {
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
    
    // Generate QR Code
    // If backend provides a specific QR code content or URL, use it.
    // Otherwise fallback to a mock URL or the paymentUrl itself.
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

const handlePaymentComplete = () => {
  showPaymentModal.value = false
  message.success('支付成功')
  router.push('/dashboard')
}
</script>