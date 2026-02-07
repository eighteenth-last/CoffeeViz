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

const formData = reactive({
  jdbcUrl: '',
  username: '',
  password: '',
  schema: ''
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
      schema: formData.schema
    })
    message.success('数据库连接成功，正在解析架构...')
    
    // 跳转到 SQL 导入页面查看结果 (或者如果有专门的图表页)
    // 假设 generateFromJdbc 更新了 store 中的 diagramData
    // 我们可以跳转到 SqlImport 页面（它似乎是展示图表的地方）或者 dashboard
    setTimeout(() => {
      router.push('/sql-import')
    }, 1000)
  } catch (error) {
    message.error('连接异常: ' + (error.message || '未知错误'))
  } finally {
    connecting.value = false
  }
}
</script>
