<template>
  <n-config-provider :theme="darkTheme">
    <n-message-provider>
      <n-dialog-provider>
        <n-notification-provider>
          <router-view v-slot="{ Component }">
            <transition name="page-fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </n-notification-provider>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { darkTheme } from 'naive-ui'
import { onMounted } from 'vue'
import { useSubscriptionStore } from '@/store/subscription'
import { useUserStore } from '@/store/user'

const subscriptionStore = useSubscriptionStore()
const userStore = useUserStore()

// 应用启动时加载订阅信息
onMounted(async () => {
  // 只有在用户已登录时才加载订阅信息
  if (userStore.isLoggedIn) {
    try {
      await subscriptionStore.fetchCurrentSubscription()
      await subscriptionStore.fetchQuotas()
    } catch (error) {
      console.error('加载订阅信息失败:', error)
    }
  }
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #18181b;
  color: #e4e4e7;
}

#app {
  width: 100%;
  min-height: 100vh;
}

.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.2s ease;
}

.page-fade-enter-from,
.page-fade-leave-to {
  opacity: 0;
}
</style>
