import request from './index'

/**
 * 归档库 API
 */
export default {
  /**
   * 获取归档库列表
   */
  getRepositoryList() {
    return request.get('/api/repository/list')
  },

  /**
   * 获取归档库详情
   */
  getRepositoryById(repositoryId) {
    return request.get(`/api/repository/${repositoryId}`)
  },

  /**
   * 创建归档库
   */
  createRepository(data) {
    return request.post('/api/repository/create', data)
  },

  /**
   * 更新归档库
   */
  updateRepository(repositoryId, data) {
    return request.put(`/api/repository/${repositoryId}`, data)
  },

  /**
   * 删除归档库
   */
  deleteRepository(repositoryId) {
    return request.delete(`/api/repository/${repositoryId}`)
  }
}
