package com.coffeeviz.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coffeeviz.common.Result;
import com.coffeeviz.dto.ProjectCreateRequest;
import com.coffeeviz.dto.ProjectListRequest;
import com.coffeeviz.dto.ProjectUpdateRequest;
import com.coffeeviz.entity.Project;
import com.coffeeviz.entity.ProjectConfig;
import com.coffeeviz.entity.ProjectVersion;
import com.coffeeviz.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目管理 Controller
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/project")
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;
    
    /**
     * 创建项目
     */
    @PostMapping("/create")
    public Result<Long> createProject(@RequestBody ProjectCreateRequest request) {
        log.info("创建项目: projectName={}", request.getProjectName());
        
        try {
            // 1. 参数校验
            if (request.getProjectName() == null || request.getProjectName().trim().isEmpty()) {
                return Result.error(400, "项目名称不能为空");
            }
            
            // 2. 获取当前用户 ID
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 3. 构建项目对象
            Project project = new Project();
            project.setUserId(userId);
            project.setProjectName(request.getProjectName());
            project.setDescription(request.getDescription());
            project.setMermaidCode(request.getMermaidCode());
            project.setStatus("active");
            
            // 4. 构建项目配置
            ProjectConfig config = new ProjectConfig();
            config.setConfigType("SQL");
            config.setSqlContent(request.getMermaidCode());
            // renderOptions 可以后续扩展为 JSON 格式存储
            
            // 5. 创建项目
            Long projectId = projectService.createProject(project, config);
            
            log.info("项目创建成功: projectId={}", projectId);
            return Result.success("项目创建成功", projectId);
            
        } catch (Exception e) {
            log.error("项目创建失败", e);
            return Result.error("项目创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新项目
     */
    @PutMapping("/update")
    public Result<String> updateProject(@RequestBody ProjectUpdateRequest request) {
        log.info("更新项目: projectId={}", request.getId());
        
        try {
            // 1. 参数校验
            if (request.getId() == null) {
                return Result.error(400, "项目 ID 不能为空");
            }
            
            // 2. 获取当前用户 ID
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 3. 查询项目（权限校验）
            Project project = projectService.getProjectById(request.getId(), userId);
            
            // 4. 更新字段
            if (request.getProjectName() != null) {
                project.setProjectName(request.getProjectName());
            }
            if (request.getDescription() != null) {
                project.setDescription(request.getDescription());
            }
            if (request.getMermaidCode() != null) {
                project.setMermaidCode(request.getMermaidCode());
            }
            
            // 5. 更新项目
            boolean createVersion = request.getCreateVersion() != null ? request.getCreateVersion() : false;
            projectService.updateProject(project, createVersion, request.getChangeLog());
            
            log.info("项目更新成功: projectId={}", request.getId());
            return Result.success("项目更新成功", "success");
            
        } catch (Exception e) {
            log.error("项目更新失败", e);
            return Result.error("项目更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除项目
     */
    @DeleteMapping("/delete/{projectId}")
    public Result<String> deleteProject(@PathVariable Long projectId) {
        log.info("删除项目: projectId={}", projectId);
        
        try {
            // 1. 获取当前用户 ID
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 2. 删除项目
            projectService.deleteProject(projectId, userId);
            
            log.info("项目删除成功: projectId={}", projectId);
            return Result.success("项目删除成功", "success");
            
        } catch (Exception e) {
            log.error("项目删除失败", e);
            return Result.error("项目删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询项目详情
     */
    @GetMapping("/detail/{projectId}")
    public Result<Map<String, Object>> getProjectDetail(@PathVariable Long projectId) {
        log.info("查询项目详情: projectId={}", projectId);
        
        try {
            // 1. 获取当前用户 ID
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 2. 查询项目
            Project project = projectService.getProjectById(projectId, userId);
            
            // 3. 查询项目配置
            ProjectConfig config = projectService.getProjectConfig(projectId);
            
            // 4. 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("project", project);
            result.put("config", config);
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("查询项目详情失败", e);
            return Result.error("查询项目详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询项目列表
     */
    @PostMapping("/list")
    public Result<Map<String, Object>> listProjects(@RequestBody ProjectListRequest request) {
        log.info("查询项目列表: page={}, size={}", request.getPage(), request.getSize());
        
        try {
            // 1. 获取当前用户 ID
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 2. 查询项目列表
            IPage<Project> page = projectService.listProjects(
                userId, 
                request.getPage(), 
                request.getSize(), 
                request.getKeyword(), 
                request.getStatus()
            );
            
            // 3. 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("list", page.getRecords());
            result.put("total", page.getTotal());
            result.put("page", page.getCurrent());
            result.put("size", page.getSize());
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("查询项目列表失败", e);
            return Result.error("查询项目列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询项目版本历史
     */
    @GetMapping("/versions/{projectId}")
    public Result<List<ProjectVersion>> listProjectVersions(@PathVariable Long projectId) {
        log.info("查询项目版本历史: projectId={}", projectId);
        
        try {
            // 1. 获取当前用户 ID
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 2. 权限校验
            projectService.getProjectById(projectId, userId);
            
            // 3. 查询版本列表
            List<ProjectVersion> versions = projectService.listProjectVersions(projectId);
            
            return Result.success(versions);
            
        } catch (Exception e) {
            log.error("查询项目版本历史失败", e);
            return Result.error("查询项目版本历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取指定版本
     */
    @GetMapping("/version/{projectId}/{versionNo}")
    public Result<ProjectVersion> getProjectVersion(
            @PathVariable Long projectId, 
            @PathVariable Integer versionNo) {
        log.info("获取项目版本: projectId={}, versionNo={}", projectId, versionNo);
        
        try {
            // 1. 获取当前用户 ID
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 2. 权限校验
            projectService.getProjectById(projectId, userId);
            
            // 3. 查询版本
            ProjectVersion version = projectService.getProjectVersion(projectId, versionNo);
            
            if (version == null) {
                return Result.error(404, "版本不存在");
            }
            
            return Result.success(version);
            
        } catch (Exception e) {
            log.error("获取项目版本失败", e);
            return Result.error("获取项目版本失败: " + e.getMessage());
        }
    }
    
    /**
     * 导出项目（返回 Mermaid 代码）
     */
    @GetMapping("/export/{projectId}")
    public Result<String> exportProject(@PathVariable Long projectId) {
        log.info("导出项目: projectId={}", projectId);
        
        try {
            // 1. 获取当前用户 ID
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 2. 查询项目
            Project project = projectService.getProjectById(projectId, userId);
            
            // 3. 返回 Mermaid 代码
            return Result.success("导出成功", project.getMermaidCode());
            
        } catch (Exception e) {
            log.error("导出项目失败", e);
            return Result.error("导出项目失败: " + e.getMessage());
        }
    }
}
