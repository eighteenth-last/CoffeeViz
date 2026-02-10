<template>
  <div class="team-members">
    <div class="members-header">
      <n-input
        v-model:value="searchKeyword"
        placeholder="搜索成员..."
        clearable
        style="max-width: 300px"
      >
        <template #prefix>
          <n-icon><SearchIcon /></n-icon>
        </template>
      </n-input>
      <div class="member-count">
        共 {{ filteredMembers.length }} 名成员
      </div>
    </div>

    <n-list bordered class="members-list">
      <n-list-item v-for="member in filteredMembers" :key="member.id">
        <template #prefix>
          <n-avatar
            :size="48"
            :src="member.avatarUrl"
            :fallback-src="defaultAvatar"
          >
            {{ member.username?.charAt(0) || 'U' }}
          </n-avatar>
        </template>

        <n-thing>
          <template #header>
            <div class="member-name">
              {{ member.username }}
              <n-tag
                v-if="member.role === 'owner'"
                type="warning"
                size="small"
                :bordered="false"
              >
                所有者
              </n-tag>
              <n-tag
                v-else
                type="info"
                size="small"
                :bordered="false"
              >
                成员
              </n-tag>
            </div>
          </template>
          <template #description>
            <n-space vertical :size="4">
              <div class="member-info">
                <n-icon><MailIcon /></n-icon>
                <span>{{ member.email || '未设置邮箱' }}</span>
              </div>
              <div class="member-info">
                <n-icon><TimeIcon /></n-icon>
                <span>加入时间：{{ formatDate(member.joinTime) }}</span>
              </div>
              <div class="member-info" v-if="member.joinSource">
                <n-icon><EnterIcon /></n-icon>
                <span>加入方式：{{ getJoinSourceText(member.joinSource) }}</span>
              </div>
            </n-space>
          </template>
        </n-thing>

        <template #suffix>
          <n-popconfirm
            v-if="teamStore.isTeamOwner && member.role !== 'owner'"
            @positive-click="handleRemoveMember(member)"
            negative-text="取消"
            positive-text="确定移除"
          >
            <template #trigger>
              <n-button type="error" size="small" ghost>
                <template #icon>
                  <n-icon><TrashIcon /></n-icon>
                </template>
                移除
              </n-button>
            </template>
            确定要移除成员 "{{ member.username }}" 吗？
          </n-popconfirm>
        </template>
      </n-list-item>
    </n-list>

    <n-empty
      v-if="filteredMembers.length === 0"
      description="没有找到匹配的成员"
      style="margin-top: 40px"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useMessage } from 'naive-ui'
import { useRoute } from 'vue-router'
import {
  Search as SearchIcon,
  Mail as MailIcon,
  Time as TimeIcon,
  Enter as EnterIcon,
  Trash as TrashIcon
} from '@vicons/ionicons5'
import { useTeamStore } from '@/store/team'

const route = useRoute()
const message = useMessage()
const teamStore = useTeamStore()

const searchKeyword = ref('')
const defaultAvatar = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"%3E%3Crect fill="%236366f1" width="100" height="100"/%3E%3C/svg%3E'

// 过滤成员
const filteredMembers = computed(() => {
  if (!searchKeyword.value) {
    return teamStore.members
  }
  const keyword = searchKeyword.value.toLowerCase()
  return teamStore.members.filter(member => 
    member.username?.toLowerCase().includes(keyword) ||
    member.email?.toLowerCase().includes(keyword) ||
    member.nickname?.toLowerCase().includes(keyword)
  )
})

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

// 获取加入方式文本
const getJoinSourceText = (source) => {
  const sourceMap = {
    'create': '创建团队',
    'invite': '邀请链接'
  }
  return sourceMap[source] || source
}

// 移除成员
const handleRemoveMember = async (member) => {
  try {
    await teamStore.removeMember(route.params.id, member.id)
    message.success(`已移除成员 "${member.username}"`)
  } catch (error) {
    message.error(error.message || '移除成员失败')
  }
}
</script>

<style scoped>
.team-members {
  padding: 16px 0;
}

.members-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.member-count {
  color: #666;
  font-size: 14px;
}

.members-list {
  background: transparent;
}

.member-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
}

.member-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  font-size: 13px;
}

.member-info .n-icon {
  font-size: 14px;
}
</style>
