import request from './index'

/**
 * 团队 API
 */
export default {
  /**
   * 创建团队
   */
  createTeam(data) {
    return request.post('/api/team/create', data)
  },

  /**
   * 获取用户的团队列表
   */
  getUserTeams() {
    return request.get('/api/team/list')
  },

  /**
   * 获取团队详情
   */
  getTeamDetail(teamId) {
    return request.get(`/api/team/${teamId}`)
  },

  /**
   * 更新团队信息
   */
  updateTeam(teamId, data) {
    return request.put(`/api/team/${teamId}`, data)
  },

  /**
   * 删除团队
   */
  deleteTeam(teamId) {
    return request.delete(`/api/team/${teamId}`)
  },

  /**
   * 生成邀请链接
   */
  createInviteLink(teamId, data) {
    return request.post(`/api/team/${teamId}/invite-link`, data)
  },

  /**
   * 获取团队的邀请链接列表
   */
  getInviteLinks(teamId) {
    return request.get(`/api/team/${teamId}/invite-links`)
  },

  /**
   * 禁用邀请链接
   */
  disableInviteLink(inviteLinkId) {
    return request.put(`/api/team/invite-link/${inviteLinkId}/disable`)
  },

  /**
   * 获取邀请链接信息（公开接口）
   */
  getInviteLinkInfo(inviteCode) {
    return request.get(`/api/team/join/${inviteCode}/info`)
  },

  /**
   * 加入团队（已登录用户）
   */
  joinTeam(inviteCode) {
    return request.post(`/api/team/join/${inviteCode}`)
  },

  /**
   * 注册并加入团队
   */
  registerAndJoinTeam(data) {
    return request.post('/api/team/register-and-join', data)
  },

  /**
   * 获取团队成员列表
   */
  getTeamMembers(teamId) {
    return request.get(`/api/team/${teamId}/members`)
  },

  /**
   * 移除团队成员
   */
  removeMember(teamId, memberId) {
    return request.delete(`/api/team/${teamId}/members/${memberId}`)
  },

  /**
   * 获取团队归档库
   */
  getTeamRepository(teamId) {
    return request.get(`/api/team/${teamId}/repository`)
  },

  /**
   * 获取团队统计信息
   */
  getTeamStats(teamId) {
    return request.get(`/api/team/${teamId}/stats`)
  }
}
