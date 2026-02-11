<template>
  <section id="page-db-connect" class="page-view active p-10">
    <div class="max-w-5xl mx-auto">
      <div class="text-center mb-12">
        <h2 class="text-4xl font-black text-white mb-4">连接你的数据库</h2>
        <p class="text-neutral-500 font-medium">CoffeeViz 通过 JDBC 驱动安全地拉取元数据，不触碰任何真实数据内容。</p>
      </div>

      <!-- 数据库类型选择 -->
      <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 mb-12">
        <div
          v-for="db in dbTypes" :key="db.key"
          @click="selectDb(db.key)"
          :class="[
            'db-card glass-card p-6 rounded-2xl cursor-pointer transition-all border-2',
            selectedDb === db.key ? 'border-amber-600 bg-amber-600/5' : 'border-transparent hover:border-neutral-800'
          ]"
        >
          <div class="flex items-center justify-between mb-4">
            <div :class="['w-12 h-12 rounded-xl flex items-center justify-center text-2xl', db.iconBg, db.iconColor]">
              <i :class="db.icon"></i>
            </div>
            <div class="text-amber-500 text-xl" v-show="selectedDb === db.key">
              <i class="fas fa-check-circle"></i>
            </div>
          </div>
          <h3 class="text-lg font-black text-white mb-1">{{ db.name }}</h3>
          <p class="text-xs text-neutral-500 mb-3 line-clamp-2">{{ db.desc }}</p>
          <div class="flex flex-wrap gap-1">
            <span
              v-for="tag in db.tags" :key="tag"
              class="text-[9px] font-bold px-1.5 py-0.5 rounded bg-neutral-900 border border-neutral-800 text-neutral-400"
            >{{ tag }}</span>
          </div>
        </div>
      </div>

      <!-- 连接表单 -->
      <div id="db-config-form" :class="['glass-card p-10 rounded-3xl transition-all duration-500', selectedDb ? 'opacity-100' : 'opacity-30 pointer-events-none']">
        <div class="flex items-center mb-6" v-if="selectedDb">
          <div :class="['w-8 h-8 rounded-lg flex items-center justify-center text-lg mr-3', currentDb.iconBg, currentDb.iconColor]">
            <i :class="currentDb.icon"></i>
          </div>
          <span class="text-white font-bold">{{ currentDb.name }}</span>
          <span class="text-neutral-500 text-xs ml-3">{{ currentDb.driverClass }}</span>
        </div>

        <div class="grid grid-cols-2 gap-x-10 gap-y-6">
          <div class="col-span-2 space-y-2">
            <label class="text-[10px] font-bold text-neutral-500 uppercase tracking-widest">JDBC 连接串</label>
            <input v-model="formData.jdbcUrl" type="text" class="w-full bg-black border border-neutral-800 rounded-xl px-5 py-3 text-sm text-white outline-none focus:border-amber-600 transition-all font-mono" :placeholder="currentDb?.urlTemplate || ''">
          </div>
          <div class="space-y-2">
            <label class="text-[10px] font-bold text-neutral-500 uppercase tracking-widest">用户名</label>
            <input v-model="formData.username" type="text" class="w-full bg-black border border-neutral-800 rounded-xl px-5 py-3 text-sm text-white outline-none focus:border-amber-600 transition-all" :placeholder="currentDb?.defaultUser || 'db_user'">
          </div>
          <div class="space-y-2">
            <label class="text-[10px] font-bold text-neutral-500 uppercase tracking-widest">密码</label>
            <input v-model="formData.password" type="password" class="w-full bg-black border border-neutral-800 rounded-xl px-5 py-3 text-sm text-white outline-none focus:border-amber-600 transition-all" placeholder="••••••••">
          </div>
          <!-- Schema 输入（部分数据库需要） -->
          <div v-if="currentDb?.showSchema" class="col-span-2 space-y-2">
            <label class="text-[10px] font-bold text-neutral-500 uppercase tracking-widest">Schema <span class="text-neutral-700 font-normal normal-case">(可选)</span></label>
            <input v-model="formData.schema" type="text" class="w-full bg-black border border-neutral-800 rounded-xl px-5 py-3 text-sm text-white outline-none focus:border-amber-600 transition-all" :placeholder="currentDb?.schemaPlaceholder || 'public'">
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
import { ref, reactive, computed } from 'vue'
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

// 支持的数据库类型配置
const dbTypes = [
  {
    key: 'mysql',
    name: 'MySQL',
    desc: '支持 v5.7, v8.0 及 MariaDB，自动推断 FK 与 1:N 关系',
    icon: 'fas fa-database',
    iconBg: 'bg-blue-600/10',
    iconColor: 'text-blue-500',
    tags: ['JDBC', 'SSL', 'MariaDB'],
    urlTemplate: 'jdbc:mysql://localhost:3306/your_db',
    defaultUser: 'root',
    driverClass: 'com.mysql.cj.jdbc.Driver',
    showSchema: false
  },
  {
    key: 'postgresql',
    name: 'PostgreSQL',
    desc: '多 Schema 结构优化，支持继承表与 JSONB 类型解析',
    icon: 'fas fa-cube',
    iconBg: 'bg-indigo-600/10',
    iconColor: 'text-indigo-500',
    tags: ['Schema', 'JSONB'],
    urlTemplate: 'jdbc:postgresql://localhost:5432/your_db',
    defaultUser: 'postgres',
    driverClass: 'org.postgresql.Driver',
    showSchema: true,
    schemaPlaceholder: 'public'
  },
  {
    key: 'oracle',
    name: 'Oracle',
    desc: '支持 Oracle 11g/12c/19c/21c，SID 与 Service Name 连接',
    icon: 'fas fa-sun',
    iconBg: 'bg-red-600/10',
    iconColor: 'text-red-500',
    tags: ['SID', 'Service', 'RAC'],
    urlTemplate: 'jdbc:oracle:thin:@localhost:1521:orcl',
    defaultUser: 'system',
    driverClass: 'oracle.jdbc.OracleDriver',
    showSchema: true,
    schemaPlaceholder: 'YOUR_SCHEMA'
  },
  {
    key: 'sqlserver',
    name: 'SQL Server',
    desc: '支持 SQL Server 2012+，Windows 认证与 SQL 认证',
    icon: 'fas fa-server',
    iconBg: 'bg-yellow-600/10',
    iconColor: 'text-yellow-500',
    tags: ['TDS', 'Azure SQL'],
    urlTemplate: 'jdbc:sqlserver://localhost:1433;databaseName=your_db;encrypt=true;trustServerCertificate=true',
    defaultUser: 'sa',
    driverClass: 'com.microsoft.sqlserver.jdbc.SQLServerDriver',
    showSchema: true,
    schemaPlaceholder: 'dbo'
  },
  {
    key: 'mariadb',
    name: 'MariaDB',
    desc: '原生 MariaDB 驱动，兼容 MySQL 协议，支持 Galera 集群',
    icon: 'fas fa-leaf',
    iconBg: 'bg-teal-600/10',
    iconColor: 'text-teal-500',
    tags: ['Galera', 'MySQL 兼容'],
    urlTemplate: 'jdbc:mariadb://localhost:3306/your_db',
    defaultUser: 'root',
    driverClass: 'org.mariadb.jdbc.Driver',
    showSchema: false
  },
  {
    key: 'sqlite',
    name: 'SQLite',
    desc: '轻量级嵌入式数据库，直接读取 .db 文件',
    icon: 'fas fa-feather',
    iconBg: 'bg-sky-600/10',
    iconColor: 'text-sky-500',
    tags: ['嵌入式', '零配置'],
    urlTemplate: 'jdbc:sqlite:/path/to/database.db',
    defaultUser: '',
    driverClass: 'org.sqlite.JDBC',
    showSchema: false
  },
  {
    key: 'dm',
    name: '达梦 DM',
    desc: '国产达梦数据库 DM7/DM8，信创环境首选',
    icon: 'fas fa-shield-alt',
    iconBg: 'bg-orange-600/10',
    iconColor: 'text-orange-500',
    tags: ['国产', '信创'],
    urlTemplate: 'jdbc:dm://localhost:5236/your_db',
    defaultUser: 'SYSDBA',
    driverClass: 'dm.jdbc.driver.DmDriver',
    showSchema: true,
    schemaPlaceholder: 'SYSDBA'
  },
  {
    key: 'kingbase',
    name: '人大金仓',
    desc: 'KingbaseES V8，兼容 PostgreSQL 协议',
    icon: 'fas fa-crown',
    iconBg: 'bg-purple-600/10',
    iconColor: 'text-purple-500',
    tags: ['国产', 'PG 兼容'],
    urlTemplate: 'jdbc:kingbase8://localhost:54321/your_db',
    defaultUser: 'system',
    driverClass: 'com.kingbase8.Driver',
    showSchema: true,
    schemaPlaceholder: 'public'
  }
]

const currentDb = computed(() => dbTypes.find(d => d.key === selectedDb.value))

const selectDb = (type) => {
  selectedDb.value = type
  const db = dbTypes.find(d => d.key === type)
  if (db) {
    formData.jdbcUrl = db.urlTemplate
    formData.username = db.defaultUser
    formData.schema = ''
  }
}

const handleTestConnection = async () => {
  if (!selectedDb.value) {
    message.warning('请先选择数据库类型')
    return
  }

  testing.value = true
  try {
    await projectStore.testJdbcConnection({
      dbType: selectedDb.value,
      jdbcUrl: formData.jdbcUrl,
      username: formData.username,
      password: formData.password,
      schemaName: formData.schema || null
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
    await projectStore.generateFromJdbc({
      dbType: selectedDb.value,
      jdbcUrl: formData.jdbcUrl,
      username: formData.username,
      password: formData.password,
      schemaName: formData.schema || null,
      viewMode: 'PHYSICAL'
    })
    message.success('数据库架构解析成功！正在跳转到编辑器...')
    setTimeout(() => {
      router.push('/sql-import')
    }, 500)
  } catch (error) {
    message.error('连接异常: ' + (error.message || '未知错误'))
  } finally {
    connecting.value = false
  }
}
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
