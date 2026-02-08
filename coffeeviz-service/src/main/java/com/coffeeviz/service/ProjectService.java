package com.coffeeviz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coffeeviz.entity.Project;
import com.coffeeviz.entity.ProjectConfig;
import com.coffeeviz.entity.ProjectVersion;
import com.coffeeviz.mapper.ProjectConfigMapper;
import com.coffeeviz.mapper.ProjectMapper;
import com.coffeeviz.mapper.ProjectVersionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 项目管理服务
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class ProjectService {
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private ProjectConfigMapper projectConfigMapper;
    
    @Autowired
    private ProjectVersionMapper projectVersionMapper;
    
    @Autowired(required = false)
    private MinioService minioService;
    
    /**
     * 创建项目
     * 
     * @param project 项目信息
     * @param config 项目配置
     * @return 项目 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createProject(Project project, ProjectConfig config) {
        log.info("创建项目: {}, 用户ID: {}", project.getProjectName(), project.getUserId());
        
        // 1. 插入项目基本信息
        projectMapper.insert(project);
        Long projectId = project.getId();
        
        // 2. 插入项目配置
        if (config != null) {
            config.setProjectId(projectId);
            projectConfigMapper.insert(config);
        }
        
        // 3. 创建初始版本
        ProjectVersion version = new ProjectVersion();
        version.setProjectId(projectId);
        version.setVersionNo(1);
        version.setMermaidCode(project.getMermaidCode());
        version.setChangeLog("初始版本");
        projectVersionMapper.insert(version);
        
        log.info("项目创建成功: projectId={}", projectId);
        return projectId;
    }
    
    /**
     * 更新项目
     * 
     * @param project 项目信息
     * @param createVersion 是否创建新版本
     * @param changeLog 变更日志
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateProject(Project project, boolean createVersion, String changeLog) {
        log.info("更新项目: projectId={}, createVersion={}", project.getId(), createVersion);
        
        // 1. 更新项目基本信息
        projectMapper.updateById(project);
        
        // 2. 如果需要创建新版本
        if (createVersion && StringUtils.hasText(project.getMermaidCode())) {
            // 获取当前最大版本号
            LambdaQueryWrapper<ProjectVersion> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProjectVersion::getProjectId, project.getId())
                   .orderByDesc(ProjectVersion::getVersionNo)
                   .last("LIMIT 1");
            ProjectVersion latestVersion = projectVersionMapper.selectOne(wrapper);
            
            int nextVersionNo = (latestVersion != null) ? latestVersion.getVersionNo() + 1 : 1;
            
            // 创建新版本
            ProjectVersion newVersion = new ProjectVersion();
            newVersion.setProjectId(project.getId());
            newVersion.setVersionNo(nextVersionNo);
            newVersion.setMermaidCode(project.getMermaidCode());
            newVersion.setChangeLog(changeLog != null ? changeLog : "版本 " + nextVersionNo);
            projectVersionMapper.insert(newVersion);
            
            log.info("创建新版本: projectId={}, versionNo={}", project.getId(), nextVersionNo);
        }
    }
    
    /**
     * 删除项目（逻辑删除）
     * 
     * @param projectId 项目 ID
     * @param userId 用户 ID（权限校验）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(Long projectId, Long userId) {
        log.info("删除项目: projectId={}, userId={}", projectId, userId);
        
        // 1. 校验项目所有权
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        if (!project.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除此项目");
        }
        
        // 2. 删除 MinIO 中的图片
        if (minioService != null && project.getImageUrl() != null && !project.getImageUrl().isEmpty()) {
            try {
                String fileName = project.getImageUrl().substring(project.getImageUrl().lastIndexOf("/") + 1);
                minioService.deleteFile("projects/" + fileName);
                log.info("已删除 MinIO 图片: {}", fileName);
            } catch (Exception e) {
                log.warn("删除 MinIO 图片失败", e);
            }
        }
        
        // 3. 逻辑删除项目（MyBatis-Plus 自动处理）
        projectMapper.deleteById(projectId);
        
        // 4. 删除项目配置
        LambdaQueryWrapper<ProjectConfig> configWrapper = new LambdaQueryWrapper<>();
        configWrapper.eq(ProjectConfig::getProjectId, projectId);
        projectConfigMapper.delete(configWrapper);
        
        // 5. 删除项目版本
        LambdaQueryWrapper<ProjectVersion> versionWrapper = new LambdaQueryWrapper<>();
        versionWrapper.eq(ProjectVersion::getProjectId, projectId);
        projectVersionMapper.delete(versionWrapper);
        
        log.info("项目删除成功: projectId={}", projectId);
    }
    
    /**
     * 查询项目详情
     * 
     * @param projectId 项目 ID
     * @param userId 用户 ID（权限校验）
     * @return 项目信息
     */
    public Project getProjectById(Long projectId, Long userId) {
        Project project = projectMapper.selectById(projectId);
        
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        
        if (!project.getUserId().equals(userId)) {
            throw new RuntimeException("无权限访问此项目");
        }
        
        return project;
    }
    
    /**
     * 查询项目配置
     * 
     * @param projectId 项目 ID
     * @return 项目配置
     */
    public ProjectConfig getProjectConfig(Long projectId) {
        LambdaQueryWrapper<ProjectConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectConfig::getProjectId, projectId);
        return projectConfigMapper.selectOne(wrapper);
    }
    
    /**
     * 分页查询项目列表
     * 
     * @param userId 用户 ID
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词
     * @param status 状态筛选
     * @return 分页结果
     */
    public IPage<Project> listProjects(Long userId, int page, int size, String keyword, String status) {
        log.info("查询项目列表: userId={}, page={}, size={}, keyword={}, status={}", 
                userId, page, size, keyword, status);
        
        Page<Project> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getUserId, userId)
               .like(StringUtils.hasText(keyword), Project::getProjectName, keyword)
               .eq(StringUtils.hasText(status), Project::getStatus, status)
               .orderByDesc(Project::getUpdateTime);
        
        return projectMapper.selectPage(pageParam, wrapper);
    }
    
    /**
     * 查询项目版本历史
     * 
     * @param projectId 项目 ID
     * @return 版本列表
     */
    public List<ProjectVersion> listProjectVersions(Long projectId) {
        LambdaQueryWrapper<ProjectVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectVersion::getProjectId, projectId)
               .orderByDesc(ProjectVersion::getVersionNo);
        
        return projectVersionMapper.selectList(wrapper);
    }
    
    /**
     * 获取指定版本
     * 
     * @param projectId 项目 ID
     * @param versionNo 版本号
     * @return 版本信息
     */
    public ProjectVersion getProjectVersion(Long projectId, Integer versionNo) {
        LambdaQueryWrapper<ProjectVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectVersion::getProjectId, projectId)
               .eq(ProjectVersion::getVersionNo, versionNo);
        
        return projectVersionMapper.selectOne(wrapper);
    }
    
    /**
     * 统计用户项目数量
     * 
     * @param userId 用户 ID
     * @return 项目数量
     */
    public long countUserProjects(Long userId) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getUserId, userId);
        return projectMapper.selectCount(wrapper);
    }
}
