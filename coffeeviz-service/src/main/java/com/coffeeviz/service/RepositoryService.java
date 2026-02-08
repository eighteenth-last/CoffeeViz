package com.coffeeviz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coffeeviz.entity.Diagram;
import com.coffeeviz.entity.Repository;
import com.coffeeviz.mapper.DiagramMapper;
import com.coffeeviz.mapper.RepositoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 架构库管理服务
 * 
 * @author CoffeeViz Team
 * @since 2.0.0
 */
@Slf4j
@Service
public class RepositoryService {
    
    @Autowired
    private RepositoryMapper repositoryMapper;
    
    @Autowired
    private DiagramMapper diagramMapper;
    
    @Autowired(required = false)
    private MinioService minioService;
    
    /**
     * 创建架构库
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createRepository(Repository repository) {
        log.info("创建架构库: {}, 用户ID: {}", repository.getRepositoryName(), repository.getUserId());
        
        repository.setDiagramCount(0);
        if (repository.getStatus() == null) {
            repository.setStatus("active");
        }
        
        repositoryMapper.insert(repository);
        log.info("架构库创建成功: repositoryId={}", repository.getId());
        return repository.getId();
    }
    
    /**
     * 更新架构库
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateRepository(Repository repository) {
        log.info("更新架构库: repositoryId={}", repository.getId());
        repositoryMapper.updateById(repository);
    }
    
    /**
     * 删除架构库（级联删除所有架构图）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRepository(Long repositoryId, Long userId) {
        log.info("删除架构库: repositoryId={}, userId={}", repositoryId, userId);
        
        // 1. 校验所有权
        Repository repository = repositoryMapper.selectById(repositoryId);
        if (repository == null) {
            throw new RuntimeException("架构库不存在");
        }
        if (!repository.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除此架构库");
        }
        
        // 2. 删除所有架构图的图片
        if (minioService != null) {
            LambdaQueryWrapper<Diagram> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Diagram::getRepositoryId, repositoryId);
            List<Diagram> diagrams = diagramMapper.selectList(wrapper);
            
            for (Diagram diagram : diagrams) {
                if (diagram.getImageUrl() != null && !diagram.getImageUrl().isEmpty()) {
                    try {
                        String fileName = diagram.getImageUrl().substring(diagram.getImageUrl().indexOf("/api/image/") + "/api/image/".length());
                        minioService.deleteFile(fileName);
                        log.info("已删除架构图图片: {}", fileName);
                    } catch (Exception e) {
                        log.warn("删除架构图图片失败", e);
                    }
                }
            }
        }
        
        // 3. 删除架构库（级联删除架构图）
        repositoryMapper.deleteById(repositoryId);
        log.info("架构库删除成功: repositoryId={}", repositoryId);
    }
    
    /**
     * 查询架构库详情
     */
    public Repository getRepositoryById(Long repositoryId, Long userId) {
        Repository repository = repositoryMapper.selectById(repositoryId);
        
        if (repository == null) {
            throw new RuntimeException("架构库不存在");
        }
        
        if (!repository.getUserId().equals(userId)) {
            throw new RuntimeException("无权限访问此架构库");
        }
        
        return repository;
    }
    
    /**
     * 分页查询架构库列表
     */
    public IPage<Repository> listRepositories(Long userId, Integer page, Integer size, String keyword, String status) {
        log.info("查询架构库列表: userId={}, page={}, size={}, keyword={}, status={}", 
                userId, page, size, keyword, status);
        
        Page<Repository> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<Repository> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Repository::getUserId, userId)
               .like(StringUtils.hasText(keyword), Repository::getRepositoryName, keyword)
               .eq(StringUtils.hasText(status), Repository::getStatus, status)
               .orderByDesc(Repository::getUpdateTime);
        
        return repositoryMapper.selectPage(pageParam, wrapper);
    }
    
    /**
     * 统计用户架构库数量
     */
    public long countUserRepositories(Long userId) {
        LambdaQueryWrapper<Repository> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Repository::getUserId, userId);
        return repositoryMapper.selectCount(wrapper);
    }
}
