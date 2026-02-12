<template>
  <div class="space-y-6">
    <!-- Tab 切换 -->
    <n-tabs v-model:value="activeTab" type="segment" animated>
      <n-tab-pane name="config" tab="邮件配置">
        <div class="mt-6 bg-bg-card rounded-xl border border-white/5 overflow-hidden">
          <div class="border-b border-white/5 px-6 py-4 flex justify-between items-center">
            <div>
              <h3 class="text-lg font-medium text-white">阿里云邮件推送</h3>
              <p class="text-gray-500 text-xs mt-1">配置 DirectMail API 密钥和发件人信息</p>
            </div>
            <n-switch v-model:value="config.enabled" @update:value="saveConfig" />
          </div>
          <div class="p-6 space-y-6">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <n-form-item label="服务商" label-placement="top">
                <n-select v-model:value="config.provider" :options="[{label:'阿里云 DirectMail',value:'aliyun'}]" />
              </n-form-item>
              <n-form-item label="发件人名称" label-placement="top">
                <n-input v-model:value="config.senderName" placeholder="CoffeeViz" />
              </n-form-item>
            </div>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <n-form-item label="AccessKey ID" label-placement="top">
                <n-input v-model:value="config.apiKey" placeholder="LTAI5t..." />
              </n-form-item>
              <n-form-item label="AccessKey Secret" label-placement="top">
                <n-input v-model:value="config.secretKey" type="password" show-password-on="click" placeholder="Secret Key" />
              </n-form-item>
            </div>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <n-form-item label="发件人邮箱" label-placement="top">
                <n-input v-model:value="config.senderEmail" placeholder="noreply@mail.yourdomain.com" />
                <template #feedback><span class="text-xs text-gray-500">需在阿里云 DirectMail 控制台验证的发信地址</span></template>
              </n-form-item>
              <n-form-item label="测试收件邮箱" label-placement="top">
                <n-input v-model:value="config.testEmail" placeholder="test@example.com" />
              </n-form-item>
            </div>
            <div class="flex items-center gap-4">
              <n-checkbox v-model:checked="config.testMode">测试模式（所有邮件发送到测试邮箱）</n-checkbox>
            </div>
          </div>
          <div class="bg-bg-input/30 px-6 py-4 border-t border-white/5 flex justify-between">
            <n-button :loading="testing" @click="sendTest" :disabled="!config.enabled">
              <template #icon><n-icon><PaperPlaneOutline /></n-icon></template>
              发送测试邮件
            </n-button>
            <n-button type="primary" :loading="saving" @click="saveConfig">保存配置</n-button>
          </div>
        </div>
      </n-tab-pane>

      <n-tab-pane name="templates" tab="邮件模板">
        <div class="mt-6 space-y-4">
          <div class="flex justify-between items-center">
            <p class="text-gray-400 text-sm">管理邮件模板，支持 HTML 和变量替换（{{username}} {{planName}} {{endTime}} {{title}} {{content}}）</p>
            <n-button type="primary" size="small" @click="openTemplateEditor(null)">
              <template #icon><n-icon><AddOutline /></n-icon></template>
              新建模板
            </n-button>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div v-for="tpl in templates" :key="tpl.id"
              class="bg-bg-card rounded-xl border border-white/5 p-5 hover:border-primary-500/30 transition-all group"
            >
              <div class="flex justify-between items-start mb-3">
                <div>
                  <h4 class="text-white font-medium">{{ tpl.templateName }}</h4>
                  <p class="text-gray-500 text-xs mt-1">{{ tpl.templateCode }}</p>
                </div>
                <n-tag :type="tpl.status === 1 ? 'success' : 'default'" size="small">{{ tpl.status === 1 ? '启用' : '禁用' }}</n-tag>
              </div>
              <p class="text-gray-400 text-xs mb-3 line-clamp-2">{{ tpl.description || tpl.subject }}</p>
              <div class="flex gap-2">
                <n-button size="tiny" secondary @click="openTemplateEditor(tpl)">编辑</n-button>
                <n-button size="tiny" secondary @click="previewTemplate(tpl)">预览</n-button>
                <n-button size="tiny" secondary type="error" @click="deleteTemplate(tpl.id)">删除</n-button>
              </div>
            </div>
          </div>
        </div>
      </n-tab-pane>

      <n-tab-pane name="send" tab="发送邮件">
        <div class="mt-6 grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div class="bg-bg-card rounded-xl border border-white/5 p-6">
            <h3 class="text-white font-semibold mb-6">发送邮件</h3>
            <n-form label-placement="top">
              <n-form-item label="发送目标">
                <n-select v-model:value="sendForm.target" :options="targetOptions" />
              </n-form-item>
              <n-form-item v-if="sendForm.target === 'specific'" label="收件人邮箱（逗号分隔）">
                <n-input v-model:value="sendForm.emails" type="textarea" :rows="2" placeholder="user1@example.com, user2@example.com" />
              </n-form-item>
              <n-form-item label="使用模板">
                <n-select v-model:value="sendForm.templateCode" :options="templateOptions" clearable placeholder="选择模板（可选）" @update:value="onTemplateSelect" />
              </n-form-item>
              <n-form-item label="邮件主题">
                <n-input v-model:value="sendForm.subject" placeholder="邮件主题" />
              </n-form-item>
              <n-form-item label="邮件内容（HTML）">
                <n-input v-model:value="sendForm.content" type="textarea" :rows="8" placeholder="<h2>标题</h2><p>内容</p>" />
              </n-form-item>
              <n-button type="primary" block :loading="sendingEmail" @click="sendEmail">
                <template #icon><n-icon><PaperPlaneOutline /></n-icon></template>
                发送邮件
              </n-button>
            </n-form>
          </div>
          <div class="bg-bg-card rounded-xl border border-white/5 p-6">
            <h3 class="text-white font-semibold mb-4">实时预览</h3>
            <div class="bg-white rounded-lg p-4 min-h-[300px]" v-html="sendForm.content || '<p style=&quot;color:#999&quot;>在左侧输入 HTML 内容后这里会实时预览</p>'"></div>
          </div>
        </div>
      </n-tab-pane>

      <n-tab-pane name="logs" tab="发送记录">
        <div class="mt-6 bg-bg-card rounded-xl border border-white/5 p-6">
          <div class="flex justify-between items-center mb-4">
            <h3 class="text-white font-semibold">发送日志</h3>
            <n-button size="small" secondary @click="loadLogs">刷新</n-button>
          </div>
          <n-data-table :columns="logColumns" :data="logs" :pagination="pagination" remote size="small" />
        </div>
      </n-tab-pane>
    </n-tabs>

    <!-- 模板编辑弹窗 -->
    <n-modal v-model:show="showTemplateModal" preset="card" :title="editingTemplate.id ? '编辑模板' : '新建模板'" class="w-[700px]" :bordered="false">
      <div class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <n-form-item label="模板编码" label-placement="top">
            <n-input v-model:value="editingTemplate.templateCode" placeholder="welcome" :disabled="!!editingTemplate.id" />
          </n-form-item>
          <n-form-item label="模板名称" label-placement="top">
            <n-input v-model:value="editingTemplate.templateName" placeholder="欢迎注册" />
          </n-form-item>
        </div>
        <n-form-item label="邮件主题" label-placement="top">
          <n-input v-model:value="editingTemplate.subject" placeholder="邮件主题，支持 {{planName}} 等变量" />
        </n-form-item>
        <n-form-item label="邮件内容（HTML）" label-placement="top">
          <n-input v-model:value="editingTemplate.content" type="textarea" :rows="10" placeholder="<h2>标题</h2><p>Hi {{username}}，</p>" />
        </n-form-item>
        <n-form-item label="模板说明" label-placement="top">
          <n-input v-model:value="editingTemplate.description" placeholder="用途说明" />
        </n-form-item>
        <div class="flex items-center gap-2">
          <span class="text-gray-400 text-sm">状态：</span>
          <n-switch v-model:value="editingTemplate.statusBool" />
          <span class="text-gray-500 text-xs">{{ editingTemplate.statusBool ? '启用' : '禁用' }}</span>
        </div>
      </div>
      <template #footer>
        <div class="flex justify-end gap-2">
          <n-button @click="showTemplateModal = false">取消</n-button>
          <n-button type="primary" :loading="savingTemplate" @click="saveTemplate">保存</n-button>
        </div>
      </template>
    </n-modal>

    <!-- 模板预览弹窗 -->
    <n-modal v-model:show="showPreviewModal" preset="card" title="模板预览" class="w-[600px]" :bordered="false">
      <div class="bg-white rounded-lg p-4" v-html="previewHtml"></div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, h } from 'vue'
import { useMessage, NTag } from 'naive-ui'
import { PaperPlaneOutline, AddOutline } from '@vicons/ionicons5'
import api from '@/api'

const message = useMessage()
const activeTab = ref('config')
const saving = ref(false)
const testing = ref(false)
const sendingEmail = ref(false)
const savingTemplate = ref(false)
const showTemplateModal = ref(false)
const showPreviewModal = ref(false)
const previewHtml = ref('')

// ========== 邮件配置 ==========
const config = reactive({
  enabled: false,
  provider: 'aliyun',
  apiKey: '',
  secretKey: '',
  senderEmail: '',
  senderName: 'CoffeeViz',
  testEmail: '',
  testMode: false
})

const loadConfig = async () => {
  try {
    const res = await api.get('/api/admin/settings/email')
    if (res.data) Object.assign(config, res.data)
  } catch { /* 使用默认值 */ }
}

const saveConfig = async () => {
  saving.value = true
  try {
    await api.post('/api/admin/settings/email', config)
    message.success('邮件配置已保存')
  } catch (e) {
    message.error('保存失败: ' + (e.message || e))
  } finally {
    saving.value = false
  }
}

const sendTest = async () => {
  const email = config.testEmail || config.senderEmail
  if (!email) { message.warning('请先填写测试收件邮箱'); return }
  testing.value = true
  try {
    const res = await api.post('/api/admin/email/test', { email })
    message.success(res.data || '测试邮件已发送')
  } catch (e) {
    message.error(e.message || e)
  } finally {
    testing.value = false
  }
}

// ========== 邮件模板 ==========
const templates = ref([])

const loadTemplates = async () => {
  try {
    const res = await api.get('/api/admin/email/templates')
    templates.value = res.data || []
  } catch { /* ignore */ }
}

const editingTemplate = reactive({
  id: null, templateCode: '', templateName: '', subject: '', content: '', description: '', statusBool: true
})

const openTemplateEditor = (tpl) => {
  if (tpl) {
    Object.assign(editingTemplate, {
      id: tpl.id, templateCode: tpl.templateCode, templateName: tpl.templateName,
      subject: tpl.subject, content: tpl.content, description: tpl.description,
      statusBool: tpl.status === 1
    })
  } else {
    Object.assign(editingTemplate, {
      id: null, templateCode: '', templateName: '', subject: '', content: '', description: '', statusBool: true
    })
  }
  showTemplateModal.value = true
}

const saveTemplate = async () => {
  if (!editingTemplate.templateCode || !editingTemplate.templateName) {
    message.warning('请填写模板编码和名称'); return
  }
  savingTemplate.value = true
  try {
    await api.post('/api/admin/email/templates', {
      id: editingTemplate.id,
      templateCode: editingTemplate.templateCode,
      templateName: editingTemplate.templateName,
      subject: editingTemplate.subject,
      content: editingTemplate.content,
      description: editingTemplate.description,
      status: editingTemplate.statusBool ? 1 : 0
    })
    message.success('模板已保存')
    showTemplateModal.value = false
    loadTemplates()
  } catch (e) {
    message.error('保存失败: ' + (e.message || e))
  } finally {
    savingTemplate.value = false
  }
}

const deleteTemplate = async (id) => {
  try {
    await api.delete(`/api/admin/email/templates/${id}`)
    message.success('已删除')
    loadTemplates()
  } catch (e) {
    message.error('删除失败')
  }
}

const previewTemplate = (tpl) => {
  previewHtml.value = (tpl.content || '')
    .replace(/\{\{username\}\}/g, '张三')
    .replace(/\{\{planName\}\}/g, 'Pro')
    .replace(/\{\{endTime\}\}/g, '2026-03-12')
    .replace(/\{\{title\}\}/g, '通知标题')
    .replace(/\{\{content\}\}/g, '这是通知内容')
    .replace(/\{\{email\}\}/g, 'test@example.com')
  showPreviewModal.value = true
}

// ========== 发送邮件 ==========
const targetOptions = [
  { label: '所有用户', value: 'all' },
  { label: 'Pro 用户', value: 'pro' },
  { label: '指定邮箱', value: 'specific' }
]

const sendForm = reactive({
  target: 'all', emails: '', templateCode: null, subject: '', content: ''
})

const templateOptions = computed(() =>
  templates.value.filter(t => t.status === 1).map(t => ({ label: t.templateName, value: t.templateCode }))
)

const onTemplateSelect = (code) => {
  if (!code) return
  const tpl = templates.value.find(t => t.templateCode === code)
  if (tpl) {
    sendForm.subject = tpl.subject
    sendForm.content = tpl.content
  }
}

const sendEmail = async () => {
  if (!sendForm.subject && !sendForm.templateCode) { message.warning('请填写邮件主题或选择模板'); return }
  if (sendForm.target === 'specific' && !sendForm.emails) { message.warning('请填写收件人邮箱'); return }
  sendingEmail.value = true
  try {
    await api.post('/api/admin/email/send', sendForm)
    message.success('邮件发送任务已提交')
  } catch (e) {
    message.error('发送失败: ' + (e.message || e))
  } finally {
    sendingEmail.value = false
  }
}

// ========== 发送日志 ==========
const logs = ref([])
const pagination = reactive({
  page: 1,
  pageSize: 15,
  itemCount: 0,
  prefix: ({ itemCount }) => `共 ${itemCount} 条`,
  onChange: (page) => {
    pagination.page = page
    loadLogs()
  }
})

const loadLogs = async () => {
  try {
    const res = await api.get('/api/admin/email/logs', {
      params: {
        page: pagination.page,
        size: pagination.pageSize
      }
    })
    logs.value = res.data?.list || []
    pagination.itemCount = Number(res.data?.total || 0)
  } catch { /* ignore */ }
}

const logColumns = [
  { title: '收件人', key: 'toEmail', width: 200, ellipsis: { tooltip: true } },
  { title: '主题', key: 'subject', ellipsis: { tooltip: true } },
  { title: '目标', key: 'target', width: 80 },
  {
    title: '状态', key: 'status', width: 80,
    render: (row) => h(NTag, { size: 'small', type: row.status === 'sent' ? 'success' : row.status === 'failed' ? 'error' : 'default' }, () => row.status)
  },
  { title: '错误', key: 'errorMsg', width: 160, ellipsis: { tooltip: true } },
  {
    title: '时间', key: 'createTime', width: 160,
    render: (row) => row.createTime ? new Date(row.createTime).toLocaleString('zh-CN') : ''
  }
]

// ========== 初始化 ==========
onMounted(() => {
  loadConfig()
  loadTemplates()
  loadLogs()
})
</script>
