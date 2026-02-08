<template>
  <div class="min-h-screen bg-black text-white font-sans">
    <!-- Navbar -->
    <nav class="border-b border-neutral-800 px-6 py-4 flex items-center">
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

        <div v-if="plan" class="flex-1">
          <div class="flex justify-between items-center py-4 border-b border-neutral-800">
            <span class="text-xl font-medium">{{ plan.planName }} Plan</span>
            <span class="text-xl font-bold">¥{{ plan.priceMonthly }}</span>
          </div>
          <div class="py-2 text-neutral-500 text-sm">按月计费</div>

          <div class="flex justify-between items-center py-4 border-b border-neutral-800 mt-4 text-green-500">
            <span>限时折扣</span>
            <span>-¥0.00</span>
          </div>
          <div class="py-2 text-neutral-500 text-sm">暂无优惠</div>

          <div class="mt-auto pt-8">
            <div class="flex justify-between items-center py-2">
              <span class="text-neutral-400">税前总计</span>
              <span>¥{{ plan.priceMonthly }}</span>
            </div>
            <div class="flex justify-between items-center py-2">
              <span class="text-neutral-400">税费</span>
              <span>¥0.00</span>
            </div>
            <div class="flex justify-between items-center py-6 text-2xl font-bold border-t border-neutral-800 mt-4">
              <span>今日应付总额</span>
              <span>¥{{ plan.priceMonthly }}</span>
            </div>
          </div>
        </div>
        <div v-else class="flex items-center justify-center h-full text-neutral-500">
          加载订单信息...
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
          @click="confirmPayment"
          :disabled="processing || !plan"
          class="w-full py-4 bg-green-600 hover:bg-green-500 text-white rounded-lg font-bold text-lg transition-colors flex items-center justify-center disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <i v-if="processing" class="fas fa-spinner fa-spin mr-2"></i>
          支付 ¥{{ plan ? plan.priceMonthly : '0.00' }}
        </button>

        <div class="mt-6 text-xs text-neutral-500 space-y-2">
          <p>• 继续操作即表示你确认已阅读、理解并同意我们的 <a href="#" class="underline hover:text-neutral-300">服务条款</a> 和 <a href="#" class="underline hover:text-neutral-300">隐私政策</a>。</p>
          <p>• 本服务由 CoffeeViz 提供技术支持，你的支付方式将被安全保存以便于未来使用。</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useMessage } from 'naive-ui'
import api from '@/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const message = useMessage()

const plan = ref(null)
const selectedMethod = ref('ALIPAY')
const processing = ref(false)

onMounted(async () => {
  // If plan details are passed via state (from router.push)
  if (history.state && history.state.plan) {
    plan.value = history.state.plan
  } else {
    // If refreshed, maybe try to fetch plan by ID from query param if we implemented that
    const planId = route.query.planId
    if (planId) {
      try {
        const res = await api.get('/api/subscription/plans')
        const plans = res.data || []
        plan.value = plans.find(p => p.id == planId)
      } catch (e) {
        console.error(e)
      }
    }
  }

  if (!plan.value) {
    message.error('无法加载订单信息')
    router.push('/subscribe')
  }
})

const confirmPayment = async () => {
  if (!plan.value) return
  
  processing.value = true
  try {
    const res = await api.post('/api/payment/create', {
      planId: plan.value.id,
      billingCycle: 'monthly',
      paymentMethod: selectedMethod.value
    })
    
    const paymentData = res.data
    
    // Check if it's a URL (mock or real)
    if (paymentData.paymentUrl) {
      window.location.href = paymentData.paymentUrl
    } else if (paymentData.qrCode) {
        // If it's a QR code URL (our mock handler returns a URL now)
        if (paymentData.qrCode.startsWith('http')) {
             window.location.href = paymentData.qrCode
        } else {
             // Fallback for raw content, though we plan to fix backend to return URL
             // If it is raw content, we might need to show QR code here. 
             // But for Qoder style, usually it redirects to a payment page or shows QR overlay.
             // Given the mock handler change, we expect a URL.
             console.log("Received QR Code content:", paymentData.qrCode)
             // For now, let's assume backend update will make it a URL to the mock gateway
        }
    } else {
      message.success('订单创建成功')
      router.push('/dashboard')
    }
    
  } catch (error) {
    console.error(error)
    message.error(error.message || '创建支付订单失败')
    processing.value = false
  }
}
</script>