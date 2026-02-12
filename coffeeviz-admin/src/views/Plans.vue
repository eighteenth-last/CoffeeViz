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
        <div class="space-y-3 mb-6">
          <div class="flex justify-between text-sm">
            <span class="text-gray-400">图表上限/库</span>
            <span class="text-white">{{ plan.maxDiagramsPerRepo === -1 ? '无限' : plan.maxDiagramsPerRepo }}</span>
          </div>
          <div class="flex justify-between text-sm">
            <span class="text-gray-400">项目数量</span>
            <span class="text-white">{{ plan.maxRepositories }} 个</span>
          </div>
          <div class="flex justify-between text-sm">
            <span class="text-gray-400">AI 支持</span>
            <span class="text-white">{{ plan.supportAi ? '是' : '否' }}</span>
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
    <n-modal v-model:show="showPlanModal" preset="card" :title="editingPlan ? '编辑计划' : '新增计划'" style="width: 520px;" class="bg-bg-card">
      <n-form :model="planForm" label-placement="top">
        <div class="grid grid-cols-2 gap-4">
          <n-form-item label="计划名称"><n-input v-model:value="planForm.name" placeholder="例如: Pro Plan" /></n-form-item>
          <n-form-item label="价格 (CNY/月)"><n-input-number v-model:value="planForm.price" :min="0" placeholder="29" class="w-full" /></n-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <n-form-item label="生成次数/月 (-1无限)"><n-input-number v-model:value="planForm.generateLimit" :min="-1" class="w-full" /></n-form-item>
          <n-form-item label="项目上限"><n-input-number v-model:value="planForm.projectLimit" :min="1" class="w-full" /></n-form-item>
        </div>
        <n-form-item label="可用模型">
          <n-checkbox-group v-model:value="planForm.models">
            <n-space><n-checkbox value="gpt-3.5">GPT-3.5</n-checkbox><n-checkbox value="gpt-4">GPT-4</n-checkbox><n-checkbox value="claude-3">Claude 3</n-checkbox></n-space>
          </n-checkbox-group>
        </n-form-item>
        <n-form-item label="权益描述 (每行一项)">
          <n-input v-model:value="planForm.benefits" type="textarea" :rows="3" placeholder="支持导出高清图片&#10;优先客服支持" />
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

const planForm = reactive({
  name: '', price: 0, generateLimit: -1, projectLimit: 10,
  models: ['gpt-3.5'], benefits: '', enabled: true
})

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
  Object.assign(planForm, { name: '', price: 0, generateLimit: -1, projectLimit: 10, models: ['gpt-3.5'], benefits: '', enabled: true })
  showPlanModal.value = true
}

const editPlan = (plan) => {
  editingPlan.value = plan
  planForm.name = plan.planName
  planForm.price = plan.priceMonthly ? Number(plan.priceMonthly) : 0
  planForm.generateLimit = plan.maxDiagramsPerRepo || -1
  planForm.projectLimit = plan.maxRepositories || 10
  planForm.models = ['gpt-3.5']
  planForm.benefits = plan.features || ''
  planForm.enabled = plan.status === 'active'
  showPlanModal.value = true
}

const savePlan = async () => {
  saving.value = true
  try {
    if (editingPlan.value) {
      await api.put(`/api/admin/plans/${editingPlan.value.id}`, planForm)
    } else {
      await api.post('/api/admin/plans', planForm)
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
