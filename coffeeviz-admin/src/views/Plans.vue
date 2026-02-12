<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h3 class="text-lg font-medium text-white">订阅计划管理</h3>
      <n-button type="primary" @click="openCreateModal">
        <template #icon><n-icon><AddOutline /></n-icon></template>
        新增计划
      </n-button>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div v-for="plan in plans" :key="plan.id"
        :class="['bg-bg-card rounded-xl p-6 hover:border-primary-500/50 transition-all group relative overflow-hidden',
          plan.planCode === 'PRO' ? 'border border-blue-500/30' : 'border border-white/5']"
      >
        <div class="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
          <n-icon :size="60" class="text-white"><component :is="iconForPlan(plan.planCode)" /></n-icon>
        </div>
        <div class="flex justify-between items-start mb-4">
          <div>
            <h4 class="text-xl font-bold text-white">{{ plan.planName }}</h4>
            <p class="text-gray-400 text-xs">{{ plan.description }}</p>
          </div>
          <n-tag :type="tagTypeForPlan(plan.planCode)" size="small">
            ¥{{ plan.priceMonthly || 0 }}/月
          </n-tag>
        </div>
        <!-- 配额展示 -->
        <div class="space-y-3 mb-4">
          <div v-for="q in (plan.quotas || [])" :key="q.quotaType" class="flex justify-between text-sm">
            <span class="text-gray-400">{{ quotaTypeLabel(q.quotaType) }}</span>
            <span class="text-white">{{ formatQuotaValue(q) }}</span>
          </div>
          <div v-if="!plan.quotas || plan.quotas.length === 0" class="text-gray-500 text-sm">暂无配额配置</div>
        </div>
        <!-- 功能开关展示 -->
        <div class="space-y-2 mb-6 border-t border-white/5 pt-3">
          <div class="flex flex-wrap gap-2">
            <n-tag v-if="plan.supportJdbc === 1" size="tiny" type="success">JDBC</n-tag>
            <n-tag v-if="plan.supportAi === 1" size="tiny" type="info">AI</n-tag>
            <n-tag v-if="plan.supportExport === 1" size="tiny" type="warning">导出</n-tag>
            <n-tag v-if="plan.supportTeam === 1" size="tiny" type="primary">团队</n-tag>
          </div>
        </div>
        <div class="flex gap-2">
          <n-button class="flex-1" size="small" @click="editPlan(plan)">编辑</n-button>
          <n-button size="small" type="error" ghost @click="deletePlan(plan)">
            <template #icon><n-icon><TrashOutline /></n-icon></template>
          </n-button>
        </div>
      </div>
    </div>

    <!-- Plan Modal -->
    <n-modal v-model:show="showPlanModal" preset="card" :title="editingPlan ? '编辑计划' : '新增计划'" style="width: 620px;" class="bg-bg-card">
      <n-form :model="planForm" label-placement="top">
        <div class="grid grid-cols-2 gap-4">
          <n-form-item label="计划名称"><n-input v-model:value="planForm.name" placeholder="例如: Pro Plan" /></n-form-item>
          <n-form-item label="价格 (CNY/月)"><n-input-number v-model:value="planForm.price" :min="0" placeholder="29" class="w-full" /></n-form-item>
        </div>

        <!-- 功能开关 -->
        <div class="grid grid-cols-4 gap-4 mb-4">
          <n-form-item label="JDBC"><n-switch v-model:value="planForm.supportJdbc" :checked-value="1" :unchecked-value="0" /></n-form-item>
          <n-form-item label="AI"><n-switch v-model:value="planForm.supportAi" :checked-value="1" :unchecked-value="0" /></n-form-item>
          <n-form-item label="导出"><n-switch v-model:value="planForm.supportExport" :checked-value="1" :unchecked-value="0" /></n-form-item>
          <n-form-item label="团队"><n-switch v-model:value="planForm.supportTeam" :checked-value="1" :unchecked-value="0" /></n-form-item>
        </div>

        <!-- 配额列表 -->
        <n-form-item label="配额配置">
          <div class="w-full space-y-3">
            <div v-for="(q, idx) in planForm.quotas" :key="idx" class="flex gap-2 items-end">
              <n-select v-model:value="q.quotaType" :options="quotaTypeOptions" placeholder="类型" style="width: 140px;" />
              <n-input-number v-model:value="q.quotaLimit" :min="-1" placeholder="限额 (-1无限)" style="width: 130px;" />
              <n-select v-model:value="q.resetCycle" :options="resetCycleOptions" placeholder="周期" style="width: 110px;" />
              <n-input v-model:value="q.description" placeholder="描述(可选)" style="width: 140px;" />
              <n-button type="error" ghost @click="removeQuota(idx)">
                <template #icon><n-icon><TrashOutline /></n-icon></template>
              </n-button>
            </div>
            <n-button dashed block @click="addQuota">
              <template #icon><n-icon><AddOutline /></n-icon></template>
              添加配额
            </n-button>
          </div>
        </n-form-item>

        <n-form-item label="权益描述">
          <div class="w-full space-y-2">
            <div v-for="(benefit, index) in planForm.benefits" :key="index" class="flex gap-2">
              <n-input v-model:value="planForm.benefits[index]" placeholder="输入权益内容" />
              <n-button type="error" ghost @click="removeBenefit(index)">
                <template #icon><n-icon><TrashOutline /></n-icon></template>
              </n-button>
            </div>
            <n-button dashed block @click="addBenefit">
              <template #icon><n-icon><AddOutline /></n-icon></template>
              添加权益
            </n-button>
          </div>
        </n-form-item>
        <n-form-item label="上架状态">
          <n-switch v-model:value="planForm.enabled" />
        </n-form-item>
      </n-form>
      <template #action>
        <n-space justify="end">
          <n-button @click="showPlanModal = false">取消</n-button>
          <n-button type="primary" :loading="saving" @click="savePlan">保存计划</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import { AddOutline, TrashOutline, PaperPlaneOutline, RocketOutline, BusinessOutline } from '@vicons/ionicons5'
import api from '@/api'

const message = useMessage()
const dialog = useDialog()
const showPlanModal = ref(false)
const saving = ref(false)
const editingPlan = ref(null)

const quotaTypeOptions = [
  { label: '架构库数量', value: 'repository' },
  { label: '架构图/库', value: 'diagram' },
  { label: 'SQL解析', value: 'sql_parse' },
  { label: 'AI生成', value: 'ai_generate' }
]
const resetCycleOptions = [
  { label: '每天', value: 'daily' },
  { label: '每月', value: 'monthly' },
  { label: '每年', value: 'yearly' },
  { label: '永不重置', value: 'never' }
]

const quotaTypeLabel = (type) => {
  const map = { repository: '架构库数量', diagram: '架构图/库', sql_parse: 'SQL解析额度', ai_generate: 'AI生成额度' }
  return map[type] || type
}
const formatQuotaValue = (q) => {
  if (q.quotaLimit === -1) return '无限'
  const cycleMap = { daily: '/天', monthly: '/月', yearly: '/年', never: '' }
  return q.quotaLimit + (cycleMap[q.resetCycle] || '')
}

const planForm = reactive({
  name: '', price: 0,
  supportJdbc: 0, supportAi: 0, supportExport: 0, supportTeam: 0,
  quotas: [],
  benefits: [], enabled: true
})

const addQuota = () => {
  planForm.quotas.push({ quotaType: null, quotaLimit: 0, resetCycle: 'monthly', description: '' })
}
const removeQuota = (idx) => { planForm.quotas.splice(idx, 1) }
const addBenefit = () => { planForm.benefits.push('') }
const removeBenefit = (index) => { planForm.benefits.splice(index, 1) }

const plans = ref([])

const iconForPlan = (code) => {
  if (code === 'PRO') return RocketOutline
  if (code === 'TEAM') return BusinessOutline
  return PaperPlaneOutline
}
const tagTypeForPlan = (code) => {
  if (code === 'PRO') return 'info'
  if (code === 'TEAM') return 'warning'
  return 'default'
}

const loadPlans = async () => {
  try {
    const res = await api.get('/api/admin/plans')
    plans.value = res.data || []
  } catch (e) {
    message.error('加载计划列表失败')
  }
}

const openCreateModal = () => {
  editingPlan.value = null
  Object.assign(planForm, {
    name: '', price: 0,
    supportJdbc: 0, supportAi: 0, supportExport: 0, supportTeam: 0,
    quotas: [], benefits: [], enabled: true
  })
  showPlanModal.value = true
}

const editPlan = (plan) => {
  editingPlan.value = plan
  planForm.name = plan.planName
  planForm.price = plan.priceMonthly ? Number(plan.priceMonthly) : 0
  planForm.supportJdbc = plan.supportJdbc || 0
  planForm.supportAi = plan.supportAi || 0
  planForm.supportExport = plan.supportExport || 0
  planForm.supportTeam = plan.supportTeam || 0
  // 配额
  planForm.quotas = (plan.quotas || []).map(q => ({
    quotaType: q.quotaType,
    quotaLimit: q.quotaLimit,
    resetCycle: q.resetCycle || 'monthly',
    description: q.description || ''
  }))
  // 权益
  try {
    planForm.benefits = plan.features ? JSON.parse(plan.features) : []
    if (!Array.isArray(planForm.benefits)) planForm.benefits = plan.features ? [plan.features] : []
  } catch (e) {
    planForm.benefits = plan.features ? [plan.features] : []
  }
  planForm.enabled = plan.status === 'active'
  showPlanModal.value = true
}

const savePlan = async () => {
  saving.value = true
  try {
    const payload = {
      name: planForm.name,
      price: planForm.price,
      supportJdbc: planForm.supportJdbc,
      supportAi: planForm.supportAi,
      supportExport: planForm.supportExport,
      supportTeam: planForm.supportTeam,
      quotas: planForm.quotas.filter(q => q.quotaType),
      benefits: JSON.stringify(planForm.benefits.filter(b => b && b.trim())),
      enabled: planForm.enabled
    }
    if (editingPlan.value) {
      await api.put(`/api/admin/plans/${editingPlan.value.id}`, payload)
    } else {
      await api.post('/api/admin/plans', payload)
    }
    message.success('保存成功')
    showPlanModal.value = false
    loadPlans()
  } catch (e) {
    message.error('保存失败: ' + (e.message || e))
  } finally {
    saving.value = false
  }
}

const deletePlan = (plan) => {
  dialog.error({
    title: '确认删除',
    content: `确定要删除计划 ${plan.planName} 吗？`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await api.delete(`/api/admin/plans/${plan.id}`)
        message.success('删除成功')
        loadPlans()
      } catch (e) {
        message.error('删除失败')
      }
    }
  })
}

onMounted(() => { loadPlans() })
</script>
