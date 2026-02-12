<template>
  <div class="space-y-6">
    <!-- Filters -->
    <div class="flex justify-between items-center">
      <n-space>
        <n-button :type="filter === 'all' ? 'primary' : 'default'" size="small" @click="filter = 'all'; loadTeams()">全部 ({{ totalCount }})</n-button>
        <n-button :type="filter === 'banned' ? 'error' : 'default'" size="small" @click="filter = 'banned'; loadTeams()">异常 ({{ bannedCount }})</n-button>
      </n-space>
      <n-input placeholder="搜索团队..." size="small" class="w-48" v-model:value="searchKey" @keyup.enter="loadTeams">
        <template #prefix><n-icon><SearchOutline /></n-icon></template>
      </n-input>
    </div>

    <!-- Team Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div v-for="team in teamList" :key="team.id"
        :class="['bg-bg-card p-5 rounded-xl transition-colors', team.status === 'suspended' ? 'border border-red-500/20 hover:border-red-500/40' : 'border border-white/5 hover:border-white/10']"
      >
        <div class="flex justify-between items-start">
          <div class="flex items-center gap-3">
            <div :class="['w-10 h-10 rounded flex items-center justify-center text-white font-bold text-lg', team.status === 'suspended' ? 'bg-gray-700 text-gray-400' : colorForTeam(team.id)]">
              {{ team.name.charAt(0) }}
            </div>
            <div>
              <h4 :class="[team.status === 'suspended' ? 'text-gray-300' : 'text-white', 'font-medium']">{{ team.name }}</h4>
              <p class="text-gray-500 text-xs">ID: #{{ team.id }}</p>
            </div>
          </div>
          <n-tag :type="team.status === 'suspended' ? 'error' : 'success'" size="small" :bordered="true">
            {{ team.status === 'suspended' ? 'Banned' : 'Active' }}
          </n-tag>
        </div>
        <div class="mt-4 grid grid-cols-2 gap-2 text-xs">
          <div class="bg-bg-input p-2 rounded">
            <span class="block text-gray-500">成员</span>
            <span class="text-white font-medium">{{ team.memberCount }} 人</span>
          </div>
          <div class="bg-bg-input p-2 rounded">
            <span class="block text-gray-500">Owner</span>
            <span class="text-white font-medium">{{ team.owner }}</span>
          </div>
        </div>
        <div class="mt-4 pt-4 border-t border-white/5 flex justify-end gap-2">
          <n-button size="tiny" secondary>详情</n-button>
          <n-button size="tiny" :type="team.status === 'suspended' ? 'success' : 'warning'" ghost @click="toggleBan(team)">
            {{ team.status === 'suspended' ? '解封' : '封禁' }}
          </n-button>
          <n-button size="tiny" type="error" ghost @click="deleteTeam(team)">删除</n-button>
        </div>
      </div>
    </div>

    <div v-if="teamList.length === 0" class="text-center text-gray-500 py-12">暂无团队数据</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import { SearchOutline } from '@vicons/ionicons5'
import api from '@/api'

const message = useMessage()
const dialog = useDialog()
const filter = ref('all')
const searchKey = ref('')
const teamList = ref([])
const totalCount = ref(0)
const bannedCount = ref(0)

const colors = ['bg-indigo-500', 'bg-cyan-500', 'bg-emerald-500', 'bg-amber-500', 'bg-rose-500', 'bg-violet-500']
const colorForTeam = (id) => colors[id % colors.length]

const loadTeams = async () => {
  try {
    const params = { keyword: searchKey.value || undefined }
    if (filter.value === 'banned') params.status = 'suspended'
    const res = await api.get('/api/admin/teams', { params })
    teamList.value = res.data?.list || []
    totalCount.value = res.data?.total || teamList.value.length

    // 统计异常数
    const allRes = await api.get('/api/admin/teams', { params: { status: 'suspended' } })
    bannedCount.value = allRes.data?.total || 0
  } catch (e) {
    message.error('加载团队列表失败')
  }
}

const toggleBan = (team) => {
  const newStatus = team.status === 'suspended' ? 'active' : 'suspended'
  const action = newStatus === 'suspended' ? '封禁' : '解封'
  dialog.warning({
    title: `确认${action}`,
    content: `确定要${action}团队 ${team.name} 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await api.put(`/api/admin/teams/${team.id}/status`, { status: newStatus })
        message.success(`${action}成功`)
        loadTeams()
      } catch (e) {
        message.error(`${action}失败`)
      }
    }
  })
}

const deleteTeam = (team) => {
  dialog.error({
    title: '确认删除',
    content: `确定要删除团队 ${team.name} 吗？此操作不可恢复。`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await api.delete(`/api/admin/teams/${team.id}`)
        message.success('删除成功')
        loadTeams()
      } catch (e) {
        message.error('删除失败')
      }
    }
  })
}

onMounted(() => { loadTeams() })
</script>
