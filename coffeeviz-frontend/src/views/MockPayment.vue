<template>
  <div class="min-h-screen bg-[#f5f5f5] flex flex-col items-center justify-center p-4">
    <div class="bg-white rounded-lg shadow-lg max-w-md w-full overflow-hidden">
      <!-- Header -->
      <div class="bg-[#1677ff] p-6 text-white text-center" v-if="method === 'ALIPAY'">
        <i class="fab fa-alipay text-4xl mb-2"></i>
        <h2 class="text-xl font-bold">支付宝收银台</h2>
      </div>
      <div class="bg-[#07c160] p-6 text-white text-center" v-else-if="method === 'WECHAT'">
        <i class="fab fa-weixin text-4xl mb-2"></i>
        <h2 class="text-xl font-bold">微信支付</h2>
      </div>
      <div class="bg-gray-800 p-6 text-white text-center" v-else>
        <i class="fas fa-credit-card text-4xl mb-2"></i>
        <h2 class="text-xl font-bold">支付网关</h2>
      </div>

      <!-- Content -->
      <div class="p-8">
        <div class="text-center mb-8">
          <p class="text-gray-500 mb-2">订单金额</p>
          <div class="text-4xl font-bold text-gray-900">¥{{ amount || '0.00' }}</div>
          <p class="text-sm text-gray-400 mt-2">订单号: {{ orderNo }}</p>
        </div>

        <div class="space-y-4">
          <div class="border rounded-lg p-4 bg-gray-50 text-sm text-gray-600">
            <p><strong>收款方：</strong> CoffeeViz Technology Co., Ltd.</p>
            <p><strong>商品：</strong> {{ subject || 'CoffeeViz 订阅服务' }}</p>
          </div>
          
          <button 
            @click="handlePayment" 
            :disabled="processing"
            :class="[
              'w-full py-3 rounded-lg font-bold text-white text-lg transition-all transform active:scale-95',
              method === 'ALIPAY' ? 'bg-[#1677ff] hover:bg-[#4096ff]' : 
              method === 'WECHAT' ? 'bg-[#07c160] hover:bg-[#2bc676]' : 'bg-gray-800 hover:bg-gray-700'
            ]"
          >
            <span v-if="processing"><i class="fas fa-spinner fa-spin mr-2"></i>处理中...</span>
            <span v-else>立即支付</span>
          </button>
          
          <button @click="cancelPayment" class="w-full py-3 text-gray-500 font-medium hover:text-gray-700">
            取消支付
          </button>
        </div>
      </div>
    </div>
    
    <div class="mt-8 text-center text-gray-400 text-sm">
      <p>此页面为模拟支付网关，仅用于开发测试环境。</p>
      <p>&copy; 2024 CoffeeViz Dev Environment</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import api from '@/api'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const method = ref(route.query.method || 'ALIPAY')
const orderNo = ref(route.query.orderNo || '')
const amount = ref(route.query.amount || '0.00')
const subject = ref(route.query.subject || '')
const processing = ref(false)

const handlePayment = () => {
  processing.value = true
  // Simulate network delay
  setTimeout(async () => {
    try {
      // In a real scenario, the payment gateway would call a callback URL.
      // Here we can call the callback endpoint directly or just redirect back.
      // Let's call the callback to actually update the order status in backend if needed.
      // But usually callbacks are server-to-server.
      
      // For simulation, we'll assume the payment is successful and redirect to a success page or back to app.
      
      // Simulate callback call (optional, depends on backend logic)
      // await api.post(`/api/payment/callback/${method.value.toLowerCase()}`, { ... })
      
      message.success('支付成功！')
      
      // Redirect back to main app
      router.push('/dashboard') // or /orders
    } catch (error) {
      message.error('支付处理失败')
    } finally {
      processing.value = false
    }
  }, 1500)
}

const cancelPayment = () => {
  message.info('支付已取消')
  router.back()
}
</script>