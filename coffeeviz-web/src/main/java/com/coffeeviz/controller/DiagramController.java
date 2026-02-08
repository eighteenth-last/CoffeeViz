package com.coffeeviz.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.coffeeviz.common.Result;
import com.coffeeviz.dto.DiagramCreateRequest;
import com.coffeeviz.entity.Diagram;
import com.coffeeviz.service.DiagramService;
import com.coffeeviz.service.MinioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 架构图管理 Controller
 * 
 * @author CoffeeViz Team
 * @since 2.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/diagram")
public class DiagramController {
    
    @Autowired
    private DiagramService diagramService;
    
    @Autowired(required = false)
    private MinioService minioService;
    
    /**
     * 创建架构图
     */
    @PostMapping("/create")
    public Result<Long> createDiagram(@RequestBody DiagramCreateRequest request) {
        log.info("创建架构图: diagramName={}, repositoryId={}", 
                request.getDiagramName(), request.getRepositoryId());
        
        try {
            // 1. 参数校验
            if (request.getRepositoryId() == null) {
                return Result.error(400, "架构库 ID 不能为空");
            }
            if (request.getDiagramName() == null || request.getDiagramName().trim().isEmpty()) {
                return Result.error(400, "架构图名称不能为空");
            }
            
            // 2. 获取当前用户 ID
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 3. 处理 PNG Base64 数据并上传到 MinIO
            String imageUrl = null;
            if (minioService != null && request.getPngBase64() != null && !request.getPngBase64().isEmpty()) {
                try {
                    // 移除 Base64 前缀
                    String base64Data = request.getPngBase64();
                    if (base64Data.startsWith("data:image/png;base64,")) {
                        base64Data = base64Data.substring("data:image/png;base64,".length());
                    }
                    
                    // 解码 Base64
                    byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
                    
                    // 生成文件名: diagrams/repositoryId/timestamp_diagramName.png
                    String fileName = "diagrams/" + request.getRepositoryId() + "/" + 
                        System.currentTimeMillis() + "_" + 
                        request.getDiagramName().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_") + ".png";
                    
                    // 上传到 MinIO
                    imageUrl = minioService.uploadFile(
                        new java.io.ByteArrayInputStream(imageBytes), 
                        fileName, 
                        "image/png", 
                        imageBytes.length
                    );
                    
                    log.info("PNG 图片已上传到 MinIO: {}", imageUrl);
                } catch (Exception e) {
                    log.error("上传 PNG 到 MinIO 失败，继续创建架构图", e);
                }
            }
            
            // 4. 构建架构图对象
            Diagram diagram = new Diagram();
            diagram.setRepositoryId(request.getRepositoryId());
            diagram.setDiagramName(request.getDiagramName());
            diagram.setDescription(request.getDescription());
            diagram.setSourceType(request.getSourceType() != null ? request.getSourceType() : "SQL");
            diagram.setDbType(request.getDbType());
            diagram.setMermaidCode(request.getMermaidCode());
            diagram.setImageUrl(imageUrl);
            diagram.setTableCount(request.getTableCount() != null ? request.getTableCount() : 0);
            diagram.setRelationCount(request.getRelationCount() != null ? request.getRelationCount() : 0);
            
            // 5. 创建架构图
            Long diagramId = diagramService.createDiagram(diagram, userId);
            
            log.info("架构图创建成功: diagramId={}", diagramId);
            return Result.success("架构图创建成功", diagramId);
            
        } catch (Exception e) {
            log.error("架构图创建失败", e);
            return Result.error("架构图创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询架构库下的架构图列表
     */
    @GetMapping("/list/{repositoryId}")
    public Result<List<Diagram>> listDiagrams(@PathVariable("repositoryId") Long repositoryId) {
        log.info("查询架构图列表: repositoryId={}", repositoryId);
        
        try {
            // 1. 获取当前用户 ID
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 2. 查询架构图列表
            List<Diagram> diagrams = diagramService.listDiagramsByRepository(repositoryId, userId);
            
            return Result.success(diagrams);
            
        } catch (Exception e) {
            log.error("查询架构图列表失败", e);
            return Result.error("查询架构图列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询架构图详情
     */
    @GetMapping("/detail/{diagramId}")
    public Result<Diagram> getDiagramDetail(@PathVariable("diagramId") Long diagramId) {
        log.info("查询架构图详情: diagramId={}", diagramId);
        
        try {
            // 1. 获取当前用户 ID
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 2. 查询架构图
            Diagram diagram = diagramService.getDiagramById(diagramId, userId);
            
            return Result.success(diagram);
            
        } catch (Exception e) {
            log.error("查询架构图详情失败", e);
            return Result.error("查询架构图详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除架构图
     */
    @DeleteMapping("/delete/{diagramId}")
    public Result<String> deleteDiagram(@PathVariable("diagramId") Long diagramId) {
        log.info("删除架构图: diagramId={}", diagramId);
        
        try {
            // 1. 获取当前用户 ID
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 2. 删除架构图
            diagramService.deleteDiagram(diagramId, userId);
            
            log.info("架构图删除成功: diagramId={}", diagramId);
            return Result.success("架构图删除成功", "success");
            
        } catch (Exception e) {
            log.error("架构图删除失败", e);
            return Result.error("架构图删除失败: " + e.getMessage());
        }
    }
}
