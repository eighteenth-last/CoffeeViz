<template>
  <div class="team-list-container">
    <n-page-header title="我的团队" subtitle="管理您的团队和协作">
      <template #extra>
        <n-button type="primary" @click="showCreateModal = true">
          <template #icon>
            <n-icon><AddIcon /></n-icon>
          </template>
          创建团队
        </n-button>
      </template>
    </n-page-header>

    <n-spin :show="teamStore.loading">
      <div class="team-grid" v-if="teamStore.teams.length > 0">
        <n-card
          v-for="team in teamStore.teams"
          :key="team.id"
          hoverable
          class="team-card"
          @click="goToTeamDetail(team.id)"
        >
          <div class="team-card-content">
            <n-avatar
              :size="64"
              :src="team.avatarUrl"
              :fallback-src="defaultAvatar"
            >
              {{ team.teamName.charAt(0) }}
            </n-avatar>
            <div class="team-info">
              <h3>{{ team.teamName }}</h3>
              <p class="team-description">{{ team.description || '暂无描述' }}</p>
              <div class="team-meta">
                <n-tag size="small" type="info">
                  <template #icon>
                    <n-icon><PeopleIcon /></n-icon>
                  </template>
                  {{ team.memberCount }}/{{ team.maxMembers }} 成员
                </n-tag>
                <n-tag size="small" :type="team.status === 'active' ? 'success' : 'default'">
                  {{ team.status === 'active' ? '活跃' : '已暂停' }}
                </n-tag>
              </div>
            </div>
          </div>
        </n-card>
      </div>

      <n-empty
        v-else
        description="您还没有加入任何团队"
        class="empty-state"
      >
        <template #extra>
          <n-button type="primary" @click="showCreateModal = true">
            创建第一个团队
          </n-button>
        </template>
      </n-empty>
    </n-spin>

    <!-- 创建团队对话框 -->
    <n-modal
      v-model:show="showCreateModal"
      preset="card"
      title="创建团队"
      style="width: 600px"
      :mask-closable="false"
    >
      <n-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="left"
        label-width="100px"
      >
        <n-form-item label="团队名称" path="teamName">
          <n-input
            v-model:value="formData.teamName"
            placeholder="请输入团队名称"
            maxlength="100"
            show-count
          />
        </n-form-item>

        <n-form-item label="选择归档库" path="repositoryId">
          <n-select
            v-model:value="formData.repositoryId"
            :options="repositoryOptions"
            placeholder="选择要绑定的归档库"
            :loading="loadingRepositories"
          />
        </n-form-item>

        <n-form-item label="团队描述" path="description">
          <n-input
            v-model:value="formData.description"
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
          <n-button @click="showCreateModal = false">取消</n-button>
          <n-button type="primary" @click="handleCreate" :loading="creating">
            创建
          </n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { Add as AddIcon, People as PeopleIcon } from '@vicons/ionicons5'
import { useTeamStore } from '@/store/team'
import repositoryApi from '@/api/repository'

const router = useRouter()
const message = useMessage()
const teamStore = useTeamStore()

const showCreateModal = ref(false)
const formRef = ref(null)
const creating = ref(false)
const loadingRepositories = ref(false)
const repositoryOptions = ref([])

const defaultAvatar = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"%3E%3Crect fill="%2318a058" width="100" height="100"/%3E%3C/svg%3E'

const formData = ref({
  teamName: '',
  repositoryId: null,
  description: ''
})

const formRules = {
  teamName: [
    { required: true, message: '请输入团队名称', trigger: 'blur' },
    { min: 2, max: 100, message: '团队名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  repositoryId: [
    { required: true, message: '请选择归档库', trigger: 'change', type: 'number' }
  ]
}

// 加载归档库列表
const loadRepositories = async () => {
  loadingRepositories.value = true
  try {
    const res = await repositoryApi.getRepositoryList()
    if (res.code === 200) {
      // 后端返回的是分页数据，需要从 list 中获取
      const repositories = res.data?.list || []
      // 过滤出未绑定团队的归档库
      repositoryOptions.value = repositories
        .filter(repo => !repo.isTeamRepository)
        .map(repo => ({
          label: repo.repositoryName,
          value: repo.id
        }))
      
      if (repositoryOptions.value.length === 0) {
        message.warning('没有可用的归档库，请先创建归档库')
      }
    }
  } catch (error) {
    message.error('加载归档库列表失败')
    console.error('加载归档库失败:', error)
  } finally {
    loadingRepositories.value = false
  }
}

// 创建团队
const handleCreate = async () => {
  try {
    await formRef.value?.validate()
    creating.value = true

    await teamStore.createTeam(formData.value)
    
    message.success('团队创建成功')
    showCreateModal.value = false
    formData.value = {
      teamName: '',
      repositoryId: null,
      description: ''
    }
  } catch (error) {
    if (error.errors) {
      // 表单验证错误
      return
    }
    message.error(error.message || '创建团队失败')
  } finally {
    creating.value = false
  }
}

// 跳转到团队详情
const goToTeamDetail = (teamId) => {
  router.push(`/team/${teamId}`)
}

onMounted(async () => {
  try {
    await teamStore.fetchUserTeams()
  } catch (error) {
    message.error(error.message || '加载团队列表失败')
  }
})

// 监听创建对话框打开，加载归档库列表
watch(showCreateModal, (newVal) => {
  if (newVal) {
    loadRepositories()
  }
})

</script>

<style scoped>
.team-list-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.team-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
  margin-top: 24px;
}

.team-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.team-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.team-card-content {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.team-info {
  flex: 1;
  min-width: 0;
}

.team-info h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.team-description {
  margin: 0 0 12px 0;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.team-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.empty-state {
  margin-top: 100px;
}
</style>
