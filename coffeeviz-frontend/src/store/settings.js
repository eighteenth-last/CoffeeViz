import { defineStore } from 'pinia'
import api from '@/api'

export const useSettingsStore = defineStore('settings', {
  state: () => ({
    systemConfig: {},
    loading: false
  }),
  
  getters: {
    getConfig: (state) => (key) => state.systemConfig[key] || null
  },
  
  actions: {
    async fetchSystemConfig() {
      this.loading = true
      try {
        const res = await api.get('/api/setting/config')
        this.systemConfig = res.data
        return res
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    
    async updateSystemConfig(configData) {
      try {
        const res = await api.put('/api/setting/config', {
          configs: configData
        })
        // 更新成功后刷新配置
        await this.fetchSystemConfig()
        return res
      } catch (error) {
        throw error
      }
    },
    
    async deleteConfig(configKey) {
      try {
        const res = await api.delete(`/api/setting/config/${configKey}`)
        // 删除成功后刷新配置
        await this.fetchSystemConfig()
        return res
      } catch (error) {
        throw error
      }
    },
    
    async initDefaultConfigs() {
      try {
        const res = await api.post('/api/setting/config/init')
        // 初始化成功后刷新配置
        await this.fetchSystemConfig()
        return res
      } catch (error) {
        throw error
      }
    }
  }
})
