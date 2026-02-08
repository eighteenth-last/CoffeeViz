import { defineStore } from 'pinia'
import api from '@/api'

export const useSubscriptionStore = defineStore('subscription', {
  state: () => ({
    currentSubscription: null,
    plans: [],
    quotas: {}
  }),
  
  getters: {
    planName: (state) => state.currentSubscription?.planName || 'Free',
    planCode: (state) => state.currentSubscription?.planCode || 'FREE',
    expiresAt: (state) => state.currentSubscription?.expiresAt,
    isExpired: (state) => {
      if (!state.currentSubscription?.expiresAt) return false
      return new Date(state.currentSubscription.expiresAt) < new Date()
    },
    
    // 配额相关
    repositoryQuota: (state) => state.quotas.repository || null,
    diagramQuota: (state) => state.quotas.diagram || null,
    sqlParseQuota: (state) => state.quotas.sql_parse || null,
    aiGenerateQuota: (state) => state.quotas.ai_generate || null,
    
    // 配额使用百分比
    repositoryUsagePercent: (state) => {
      const quota = state.quotas.repository
      if (!quota || quota.quotaLimit === -1) return 0
      return Math.round((quota.quotaUsed / quota.quotaLimit) * 100)
    },
    diagramUsagePercent: (state) => {
      const quota = state.quotas.diagram
      if (!quota || quota.quotaLimit === -1) return 0
      return Math.round((quota.quotaUsed / quota.quotaLimit) * 100)
    },
    sqlParseUsagePercent: (state) => {
      const quota = state.quotas.sql_parse
      if (!quota || quota.quotaLimit === -1) return 0
      return Math.round((quota.quotaUsed / quota.quotaLimit) * 100)
    },
    aiGenerateUsagePercent: (state) => {
      const quota = state.quotas.ai_generate
      if (!quota || quota.quotaLimit === -1) return 0
      return Math.round((quota.quotaUsed / quota.quotaLimit) * 100)
    }
  },
  
  actions: {
    async fetchCurrentSubscription() {
      try {
        const res = await api.get('/api/subscription/current')
        this.currentSubscription = res.data
        return res
      } catch (error) {
        console.error('获取订阅信息失败:', error)
        throw error
      }
    },
    
    async fetchPlans() {
      try {
        const res = await api.get('/api/subscription/plans')
        this.plans = res.data
        return res
      } catch (error) {
        console.error('获取订阅计划失败:', error)
        throw error
      }
    },
    
    async fetchQuotas() {
      try {
        const res = await api.get('/api/quota/list')
        this.quotas = res.data
        return res
      } catch (error) {
        console.error('获取配额信息失败:', error)
        throw error
      }
    },
    
    // 刷新配额信息（静默刷新，不抛出错误）
    async refreshQuotas() {
      try {
        await this.fetchQuotas()
      } catch (error) {
        console.error('刷新配额信息失败:', error)
        // 静默失败，不影响用户操作
      }
    },
    
    // 刷新所有订阅相关信息
    async refreshAll() {
      try {
        await Promise.all([
          this.fetchCurrentSubscription(),
          this.fetchQuotas()
        ])
      } catch (error) {
        console.error('刷新订阅信息失败:', error)
      }
    },
    
    async checkFeature(feature) {
      try {
        const res = await api.get('/api/subscription/check-feature', {
          params: { feature }
        })
        return res.data
      } catch (error) {
        console.error('检查功能权限失败:', error)
        return false
      }
    },
    
    async cancelSubscription(reason) {
      try {
        const res = await api.post('/api/subscription/cancel', null, {
          params: { reason }
        })
        // 取消后重新获取订阅信息
        await this.fetchCurrentSubscription()
        return res
      } catch (error) {
        console.error('取消订阅失败:', error)
        throw error
      }
    }
  }
})
