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
import com.coffeeviz.service.QuotaService;
import com.coffeeviz.service.util.FileContentExtractor;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private QuotaService quotaService;

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

            // 按模式扣减配额
            Long userId = StpUtil.getLoginIdAsLong();
            String mode = request.getMode();
            String extractMethod = result.getExtractMethod(); // rule / ai / hybrid
            
            switch (mode) {
                case "ddl" -> {
                    // DDL 解析：sql_parse + ai_generate
                    consumeQuotaSafe(userId, "sql_parse");
                    consumeQuotaSafe(userId, "ai_generate");
                }
                case "document" -> {
                    // 项目文档：规则提取免费，AI 提取扣 ai_generate
                    if ("ai".equals(extractMethod)) {
                        consumeQuotaSafe(userId, "ai_generate");
                    }
                    // rule 模式免费，不扣配额
                }
                case "hybrid" -> {
                    // 混合模式：有 DDL 扣 sql_parse，有 AI 扣 ai_generate
                    boolean hasDdl = request.getSqlText() != null && !request.getSqlText().trim().isEmpty();
                    boolean hasDoc = request.getDocContent() != null && !request.getDocContent().trim().isEmpty();
                    if (hasDdl) {
                        consumeQuotaSafe(userId, "sql_parse");
                    }
                    if (hasDdl || hasDoc) {
                        consumeQuotaSafe(userId, "ai_generate");
                    }
                }
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

    /**
     * 上传文件生成系统功能结构图
     * <p>
     * 支持 .txt / .md / .docx / .pdf 格式，由大模型解析文件内容后生成功能结构图。
     * </p>
     *
     * @param file 上传的文档文件
     * @return 功能结构图响应
     */
    @PostMapping("/generate/upload")
    @RequireSubscription
    @RateLimit(key = "arch_generate_file", time = 60, count = 10, limitType = RateLimit.LimitType.USER)
    public Result<ArchitectureResponse> generateFromFile(@RequestParam("file") MultipartFile file) {
        String filename = file.getOriginalFilename();
        log.info("收到文件上传生成请求: filename={}, size={}KB", filename, file.getSize() / 1024);

        try {
            // 1. 校验文件
            if (file.isEmpty()) {
                return Result.error(400, "上传文件为空");
            }

            if (!FileContentExtractor.isSupported(filename)) {
                return Result.error(400, "不支持的文件格式，支持: txt, md, docx, pdf");
            }

            // 2. 提取文件文本内容
            String fileContent = FileContentExtractor.extract(file);

            if (fileContent == null || fileContent.isBlank()) {
                return Result.error(400, "文件内容为空，无法提取功能结构");
            }

            log.info("文件内容提取成功，文本长度: {}", fileContent.length());

            // 3. 调用服务生成功能结构图
            ArchResult result = architectureService.generateFromFile(fileContent, filename);

            if (!result.isSuccess()) {
                return Result.error(500, result.getMessage());
            }

            // 上传文件生成成功，扣 ai_generate 配额
            Long userId = StpUtil.getLoginIdAsLong();
            consumeQuotaSafe(userId, "ai_generate");

            // 4. 构建响应
            ArchitectureResponse response = new ArchitectureResponse();
            response.setMermaidCode(result.getMermaidCode());
            response.setWarnings(result.getWarnings());
            response.setExtractMethod(result.getExtractMethod());
            response.setNodeCount(result.getNodeCount());

            if (result.getTree() != null) {
                response.setTree(convertToDTO(result.getTree()));
            }

            log.info("文件上传生成功能结构图成功，节点数: {}, 方式: {}",
                    response.getNodeCount(), response.getExtractMethod());

            return Result.success("生成成功", response);

        } catch (IllegalArgumentException e) {
            log.warn("文件校验失败: {}", e.getMessage());
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("文件上传生成功能结构图失败", e);
            return Result.error("生成失败: " + e.getMessage());
        }
    }

    /**
     * 安全扣减配额（失败不影响主流程）
     */
    private void consumeQuotaSafe(Long userId, String quotaType) {
        try {
            boolean used = quotaService.useQuota(userId, quotaType);
            if (used) {
                log.info("扣减配额成功: userId={}, quotaType={}", userId, quotaType);
            } else {
                log.warn("扣减配额失败（可能不足）: userId={}, quotaType={}", userId, quotaType);
            }
        } catch (Exception e) {
            log.warn("扣减配额异常: userId={}, quotaType={}, error={}", userId, quotaType, e.getMessage());
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
