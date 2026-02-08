<template>
  <section id="page-db-connect" class="page-view active">
    <div class="max-w-4xl mx-auto">
      <div class="text-center mb-12">
        <h2 class="text-4xl font-black text-white mb-4">连接你的数据库</h2>
        <p class="text-neutral-500 font-medium">CoffeeViz 通过 JDBC 驱动安全地拉取元数据，不触碰任何真实数据内容。</p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-8 mb-12">
        <div @click="selectDb('mysql')" :class="['db-card glass-card p-10 rounded-3xl cursor-pointer transition-all border-2', selectedDb === 'mysql' ? 'border-amber-600 bg-amber-600/5' : 'border-transparent hover:border-neutral-800']">
          <div class="flex items-center justify-between mb-8">
            <div class="w-16 h-16 rounded-2xl bg-blue-600/10 flex items-center justify-center text-blue-500 text-4xl">
              <i class="fas fa-database"></i>
            </div>
            <div class="text-amber-500 text-2xl" v-show="selectedDb === 'mysql'"><i class="fas fa-check-circle"></i></div>
          </div>
          <h3 class="text-2xl font-black text-white mb-2">MySQL</h3>
          <p class="text-sm text-neutral-500 mb-6">完美支持 v5.7, v8.0 以及 MariaDB。自动推断 Foreign Key 与 1:N 关系。</p>
          <div class="flex flex-wrap gap-2">
            <span class="text-[10px] font-bold px-2 py-1 rounded bg-neutral-900 border border-neutral-800 text-neutral-400">JDBC API</span>
            <span class="text-[10px] font-bold px-2 py-1 rounded bg-neutral-900 border border-neutral-800 text-neutral-400">SSL</span>
          </div>
        </div>

        <div @click="selectDb('pg')" :class="['db-card glass-card p-10 rounded-3xl cursor-pointer transition-all border-2', selectedDb === 'pg' ? 'border-amber-600 bg-amber-600/5' : 'border-transparent hover:border-neutral-800']">
          <div class="flex items-center justify-between mb-8">
            <div class="w-16 h-16 rounded-2xl bg-indigo-600/10 flex items-center justify-center text-indigo-500 text-4xl">
              <i class="fas fa-elephant"></i>
            </div>
            <div class="text-amber-500 text-2xl" v-show="selectedDb === 'pg'"><i class="fas fa-check-circle"></i></div>
          </div>
          <h3 class="text-2xl font-black text-white mb-2">PostgreSQL</h3>
          <p class="text-sm text-neutral-500 mb-6">针对 PG 的多 Schema 结构进行优化。支持多级继承表关系解析。</p>
          <div class="flex flex-wrap gap-2">
            <span class="text-[10px] font-bold px-2 py-1 rounded bg-neutral-900 border border-neutral-800 text-neutral-400">Schema Aware</span>
            <span class="text-[10px] font-bold px-2 py-1 rounded bg-neutral-900 border border-neutral-800 text-neutral-400">JSONB</span>
          </div>
        </div>
      </div>

      <div id="db-config-form" :class="['glass-card p-10 rounded-3xl transition-all duration-500', selectedDb ? 'opacity-100' : 'opacity-30 pointer-events-none']">
        <div class="grid grid-cols-2 gap-x-10 gap-y-6">
          <div class="col-span-2 space-y-2">
            <label class="text-[10px] font-bold text-neutral-500 uppercase tracking-widest">JDBC 连接串</label>
            <input v-model="formData.jdbcUrl" type="text" class="w-full bg-black border border-neutral-800 rounded-xl px-5 py-3 text-sm text-white outline-none focus:border-amber-600 transition-all font-mono" :placeholder="selectedDb === 'mysql' ? 'jdbc:mysql://localhost:3306/coffee_db' : 'jdbc:postgresql://localhost:5432/coffee_db'">
          </div>
          <div class="space-y-2">
            <label class="text-[10px] font-bold text-neutral-500 uppercase tracking-widest">用户名</label>
            <input v-model="formData.username" type="text" class="w-full bg-black border border-neutral-800 rounded-xl px-5 py-3 text-sm text-white outline-none focus:border-amber-600 transition-all" placeholder="db_user">
          </div>
          <div class="space-y-2">
            <label class="text-[10px] font-bold text-neutral-500 uppercase tracking-widest">密码</label>
            <input v-model="formData.password" type="password" class="w-full bg-black border border-neutral-800 rounded-xl px-5 py-3 text-sm text-white outline-none focus:border-amber-600 transition-all" placeholder="••••••••">
          </div>
        </div>
        <div class="mt-10 flex justify-end space-x-4">
          <button @click="handleTestConnection" :disabled="testing" class="px-8 py-3 bg-neutral-900 border border-neutral-800 rounded-xl text-sm font-bold text-neutral-300 hover:bg-neutral-800 hover:text-white transition-all disabled:opacity-50">
            {{ testing ? '测试中...' : '测试连接' }}
          </button>
          <button @click="handleConnect" :disabled="connecting" class="btn-amber px-10 py-3 rounded-xl text-sm font-black text-white disabled:opacity-50">
            {{ connecting ? '连接中...' : '建立同步通道' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Save Project Modal -->
    <div v-if="showSaveModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-black/80 backdrop-blur-sm"></div>
      <div class="glass-card w-full max-w-lg rounded-3xl p-8 relative z-10 border border-neutral-800 shadow-2xl animate-fade-in-up">
        <div class="flex items-center justify-between mb-8">
          <h2 class="text-2xl font-black text-white">保存到归档库</h2>
          <button @click="showSaveModal = false" class="w-8 h-8 rounded-full bg-neutral-900 flex items-center justify-center text-neutral-500 hover:text-white transition-colors">
            <i class="fas fa-times"></i>
          </button>
        </div>
        
        <div class="space-y-6">
          <div class="bg-green-500/10 border border-green-500/20 rounded-xl p-4 flex items-start">
            <i class="fas fa-check-circle text-green-500 text-xl mr-3 mt-0.5"></i>
            <div>
              <div class="text-green-500 font-bold mb-1">数据库架构解析成功</div>
              <div class="text-sm text-neutral-400">共解析 {{ projectStore.diagramData.tableCount || 0 }} 张表，{{ projectStore.diagramData.relationCount || 0 }} 个关系</div>
            </div>
          </div>

          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">项目名称</label>
            <input 
              v-model="saveFormData.projectName"
              type="text" 
              class="w-full bg-black/50 border border-neutral-800 rounded-xl px-4 py-3 text-white outline-none focus:border-amber-600 transition-all placeholder-neutral-700"
              placeholder="例如：生产环境数据库架构"
            >
          </div>
          
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">项目描述 <span class="text-neutral-700 font-normal normal-case">(可选)</span></label>
            <textarea 
              v-model="saveFormData.description"
              rows="3" 
              class="w-full bg-black/50 border border-neutral-800 rounded-xl px-4 py-3 text-white outline-none focus:border-amber-600 transition-all resize-none placeholder-neutral-700"
              placeholder="记录数据库连接信息、用途等..."
            ></textarea>
          </div>
          
          <div class="pt-4 flex space-x-4">
            <button @click="handleSkipSave" class="flex-1 py-3 rounded-xl border border-neutral-800 text-neutral-400 hover:text-white hover:bg-neutral-800 transition-all font-bold">稍后保存</button>
            <button 
              @click="handleConfirmSave" 
              :disabled="!saveFormData.projectName || saving"
              class="flex-1 py-3 bg-green-600 hover:bg-green-500 rounded-xl text-white font-bold shadow-lg shadow-green-900/20 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
            >
              <i v-if="saving" class="fas fa-spinner fa-spin mr-2"></i>
              <span>{{ saving ? '保存中...' : '保存项目' }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useProjectStore } from '@/store/project'
import { useMessage } from 'naive-ui'
import { useRouter } from 'vue-router'

const projectStore = useProjectStore()
const message = useMessage()
const router = useRouter()

const selectedDb = ref('')
const testing = ref(false)
const connecting = ref(false)
const showSaveModal = ref(false)
const saving = ref(false)

const formData = reactive({
  jdbcUrl: '',
  username: '',
  password: '',
  schema: ''
})

const saveFormData = reactive({
  projectName: '',
  description: ''
})

const selectDb = (type) => {
  selectedDb.value = type
  if (type === 'mysql') {
    formData.jdbcUrl = 'jdbc:mysql://localhost:3306/coffee_db'
  } else {
    formData.jdbcUrl = 'jdbc:postgresql://localhost:5432/coffee_db'
  }
}

const handleTestConnection = async () => {
  if (!selectedDb.value) {
    message.warning('请先选择数据库类型')
    return
  }
  
  testing.value = true
  try {
    // 调用 Store action 进行测试
    await projectStore.testJdbcConnection({
      dbType: selectedDb.value,
      jdbcUrl: formData.jdbcUrl,
      username: formData.username,
      password: formData.password,
      schema: formData.schema
    })
    message.success('连接测试成功')
  } catch (error) {
    message.error('连接失败: ' + (error.message || '未知错误'))
  } finally {
    testing.value = false
  }
}

const handleConnect = async () => {
  if (!selectedDb.value) {
    message.warning('请先选择数据库类型')
    return
  }
  
  connecting.value = true
  try {
    // 调用 Store action 建立连接并生成架构
    await projectStore.generateFromJdbc({
      dbType: selectedDb.value,
      jdbcUrl: formData.jdbcUrl,
      username: formData.username,
      password: formData.password,
      schema: formData.schema,
      viewMode: 'PHYSICAL'
    })
    message.success('数据库架构解析成功！')
    
    // 显示保存弹窗
    showSaveModal.value = true
  } catch (error) {
    message.error('连接异常: ' + (error.message || '未知错误'))
  } finally {
    connecting.value = false
  }
}

// 保存项目到归档库
const handleConfirmSave = async () => {
  if (!saveFormData.projectName) {
    message.warning('请输入项目名称')
    return
  }
  
  try {
    saving.value = true
    
    // 调用 Store 保存项目
    await projectStore.createProject({
      projectName: saveFormData.projectName,
      description: saveFormData.description,
      mermaidCode: projectStore.diagramData.mermaidCode,
      pngBase64: projectStore.diagramData.pngBase64,
      tableCount: projectStore.diagramData.tableCount,
      sourceType: 'JDBC',
      dbType: selectedDb.value
    })
    
    message.success('项目已保存到归档库')
    
    // 关闭弹窗并清空表单
    showSaveModal.value = false
    saveFormData.projectName = ''
    saveFormData.description = ''
    
    // 跳转到项目列表
    setTimeout(() => {
      router.push('/projects')
    }, 1000)
    
  } catch (error) {
    message.error('保存失败: ' + (error.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

// 跳过保存，直接查看
const handleSkipSave = () => {
  showSaveModal.value = false
  router.push('/sql-import')
}
</script>

<style scoped>
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.animate-fade-in-up {
  animation: fadeInUp 0.3s ease-out forwards;
}
</style>
