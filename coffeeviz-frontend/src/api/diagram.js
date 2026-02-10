import request from './index'

/**
 * 架构图 API
 */
export default {
  /**
   * 获取架构图列表
   */
  getDiagramList(repositoryId) {
    return request.get(`/api/diagram/list/${repositoryId}`)
  },

  /**
   * 获取架构图详情
   */
  getDiagramById(diagramId) {
    return request.get(`/api/diagram/${diagramId}`)
  },

  /**
   * 创建架构图
   */
  createDiagram(data) {
    return request.post('/api/diagram/create', data)
  },

  /**
   * 更新架构图
   */
  updateDiagram(diagramId, data) {
    return request.put(`/api/diagram/${diagramId}`, data)
  },

  /**
   * 删除架构图
   */
  deleteDiagram(diagramId) {
    return request.delete(`/api/diagram/${diagramId}`)
  }
}
