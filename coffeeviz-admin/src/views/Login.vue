<template>
  <div class="min-h-screen flex">
    <!-- Left Side: Visual/Branding (70%) -->
    <div class="hidden lg:flex lg:w-[70%] bg-[#18181c] relative items-center justify-center overflow-hidden">
      <!-- Abstract Background Effect -->
      <div class="absolute inset-0 bg-gradient-to-br from-blue-900/20 to-purple-900/20 z-0"></div>
      <div class="absolute inset-0" style="background-image: radial-gradient(circle at 50% 50%, rgba(255, 255, 255, 0.03) 1px, transparent 1px); background-size: 30px 30px;"></div>
      
      <div class="relative z-10 text-center p-10">
        <img src="/logo.png" alt="CoffeeViz" class="w-32 h-32 object-contain mx-auto mb-8 animate-float" />
        <h1 class="text-5xl font-bold text-white mb-4 tracking-tight">CoffeeViz Admin</h1>
        <p class="text-xl text-gray-400 max-w-lg mx-auto leading-relaxed">
          专业的架构可视化生成与管理平台<br>
          <span class="text-sm opacity-70 mt-2 block">Enterprise Architecture Visualization System</span>
        </p>
      </div>
    </div>

    <!-- Right Side: Login Form (30%) -->
    <div class="w-full lg:w-[30%] bg-bg flex flex-col justify-center px-8 sm:px-12 relative z-20 shadow-2xl">
      <div class="w-full max-w-md mx-auto">
        <div class="mb-10">
          <h2 class="text-3xl font-bold text-white mb-2">欢迎回来</h2>
          <p class="text-gray-500">请登录您的管理员账号</p>
        </div>

        <n-form ref="formRef" :model="form" :rules="rules" size="large">
          <n-form-item label="账号" path="username">
            <n-input v-model:value="form.username" placeholder="请输入管理员账号">
              <template #prefix>
                <div class="i-carbon-user text-gray-400"></div>
              </template>
            </n-input>
          </n-form-item>
          
          <n-form-item label="密码" path="password">
            <n-input 
              v-model:value="form.password" 
              type="password" 
              show-password-on="click" 
              placeholder="请输入密码" 
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <div class="i-carbon-password text-gray-400"></div>
              </template>
            </n-input>
          </n-form-item>

          <div class="flex justify-between items-center mb-6">
            <n-checkbox class="text-gray-400">记住我</n-checkbox>
            <a href="javascript:;" class="text-primary hover:text-primary-hover text-sm">忘记密码?</a>
          </div>

          <n-button type="primary" block size="large" :loading="loading" @click="handleLogin" class="mb-6 font-bold">
            登 录
          </n-button>
        </n-form>
        
        <div class="text-center text-gray-600 text-xs mt-8">
          &copy; {{ new Date().getFullYear() }} CoffeeViz Team. All rights reserved.
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.animate-float {
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
  100% { transform: translateY(0px); }
}

:deep(.n-input) {
  background-color: rgba(255, 255, 255, 0.03);
}
:deep(.n-input:hover), :deep(.n-input:focus-within) {
  background-color: rgba(255, 255, 255, 0.05);
}
</style>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useAuthStore } from '@/store/auth'
import api from '@/api'

const router = useRouter()
const message = useMessage()
const authStore = useAuthStore()
const loading = ref(false)
const formRef = ref(null)

const form = reactive({ username: '', password: '' })
const rules = {
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: { required: true, message: '请输入密码', trigger: 'blur' }
}

const handleLogin = async () => {
  await formRef.value?.validate()
  loading.value = true
  try {
    const res = await api.post('/api/auth/login', form)
    authStore.setLogin(res.data)
    message.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    message.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>
