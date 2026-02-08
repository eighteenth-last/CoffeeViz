package com.coffeeviz.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coffeeviz.annotation.RequireQuota;
import com.coffeeviz.annotation.RequireSubscription;
import com.coffeeviz.common.Result;
import com.coffeeviz.dto.RepositoryCreateRequest;
import com.coffeeviz.entity.Repository;
import com.coffeeviz.service.RepositoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 架构库管理 Controller
 * 
 * @author CoffeeViz Team
 * @since 2.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/repository")
public class RepositoryController {
    
    @Autowired
    private RepositoryService repositoryService;
    
    /**
     * 创建架构库
     */
    @PostMapping("/create")
    @RequireSubscription
    @RequireQuota("repository")
    public Result<Long> createRepository(@RequestBody RepositoryCreateRequest request) {
        log.info("创建架构库: repositoryName={}", request.getRepositoryName());
        
        try {
            // 1. 参数校验
            if (request.getRepositoryName() == null || request.getRepositoryName().trim().isEmpty()) {
                return Result.error(400, "架构库名称不能为空");
            }
            
            // 2. 获取当前用户 ID
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 3. 构建架构库对象
            Repository repository = new Repository();
            repository.setUserId(userId);
            repository.setRepositoryName(request.getRepositoryName());
            repository.setDescription(request.getDescription());
            repository.setStatus("active");
            
            // 4. 创建架构库
            Long repositoryId = repositoryService.createRepository(repository);
            
            log.info("架构库创建成功: repositoryId={}", repositoryId);
            return Result.success("架构库创建成功", repositoryId);
            
        } catch (Exception e) {
            log.error("架构库创建失败", e);
            return Result.error("架构库创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询架构库列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> listRepositories(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status) {
        log.info("查询架构库列表: page={}, size={}", page, size);
        
        try {
            // 1. 获取当前用户 ID
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 2. 查询架构库列表
            IPage<Repository> pageResult = repositoryService.listRepositories(
                userId, page, size, keyword, status
            );
            
            // 3. 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageResult.getRecords());
            result.put("total", pageResult.getTotal());
            result.put("page", pageResult.getCurrent());
            result.put("size", pageResult.getSize());
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("查询架构库列表失败", e);
            return Result.error("查询架构库列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询架构库详情
     */
    @GetMapping("/detail/{repositoryId}")
    public Result<Repository> getRepositoryDetail(@PathVariable("repositoryId") Long repositoryId) {
        log.info("查询架构库详情: repositoryId={}", repositoryId);
        
        try {
            // 1. 获取当前用户 ID
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 2. 查询架构库
            Repository repository = repositoryService.getRepositoryById(repositoryId, userId);
            
            return Result.success(repository);
            
        } catch (Exception e) {
            log.error("查询架构库详情失败", e);
            return Result.error("查询架构库详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除架构库
     */
    @DeleteMapping("/delete/{repositoryId}")
    public Result<String> deleteRepository(@PathVariable("repositoryId") Long repositoryId) {
        log.info("删除架构库: repositoryId={}", repositoryId);
        
        try {
            // 1. 获取当前用户 ID
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 2. 删除架构库
            repositoryService.deleteRepository(repositoryId, userId);
            
            log.info("架构库删除成功: repositoryId={}", repositoryId);
            return Result.success("架构库删除成功", "success");
            
        } catch (Exception e) {
            log.error("架构库删除失败", e);
            return Result.error("架构库删除失败: " + e.getMessage());
        }
    }
}
