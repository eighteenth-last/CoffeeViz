<template>
  <section id="page-subscribe" class="page-view active">
    <div class="text-center mb-16">
      <h1 class="text-4xl font-black text-white mb-4 tracking-tight">选择适合您的 <span class="text-amber-500">CoffeeViz</span> 计划</h1>
      <p class="text-neutral-400 max-w-2xl mx-auto">解锁高级架构可视化功能，让数据库文档管理更智能、更高效。</p>
    </div>

    <div v-if="loading" class="flex justify-center py-20">
      <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-amber-500"></div>
    </div>

    <div v-else>
      <!-- Billing Cycle Toggle -->
      <div class="flex justify-center mb-8">
        <div class="bg-neutral-900 p-0.5 rounded-full inline-flex relative border border-neutral-800">
          <div 
            class="absolute top-0.5 bottom-0.5 rounded-full bg-neutral-700 transition-all duration-300 ease-in-out shadow-sm"
            :class="billingCycle === 'monthly' ? 'left-0.5 w-[calc(50%-2px)]' : 'left-[50%] w-[calc(50%-2px)]'"
          ></div>
          <button 
            @click="billingCycle = 'monthly'"
            class="relative z-10 py-1.5 rounded-full text-sm font-bold transition-colors duration-300 w-24"
            :class="billingCycle === 'monthly' ? 'text-white' : 'text-neutral-400 hover:text-white'"
          >
            按月付费
          </button>
          <button 
            @click="billingCycle = 'yearly'"
            class="relative z-10 py-1.5 rounded-full text-sm font-bold transition-colors duration-300 w-24 flex items-center justify-center"
            :class="billingCycle === 'yearly' ? 'text-white' : 'text-neutral-400 hover:text-white'"
          >
            按年付费
            <span class="ml-1 text-[10px] bg-amber-600 text-white px-1.5 rounded-full py-0.5 transform scale-90">省20%</span>
          </button>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-6xl mx-auto">
        <div 
          v-for="plan in plans" 
          :key="plan.id"
          :class="[
            'glass-card p-8 rounded-2xl flex flex-col relative overflow-hidden transition-all group',
            getPlanStyle(plan.planCode).cardClass
          ]"
        >
          <div v-if="plan.planCode === 'PRO'" class="absolute top-0 right-0 bg-amber-600 text-white text-[10px] font-bold px-3 py-1 rounded-bl-lg">推荐</div>
          
          <div class="mb-6">
            <div :class="['text-sm font-bold uppercase tracking-widest mb-2', getPlanStyle(plan.planCode).titleClass]">{{ plan.planName }}</div>
            <div class="text-4xl font-black text-white flex items-baseline">
              ¥{{ getPrice(plan) }}
              <span class="text-lg text-neutral-500 font-medium ml-1">/{{ getUnit(plan) }}</span>
            </div>
            <p v-if="billingCycle === 'yearly' && plan.priceMonthly > 0" class="text-xs text-amber-500 mt-2 font-medium">
              相当于 ¥{{ Math.round(getPrice(plan) / 12) }}/月
            </p>
            <p v-else :class="['text-sm mt-2', getPlanStyle(plan.planCode).descClass]">{{ plan.description }}</p>
          </div>
          
          <ul class="space-y-4 mb-8 flex-1">
            <li v-for="(feature, index) in parseFeatures(plan.features)" :key="index" :class="['flex items-center text-sm', getPlanStyle(plan.planCode).featureTextClass]">
              <i :class="['fas fa-check mr-3', getPlanStyle(plan.planCode).iconClass]"></i>
              <span v-html="formatFeature(feature)"></span>
            </li>
          </ul>

          <button 
            v-if="plan.planCode === 'PRO'"
            @click="handleSubscribe(plan)"
            class="w-full py-3 btn-amber rounded-xl text-white font-bold flex items-center justify-center group"
          >
            {{ getButtonText(plan) }}
            <i class="fas fa-arrow-right ml-2 group-hover:translate-x-1 transition-transform"></i>
          </button>
          <button 
            v-else
            @click="handleSubscribe(plan)"
            :class="[
              'w-full py-3 border rounded-xl font-bold transition-all',
              isCurrentPlan(plan) 
                ? 'bg-neutral-800 border-neutral-600 text-neutral-400 cursor-default' 
                : 'bg-neutral-800 hover:bg-neutral-700 border-neutral-700 text-white'
            ]"
            :disabled="isCurrentPlan(plan)"
          >
            {{ getButtonText(plan) }}
          </button>
        </div>
      </div>
    </div>
    
    <div class="mt-20 text-center">
      <h3 class="text-xl font-bold text-white mb-8">常见问题</h3>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-8 max-w-4xl mx-auto text-left">
        <div>
          <h4 class="text-amber-500 font-bold mb-2">如何切换订阅计划？</h4>
          <p class="text-sm text-neutral-400">您可以随时在账户设置中升级或降级您的订阅计划，变更将在下一个计费周期生效。</p>
        </div>
        <div>
          <h4 class="text-amber-500 font-bold mb-2">支持哪些支付方式？</h4>
          <p class="text-sm text-neutral-400">我们支持支付宝、微信支付以及主流信用卡（Visa, MasterCard）。</p>
        </div>
        <div>
          <h4 class="text-amber-500 font-bold mb-2">数据安全如何保障？</h4>
          <p class="text-sm text-neutral-400">所有数据传输均采用 SSL 加密，敏感配置信息（如数据库密码）仅存储在您的本地浏览器中。</p>
        </div>
        <div>
          <h4 class="text-amber-500 font-bold mb-2">可以开发票吗？</h4>
          <p class="text-sm text-neutral-400">支持开具增值税电子普通发票，请在支付完成后在订单详情页申请。</p>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api'
import { useMessage } from 'naive-ui'
import { useUserStore } from '@/store/user'

const router = useRouter()
const message = useMessage()
const userStore = useUserStore()
const plans = ref([])
const currentSubscription = ref(null)
const loading = ref(false)
const billingCycle = ref('monthly')

const getPrice = (plan) => {
  if (plan.planCode === 'FREE') return 0
  if (billingCycle.value === 'monthly') return plan.priceMonthly
  // Yearly price is monthly * 10 (2 months free)
  return plan.priceMonthly * 10
}

const getUnit = (plan) => {
  const period = billingCycle.value === 'monthly' ? '月' : '年'
  return plan.planCode === 'TEAM' ? `团队/${period}` : period
}

const handleSubscribe = (plan) => {
  if (!userStore.isLoggedIn) {
    message.info('请先登录')
    router.push({ 
      path: '/login',
      query: { redirect: '/subscribe' }
    })
    return
  }
  
  if (plan.planCode === 'FREE') {
    // If they want to switch to FREE, usually means cancel or downgrade
    // For now, show info
    message.info('您当前已经是免费版用户或可以直接使用免费版功能')
    return
  }
  
  if (plan.planCode === 'TEAM') {
    window.location.href = 'mailto:sales@coffeeviz.com?subject=Inquiry about Team Plan'
    return
  }
  
  // Navigate to checkout
  router.push({
    name: 'Checkout',
    query: { planId: plan.id, billingCycle: billingCycle.value },
    state: { plan, billingCycle: billingCycle.value }
  })
}

const getPlanStyle = (code) => {
  switch (code) {
    case 'PRO':
      return {
        cardClass: 'border-amber-600/50 bg-amber-600/[0.02] transform md:-translate-y-4 shadow-2xl shadow-amber-900/20',
        titleClass: 'text-amber-500',
        descClass: 'text-amber-500/80',
        featureTextClass: 'text-white',
        iconClass: 'text-amber-500'
      }
    case 'TEAM':
      return {
        cardClass: 'border border-neutral-600 hover:border-neutral-500',
        titleClass: 'text-purple-500',
        descClass: 'text-neutral-500',
        featureTextClass: 'text-neutral-300',
        iconClass: 'text-purple-500'
      }
    default: // FREE
      return {
        cardClass: 'border border-neutral-600 hover:border-neutral-500',
        titleClass: 'text-neutral-500',
        descClass: 'text-neutral-500',
        featureTextClass: 'text-neutral-300',
        iconClass: 'text-green-500'
      }
  }
}

const parseFeatures = (featuresJson) => {
  if (!featuresJson) return []
  try {
    return typeof featuresJson === 'string' ? JSON.parse(featuresJson) : featuresJson
  } catch (e) {
    console.error('Failed to parse features:', e)
    return []
  }
}

const formatFeature = (text) => {
  // Simple bold formatting for text like "Unlimited projects" where "Unlimited" might be emphasized
  // This is a basic implementation, can be enhanced
  return text.replace(/无限/g, '<span class="font-bold">无限</span>')
}

const isCurrentPlan = (plan) => {
  if (!userStore.isLoggedIn) return false
  if (!currentSubscription.value) {
    // If logged in but no subscription found, assume FREE is current
    return plan.planCode === 'FREE'
  }
  return currentSubscription.value.planCode === plan.planCode
}

const getButtonText = (plan) => {
  if (isCurrentPlan(plan)) {
    return '当前版本'
  }
  if (plan.planCode === 'TEAM') {
    return '联系销售'
  }
  if (plan.planCode === 'FREE') {
    return '免费试用'
  }
  return '立即订阅'
}

const fetchCurrentSubscription = async () => {
  if (!userStore.isLoggedIn) return
  
  try {
    const res = await api.get('/api/subscription/current')
    if (res.data) {
      currentSubscription.value = res.data
    }
  } catch (error) {
    // If 401 or not found, just ignore, user might not be logged in or no sub
    console.log('No active subscription or not logged in')
  }
}

const fetchPlans = async () => {
  loading.value = true
  try {
    const promises = [api.get('/api/subscription/plans')]
    if (userStore.isLoggedIn) {
      promises.push(fetchCurrentSubscription())
    }
    
    const [plansRes] = await Promise.all(promises)
    plans.value = plansRes.data || []
    // Sort plans if needed, though backend usually handles it
    plans.value.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
  } catch (error) {
    message.error('获取订阅计划失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchPlans()
})
</script>
