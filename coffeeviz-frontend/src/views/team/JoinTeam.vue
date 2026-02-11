<template>
  <div class="min-h-screen bg-[#050505] flex items-center justify-center p-4 relative overflow-hidden font-sans text-neutral-200 selection:bg-indigo-500/30">
    <!-- Dynamic Background -->
    <div class="absolute inset-0 w-full h-full">
        <div class="absolute top-[-20%] left-[-10%] w-[800px] h-[800px] bg-indigo-600/10 rounded-full blur-[150px] animate-pulse-slow"></div>
        <div class="absolute bottom-[-20%] right-[-10%] w-[800px] h-[800px] bg-purple-600/10 rounded-full blur-[150px] animate-pulse-slow" style="animation-delay: 2s;"></div>
        <div class="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-full h-full bg-[url('/grid.svg')] opacity-[0.03]"></div>
    </div>

    <n-config-provider :theme="darkTheme">
      <div class="w-full max-w-5xl h-auto md:h-[650px] bg-[#18181c]/60 backdrop-blur-2xl border border-white/5 rounded-3xl shadow-2xl relative z-10 overflow-hidden flex flex-col md:flex-row transition-all duration-500 group hover:border-white/10 hover:shadow-indigo-500/10">
        
        <!-- Left Side: Team Info (Hero) -->
        <div class="w-full md:w-[45%] bg-gradient-to-br from-[#131316] via-[#101014] to-indigo-900/20 relative p-8 md:p-12 flex flex-col justify-between overflow-hidden">
           <!-- Decor -->
           <div class="absolute top-0 left-0 w-full h-full overflow-hidden pointer-events-none">
              <div class="absolute top-[-50px] left-[-50px] w-32 h-32 border border-white/5 rounded-full"></div>
              <div class="absolute bottom-[-50px] right-[-50px] w-64 h-64 border border-white/5 rounded-full"></div>
           </div>

           <!-- Content -->
           <div class="relative z-10 flex-1 flex flex-col items-center justify-center text-center">
              <div v-if="loading" class="animate-pulse flex flex-col items-center gap-4 w-full">
                 <div class="w-24 h-24 bg-white/5 rounded-full"></div>
                 <div class="w-3/4 h-8 bg-white/5 rounded"></div>
                 <div class="w-1/2 h-4 bg-white/5 rounded"></div>
              </div>

              <div v-else class="flex flex-col items-center w-full transform transition-all duration-700 hover:scale-[1.02]">
                 <div class="relative mb-8 group-avatar">
                    <div class="absolute inset-0 bg-indigo-500 blur-2xl opacity-20 group-hover:opacity-40 transition-opacity duration-500 rounded-full"></div>
                    <n-avatar
                      class="w-28 h-28 rounded-2xl border-2 border-white/10 shadow-2xl relative z-10"
                      :src="inviteInfo?.teamAvatarUrl"
                      :fallback-src="defaultAvatar"
                      object-fit="cover"
                    >
                       <span class="text-4xl font-bold">{{ inviteInfo?.teamName?.charAt(0) }}</span>
                    </n-avatar>
                    <!-- Status Badge -->
                    <div class="absolute -bottom-2 -right-2 bg-[#18181c] border border-white/10 px-3 py-1 rounded-full flex items-center gap-1.5 shadow-lg z-20">
                       <div class="w-1.5 h-1.5 rounded-full bg-green-500 animate-pulse"></div>
                       <span class="text-[10px] font-bold text-neutral-400 tracking-wider">INVITE</span>
                    </div>
                 </div>

                 <h1 class="text-3xl md:text-4xl font-black text-transparent bg-clip-text bg-gradient-to-r from-white via-neutral-200 to-neutral-400 mb-4 tracking-tight">
                    {{ inviteInfo?.teamName }}
                 </h1>
                 
                 <p class="text-neutral-400 text-sm leading-relaxed max-w-xs mx-auto mb-8 line-clamp-3">
                   {{ inviteInfo?.teamDescription || '邀请您加入团队，共同协作创造无限可能。' }}
                 </p>

                 <!-- Stats Grid -->
                 <div class="grid grid-cols-2 gap-4 w-full max-w-xs">
                    <div class="bg-white/5 border border-white/5 rounded-xl p-3 backdrop-blur-sm">
                       <div class="text-xs text-neutral-500 mb-1">当前成员</div>
                       <div class="text-xl font-bold text-white font-mono">{{ inviteInfo?.memberCount }}<span class="text-neutral-600 text-xs ml-1">/{{ inviteInfo?.maxMembers }}</span></div>
                    </div>
                    <div class="bg-white/5 border border-white/5 rounded-xl p-3 backdrop-blur-sm" v-if="inviteInfo?.maxUses > 0">
                       <div class="text-xs text-neutral-500 mb-1">剩余名额</div>
                       <div class="text-xl font-bold text-indigo-400 font-mono">{{ inviteInfo?.maxUses - inviteInfo?.usedCount }}</div>
                    </div>
                 </div>
              </div>
           </div>

           <!-- Footer -->
           <div class="relative z-10 text-center mt-8">
              <p class="text-[10px] text-neutral-600 uppercase tracking-widest font-semibold">CoffeeViz Platform</p>
           </div>
        </div>

        <!-- Right Side: Action Form -->
        <div class="w-full md:w-[55%] bg-[#18181c]/40 relative flex flex-col">
            <!-- Loading Overlay -->
            <div v-if="loading" class="absolute inset-0 z-20 flex items-center justify-center bg-[#18181c]">
                <div class="w-10 h-10 border-2 border-indigo-500/20 border-t-indigo-500 rounded-full animate-spin"></div>
            </div>

            <!-- Error State -->
            <div v-if="error" class="flex-1 flex flex-col items-center justify-center p-12 text-center">
                <div class="w-16 h-16 bg-red-500/10 rounded-full flex items-center justify-center mb-6">
                    <n-icon size="32" color="#ef4444"><AlertIcon /></n-icon>
                </div>
                <h3 class="text-xl font-bold text-white mb-2">链接无效</h3>
                <p class="text-neutral-400 mb-8 max-w-xs mx-auto">{{ error }}</p>
                <n-button type="primary" secondary @click="$router.push('/')">返回首页</n-button>
            </div>

            <!-- Main Content -->
            <div v-else class="flex-1 flex flex-col justify-center p-8 md:p-12 overflow-y-auto">
                <!-- Logged In State -->
                <div v-if="isLoggedIn" class="w-full max-w-sm mx-auto animate-fade-in-up">
                    <div class="mb-8">
                        <div class="text-sm font-medium text-indigo-400 mb-2">欢迎回来</div>
                        <h2 class="text-2xl font-bold text-white">确认加入团队</h2>
                    </div>

                    <div class="bg-[#101014] border border-white/5 rounded-2xl p-4 mb-8 flex items-center gap-4">
                         <n-avatar :size="48" round :src="userStore.userInfo?.avatarUrl || undefined" class="border border-white/10">
                            {{ userStore.userInfo?.username?.charAt(0).toUpperCase() }}
                         </n-avatar>
                         <div class="flex-1 min-w-0">
                            <div class="text-white font-medium truncate">{{ userStore.userInfo?.username }}</div>
                            <div class="text-neutral-500 text-xs truncate">{{ userStore.userInfo?.email }}</div>
                         </div>
                         <div class="w-2 h-2 rounded-full bg-green-500"></div>
                    </div>

                    <n-button
                        type="primary"
                        size="large"
                        block
                        :loading="joining"
                        @click="handleJoinTeam"
                        class="!h-12 !text-base !font-bold !rounded-xl !bg-indigo-600 hover:!bg-indigo-500 shadow-lg shadow-indigo-500/20 transition-all hover:scale-[1.02]"
                    >
                        立即加入
                        <template #icon><n-icon><ArrowForwardIcon /></n-icon></template>
                    </n-button>
                    
                    <div class="mt-6 text-center">
                         <n-button text size="tiny" class="text-neutral-500 hover:text-neutral-300" @click="handleLogout">
                            切换账号
                         </n-button>
                    </div>
                </div>

                <!-- Not Logged In State -->
                <div v-else class="w-full max-w-sm mx-auto">
                    <n-tabs 
                        v-model:value="activeTab" 
                        animated 
                        class="custom-tabs mb-8"
                    >
                        <n-tab-pane name="register" tab="注册加入"></n-tab-pane>
                        <n-tab-pane name="login" tab="登录账号"></n-tab-pane>
                    </n-tabs>

                    <div v-if="activeTab === 'register'" class="animate-fade-in-up">
                        <div class="mb-6">
                            <h2 class="text-2xl font-bold text-white mb-2">创建账号</h2>
                            <p class="text-neutral-400 text-sm">注册后将自动加入团队</p>
                        </div>
                        
                        <n-form
                            ref="registerFormRef"
                            :model="registerForm"
                            :rules="registerRules"
                            :show-label="false"
                            size="large"
                        >
                            <div class="space-y-4">
                                <n-form-item path="username" class="!m-0">
                                    <n-input v-model:value="registerForm.username" placeholder="用户名" class="custom-input">
                                        <template #prefix><n-icon :component="PersonIcon" /></template>
                                    </n-input>
                                </n-form-item>
                                <n-form-item path="email" class="!m-0">
                                    <n-input v-model:value="registerForm.email" placeholder="电子邮箱" class="custom-input">
                                        <template #prefix><n-icon :component="MailIcon" /></template>
                                    </n-input>
                                </n-form-item>
                                <div class="grid grid-cols-2 gap-4">
                                    <n-form-item path="password" class="!m-0">
                                        <n-input v-model:value="registerForm.password" type="password" show-password-on="click" placeholder="密码" class="custom-input">
                                            <template #prefix><n-icon :component="LockClosedIcon" /></template>
                                        </n-input>
                                    </n-form-item>
                                    <n-form-item path="confirmPassword" class="!m-0">
                                        <n-input v-model:value="registerForm.confirmPassword" type="password" show-password-on="click" placeholder="确认" class="custom-input">
                                            <template #prefix><n-icon :component="LockClosedIcon" /></template>
                                        </n-input>
                                    </n-form-item>
                                </div>
                            </div>

                            <n-button
                                type="primary"
                                size="large"
                                block
                                :loading="registering"
                                @click="handleRegisterAndJoin"
                                class="!mt-8 !h-12 !text-base !font-bold !rounded-xl !bg-indigo-600 hover:!bg-indigo-500 shadow-lg shadow-indigo-500/20 transition-all hover:scale-[1.02]"
                            >
                                注册并加入
                            </n-button>
                        </n-form>
                    </div>

                    <div v-else class="animate-fade-in-up">
                        <div class="mb-8">
                            <h2 class="text-2xl font-bold text-white mb-2">欢迎回来</h2>
                            <p class="text-neutral-400 text-sm">请登录您的账号以继续</p>
                        </div>

                        <div class="bg-[#101014] border border-white/5 rounded-2xl p-6 text-center mb-8">
                            <div class="w-12 h-12 bg-indigo-500/10 rounded-full flex items-center justify-center mx-auto mb-4 text-indigo-500">
                                <n-icon size="24"><LogInIcon /></n-icon>
                            </div>
                            <p class="text-sm text-neutral-400">登录后将跳转回此页面完成加入</p>
                        </div>
                        
                        <n-button
                            secondary
                            type="primary"
                            size="large"
                            block
                            @click="goToLogin"
                            class="!h-12 !text-base !font-bold !rounded-xl"
                        >
                            前往登录页面
                            <template #icon><n-icon><ArrowForwardIcon /></n-icon></template>
                        </n-button>
                    </div>
                </div>
            </div>
        </div>
      </div>
    </n-config-provider>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage, darkTheme, NConfigProvider } from 'naive-ui'
import {
  PersonAdd as PersonAddIcon,
  LogIn as LogInIcon,
  Person as PersonIcon,
  Mail as MailIcon,
  LockClosed as LockClosedIcon,
  AlertCircle as AlertIcon,
  ArrowForward as ArrowForwardIcon
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

const defaultAvatar = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"%3E%3Crect fill="%234f46e5" width="100" height="100"/%3E%3C/svg%3E'

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

const isLoggedIn = computed(() => userStore.isLoggedIn)

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

const handleJoinTeam = async () => {
  joining.value = true
  try {
    const res = await teamApi.joinTeam(route.params.inviteCode)
    if (res.code === 200) {
      message.success('成功加入团队！')
      setTimeout(() => router.push(`/team/${inviteInfo.value.teamId}`), 1000)
    } else {
      message.error(res.message || '加入团队失败')
    }
  } catch (error) {
    message.error(error.message || '加入团队失败')
  } finally {
    joining.value = false
  }
}

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
      const token = res.data
      sessionStorage.setItem('token', token)
      userStore.token = token
      await userStore.fetchUserInfo()
      setTimeout(() => router.push(`/team/${inviteInfo.value.teamId}`), 1000)
    } else {
      message.error(res.message || '注册失败')
    }
  } catch (error) {
    if (!error.errors) message.error(error.message || '注册失败')
  } finally {
    registering.value = false
  }
}

const goToLogin = () => {
  localStorage.setItem('pendingInviteCode', route.params.inviteCode)
  router.push('/login')
}

const handleLogout = () => {
    userStore.logout()
    // Stay on page to refresh state
}

onMounted(() => {
  loadInviteInfo()
})
</script>

<style scoped>
.animate-pulse-slow {
    animation: pulse 8s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
    0%, 100% { opacity: 0.1; }
    50% { opacity: 0.2; }
}

.animate-fade-in-up {
    animation: fadeInUp 0.5s ease-out;
}

@keyframes fadeInUp {
    from { opacity: 0; transform: translateY(10px); }
    to { opacity: 1; transform: translateY(0); }
}

/* Custom Input Styles */
:deep(.custom-input) {
    background-color: rgba(255, 255, 255, 0.03) !important;
    border: 1px solid rgba(255, 255, 255, 0.05) !important;
    border-radius: 12px !important;
    transition: all 0.3s ease !important;
}

:deep(.custom-input:hover) {
    background-color: rgba(255, 255, 255, 0.05) !important;
    border-color: rgba(255, 255, 255, 0.1) !important;
}

:deep(.custom-input.n-input--focus) {
    background-color: rgba(255, 255, 255, 0.05) !important;
    border-color: #6366f1 !important; /* Indigo-500 */
    box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.2) !important;
}

:deep(.n-input .n-input__input-el) {
    height: 44px !important;
    color: white !important;
    caret-color: #6366f1;
}

:deep(.n-input .n-icon) {
    color: #71717a !important; /* Neutral-500 */
}

/* Custom Tabs Styles */
:deep(.custom-tabs .n-tabs-rail) {
    background-color: transparent !important;
    border-bottom: 2px solid rgba(255, 255, 255, 0.05);
    border-radius: 0 !important;
    padding: 0 !important;
}

:deep(.custom-tabs .n-tabs-tab) {
    background-color: transparent !important;
    color: #71717a !important;
    font-weight: 500;
    padding: 12px 24px !important;
    border-radius: 0 !important;
    border-bottom: 2px solid transparent;
    margin-bottom: -2px;
    transition: all 0.3s ease;
}

:deep(.custom-tabs .n-tabs-tab:hover) {
    color: white !important;
}

:deep(.custom-tabs .n-tabs-tab--active) {
    color: white !important;
    border-bottom-color: #6366f1 !important;
    background-color: transparent !important;
    box-shadow: none !important;
}
</style>