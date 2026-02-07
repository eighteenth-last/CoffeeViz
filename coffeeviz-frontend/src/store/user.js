import { defineStore } from 'pinia'
import api from '@/api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo?.username || '',
    displayName: (state) => state.userInfo?.displayName || state.userInfo?.username || ''
  },
  
  actions: {
    async login(credentials) {
      try {
        const res = await api.post('/api/auth/login', credentials)
        this.token = res.data.token
        this.userInfo = res.data.userInfo
        localStorage.setItem('token', this.token)
        return res
      } catch (error) {
        throw error
      }
    },
    
    async register(userData) {
      try {
        const res = await api.post('/api/auth/register', userData)
        // 注册成功后自动登录
        if (res.data) {
          await this.login({ username: userData.username, password: userData.password })
        }
        return res
      } catch (error) {
        throw error
      }
    },
    
    async fetchUserInfo() {
      try {
        const res = await api.get('/api/auth/userinfo')
        this.userInfo = res.data
        return res
      } catch (error) {
        this.logout()
        throw error
      }
    },
    
    async updateUserInfo(userData) {
      try {
        const res = await api.put('/api/setting/user', userData)
        // 更新成功后刷新用户信息
        await this.fetchUserInfo()
        return res
      } catch (error) {
        throw error
      }
    },
    
    async updatePassword(passwordData) {
      try {
        const res = await api.put('/api/setting/password', passwordData)
        return res
      } catch (error) {
        throw error
      }
    },
    
    async logout() {
      try {
        await api.post('/api/auth/logout')
      } catch (error) {
        console.error('登出失败:', error)
      } finally {
        this.token = ''
        this.userInfo = null
        localStorage.removeItem('token')
      }
    },
    
    async generateWechatQrCode() {
      try {
        const res = await api.get('/api/auth/wechat/qrcode')
        return res
      } catch (error) {
        throw error
      }
    },
    
    async checkWechatQrCode(qrCodeId) {
      try {
        const res = await api.get(`/api/auth/wechat/check/${qrCodeId}`)
        
        if (res.data.status === 'confirmed') {
          this.token = res.data.token
          this.userInfo = {
            id: res.data.userId
          }
          localStorage.setItem('token', res.data.token)
          await this.fetchUserInfo()
        }
        
        return res
      } catch (error) {
        throw error
      }
    }
  }
})
