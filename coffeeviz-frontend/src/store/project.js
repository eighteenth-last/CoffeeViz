import { defineStore } from 'pinia'
import api from '@/api'

export const useProjectStore = defineStore('project', {
  state: () => ({
    currentProject: null,
    projectList: [],
    total: 0,
    diagramData: {
      mermaidCode: '',
      svgContent: '',
      pngBase64: '',
      warnings: [],
      tableCount: 0,
      relationCount: 0
    },
    loading: false
  }),
  
  getters: {
    hasDiagram: (state) => !!state.diagramData.mermaidCode
  },
  
  actions: {
    async generateFromSql(sqlText, options = {}) {
      this.loading = true
      try {
        const res = await api.post('/api/er/parse-sql', {
          sqlText,
          viewMode: options.viewMode || 'PHYSICAL',
          inferRelations: options.inferRelations || false,
          tableFilter: options.tableFilter || null,
          relationDepth: options.relationDepth || -1
        })
        this.diagramData = res.data
        return res
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    
    async generateFromJdbc(config) {
      this.loading = true
      try {
        const res = await api.post('/api/er/connect-jdbc', config)
        this.diagramData = res.data
        return res
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    
    async testJdbcConnection(config) {
      try {
        const res = await api.post('/api/er/test-connection', config)
        return res
      } catch (error) {
        throw error
      }
    },
    
    async fetchProjects(params = {}) {
      try {
        const res = await api.post('/api/project/list', {
          page: params.page || 1,
          size: params.size || 10,
          keyword: params.keyword || null,
          status: params.status || null
        })
        this.projectList = res.data.list
        this.total = res.data.total
        return res
      } catch (error) {
        throw error
      }
    },
    
    async getProjectDetail(projectId) {
      try {
        const res = await api.get(`/api/project/detail/${projectId}`)
        this.currentProject = res.data.project
        return res
      } catch (error) {
        throw error
      }
    },
    
    async createProject(projectData) {
      try {
        const res = await api.post('/api/project/create', projectData)
        return res
      } catch (error) {
        throw error
      }
    },
    
    async updateProject(projectData) {
      try {
        const res = await api.put('/api/project/update', projectData)
        return res
      } catch (error) {
        throw error
      }
    },
    
    async deleteProject(projectId) {
      try {
        const res = await api.delete(`/api/project/delete/${projectId}`)
        return res
      } catch (error) {
        throw error
      }
    },
    
    async getProjectVersions(projectId) {
      try {
        const res = await api.get(`/api/project/versions/${projectId}`)
        return res
      } catch (error) {
        throw error
      }
    },
    
    async exportProject(projectId) {
      try {
        const res = await api.get(`/api/project/export/${projectId}`)
        return res
      } catch (error) {
        throw error
      }
    },
    
    clearDiagram() {
      this.diagramData = {
        mermaidCode: '',
        svgContent: '',
        pngBase64: '',
        warnings: [],
        tableCount: 0,
        relationCount: 0
      }
    }
  }
})
