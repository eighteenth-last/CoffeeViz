<template>
  <div class="join-team-container">
    <n-card class="join-team-card">
      <n-spin :show="loading">
        <!-- 团队信息 -->
        <div v-if="inviteInfo" class="team-info-section">
          <n-result
            status="info"
            :title="`加入团队：${inviteInfo.teamName}`"
            :description="inviteInfo.teamDescription || '欢迎加入我们的团队！'"
          >
            <template #icon>
              <n-avatar
                :size="80"
                :src="inviteInfo.teamAvatarUrl"
                :fallback-src="defaultAvatar"
              >
                {{ inviteInfo.teamName?.charAt(0) }}
              </n-avatar>
            </template>
          </n-result>

          <n-divider />

          <!-- 团队统计 -->
          <n-space justify="center" :size="40" class="team-stats">
            <n-statistic label="当前成员" :value="inviteInfo.memberCount">
              <template #suffix>/ {{ inviteInfo.maxMembers }}</template>
            </n-statistic>
            <n-statistic
              v-if="inviteInfo.maxUses > 0"
              label="邀请使用"
              :value="inviteInfo.usedCount"
            >
              <template #suffix>/ {{ inviteInfo.maxUses }}</template>
            </n-statistic>
          </n-space>

          <n-divider />

          <!-- 已登录用户 - 直接加入 -->
          <div v-if="isLoggedIn" class="action-section">
            <n-alert type="success" title="您已登录" style="margin-bottom: 16px">
              点击下方按钮即可加入团队
            </n-alert>
            <n-button
              type="primary"
              size="large"
              block
              :loading="joining"
              @click="handleJoinTeam"
            >
              <template #icon>
                <n-icon><CheckmarkIcon /></n-icon>
              </template>
              加入团队
            </n-button>
          </div>

          <!-- 未登录用户 - 注册表单 -->
          <div v-else class="action-section">
            <n-tabs v-model:value="activeTab" type="segment" animated>
              <!-- 注册并加入 -->
              <n-tab-pane name="register" tab="注册并加入">
                <n-form
                  ref="registerFormRef"
                  :model="registerForm"
                  :rules="registerRules"
                  label-placement="top"
                  style="margin-top: 16px"
                >
                  <n-form-item label="用户名" path="username">
                    <n-input
                      v-model:value="registerForm.username"
                      placeholder="请输入用户名"
                      size="large"
                    />
                  </n-form-item>

                  <n-form-item label="邮箱" path="email">
                    <n-input
                      v-model:value="registerForm.email"
                      placeholder="请输入邮箱"
                      size="large"
                    />
                  </n-form-item>

                  <n-form-item label="密码" path="password">
                    <n-input
                      v-model:value="registerForm.password"
                      type="password"
                      show-password-on="click"
                      placeholder="请输入密码"
                      size="large"
                    />
                  </n-form-item>

                  <n-form-item label="确认密码" path="confirmPassword">
                    <n-input
                      v-model:value="registerForm.confirmPassword"
                      type="password"
                      show-password-on="click"
                      placeholder="请再次输入密码"
                      size="large"
                    />
                  </n-form-item>

                  <n-button
                    type="primary"
                    size="large"
                    block
                    :loading="registering"
                    @click="handleRegisterAndJoin"
                  >
                    <template #icon>
                      <n-icon><PersonAddIcon /></n-icon>
                    </template>
                    注册并加入团队
                  </n-button>
                </n-form>
              </n-tab-pane>

              <!-- 已有账号 - 登录 -->
              <n-tab-pane name="login" tab="已有账号">
                <n-alert type="info" style="margin-top: 16px">
                  如果您已经有账号，请先登录，然后再次访问此邀请链接。
                </n-alert>
                <n-button
                  type="primary"
                  size="large"
                  block
                  style="margin-top: 16px"
                  @click="goToLogin"
                >
                  <template #icon>
                    <n-icon><LogInIcon /></n-icon>
                  </template>
                  前往登录
                </n-button>
              </n-tab-pane>
            </n-tabs>
          </div>
        </div>

        <!-- 错误状态 -->
        <n-result
          v-else-if="error"
          status="error"
          title="邀请链接无效"
          :description="error"
        >
          <template #footer>
            <n-button @click="$router.push('/')">返回首页</n-button>
          </template>
        </n-result>
      </n-spin>
    </n-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import {
  Checkmark as CheckmarkIcon,
  PersonAdd as PersonAddIcon,
  LogIn as LogInIcon
} from '@vicons/ionicons5'
import teamApi from '@/api/team'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const userStore = useUserStore()

const loading = ref(true)
const joining = ref(false)
const registering = ref(false)
const inviteInfo = ref(null)
const error = ref(null)
const activeTab = ref('register')
const registerFormRef = ref(null)

const defaultAvatar = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"%3E%3Crect fill="%2318a058" width="100" height="100"/%3E%3C/svg%3E'

const registerForm = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value) => {
        return value === registerForm.value.password
      },
      message: '两次输入的密码不一致',
      trigger: 'blur'
    }
  ]
}

// 是否已登录
const isLoggedIn = computed(() => {
  return userStore.isLoggedIn
})

// 加载邀请信息
const loadInviteInfo = async () => {
  loading.value = true
  try {
    const res = await teamApi.getInviteLinkInfo(route.params.inviteCode)
    if (res.code === 200) {
      inviteInfo.value = res.data
    } else {
      error.value = res.message || '邀请链接无效或已过期'
    }
  } catch (err) {
    error.value = err.message || '加载邀请信息失败'
  } finally {
    loading.value = false
  }
}

// 已登录用户加入团队
const handleJoinTeam = async () => {
  joining.value = true
  try {
    const res = await teamApi.joinTeam(route.params.inviteCode)
    if (res.code === 200) {
      message.success('成功加入团队！')
      // 跳转到团队详情页
      setTimeout(() => {
        router.push(`/team/${inviteInfo.value.teamId}`)
      }, 1000)
    } else {
      message.error(res.message || '加入团队失败')
    }
  } catch (error) {
    message.error(error.message || '加入团队失败')
  } finally {
    joining.value = false
  }
}

// 注册并加入团队
const handleRegisterAndJoin = async () => {
  try {
    await registerFormRef.value?.validate()
    registering.value = true

    const data = {
      inviteCode: route.params.inviteCode,
      username: registerForm.value.username,
      email: registerForm.value.email,
      password: registerForm.value.password
    }

    const res = await teamApi.registerAndJoinTeam(data)
    if (res.code === 200) {
      message.success('注册成功，已自动加入团队！')
      
      // 保存 token 并更新用户状态
      const token = res.data
      localStorage.setItem('token', token)
      await userStore.fetchUserInfo()
      
      // 跳转到团队详情页
      setTimeout(() => {
        router.push(`/team/${inviteInfo.value.teamId}`)
      }, 1000)
    } else {
      message.error(res.message || '注册失败')
    }
  } catch (error) {
    if (error.errors) {
      // 表单验证错误
      return
    }
    message.error(error.message || '注册失败')
  } finally {
    registering.value = false
  }
}

// 前往登录页
const goToLogin = () => {
  // 保存当前邀请码，登录后自动跳转回来
  localStorage.setItem('pendingInviteCode', route.params.inviteCode)
  router.push('/login')
}

onMounted(() => {
  loadInviteInfo()
})
</script>

<style scoped>
.join-team-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.join-team-card {
  width: 100%;
  max-width: 600px;
}

.team-info-section {
  padding: 24px 0;
}

.team-stats {
  margin: 24px 0;
}

.action-section {
  margin-top: 24px;
}

:deep(.n-result) {
  padding: 0;
}

:deep(.n-result .n-result-icon) {
  margin-bottom: 16px;
}

:deep(.n-result .n-result-header) {
  margin-bottom: 8px;
}
</style>
