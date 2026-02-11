package com.coffeeviz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coffeeviz.entity.Diagram;
import com.coffeeviz.entity.Repository;
import com.coffeeviz.entity.Team;
import com.coffeeviz.mapper.DiagramMapper;
import com.coffeeviz.mapper.RepositoryMapper;
import com.coffeeviz.mapper.TeamMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 架构图管理服务
 * 
 * @author CoffeeViz Team
 * @since 2.0.0
 */
@Slf4j
@Service
public class DiagramService {
    
    @Autowired
    private DiagramMapper diagramMapper;
    
    @Autowired
    private RepositoryMapper repositoryMapper;
    
    @Autowired
    private TeamMapper teamMapper;
    
    @Autowired(required = false)
    private MinioService minioService;
    
    /**
     * 检查用户是否有权限访问指定架构库（自己的或团队的）
     */
    private boolean hasRepositoryAccess(Repository repository, Long userId) {
        if (repository.getUserId().equals(userId)) {
            return true;
        }
        // 检查是否为团队架构库且用户是团队成员
        if (Boolean.TRUE.equals(repository.getIsTeamRepository())) {
            List<Team> teams = teamMapper.selectUserTeams(userId);
            if (teams != null) {
                return teams.stream()
                        .anyMatch(t -> repository.getId().equals(t.getRepositoryId()));
            }
        }
        return false;
    }
    
    /**
     * 创建架构图
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createDiagram(Diagram diagram, Long userId) {
        log.info("创建架构图: {}, repositoryId: {}", diagram.getDiagramName(), diagram.getRepositoryId());
        
        // 1. 校验架构库访问权限（自己的或团队的）
        Repository repository = repositoryMapper.selectById(diagram.getRepositoryId());
        if (repository == null) {
            throw new RuntimeException("架构库不存在");
        }
        if (!hasRepositoryAccess(repository, userId)) {
            throw new RuntimeException("无权限在此架构库中创建架构图");
        }
        
        // 2. 插入架构图
        diagramMapper.insert(diagram);
        log.info("架构图创建成功: diagramId={}", diagram.getId());
        
        return diagram.getId();
    }
    
    /**
     * 更新架构图
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDiagram(Diagram diagram, Long userId) {
        log.info("更新架构图: diagramId={}", diagram.getId());
        
        // 1. 查询原架构图
        Diagram existingDiagram = diagramMapper.selectById(diagram.getId());
        if (existingDiagram == null) {
            throw new RuntimeException("架构图不存在");
        }
        
        // 2. 校验所有权
        Repository repository = repositoryMapper.selectById(existingDiagram.getRepositoryId());
        if (repository == null || !hasRepositoryAccess(repository, userId)) {
            throw new RuntimeException("无权限更新此架构图");
        }
        
        // 3. 更新架构图
        diagramMapper.updateById(diagram);
    }
    
    /**
     * 删除架构图
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDiagram(Long diagramId, Long userId) {
        log.info("删除架构图: diagramId={}", diagramId);
        
        // 1. 查询架构图
        Diagram diagram = diagramMapper.selectById(diagramId);
        if (diagram == null) {
            throw new RuntimeException("架构图不存在");
        }
        
        // 2. 校验所有权
        Repository repository = repositoryMapper.selectById(diagram.getRepositoryId());
        if (repository == null || !hasRepositoryAccess(repository, userId)) {
            throw new RuntimeException("无权限删除此架构图");
        }
        
        // 3. 删除 MinIO 图片
        if (minioService != null && diagram.getImageUrl() != null && !diagram.getImageUrl().isEmpty()) {
            try {
                String fileName = diagram.getImageUrl().substring(diagram.getImageUrl().indexOf("/api/image/") + "/api/image/".length());
                minioService.deleteFile(fileName);
                log.info("已删除架构图图片: {}", fileName);
            } catch (Exception e) {
                log.warn("删除架构图图片失败", e);
            }
        }
        
        // 4. 删除架构图
        diagramMapper.deleteById(diagramId);
        log.info("架构图删除成功: diagramId={}", diagramId);
    }
    
    /**
     * 查询架构图详情
     */
    public Diagram getDiagramById(Long diagramId, Long userId) {
        Diagram diagram = diagramMapper.selectById(diagramId);
        
        if (diagram == null) {
            throw new RuntimeException("架构图不存在");
        }
        
        // 校验所有权
        Repository repository = repositoryMapper.selectById(diagram.getRepositoryId());
        if (repository == null || !hasRepositoryAccess(repository, userId)) {
            throw new RuntimeException("无权限访问此架构图");
        }
        
        return diagram;
    }
    
    /**
     * 查询架构库下的所有架构图
     */
    public List<Diagram> listDiagramsByRepository(Long repositoryId, Long userId) {
        log.info("查询架构库下的架构图列表: repositoryId={}", repositoryId);
        
        // 1. 校验访问权限
        Repository repository = repositoryMapper.selectById(repositoryId);
        if (repository == null) {
            throw new RuntimeException("架构库不存在");
        }
        if (!hasRepositoryAccess(repository, userId)) {
            throw new RuntimeException("无权限访问此架构库");
        }
        
        // 2. 查询架构图列表
        LambdaQueryWrapper<Diagram> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Diagram::getRepositoryId, repositoryId)
               .orderByDesc(Diagram::getUpdateTime);
        
        return diagramMapper.selectList(wrapper);
    }
    
    /**
     * 统计架构库下的架构图数量
     */
    public long countDiagramsByRepository(Long repositoryId) {
        LambdaQueryWrapper<Diagram> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Diagram::getRepositoryId, repositoryId);
        return diagramMapper.selectCount(wrapper);
    }
    
    /**
     * 统计用户所有架构图的表总数
     */
    public Long sumTableCountByUserId(Long userId) {
        return diagramMapper.sumTableCountByUserId(userId);
    }
    
    /**
     * 统计用户所有架构图的关系总数
     */
    public Long sumRelationCountByUserId(Long userId) {
        return diagramMapper.sumRelationCountByUserId(userId);
    }
}
