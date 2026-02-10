<template>
  <div class="team-repository">
    <n-spin :show="loading">
      <div v-if="repository">
        <!-- 归档库信息卡片 -->
        <n-card title="归档库信息" :bordered="false" class="repository-card">
          <n-descriptions :column="2" label-placement="left">
            <n-descriptions-item label="归档库名称">
              {{ repository.repositoryName }}
            </n-descriptions-item>
            <n-descriptions-item label="架构图数量">
              {{ repository.diagramCount || 0 }}
            </n-descriptions-item>
            <n-descriptions-item label="状态">
              <n-tag :type="repository.status === 'PUBLISHED' ? 'success' : 'default'" size="small">
                {{ repository.status === 'PUBLISHED' ? '已发布' : '草稿' }}
              </n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="创建时间">
              {{ formatDate(repository.createTime) }}
            </n-descriptions-item>
            <n-descriptions-item label="描述" :span="2">
              {{ repository.description || '暂无描述' }}
            </n-descriptions-item>
          </n-descriptions>

          <template #action>
            <n-space>
              <n-button type="primary" @click="goToRepository">
                <template #icon>
                  <n-icon><FolderOpenIcon /></n-icon>
                </template>
                查看归档库
              </n-button>
              <n-button @click="refreshRepository">
                <template #icon>
                  <n-icon><RefreshIcon /></n-icon>
                </template>
                刷新
              </n-button>
            </n-space>
          </template>
        </n-card>

        <!-- 最近的架构图 -->
        <n-card title="最近的架构图" :bordered="false" class="diagrams-card">
          <n-list v-if="diagrams.length > 0" bordered>
            <n-list-item v-for="diagram in diagrams" :key="diagram.id">
              <template #prefix>
                <n-icon size="24" color="#18a058">
                  <DiagramIcon />
                </n-icon>
              </template>

              <n-thing>
                <template #header>
                  {{ diagram.diagramName }}
                </template>
                <template #description>
                  <n-space :size="16">
                    <span>
                      <n-icon><LayersIcon /></n-icon>
                      {{ diagram.tableCount || 0 }} 张表
                    </span>
                    <span>
                      <n-icon><GitNetworkIcon /></n-icon>
                      {{ diagram.relationCount || 0 }} 个关系
                    </span>
                    <span>
                      <n-icon><TimeIcon /></n-icon>
                      {{ formatDate(diagram.createTime) }}
                    </span>
                  </n-space>
                </template>
              </n-thing>

              <template #suffix>
                <n-button text type="primary" @click="viewDiagram(diagram.id)">
                  查看详情
                </n-button>
              </template>
            </n-list-item>
          </n-list>

          <n-empty
            v-else
            description="归档库中还没有架构图"
            style="margin: 40px 0"
          >
            <template #extra>
              <n-button type="primary" @click="goToRepository">
                去创建架构图
              </n-button>
            </template>
          </n-empty>
        </n-card>

        <!-- 团队成员贡献 -->
        <n-card title="成员贡献" :bordered="false" class="contribution-card">
          <n-alert type="info" :show-icon="false">
            团队成员可以将自己设计的架构图保存到这个归档库中，实现团队协作。
          </n-alert>
        </n-card>
      </div>
    </n-spin>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import {
  FolderOpen as FolderOpenIcon,
  Refresh as RefreshIcon,
  GitNetwork as DiagramIcon,
  Layers as LayersIcon,
  GitNetwork as GitNetworkIcon,
  Time as TimeIcon
} from '@vicons/ionicons5'
import { useTeamStore } from '@/store/team'
import repositoryApi from '@/api/repository'
import diagramApi from '@/api/diagram'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const teamStore = useTeamStore()

const loading = ref(false)
const repository = ref(null)
const diagrams = ref([])

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 加载归档库信息
const loadRepository = async () => {
  loading.value = true
  try {
    const res = await repositoryApi.getRepositoryById(teamStore.currentTeam.repositoryId)
    if (res.code === 200) {
      repository.value = res.data
      // 加载最近的架构图
      await loadDiagrams()
    }
  } catch (error) {
    message.error(error.message || '加载归档库信息失败')
  } finally {
    loading.value = false
  }
}

// 加载架构图列表
const loadDiagrams = async () => {
  if (!repository.value) return
  
  try {
    const res = await diagramApi.getDiagramList(repository.value.id)
    if (res.code === 200) {
      // 只显示最近的 5 个
      diagrams.value = (res.data || []).slice(0, 5)
    }
  } catch (error) {
    console.error('加载架构图列表失败:', error)
  }
}

// 刷新归档库
const refreshRepository = () => {
  loadRepository()
}

// 跳转到归档库详情
const goToRepository = () => {
  if (repository.value) {
    router.push(`/repository/${repository.value.id}`)
  }
}

// 查看架构图详情
const viewDiagram = (diagramId) => {
  router.push(`/diagram/${diagramId}`)
}

onMounted(() => {
  loadRepository()
})
</script>

<style scoped>
.team-repository {
  padding: 16px 0;
}

.repository-card,
.diagrams-card,
.contribution-card {
  margin-bottom: 16px;
}

.repository-card :deep(.n-card__action) {
  padding-top: 16px;
}

.n-list-item :deep(.n-thing-header) {
  font-size: 15px;
  font-weight: 500;
}

.n-list-item :deep(.n-thing-header-extra) {
  margin-top: 0;
}

.n-list-item :deep(.n-space) {
  font-size: 13px;
  color: #666;
}

.n-list-item :deep(.n-space .n-icon) {
  font-size: 14px;
  vertical-align: middle;
  margin-right: 4px;
}
</style>
