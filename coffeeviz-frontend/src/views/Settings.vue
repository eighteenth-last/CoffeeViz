<template>
  <div class="settings fade-in">
    <!-- Header Section -->
    <div class="flex justify-between items-end mb-10">
      <div>
        <h1 class="text-4xl font-black text-white mb-2 tracking-tight">系统<span class="text-amber-500">设置</span></h1>
        <p class="text-neutral-500">管理您的个人资料与安全设置。</p>
      </div>
    </div>

    <div class="max-w-6xl mx-auto grid grid-cols-1 lg:grid-cols-2 gap-8">
      <!-- Profile Card -->
      <div class="glass-card p-8 rounded-3xl relative overflow-hidden h-full flex flex-col">
        <div class="flex items-center space-x-4 mb-8">
          <div class="w-12 h-12 rounded-xl bg-blue-500/10 flex items-center justify-center text-blue-500 border border-blue-500/20">
            <i class="fas fa-user-circle text-2xl"></i>
          </div>
          <div>
            <h3 class="text-xl font-bold text-white">个人资料</h3>
            <p class="text-xs text-neutral-500">更新您的基本联系信息</p>
          </div>
        </div>

        <div class="space-y-5 flex-1">
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">显示名称</label>
            <input 
              v-model="profileForm.displayName"
              type="text" 
              class="w-full bg-black/40 border border-neutral-800 rounded-xl px-4 py-3 text-white outline-none focus:border-blue-500 transition-all placeholder-neutral-700"
              placeholder="神阁绘XXXXXXXXXX"
            >
          </div>
          
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">电子邮箱</label>
            <input 
              v-model="profileForm.email"
              type="email" 
              class="w-full bg-black/40 border border-neutral-800 rounded-xl px-4 py-3 text-white outline-none focus:border-blue-500 transition-all placeholder-neutral-700"
              placeholder="example@domain.com"
            >
          </div>
          
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">手机号码</label>
            <input 
              v-model="profileForm.phone"
              type="tel" 
              class="w-full bg-black/40 border border-neutral-800 rounded-xl px-4 py-3 text-white outline-none focus:border-blue-500 transition-all placeholder-neutral-700"
              placeholder="+86 1xx xxxx xxxx"
            >
          </div>

          <div class="pt-4 mt-auto">
            <button 
              @click="handleSaveProfile" 
              :disabled="profileLoading"
              class="w-full py-3 bg-blue-600 hover:bg-blue-500 rounded-xl text-white font-bold shadow-lg shadow-blue-900/20 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
            >
              <i v-if="profileLoading" class="fas fa-spinner fa-spin mr-2"></i>
              <span>保存资料</span>
            </button>
          </div>
        </div>
      </div>

      <!-- Security Card -->
      <div class="glass-card p-8 rounded-3xl relative overflow-hidden h-full flex flex-col">
        <div class="flex items-center space-x-4 mb-8">
          <div class="w-12 h-12 rounded-xl bg-red-500/10 flex items-center justify-center text-red-500 border border-red-500/20">
            <i class="fas fa-shield-alt text-2xl"></i>
          </div>
          <div>
            <h3 class="text-xl font-bold text-white">安全设置</h3>
            <p class="text-xs text-neutral-500">修改您的登录密码</p>
          </div>
        </div>

        <div class="space-y-5 flex-1">
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">当前密码</label>
            <input 
              v-model="passwordForm.oldPassword"
              type="password" 
              class="w-full bg-black/40 border border-neutral-800 rounded-xl px-4 py-3 text-white outline-none focus:border-red-500 transition-all placeholder-neutral-700"
              placeholder="••••••••"
            >
          </div>
          
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">新密码</label>
            <input 
              v-model="passwordForm.newPassword"
              type="password" 
              class="w-full bg-black/40 border border-neutral-800 rounded-xl px-4 py-3 text-white outline-none focus:border-red-500 transition-all placeholder-neutral-700"
              placeholder="至少 6 位字符"
            >
          </div>
          
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-2">确认新密码</label>
            <input 
              v-model="passwordForm.confirmPassword"
              type="password" 
              class="w-full bg-black/40 border border-neutral-800 rounded-xl px-4 py-3 text-white outline-none focus:border-red-500 transition-all placeholder-neutral-700"
              placeholder="再次输入新密码"
            >
          </div>

          <div class="pt-4 mt-auto">
            <button 
              @click="handleChangePassword" 
              :disabled="passwordLoading"
              class="w-full py-3 bg-red-600 hover:bg-red-500 rounded-xl text-white font-bold shadow-lg shadow-red-900/20 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
            >
              <i v-if="passwordLoading" class="fas fa-spinner fa-spin mr-2"></i>
              <span>修改密码</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { useMessage } from 'naive-ui'

const userStore = useUserStore()
const message = useMessage()

const profileLoading = ref(false)
const passwordLoading = ref(false)

const profileForm = reactive({
  displayName: '',
  email: '',
  phone: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 加载用户信息
const loadUserInfo = async () => {
  try {
    await userStore.fetchUserInfo()
    profileForm.displayName = userStore.userInfo?.displayName || ''
    profileForm.email = userStore.userInfo?.email || ''
    profileForm.phone = userStore.userInfo?.phone || ''
  } catch (error) {
    message.error('加载用户信息失败')
  }
}

// 保存个人资料
const handleSaveProfile = async () => {
  profileLoading.value = true
  try {
    await userStore.updateUserInfo({
      displayName: profileForm.displayName,
      email: profileForm.email,
      phone: profileForm.phone
    })
    message.success('个人资料保存成功')
  } catch (error) {
    message.error(error.message || '保存失败')
  } finally {
    profileLoading.value = false
  }
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    message.warning('请填写完整的密码信息')
    return
  }
  
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    message.warning('两次输入的新密码不一致')
    return
  }
  
  if (passwordForm.newPassword.length < 6) {
    message.warning('新密码至少6位')
    return
  }
  
  passwordLoading.value = true
  try {
    await userStore.updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    message.success('密码修改成功')
    
    // 清空表单
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    message.error(error.message || '修改失败')
  } finally {
    passwordLoading.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.settings {
  min-height: calc(100vh - 80px);
}

.fade-in {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>