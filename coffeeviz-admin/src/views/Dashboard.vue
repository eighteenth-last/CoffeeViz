<template>
  <div class="space-y-6 animate-fadeIn">
    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      <div v-for="stat in stats" :key="stat.label" class="bg-bg-card p-6 rounded-xl border border-white/5">
        <div class="flex justify-between items-start">
          <div>
            <p class="text-gray-400 text-xs uppercase font-semibold">{{ stat.label }}</p>
            <h3 class="text-2xl font-bold text-white mt-1">{{ stat.value }}</h3>
            <p :class="[stat.trend > 0 ? 'text-green-500' : 'text-red-500', 'text-xs mt-2 flex items-center']">
              <n-icon :size="12" class="mr-1"><component :is="stat.trend > 0 ? TrendingUpOutline : TrendingDownOutline" /></n-icon>
              {{ stat.trend > 0 ? '+' : '' }}{{ stat.trend }}% 较上周
            </p>
          </div>
          <div :class="['p-3 rounded-lg', stat.iconBg]">
            <n-icon :size="20" :class="stat.iconColor"><component :is="stat.icon" /></n-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- Charts -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <div class="lg:col-span-2 bg-bg-card p-6 rounded-xl border border-white/5">
        <h3 class="text-white font-semibold mb-4">增长趋势</h3>
        <div class="h-64">
          <Line :data="lineChartData" :options="lineChartOptions" />
        </div>
      </div>
      <div class="bg-bg-card p-6 rounded-xl border border-white/5">
        <h3 class="text-white font-semibold mb-4">订阅分布</h3>
        <div class="h-64 flex justify-center">
          <Doughnut :data="doughnutData" :options="doughnutOptions" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Line, Doughnut } from 'vue-chartjs'
import {
  Chart as ChartJS, CategoryScale, LinearScale, PointElement,
  LineElement, ArcElement, Tooltip, Legend, Filler
} from 'chart.js'
import {
  PeopleOutline, BusinessOutline, WalletOutline, SparklesOutline,
  TrendingUpOutline, TrendingDownOutline
} from '@vicons/ionicons5'
import api from '@/api'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, ArcElement, Tooltip, Legend, Filler)

const stats = ref([
  { label: '总用户数', value: '—', trend: 0, icon: PeopleOutline, iconBg: 'bg-blue-500/10', iconColor: 'text-blue-500' },
  { label: '活跃团队', value: '—', trend: 0, icon: BusinessOutline, iconBg: 'bg-purple-500/10', iconColor: 'text-purple-500' },
  { label: '月收入', value: '—', trend: 0, icon: WalletOutline, iconBg: 'bg-green-500/10', iconColor: 'text-green-500' },
  { label: 'AI 调用次数', value: '—', trend: 0, icon: SparklesOutline, iconBg: 'bg-orange-500/10', iconColor: 'text-orange-500' }
])

const lineChartData = ref({
  labels: [],
  datasets: []
})

const lineChartOptions = {
  responsive: true, maintainAspectRatio: false,
  plugins: { legend: { labels: { color: '#e5e7eb' } } },
  scales: {
    y: { grid: { color: '#27272a' }, ticks: { color: '#9ca3af' } },
    x: { grid: { display: false }, ticks: { color: '#9ca3af' } }
  }
}

const doughnutData = ref({
  labels: [],
  datasets: []
})

const doughnutOptions = {
  responsive: true, maintainAspectRatio: false,
  plugins: { legend: { position: 'right', labels: { color: '#e5e7eb', padding: 20 } } },
  cutout: '70%'
}

const loadStats = async () => {
  try {
    const res = await api.get('/api/admin/dashboard/stats')
    const d = res.data
    stats.value[0].value = (d.totalUsers ?? 0).toLocaleString()
    stats.value[0].trend = d.userGrowth ?? 0
    stats.value[1].value = (d.activeTeams ?? 0).toLocaleString()
    stats.value[1].trend = d.teamGrowth ?? 0
    stats.value[2].value = '¥ ' + (d.monthlyRevenue ?? 0).toLocaleString()
    stats.value[2].trend = d.revenueGrowth ?? 0
    stats.value[3].value = (d.aiCalls ?? 0).toLocaleString()
    stats.value[3].trend = d.aiCallGrowth ?? 0

    // Update Charts
    if (d.chartDates) {
      lineChartData.value = {
        labels: d.chartDates,
        datasets: [
          {
            label: 'API Calls',
            data: d.chartApiCalls || [],
            borderColor: '#3b82f6',
            backgroundColor: 'rgba(59,130,246,0.1)',
            fill: true, tension: 0.4
          },
          {
            label: 'New Users',
            data: d.chartNewUsers || [],
            borderColor: '#10b981',
            backgroundColor: 'transparent',
            borderDash: [5, 5], tension: 0.4
          }
        ]
      }
    }

    if (d.subscriptionDistribution) {
      const labels = Object.keys(d.subscriptionDistribution)
      const data = Object.values(d.subscriptionDistribution)
      // Dynamic colors based on plan names if possible, or just cycle
      const colorMap = {
        'Free': '#374151', '免费版': '#374151',
        'Pro': '#3b82f6', '专业版': '#3b82f6',
        'Team': '#8b5cf6', '团队版': '#8b5cf6',
        'Enterprise': '#f59e0b', '企业版': '#f59e0b'
      }
      const bgColors = labels.map((label, i) => colorMap[label] || ['#ec4899', '#14b8a6', '#f43f5e'][i % 3])

      doughnutData.value = {
        labels: labels,
        datasets: [{
          data: data,
          backgroundColor: bgColors,
          borderWidth: 0
        }]
      }
    }
  } catch (e) {
    console.error('Dashboard 加载失败:', e)
    stats.value[0].value = '0'; stats.value[0].trend = 0
    stats.value[1].value = '0'; stats.value[1].trend = 0
    stats.value[2].value = '¥ 0'; stats.value[2].trend = 0
    stats.value[3].value = '0'; stats.value[3].trend = 0
  }
}

onMounted(() => { loadStats() })
</script>

<style scoped>
.animate-fadeIn { animation: fadeIn 0.3s ease-in-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
</style>
