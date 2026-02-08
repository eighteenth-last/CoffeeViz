import axios from 'axios'
import { useUserStore } from '@/store/user'
import { useSubscriptionStore } from '@/store/subscription'

// 需要刷新配额的 API 路径
const QUOTA_REFRESH_APIS = [
  '/api/er/parse-sql',           // SQL 解析
  '/api/er/connect-jdbc',        // JDBC 连接
  '/api/repository/create',      // 创建架构库
  '/api/diagram/create',         // 创建架构图
  '/api/project/create'          // 创建项目
]

// 创建 axios 实例
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      // Sa-Token 使用 Authorization 作为 header 名称
      config.headers['Authorization'] = userStore.token
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    const res = response.data
    
    // 如果返回的状态码不是 200，则认为是错误
    if (res.code !== 200) {
      console.error('接口错误:', res.message)
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    // 检查是否需要刷新配额
    const requestUrl = response.config.url
    const shouldRefreshQuota = QUOTA_REFRESH_APIS.some(api => requestUrl.includes(api))
    
    if (shouldRefreshQuota) {
      // 异步刷新配额，不阻塞响应
      setTimeout(() => {
        try {
          const subscriptionStore = useSubscriptionStore()
          subscriptionStore.refreshQuotas()
        } catch (error) {
          console.error('刷新配额失败:', error)
        }
      }, 100)
    }
    
    return res
  },
  (error) => {
    console.error('响应错误:', error)
    
    // 处理 401 未授权
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      window.location.href = '/login'
    }
    
    // 处理 403 禁止访问
    if (error.response?.status === 403) {
      return Promise.reject(new Error('没有权限访问'))
    }
    
    // 处理 404 未找到
    if (error.response?.status === 404) {
      return Promise.reject(new Error('请求的资源不存在'))
    }
    
    // 处理 500 服务器错误
    if (error.response?.status === 500) {
      return Promise.reject(new Error('服务器内部错误'))
    }
    
    return Promise.reject(error.response?.data?.message || error.message || '网络错误')
  }
)

export default api
