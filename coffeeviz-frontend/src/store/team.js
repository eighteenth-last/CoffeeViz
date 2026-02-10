import { defineStore } from 'pinia'
import teamApi from '@/api/team'

export const useTeamStore = defineStore('team', {
  state: () => ({
    // 当前团队
    currentTeam: null,
    // 用户的团队列表
    teams: [],
    // 团队成员列表
    members: [],
    // 邀请链接列表
    inviteLinks: [],
    // 团队统计
    stats: null,
    // 加载状态
    loading: false
  }),

  getters: {
    // 当前用户在团队中的角色
    currentTeamRole: (state) => {
      return state.currentTeam?.userRole || null
    },

    // 是否是团队所有者
    isTeamOwner() {
      return this.currentTeamRole === 'owner'
    },

    // 是否是团队成员
    isTeamMember: (state) => {
      return state.currentTeam !== null
    },

    // 是否可以管理成员
    canManageMembers() {
      return this.isTeamOwner
    },

    // 是否可以编辑团队
    canEditTeam() {
      return this.isTeamOwner
    },

    // 是否可以删除团队
    canDeleteTeam() {
      return this.isTeamOwner
    },

    // 团队成员数量是否已满
    isMembersFull: (state) => {
      if (!state.currentTeam) return false
      return state.currentTeam.memberCount >= state.currentTeam.maxMembers
    },

    // 有效的邀请链接
    activeInviteLinks: (state) => {
      return state.inviteLinks.filter(link => link.status === 'active')
    }
  },

  actions: {
    /**
     * 创建团队
     */
    async createTeam(data) {
      this.loading = true
      try {
        const res = await teamApi.createTeam(data)
        if (res.code === 200) {
          this.teams.push(res.data)
          return res.data
        }
        throw new Error(res.message || '创建团队失败')
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取用户的团队列表
     */
    async fetchUserTeams() {
      this.loading = true
      try {
        const res = await teamApi.getUserTeams()
        if (res.code === 200) {
          this.teams = res.data || []
          return res.data
        }
        throw new Error(res.message || '获取团队列表失败')
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取团队详情
     */
    async fetchTeamDetail(teamId) {
      this.loading = true
      try {
        const res = await teamApi.getTeamDetail(teamId)
        if (res.code === 200) {
          this.currentTeam = res.data
          this.members = res.data.members || []
          return res.data
        }
        throw new Error(res.message || '获取团队详情失败')
      } finally {
        this.loading = false
      }
    },

    /**
     * 更新团队信息
     */
    async updateTeam(teamId, data) {
      this.loading = true
      try {
        const res = await teamApi.updateTeam(teamId, data)
        if (res.code === 200) {
          // 更新列表中的团队
          const index = this.teams.findIndex(t => t.id === teamId)
          if (index !== -1) {
            this.teams[index] = { ...this.teams[index], ...data }
          }
          // 更新当前团队
          if (this.currentTeam && this.currentTeam.id === teamId) {
            this.currentTeam = { ...this.currentTeam, ...data }
          }
          return true
        }
        throw new Error(res.message || '更新团队失败')
      } finally {
        this.loading = false
      }
    },

    /**
     * 删除团队
     */
    async deleteTeam(teamId) {
      this.loading = true
      try {
        const res = await teamApi.deleteTeam(teamId)
        if (res.code === 200) {
          this.teams = this.teams.filter(t => t.id !== teamId)
          if (this.currentTeam && this.currentTeam.id === teamId) {
            this.currentTeam = null
          }
          return true
        }
        throw new Error(res.message || '删除团队失败')
      } finally {
        this.loading = false
      }
    },

    /**
     * 生成邀请链接
     */
    async createInviteLink(teamId, data) {
      this.loading = true
      try {
        const res = await teamApi.createInviteLink(teamId, data)
        if (res.code === 200) {
          this.inviteLinks.unshift(res.data)
          return res.data
        }
        throw new Error(res.message || '生成邀请链接失败')
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取邀请链接列表
     */
    async fetchInviteLinks(teamId) {
      this.loading = true
      try {
        const res = await teamApi.getInviteLinks(teamId)
        if (res.code === 200) {
          this.inviteLinks = res.data || []
          return res.data
        }
        throw new Error(res.message || '获取邀请链接失败')
      } finally {
        this.loading = false
      }
    },

    /**
     * 禁用邀请链接
     */
    async disableInviteLink(inviteLinkId) {
      this.loading = true
      try {
        const res = await teamApi.disableInviteLink(inviteLinkId)
        if (res.code === 200) {
          const index = this.inviteLinks.findIndex(i => i.id === inviteLinkId)
          if (index !== -1) {
            this.inviteLinks[index].status = 'disabled'
          }
          return true
        }
        throw new Error(res.message || '禁用邀请链接失败')
      } finally {
        this.loading = false
      }
    },

    /**
     * 加入团队
     */
    async joinTeam(inviteCode) {
      this.loading = true
      try {
        const res = await teamApi.joinTeam(inviteCode)
        if (res.code === 200) {
          return true
        }
        throw new Error(res.message || '加入团队失败')
      } finally {
        this.loading = false
      }
    },

    /**
     * 注册并加入团队
     */
    async registerAndJoinTeam(data) {
      this.loading = true
      try {
        const res = await teamApi.registerAndJoinTeam(data)
        if (res.code === 200) {
          return res.data // 返回 token
        }
        throw new Error(res.message || '注册失败')
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取团队成员列表
     */
    async fetchTeamMembers(teamId) {
      this.loading = true
      try {
        const res = await teamApi.getTeamMembers(teamId)
        if (res.code === 200) {
          this.members = res.data || []
          return res.data
        }
        throw new Error(res.message || '获取成员列表失败')
      } finally {
        this.loading = false
      }
    },

    /**
     * 移除团队成员
     */
    async removeMember(teamId, memberId) {
      this.loading = true
      try {
        const res = await teamApi.removeMember(teamId, memberId)
        if (res.code === 200) {
          this.members = this.members.filter(m => m.id !== memberId)
          if (this.currentTeam) {
            this.currentTeam.memberCount--
          }
          return true
        }
        throw new Error(res.message || '移除成员失败')
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取团队统计
     */
    async fetchTeamStats(teamId) {
      this.loading = true
      try {
        const res = await teamApi.getTeamStats(teamId)
        if (res.code === 200) {
          this.stats = res.data
          return res.data
        }
        throw new Error(res.message || '获取统计信息失败')
      } finally {
        this.loading = false
      }
    },

    /**
     * 清空当前团队
     */
    clearCurrentTeam() {
      this.currentTeam = null
      this.members = []
      this.inviteLinks = []
      this.stats = null
    }
  }
})
