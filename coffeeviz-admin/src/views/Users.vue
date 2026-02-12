<template>
  <div class="space-y-6">
    <!-- Filters -->
    <div class="flex flex-col md:flex-row gap-4 justify-between bg-bg-card p-4 rounded-xl border border-white/5">
      <div class="flex gap-4">
        <n-input placeholder="搜索用户 (邮箱/昵称)" class="w-64" v-model:value="searchKey" @keyup.enter="loadUsers">
          <template #prefix><n-icon><SearchOutline /></n-icon></template>
        </n-input>
        <n-select v-model:value="planFilter" :options="planOptions" placeholder="所有订阅" class="w-36" clearable @update:value="loadUsers" />
      </div>
      <n-button type="primary" @click="exportData">
        <template #icon><n-icon><DownloadOutline /></n-icon></template>
        导出数据
      </n-button>
    </div>

    <!-- User List -->
    <div class="space-y-3">
      <div v-for="user in users" :key="user.id"
        class="bg-bg-card p-4 rounded-xl border border-white/5 flex flex-col md:flex-row items-center justify-between gap-4 hover:bg-bg-hover transition-colors"
      >
        <div class="flex items-center gap-4 w-full md:w-auto">
          <n-avatar :size="40" round :src="`https://ui-avatars.com/api/?name=${encodeURIComponent(user.name)}&background=random`" />
          <div>
            <h4 class="text-white font-medium text-sm">{{ user.name }}</h4>
            <p class="text-gray-500 text-xs">{{ user.email }}</p>
          </div>
        </div>
        <div class="flex items-center gap-6 w-full md:w-auto justify-between md:justify-start">
          <div class="text-center md:text-left">
            <p class="text-gray-500 text-xs">当前订阅</p>
            <n-tag :type="planTagType(user.plan)" size="small" class="mt-1">{{ user.plan }}</n-tag>
          </div>
          <div class="text-center md:text-left">
            <p class="text-gray-500 text-xs">注册时间</p>
            <p class="text-gray-300 text-xs mt-1">{{ formatDate(user.createdAt) }}</p>
          </div>
          <div class="text-center md:text-left">
            <p class="text-gray-500 text-xs">状态</p>
            <span :class="user.status === 1 ? 'text-green-500' : 'text-red-500'" class="text-xs">
              ● {{ user.status === 1 ? '正常' : '已封禁' }}
            </span>
          </div>
        </div>
        <div class="flex gap-2 w-full md:w-auto">
          <n-button size="small" type="primary" ghost @click="openGiftModal(user)">
            <template #icon><n-icon><GiftOutline /></n-icon></template>
            赠送订阅
          </n-button>
          <n-dropdown :options="userActions(user)" @select="(key) => handleUserAction(key, user)">
            <n-button size="small" secondary>
              <template #icon><n-icon><EllipsisHorizontalOutline /></n-icon></template>
            </n-button>
          </n-dropdown>
        </div>
      </div>
    </div>

    <div v-if="users.length === 0" class="text-center text-gray-500 py-12">暂无用户数据</div>

    <!-- Gift Modal -->
    <n-modal v-model:show="showGiftModal" preset="card" title="赠送订阅会员" style="width: 450px;" class="bg-bg-card">
      <n-form :model="giftForm" label-placement="top">
        <n-form-item label="选择用户">
          <n-input :value="giftForm.userName" disabled />
        </n-form-item>
        <n-form-item label="赠送计划">
          <n-select v-model:value="giftForm.plan" :options="giftPlanOptions" />
        </n-form-item>
        <n-form-item label="赠送原因 (备注)">
          <n-input v-model:value="giftForm.reason" type="textarea" :rows="2" />
        </n-form-item>
      </n-form>
      <template #action>
        <n-space justify="end">
          <n-button @click="showGiftModal = false">取消</n-button>
          <n-button type="primary" :loading="giftLoading" @click="confirmGift">确认赠送</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import { SearchOutline, DownloadOutline, GiftOutline, EllipsisHorizontalOutline } from '@vicons/ionicons5'
import api from '@/api'

const message = useMessage()
const dialog = useDialog()
const searchKey = ref('')
const planFilter = ref(null)
const showGiftModal = ref(false)
const giftLoading = ref(false)

const giftForm = reactive({ userId: null, userName: '', plan: 'pro_month', reason: '' })
const giftPlanOptions = [
  { label: 'Pro 月卡 (30天)', value: 'pro_month' },
  { label: 'Pro 年卡 (365天)', value: 'pro_year' },
  { label: 'Team 月卡 (30天)', value: 'team_month' }
]
const planOptions = [
  { label: 'Free', value: 'Free' },
  { label: 'Pro Plan', value: 'Pro Plan' },
  { label: 'Team Plan', value: 'Team Plan' }
]

const userActions = (user) => [
  { label: '查看详情', key: 'detail' },
  { label: '重置密码', key: 'reset' },
  { label: user.status === 1 ? '封禁用户' : '解封用户', key: 'ban' }
]

const users = ref([])

const loadUsers = async () => {
  try {
    const res = await api.get('/api/admin/users', {
      params: { keyword: searchKey.value || undefined, planFilter: planFilter.value || undefined }
    })
    users.value = res.data?.list || []
  } catch (e) {
    message.error('加载用户列表失败')
  }
}

const formatDate = (dt) => {
  if (!dt) return ''
  return new Date(dt).toLocaleDateString('zh-CN')
}

const planTagType = (plan) => {
  if (plan && plan.includes('Pro')) return 'info'
  if (plan && plan.includes('Team')) return 'warning'
  return 'default'
}

const openGiftModal = (user) => {
  giftForm.userId = user.id
  giftForm.userName = `${user.name} (${user.email})`
  giftForm.plan = 'pro_month'
  giftForm.reason = ''
  showGiftModal.value = true
}

const confirmGift = async () => {
  giftLoading.value = true
  try {
    await api.post(`/api/admin/users/${giftForm.userId}/gift`, {
      userId: giftForm.userId,
      plan: giftForm.plan,
      reason: giftForm.reason
    })
    message.success('赠送成功')
    showGiftModal.value = false
    loadUsers()
  } catch (e) {
    message.error('赠送失败: ' + (e.message || e))
  } finally {
    giftLoading.value = false
  }
}

const handleUserAction = async (key, user) => {
  if (key === 'ban') {
    const newStatus = user.status === 1 ? 0 : 1
    const action = newStatus === 0 ? '封禁' : '解封'
    dialog.warning({
      title: `确认${action}`,
      content: `确定要${action}用户 ${user.name} 吗？`,
      positiveText: '确定',
      negativeText: '取消',
      onPositiveClick: async () => {
        try {
          await api.put(`/api/admin/users/${user.id}/status`, { status: newStatus })
          message.success(`${action}成功`)
          loadUsers()
        } catch (e) {
          message.error(`${action}失败`)
        }
      }
    })
  } else if (key === 'reset') {
    dialog.warning({
      title: '确认重置密码',
      content: `确定要重置用户 ${user.name} 的密码吗？密码将重置为默认密码。`,
      positiveText: '确定',
      negativeText: '取消',
      onPositiveClick: async () => {
        try {
          await api.put(`/api/admin/users/${user.id}/reset-password`)
          message.success('密码已重置')
        } catch (e) {
          message.error('重置失败')
        }
      }
    })
  } else if (key === 'detail') {
    message.info(`用户详情: ${user.name}`)
  }
}

const exportData = () => { message.info('导出功能开发中') }

onMounted(() => { loadUsers() })
</script>
