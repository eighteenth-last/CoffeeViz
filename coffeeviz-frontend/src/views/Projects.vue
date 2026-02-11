<template>
  <div class="projects p-10 fade-in">
    <!-- Header Section -->
    <div class="flex justify-between items-end mb-10">
      <div>
        <h1 class="text-4xl font-black text-white mb-2 tracking-tight">架构<span class="text-amber-500">归档库</span></h1>
        <p class="text-neutral-500">管理您的所有数据库架构库和架构图。</p>
      </div>
      <div class="flex space-x-3">
        <button @click="showCreateModal = true" class="btn-amber px-6 py-2.5 rounded-xl text-sm font-black text-white flex items-center shadow-lg hover:shadow-amber-600/20 transition-all">
          <i class="fas fa-plus mr-2"></i> 新建架构库
        </button>
      </div>
    </div>

    <!-- Search & Filter Bar -->
    <div class="glass-card p-4 rounded-2xl mb-8 flex flex-wrap items-center justify-between gap-4">
      <div class="flex items-center space-x-4 flex-1">
        <div class="relative group flex-1 max-w-md">
          <i class="fas fa-search absolute left-4 top-1/2 -translate-y-1/2 text-neutral-600 text-sm group-focus-within:text-amber-500 transition-colors"></i>
          <input 
            v-model="searchKeyword" 
            @input="handleSearch"
            type="text" 
            placeholder="搜索架构库名称、描述..." 
            class="w-full bg-black/40 border border-neutral-800 rounded-xl py-2.5 pl-10 pr-4 text-sm text-white outline-none focus:border-amber-600 focus:bg-black/60 transition-all placeholder-neutral-600"
          >
        </div>
        
        <div class="relative">
          <select 
            v-model="filterStatus" 
            @change="handleSearch"
            class="appearance-none bg-black/40 border border-neutral-800 rounded-xl py-2.5 pl-4 pr-10 text-sm text-neutral-300 outline-none focus:border-amber-600 cursor-pointer hover:bg-black/60 transition-all"
          >
            <option :value="null">所有状态</option>
            <option :value="1">正常</option>
            <option :value="0">已删除</option>
          </select>
          <i class="fas fa-chevron-down absolute right-3 top-1/2 -translate-y-1/2 text-neutral-600 text-xs pointer-events-none"></i>
        </div>
      </div>
      
      <div class="text-xs text-neutral-500 font-mono">
        共 <span class="text-amber-500 font-bold">{{ total }}</span> 个架构库
      </div>
    </div>

    <!-- Project Grid -->
    <div v-if="loading" class="flex justify-center items-center py-20">
      <div class="flex flex-col items-center">
        <i class="fas fa-circle-notch fa-spin text-4xl text-amber-600 mb-4"></i>
        <p class="text-neutral-500 text-sm">加载架构库中...</p>
      </div>
    </div>
    
    <div v-else-if="projectList.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 mb-8">
      <div 
        v-for="repository in projectList" 
        :key="repository.id" 
        class="glass-card group p-6 rounded-2xl relative overflow-hidden hover:-translate-y-1 transition-all duration-300 cursor-pointer border border-neutral-800 hover:border-amber-600/30"
        @click="handleProjectClick(repository)"
      >
        <!-- Card Decoration -->
        <div class="absolute top-0 right-0 p-4 opacity-0 group-hover:opacity-100 transition-opacity z-10">
          <button @click.stop="openMenu(repository)" class="w-8 h-8 rounded-lg bg-black/50 hover:bg-amber-600 text-neutral-400 hover:text-white flex items-center justify-center transition-colors backdrop-blur-sm">
            <i class="fas fa-ellipsis-v"></i>
          </button>
        </div>

        <div class="flex items-start justify-between mb-4">
          <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-neutral-800 to-black border border-neutral-800 flex items-center justify-center text-2xl group-hover:scale-110 transition-transform duration-500 shadow-lg">
            <i class="fas fa-folder text-amber-600"></i>
          </div>
          <div class="flex flex-col items-end">
             <span :class="['px-2 py-1 rounded text-[10px] font-bold uppercase tracking-wider border', repository.status === 'active' ? 'bg-green-500/10 text-green-500 border-green-500/20' : 'bg-neutral-500/10 text-neutral-500 border-neutral-500/20']">
              {{ repository.status === 'active' ? 'ACTIVE' : 'ARCHIVED' }}
            </span>
          </div>
        </div>

        <h3 class="text-lg font-bold text-white mb-2 line-clamp-1 group-hover:text-amber-500 transition-colors">{{ repository.repositoryName }}</h3>
        <p class="text-sm text-neutral-500 mb-6 line-clamp-2 h-10">{{ repository.description || '暂无描述信息...' }}</p>

        <div class="pt-4 border-t border-neutral-800 flex items-center justify-between text-xs text-neutral-500 font-mono">
          <div class="flex items-center">
            <i class="fas fa-file-alt mr-2 text-neutral-600"></i>
            <span>{{ repository.diagramCount || 0 }} 架构图</span>
          </div>
          <div>{{ formatDate(repository.updateTime) }}</div>
        </div>
        
        <!-- Hover Gradient -->
        <div class="absolute inset-0 bg-gradient-to-br from-amber-600/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none"></div>
      </div>
    </div>
    
    <div v-else class="flex flex-col items-center justify-center py-20 text-neutral-600">
      <div class="w-20 h-20 bg-neutral-900 rounded-full flex items-center justify-center mb-4">
        <i class="fas fa-box-open text-3xl opacity-50"></i>
      </div>
      <p>暂无架构库数据</p>
    </div>

    <!-- Pagination -->
    <div v-if="total > pageSize" class="flex justify-center space-x-2 mt-8">
      <button 
        @click="changePage(currentPage - 1)" 
        :disabled="currentPage <= 1"
        class="w-10 h-10 rounded-xl bg-neutral-900 border border-neutral-800 flex items-center justify-center text-neutral-400 hover:text-white hover:border-amber-600 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
      >
        <i class="fas fa-chevron-left"></i>
      </button>
      <div class="flex items-center px-4 text-sm font-mono text-neutral-500">
        Page <span class="text-white mx-2">{{ currentPage }}</span> of <span class="text-white mx-2">{{ Math.ceil(total / pageSize) }}</span>
      </div>
      <button 
        @click="changePage(currentPage + 1)" 
        :disabled="currentPage >= Math.ceil(total / pageSize)"
        class="w-10 h-10 rounded-xl bg-neutral-900 border border-neutral-800 flex items-center justify-center text-neutral-400 hover:text-white hover:border-amber-600 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
      >
        <i class="fas fa-chevron-right"></i>
      </button>
    </div>

    <!-- Create Project Modal -->
    <div v-if="showCreateModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-black/80 backdrop-blur-sm" @click="showCreateModal = false"></div>
      <div class="glass-card w-full max-w-lg rounded-3xl p-8 relative z-10 border border-neutral-800 shadow-2xl animate-fade-in-up">
        <div class="flex items-center justify-between mb-8">
          <h2 class="text-2xl font-black text-white">新建架构库</h2>
          <button @click="showCreateModal = false" class="w-8 h-8 rounded-full bg-neutral-900 flex items-center justify-center text-neutral-500 hover:text-white transition-colors">
            <i class="fas fa-times"></i>
          </button>
        </div>
        
        <div class="space-y-6">
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">架构库名称</label>
            <input 
              v-model="createFormData.repositoryName"
              type="text" 
              class="w-full bg-black/50 border border-neutral-800 rounded-xl px-4 py-3 text-white outline-none focus:border-amber-600 transition-all placeholder-neutral-700"
              placeholder="例如：电商系统架构库"
            >
          </div>
          
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">架构库描述 <span class="text-neutral-700 font-normal normal-case">(可选)</span></label>
            <textarea 
              v-model="createFormData.description"
              rows="4" 
              class="w-full bg-black/50 border border-neutral-800 rounded-xl px-4 py-3 text-white outline-none focus:border-amber-600 transition-all resize-none placeholder-neutral-700"
              placeholder="简要描述该架构库的用途..."
            ></textarea>
          </div>
          
          <div class="pt-4 flex space-x-4">
            <button @click="showCreateModal = false" class="flex-1 py-3 rounded-xl border border-neutral-800 text-neutral-400 hover:text-white hover:bg-neutral-800 transition-all font-bold">取消</button>
            <button 
              @click="handleCreate" 
              :disabled="!createFormData.repositoryName || creating"
              class="flex-1 py-3 bg-amber-600 hover:bg-amber-500 rounded-xl text-white font-bold shadow-lg shadow-amber-900/20 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
            >
              <i v-if="creating" class="fas fa-spinner fa-spin mr-2"></i>
              <span>立即创建</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Project Context Menu (Custom) -->
    <div 
      v-if="activeMenuProject" 
      class="fixed z-50 w-48 bg-[#0a0a0a] border border-neutral-800 rounded-xl shadow-2xl py-2"
      :style="{ top: menuPos.y + 'px', left: menuPos.x + 'px' }"
      v-click-outside="closeMenu"
    >
      <div class="px-3 py-2 text-[10px] font-bold text-neutral-600 uppercase tracking-widest border-b border-neutral-900 mb-1">操作菜单</div>
      <button @click="handleProjectMenu('view', activeMenuProject)" class="w-full text-left px-4 py-2 text-sm text-neutral-400 hover:text-white hover:bg-neutral-800 transition-colors flex items-center">
        <i class="fas fa-eye w-6"></i> 查看详情
      </button>
      <button @click="handleProjectMenu('edit', activeMenuProject)" class="w-full text-left px-4 py-2 text-sm text-neutral-400 hover:text-white hover:bg-neutral-800 transition-colors flex items-center">
        <i class="fas fa-edit w-6"></i> 编辑信息
      </button>
      <button @click="handleProjectMenu('export', activeMenuProject)" class="w-full text-left px-4 py-2 text-sm text-neutral-400 hover:text-white hover:bg-neutral-800 transition-colors flex items-center">
        <i class="fas fa-file-export w-6"></i> 导出项目
      </button>
      <div class="h-px bg-neutral-900 my-1"></div>
      <button @click="handleProjectMenu('delete', activeMenuProject)" class="w-full text-left px-4 py-2 text-sm text-red-500 hover:bg-red-500/10 transition-colors flex items-center">
        <i class="fas fa-trash w-6"></i> 删除架构库
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useProjectStore } from '@/store/project'
import { useMessage, useDialog } from 'naive-ui'
import { debounce } from '@/utils/debounce'

const router = useRouter()
const projectStore = useProjectStore()
const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const creating = ref(false)
const searchKeyword = ref('')
const filterStatus = ref(null)
const showCreateModal = ref(false)
const projectList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

const createFormData = reactive({
  repositoryName: '',
  description: ''
})

// Menu state
const activeMenuProject = ref(null)
const menuPos = reactive({ x: 0, y: 0 })

// Click outside directive for menu
const vClickOutside = {
  mounted(el, binding) {
    el.clickOutsideEvent = function(event) {
      if (!(el === event.target || el.contains(event.target))) {
        binding.value(event, el);
      }
    };
    document.body.addEventListener('click', el.clickOutsideEvent);
  },
  unmounted(el) {
    document.body.removeEventListener('click', el.clickOutsideEvent);
  }
}

const openMenu = (project) => {
  const event = window.event;
  if (event) {
    menuPos.x = event.clientX - 180; // Align left a bit
    menuPos.y = event.clientY + 10;
  }
  activeMenuProject.value = project;
}

const closeMenu = () => {
  activeMenuProject.value = null;
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  
  return date.toLocaleDateString('zh-CN')
}

const fetchProjects = async () => {
  loading.value = true
  try {
    const response = await fetch(`/api/repository/list?page=${currentPage.value}&size=${pageSize.value}&keyword=${searchKeyword.value || ''}&status=${filterStatus.value || ''}`, {
      headers: {
        'Authorization': sessionStorage.getItem('token') || ''
      }
    })
    const result = await response.json()
    if (result.code === 200) {
      projectList.value = result.data.list || []
      total.value = result.data.total || 0
    } else {
      throw new Error(result.message || '加载失败')
    }
  } catch (error) {
    message.error('加载架构库失败：' + (error.message || error))
  } finally {
    loading.value = false
  }
}

const handleSearch = debounce(() => {
  currentPage.value = 1
  fetchProjects()
}, 500)

const changePage = (page) => {
  currentPage.value = page
  fetchProjects()
}

const handleProjectClick = (repository) => {
  // 跳转到架构库详情页（显示架构图列表）
  router.push(`/repository/${repository.id}`)
}

const handleCreate = async () => {
  if (!createFormData.repositoryName) return
  
  try {
    creating.value = true
    const response = await fetch('/api/repository/create', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': sessionStorage.getItem('token') || ''
      },
      body: JSON.stringify(createFormData)
    })
    const result = await response.json()
    if (result.code !== 200) {
      throw new Error(result.message || '创建失败')
    }
    
    message.success('架构库创建成功')
    
    showCreateModal.value = false
    createFormData.repositoryName = ''
    createFormData.description = ''
    
    // 刷新列表
    await fetchProjects()
  } catch (error) {
    message.error(error.message || '创建失败')
  } finally {
    creating.value = false
  }
}

const handleProjectMenu = async (key, repository) => {
  closeMenu()
  
  if (key === 'view') {
    // 查看架构库详情（架构图列表）
    router.push(`/repository/${repository.id}`)
  } else if (key === 'edit') {
    message.info(`编辑架构库：${repository.repositoryName}`)
  } else if (key === 'export') {
    message.info('导出功能开发中')
  } else if (key === 'delete') {
    dialog.warning({
      title: '确认删除',
      content: `确定要删除架构库 "${repository.repositoryName}" 吗？此操作将删除库中所有架构图，不可恢复。`,
      positiveText: '确定',
      negativeText: '取消',
      onPositiveClick: async () => {
        try {
          const response = await fetch(`/api/repository/delete/${repository.id}`, {
            method: 'DELETE',
            headers: {
              'Authorization': sessionStorage.getItem('token') || ''
            }
          })
          const result = await response.json()
          if (result.code !== 200) {
            throw new Error(result.message || '删除失败')
          }
          message.success('删除成功')
          await fetchProjects()
        } catch (error) {
          message.error('删除失败：' + (error.message || error))
        }
      }
    })
  }
}

onMounted(() => {
  fetchProjects()
})
</script>

<style scoped>
/* Custom Scrollbar for the page if needed */
.projects {
  min-height: calc(100vh - 80px); /* Adjust based on header height */
}

/* Animation for Modal */
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.animate-fade-in-up {
  animation: fadeInUp 0.3s ease-out forwards;
}

.fade-in {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>
