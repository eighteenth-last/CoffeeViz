<template>
  <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
    <!-- Send Form -->
    <div class="lg:col-span-1 bg-bg-card p-6 rounded-xl border border-white/5 h-fit">
      <h3 class="text-white font-semibold mb-6">发送通知</h3>
      <n-form :model="form" label-placement="top">
        <n-form-item label="接收对象">
          <n-select v-model:value="form.target" :options="targetOptions" />
        </n-form-item>
        <n-form-item label="发送渠道">
          <n-checkbox-group v-model:value="form.channels">
            <n-space><n-checkbox value="inbox">站内信</n-checkbox><n-checkbox value="email">邮件</n-checkbox><n-checkbox value="sms">短信</n-checkbox></n-space>
          </n-checkbox-group>
        </n-form-item>
        <n-form-item label="标题">
          <n-input v-model:value="form.title" placeholder="通知标题" />
        </n-form-item>
        <n-form-item label="内容">
          <n-input v-model:value="form.content" type="textarea" :rows="4" placeholder="通知内容" />
        </n-form-item>
        <n-button type="primary" block :loading="sending" @click="sendNotification">
          <template #icon><n-icon><SendOutline /></n-icon></template>
          发送通知
        </n-button>
      </n-form>
    </div>

    <!-- History -->
    <div class="lg:col-span-2 bg-bg-card p-6 rounded-xl border border-white/5">
      <div class="flex items-center justify-between mb-6">
        <h3 class="text-white font-semibold">发送记录</h3>
        <n-pagination
          v-if="history.length > 0"
          v-model:page="page"
          v-model:page-size="pageSize"
          :item-count="history.length"
          :page-sizes="[10, 20, 50]"
          size="small"
        />
      </div>
      <div class="space-y-4">
        <div v-for="item in paginatedHistory" :key="item.id"
          class="flex items-start gap-4 p-4 rounded-lg bg-bg-input/50 border border-white/5"
        >
          <div :class="['w-8 h-8 rounded flex items-center justify-center shrink-0',
            item.status === 'sent' ? 'bg-green-500/10 text-green-500' : 'bg-red-500/10 text-red-500']">
            <n-icon><CheckmarkOutline /></n-icon>
          </div>
          <div class="flex-1">
            <div class="flex justify-between">
              <h4 class="text-white text-sm font-medium">{{ item.title }}</h4>
              <span class="text-gray-500 text-xs">{{ formatTime(item.time) }}</span>
            </div>
            <p class="text-gray-400 text-xs mt-1 line-clamp-2">{{ item.content }}</p>
            <div class="flex gap-2 mt-2">
              <n-tag size="tiny" type="info">{{ item.target }}</n-tag>
              <n-tag size="tiny">{{ item.channels }}</n-tag>
            </div>
          </div>
        </div>
        <div v-if="history.length === 0" class="text-center text-gray-500 py-8">暂无发送记录</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useMessage } from 'naive-ui'
import { SendOutline, CheckmarkOutline } from '@vicons/ionicons5'
import api from '@/api'

const message = useMessage()
const sending = ref(false)

// 分页状态
const page = ref(1)
const pageSize = ref(10)

const targetOptions = [
  { label: '所有用户', value: 'all' },
  { label: 'Pro 用户', value: 'pro' },
  { label: '团队管理员', value: 'team' },
  { label: '指定用户 ID', value: 'specific' }
]

const form = reactive({
  target: 'all', channels: ['inbox'], title: '', content: ''
})

const history = ref([])

const paginatedHistory = computed(() => {
  const start = (page.value - 1) * pageSize.value
  const end = start + pageSize.value
  return history.value.slice(start, end)
})

const loadHistory = async () => {
  try {
    const res = await api.get('/api/admin/notifications/history')
    history.value = res.data || []
  } catch { /* ignore */ }
}

const formatTime = (dt) => {
  if (!dt) return ''
  const d = new Date(dt)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return d.toLocaleDateString('zh-CN')
}

const sendNotification = async () => {
  if (!form.title || !form.content) { message.warning('请填写标题和内容'); return }
  sending.value = true
  try {
    await api.post('/api/admin/notifications/send', {
      target: form.target,
      channels: form.channels,
      title: form.title,
      content: form.content
    })
    message.success('通知已发送')
    form.title = ''
    form.content = ''
    loadHistory()
  } catch (e) {
    message.error('发送失败: ' + (e.message || e))
  } finally {
    sending.value = false
  }
}

onMounted(() => { loadHistory() })
</script>
