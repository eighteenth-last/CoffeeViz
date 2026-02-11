package com.coffeeviz.controller;

import com.coffeeviz.annotation.RateLimit;
import com.coffeeviz.annotation.RequireSubscription;
import com.coffeeviz.common.Result;
import com.coffeeviz.dto.ArchitectureRequest;
import com.coffeeviz.dto.ArchitectureResponse;
import com.coffeeviz.dto.ArchitectureResponse.TreeNodeDTO;
import com.coffeeviz.service.ArchitectureService;
import com.coffeeviz.service.ArchitectureService.ArchResult;
import com.coffeeviz.service.ArchitectureService.TreeNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 系统功能结构图生成 Controller
 *
 * @author CoffeeViz Team
 * @since 1.3.0
 */
@Slf4j
@RestController
@RequestMapping("/api/architecture")
public class ArchitectureController {

    @Autowired
    private ArchitectureService architectureService;

    /**
     * 生成系统功能结构图
     */
    @PostMapping("/generate")
    @RequireSubscription
    @RateLimit(key = "arch_generate", time = 60, count = 20, limitType = RateLimit.LimitType.USER)
    public Result<ArchitectureResponse> generate(@RequestBody ArchitectureRequest request) {
        log.info("收到功能结构图生成请求，模式: {}", request.getMode());

        try {
            if (request.getMode() == null) {
                request.setMode("document");
            }

            ArchResult result;

            switch (request.getMode()) {
                case "ddl" -> {
                    if (request.getSqlText() == null || request.getSqlText().trim().isEmpty()) {
                        return Result.error(400, "DDL 模式下 SQL 文本不能为空");
                    }
                    if (request.getSqlText().length() > 1_000_000) {
                        return Result.error(400, "SQL 文本过长，最大支持 1MB");
                    }
                    result = architectureService.generateFromDdl(request.getSqlText());
                }
                case "document" -> {
                    if (request.getDocContent() == null || request.getDocContent().trim().isEmpty()) {
                        return Result.error(400, "文档模式下文档内容不能为空");
                    }
                    if (request.getDocContent().length() > 500_000) {
                        return Result.error(400, "文档内容过长，最大支持 500KB");
                    }
                    result = architectureService.generateFromDocument(
                            request.getDocContent(),
                            Boolean.TRUE.equals(request.getForceAi()));
                }
                case "hybrid" -> {
                    if ((request.getSqlText() == null || request.getSqlText().trim().isEmpty())
                            && (request.getDocContent() == null || request.getDocContent().trim().isEmpty())) {
                        return Result.error(400, "混合模式下至少需要提供 SQL 或文档内容");
                    }
                    result = architectureService.generateHybrid(request.getSqlText(), request.getDocContent());
                }
                default -> {
                    return Result.error(400, "不支持的模式: " + request.getMode());
                }
            }

            if (!result.isSuccess()) {
                return Result.error(500, result.getMessage());
            }

            // 构建响应
            ArchitectureResponse response = new ArchitectureResponse();
            response.setMermaidCode(result.getMermaidCode());
            response.setWarnings(result.getWarnings());
            response.setExtractMethod(result.getExtractMethod());
            response.setNodeCount(result.getNodeCount());

            if (result.getTree() != null) {
                response.setTree(convertToDTO(result.getTree()));
            }

            log.info("功能结构图生成成功，节点数: {}, 方式: {}",
                    response.getNodeCount(), response.getExtractMethod());

            return Result.success("生成成功", response);

        } catch (Exception e) {
            log.error("功能结构图生成失败", e);
            return Result.error("生成失败: " + e.getMessage());
        }
    }

    private TreeNodeDTO convertToDTO(TreeNode node) {
        TreeNodeDTO dto = new TreeNodeDTO();
        dto.setId(node.getId());
        dto.setName(node.getName());
        dto.setLevel(node.getLevel());
        dto.setChildren(node.getChildren() != null
                ? node.getChildren().stream().map(this::convertToDTO).collect(Collectors.toList())
                : new ArrayList<>());
        return dto;
    }
}
