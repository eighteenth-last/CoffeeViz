<template>
  <div class="team-detail-container">
    <n-spin :show="teamStore.loading">
      <div v-if="teamStore.currentTeam">
        <!-- 团队头部 -->
        <n-page-header @back="goBack">
          <template #title>
            <div class="team-header">
              <n-avatar
                :size="48"
                :src="teamStore.currentTeam.avatarUrl"
                :fallback-src="defaultAvatar"
              >
                {{ teamStore.currentTeam.teamName.charAt(0) }}
              </n-avatar>
              <div class="team-header-info">
                <h2>{{ teamStore.currentTeam.teamName }}</h2>
                <p>{{ teamStore.currentTeam.description || '暂无描述' }}</p>
              </div>
            </div>
          </template>
          <template #extra>
            <n-space>
              <n-button v-if="teamStore.isTeamOwner" @click="showInviteModal = true">
                <template #icon>
                  <n-icon><LinkIcon /></n-icon>
                </template>
                邀请成员
              </n-button>
              <n-button v-if="teamStore.isTeamOwner" @click="showEditModal = true">
                <template #icon>
                  <n-icon><EditIcon /></n-icon>
                </template>
                编辑团队
              </n-button>
            </n-space>
          </template>
        </n-page-header>

        <!-- 统计卡片 -->
        <n-grid :cols="4" :x-gap="16" class="stats-grid">
          <n-gi>
            <n-statistic label="团队成员" :value="teamStore.currentTeam.memberCount">
              <template #suffix>/ {{ teamStore.currentTeam.maxMembers }}</template>
            </n-statistic>
          </n-gi>
          <n-gi>
            <n-statistic label="架构图" :value="stats?.diagramCount || 0" />
          </n-gi>
          <n-gi>
            <n-statistic label="归档库" :value="teamStore.currentTeam.repositoryName" />
          </n-gi>
          <n-gi>
            <n-statistic label="创建时间" :value="formatDate(teamStore.currentTeam.createTime)" />
          </n-gi>
        </n-grid>

        <!-- 标签页 -->
        <n-tabs v-model:value="activeTab" type="line" class="team-tabs">
          <n-tab-pane name="members" tab="成员管理">
            <TeamMembers />
          </n-tab-pane>
          <n-tab-pane name="invites" tab="邀请链接" v-if="teamStore.isTeamOwner">
            <TeamInvites @create-invite="showInviteModal = true" />
          </n-tab-pane>
          <n-tab-pane name="repository" tab="团队归档库">
            <TeamRepository />
          </n-tab-pane>
        </n-tabs>
      </div>
    </n-spin>

    <!-- 编辑团队对话框 -->
    <n-modal
      v-model:show="showEditModal"
      preset="card"
      title="编辑团队"
      style="width: 600px"
      :mask-closable="false"
    >
      <n-form
        ref="editFormRef"
        :model="editFormData"
        :rules="editFormRules"
        label-placement="left"
        label-width="100px"
      >
        <n-form-item label="团队名称" path="teamName">
          <n-input
            v-model:value="editFormData.teamName"
            placeholder="请输入团队名称"
            maxlength="100"
            show-count
          />
        </n-form-item>

        <n-form-item label="团队描述" path="description">
          <n-input
            v-model:value="editFormData.description"
            type="textarea"
            placeholder="请输入团队描述（可选）"
            :rows="3"
            maxlength="500"
            show-count
          />
        </n-form-item>
      </n-form>

      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 12px">
          <n-button @click="showEditModal = false">取消</n-button>
          <n-button type="primary" @click="handleUpdate" :loading="updating">
            保存
          </n-button>
        </div>
      </template>
    </n-modal>

    <!-- 邀请成员对话框 -->
    <n-modal
      v-model:show="showInviteModal"
      preset="card"
      title="邀请成员"
      style="width: 600px"
      :mask-closable="false"
    >
      <n-form
        ref="inviteFormRef"
        :model="inviteFormData"
        label-placement="left"
        label-width="120px"
      >
        <n-form-item label="最大使用次数">
          <n-input-number
            v-model:value="inviteFormData.maxUses"
            :min="0"
            placeholder="0 表示无限制"
            style="width: 100%"
          />
        </n-form-item>

        <n-form-item label="过期时间">
          <n-date-picker
            v-model:value="inviteFormData.expireTime"
            type="datetime"
            clearable
            placeholder="不设置表示永久有效"
            style="width: 100%"
          />
        </n-form-item>
      </n-form>

      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 12px">
          <n-button @click="showInviteModal = false">取消</n-button>
          <n-button type="primary" @click="handleCreateInvite" :loading="creatingInvite">
            生成邀请链接
          </n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { Link as LinkIcon, Create as EditIcon } from '@vicons/ionicons5'
import { useTeamStore } from '@/store/team'
import TeamMembers from './components/TeamMembers.vue'
import TeamInvites from './components/TeamInvites.vue'
import TeamRepository from './components/TeamRepository.vue'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const teamStore = useTeamStore()

const activeTab = ref('members')
const showEditModal = ref(false)
const showInviteModal = ref(false)
const editFormRef = ref(null)
const inviteFormRef = ref(null)
const updating = ref(false)
const creatingInvite = ref(false)
const stats = ref(null)

const defaultAvatar = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"%3E%3Crect fill="%2318a058" width="100" height="100"/%3E%3C/svg%3E'

const editFormData = ref({
  teamName: '',
  description: ''
})

const inviteFormData = ref({
  maxUses: 0,
  expireTime: null
})

const editFormRules = {
  teamName: [
    { required: true, message: '请输入团队名称', trigger: 'blur' },
    { min: 2, max: 100, message: '团队名称长度在 2 到 100 个字符', trigger: 'blur' }
  ]
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

// 返回
const goBack = () => {
  router.push('/team')
}

// 更新团队
const handleUpdate = async () => {
  try {
    await editFormRef.value?.validate()
    updating.value = true

    await teamStore.updateTeam(route.params.id, editFormData.value)
    
    message.success('团队信息更新成功')
    showEditModal.value = false
  } catch (error) {
    if (error.errors) return
    message.error(error.message || '更新失败')
  } finally {
    updating.value = false
  }
}

// 生成邀请链接
const handleCreateInvite = async () => {
  try {
    creatingInvite.value = true

    const data = {
      maxUses: inviteFormData.value.maxUses || 0,
      expireTime: inviteFormData.value.expireTime 
        ? new Date(inviteFormData.value.expireTime).toISOString()
        : null
    }

    const invite = await teamStore.createInviteLink(route.params.id, data)
    
    message.success('邀请链接生成成功')
    showInviteModal.value = false
    
    // 切换到邀请链接标签页
    activeTab.value = 'invites'
    
    // 重置表单
    inviteFormData.value = {
      maxUses: 0,
      expireTime: null
    }
  } catch (error) {
    message.error(error.message || '生成邀请链接失败')
  } finally {
    creatingInvite.value = false
  }
}

// 加载团队详情
const loadTeamDetail = async () => {
  try {
    await teamStore.fetchTeamDetail(route.params.id)
    
    // 填充编辑表单
    editFormData.value = {
      teamName: teamStore.currentTeam.teamName,
      description: teamStore.currentTeam.description || ''
    }

    // 加载统计信息
    stats.value = await teamStore.fetchTeamStats(route.params.id)
  } catch (error) {
    message.error(error.message || '加载团队详情失败')
    router.push('/team')
  }
}

onMounted(() => {
  loadTeamDetail()
})

</script>

<style scoped>
.team-detail-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.team-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.team-header-info h2 {
  margin: 0 0 4px 0;
  font-size: 24px;
  font-weight: 600;
}

.team-header-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.stats-grid {
  margin: 24px 0;
}

.team-tabs {
  margin-top: 24px;
}
</style>
