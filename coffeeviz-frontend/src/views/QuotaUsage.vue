<template>
  <div class="p-8 ml-0">
    <div class="max-w-6xl mx-auto">
      <!-- 标题 -->
      <div class="mb-8">
        <div class="text-xs text-neutral-500 mb-2">资源管理 &gt; 额度使用记录</div>
        <h1 class="text-3xl font-black text-white">额度使用记录</h1>
        <p class="text-neutral-500 text-sm mt-1">查看您的配额消耗明细</p>
      </div>

      <!-- 筛选 -->
      <div class="flex items-center justify-between mb-6">
        <div class="flex items-center gap-4">
          <n-select v-model:value="filterType" :options="typeOptions" placeholder="筛选类型"
                    clearable size="medium" class="w-48" @update:value="loadLogs" />
        </div>
        <div class="flex items-center gap-2 text-xs text-neutral-500">
          <span>共 {{ total }} 条记录</span>
        </div>
      </div>

      <!-- 表格 -->
      <div class="bg-[#0a0a0a] border border-neutral-800 rounded-xl overflow-hidden shadow-sm">
        <n-data-table 
          :columns="columns" 
          :data="logs" 
          :loading="loading"
          :row-class-name="() => 'table-row hover:bg-white/5 transition-colors'" 
          size="large"
          :bordered="false"
        />
        
        <!-- 分页 -->
        <div class="px-6 py-4 border-t border-neutral-800 flex justify-end items-center bg-[#0d0d0d]" v-if="total > 0">
          <n-pagination 
            v-model:page="currentPage" 
            :page-count="Math.ceil(total / pageSize)"
            :page-size="pageSize" 
            @update:page="loadLogs" 
          />
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && logs.length === 0" class="text-center py-16 text-neutral-600">
        <i class="fas fa-inbox text-4xl mb-4 block"></i>
        <p>暂无使用记录</p>
      </div>
    </div>
  </div>
</template>



<script>
export default { name: 'QuotaUsage' }
</script>

<script setup>
import { ref, onMounted, h } from 'vue'
import { NSelect, NDataTable, NPagination, NTag } from 'naive-ui'
import api from '@/api'

const logs = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = 20
const total = ref(0)
const filterType = ref(null)

const typeOptions = [
  { label: 'SQL 解析', value: 'sql_parse' },
  { label: 'AI 生成', value: 'ai_generate' },
  { label: '创建架构库', value: 'repository' },
  { label: '创建架构图', value: 'diagram' }
]

const columns = [
  {
    title: '时间',
    key: 'createTime',
    render: (row) => {
      const t = row.createTime
      if (!t) return '-'
      return h('span', { class: 'text-neutral-400 font-mono text-sm' }, t.replace('T', ' ').substring(0, 19))
    }
  },
  {
    title: '类型',
    key: 'quotaType',
    width: 150,
    render: (row) => {
      const map = { 
        sql_parse: { label: 'SQL 解析', type: 'info', icon: 'fa-code' }, 
        ai_generate: { label: 'AI 生成', type: 'success', icon: 'fa-robot' }, 
        repository: { label: '架构库', type: 'warning', icon: 'fa-folder' }, 
        diagram: { label: '架构图', type: 'default', icon: 'fa-project-diagram' } 
      }
      const conf = map[row.quotaType] || { label: row.quotaType, type: 'default', icon: 'fa-circle' }
      
      return h(NTag, { size: 'small', type: conf.type, bordered: false, class: 'px-3 py-1' }, {
        default: () => h('div', { class: 'flex items-center gap-1.5' }, [
          h('i', { class: `fas ${conf.icon} text-xs opacity-70` }),
          h('span', null, conf.label)
        ])
      })
    }
  },
  {
    title: '消耗额度',
    key: 'amount',
    width: 120,
    render: (row) => h('div', { class: 'flex items-center gap-1' }, [
      h('span', { class: 'text-amber-500 font-bold font-mono text-base' }, `-${row.amount}`),
      h('span', { class: 'text-neutral-600 text-xs' }, 'pts')
    ])
  },
  {
    title: '操作详情',
    key: 'action',
    render: (row) => h('span', { class: 'text-neutral-400 text-sm' }, row.action || '-')
  }
]

async function loadLogs() {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize }
    if (filterType.value) params.quotaType = filterType.value
    const res = await api.get('/api/quota/usage-logs', { params })
    logs.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('加载使用记录失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
:deep(.table-row td) {
  background: #0a0a0a !important;
  border-bottom: 1px solid #1a1a1a !important;
}
:deep(.n-data-table-th) {
  background: #080808 !important;
  border-bottom: 1px solid #1a1a1a !important;
}
</style>
