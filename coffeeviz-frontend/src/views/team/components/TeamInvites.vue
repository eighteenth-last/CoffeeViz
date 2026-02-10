<template>
  <div class="team-invites">
    <n-alert
      v-if="teamStore.isMembersFull"
      type="warning"
      title="成员已满"
      style="margin-bottom: 16px"
    >
      团队成员数量已达上限（{{ teamStore.currentTeam.memberCount }}/{{ teamStore.currentTeam.maxMembers }}），
      新成员将无法通过邀请链接加入。
    </n-alert>

    <n-list bordered class="invites-list">
      <n-list-item v-for="invite in teamStore.inviteLinks" :key="invite.id">
        <n-thing>
          <template #header>
            <div class="invite-header">
              <n-tag
                :type="getStatusType(invite.status)"
                size="small"
                :bordered="false"
              >
                {{ getStatusText(invite.status) }}
              </n-tag>
              <span class="invite-code">{{ invite.inviteCode }}</span>
            </div>
          </template>
          <template #description>
            <n-space vertical :size="8">
              <div class="invite-info">
                <n-icon><LinkIcon /></n-icon>
                <span class="invite-url">{{ invite.inviteUrl }}</span>
                <n-button
                  text
                  type="primary"
                  size="small"
                  @click="copyInviteUrl(invite.inviteUrl)"
                >
                  <template #icon>
                    <n-icon><CopyIcon /></n-icon>
                  </template>
                  复制链接
                </n-button>
              </div>
              <div class="invite-meta">
                <div class="meta-item">
                  <n-icon><TimeIcon /></n-icon>
                  <span>创建时间：{{ formatDate(invite.createTime) }}</span>
                </div>
                <div class="meta-item" v-if="invite.expireTime">
                  <n-icon><CalendarIcon /></n-icon>
                  <span>过期时间：{{ formatDate(invite.expireTime) }}</span>
                </div>
                <div class="meta-item">
                  <n-icon><PeopleIcon /></n-icon>
                  <span>
                    使用次数：{{ invite.usedCount }}
                    <template v-if="invite.maxUses > 0">
                      / {{ invite.maxUses }}
                    </template>
                    <template v-else>
                      / 无限制
                    </template>
                  </span>
                </div>
              </div>
            </n-space>
          </template>
        </n-thing>

        <template #suffix>
          <n-popconfirm
            v-if="invite.status === 'active'"
            @positive-click="handleDisableInvite(invite)"
            negative-text="取消"
            positive-text="确定禁用"
          >
            <template #trigger>
              <n-button type="warning" size="small" ghost>
                <template #icon>
                  <n-icon><CloseIcon /></n-icon>
                </template>
                禁用
              </n-button>
            </template>
            确定要禁用这个邀请链接吗？禁用后将无法使用。
          </n-popconfirm>
          <n-tag v-else type="default" size="small">
            已禁用
          </n-tag>
        </template>
      </n-list-item>
    </n-list>

    <n-empty
      v-if="teamStore.inviteLinks.length === 0"
      description="还没有创建邀请链接"
      style="margin-top: 40px"
    >
      <template #extra>
        <n-button type="primary" @click="$emit('create-invite')">
          创建邀请链接
        </n-button>
      </template>
    </n-empty>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useMessage } from 'naive-ui'
import {
  Link as LinkIcon,
  Copy as CopyIcon,
  Time as TimeIcon,
  Calendar as CalendarIcon,
  People as PeopleIcon,
  Close as CloseIcon
} from '@vicons/ionicons5'
import { useTeamStore } from '@/store/team'

const route = useRoute()
const message = useMessage()
const teamStore = useTeamStore()

defineEmits(['create-invite'])

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'active': 'success',
    'disabled': 'default',
    'expired': 'warning'
  }
  return typeMap[status] || 'default'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'active': '有效',
    'disabled': '已禁用',
    'expired': '已过期'
  }
  return textMap[status] || status
}

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

// 复制邀请链接
const copyInviteUrl = async (url) => {
  try {
    await navigator.clipboard.writeText(url)
    message.success('邀请链接已复制到剪贴板')
  } catch (error) {
    // 降级方案
    const textarea = document.createElement('textarea')
    textarea.value = url
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    try {
      document.execCommand('copy')
      message.success('邀请链接已复制到剪贴板')
    } catch (err) {
      message.error('复制失败，请手动复制')
    }
    document.body.removeChild(textarea)
  }
}

// 禁用邀请链接
const handleDisableInvite = async (invite) => {
  try {
    await teamStore.disableInviteLink(invite.id)
    message.success('邀请链接已禁用')
  } catch (error) {
    message.error(error.message || '禁用失败')
  }
}

// 加载邀请链接列表
onMounted(async () => {
  try {
    await teamStore.fetchInviteLinks(route.params.id)
  } catch (error) {
    message.error(error.message || '加载邀请链接失败')
  }
})
</script>

<style scoped>
.team-invites {
  padding: 16px 0;
}

.invites-list {
  background: transparent;
}

.invite-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.invite-code {
  font-family: 'Courier New', monospace;
  font-size: 14px;
  color: #666;
}

.invite-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.invite-url {
  flex: 1;
  font-size: 13px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.invite-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  font-size: 13px;
}

.meta-item .n-icon {
  font-size: 14px;
}
</style>
