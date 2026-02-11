package com.coffeeviz.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.coffeeviz.core.model.ParseResult;
import com.coffeeviz.core.model.TableModel;
import com.coffeeviz.llm.service.OpenAiService;
import com.coffeeviz.llm.service.impl.OpenAiServiceImpl;
import com.coffeeviz.sql.parser.SqlParser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 系统功能结构图生成服务
 *
 * <p>生成层级树状功能结构图（Functional Breakdown Structure）：</p>
 * <pre>
 *   系统 → 子系统 → 模块 → 功能
 * </pre>
 *
 * <p>混合架构：结构化数据用规则，非结构化数据用 AI</p>
 *
 * @author CoffeeViz Team
 * @since 1.3.0
 */
@Slf4j
@Service
public class ArchitectureService {

    @Autowired
    private SqlParser sqlParser;

    @Autowired(required = false)
    private OpenAiService openAiService;

    @Autowired(required = false)
    private ConfigService configService;

    // ==================== 数据模型：树形节点 ====================

    /**
     * 树形节点（系统 → 子系统 → 模块 → 功能）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TreeNode implements Serializable {
        private String id;
        private String name;
        /** 层级: 0=系统, 1=子系统, 2=模块, 3=功能 */
        private int level;
        @Builder.Default
        private List<TreeNode> children = new ArrayList<>();
    }

    /**
     * 生成结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArchResult implements Serializable {
        private boolean success;
        private String message;
        /** 树形根节点 */
        private TreeNode tree;
        /** Mermaid graph TD 代码 */
        private String mermaidCode;
        private List<String> warnings;
        /** rule / ai / hybrid */
        private String extractMethod;
        /** 总节点数 */
        private int nodeCount;
    }

    // ==================== 核心入口 ====================

    /**
     * 从 DDL 提取功能结构树（零 AI）
     */
    public ArchResult generateFromDdl(String sqlText) {
        log.info("从 DDL 提取功能结构，SQL 长度: {}", sqlText.length());
        List<String> warnings = new ArrayList<>();

        try {
            ParseResult parseResult = sqlParser.parse(sqlText, "auto");
            if (!parseResult.isSuccess()) {
                return ArchResult.builder().success(false)
                        .message("SQL 解析失败: " + parseResult.getMessage()).build();
            }

            List<TableModel> tables = parseResult.getDatabaseModel().getTables();
            if (tables.isEmpty()) {
                return ArchResult.builder().success(false).message("未解析到任何表").build();
            }

            // 按前缀分组 → 构建树
            TreeNode root = buildTreeFromTables(tables);
            String mermaidCode = renderTreeToMermaid(root);
            int nodeCount = countNodes(root);

            warnings.addAll(parseResult.getWarnings());
            warnings.add(0, "使用 DDL 规则提取（零 AI），共 " + tables.size() + " 张表");

            return ArchResult.builder()
                    .success(true).message("生成成功")
                    .tree(root).mermaidCode(mermaidCode)
                    .warnings(warnings).extractMethod("rule")
                    .nodeCount(nodeCount)
                    .build();

        } catch (Exception e) {
            log.error("DDL 架构提取失败", e);
            return ArchResult.builder().success(false)
                    .message("提取失败: " + e.getMessage()).build();
        }
    }

    /**
     * 从文档提取功能结构树（规则优先，降级 AI）
     */
    public ArchResult generateFromDocument(String docContent, boolean forceAi) {
        log.info("从文档提取功能结构，文档长度: {}, 强制AI: {}", docContent.length(), forceAi);
        List<String> warnings = new ArrayList<>();

        if (!forceAi) {
            // Layer 1: 规则提取
            TreeNode ruleTree = extractTreeByRules(docContent);
            if (ruleTree != null && !ruleTree.getChildren().isEmpty()
                    && ruleTree.getChildren().stream().anyMatch(c -> !c.getChildren().isEmpty())) {
                log.info("规则提取成功，子系统数: {}", ruleTree.getChildren().size());
                String mermaidCode = renderTreeToMermaid(ruleTree);
                warnings.add("使用规则提取（零 AI）");
                return ArchResult.builder()
                        .success(true).message("生成成功")
                        .tree(ruleTree).mermaidCode(mermaidCode)
                        .warnings(warnings).extractMethod("rule")
                        .nodeCount(countNodes(ruleTree))
                        .build();
            }
            warnings.add("规则提取结果不足，降级到 AI 提取");
        }

        // Layer 2: AI 提取
        return generateFromDocumentWithAi(docContent, warnings);
    }

    /**
     * 混合模式：DDL + 文档
     */
    public ArchResult generateHybrid(String sqlText, String docContent) {
        log.info("混合模式，SQL长度: {}, 文档长度: {}",
                sqlText != null ? sqlText.length() : 0,
                docContent != null ? docContent.length() : 0);

        List<String> warnings = new ArrayList<>();
        TreeNode root = TreeNode.builder().id("root").name("系统").level(0)
                .children(new ArrayList<>()).build();

        // 1. DDL 部分
        if (sqlText != null && !sqlText.trim().isEmpty()) {
            ArchResult ddlResult = generateFromDdl(sqlText);
            if (ddlResult.isSuccess() && ddlResult.getTree() != null) {
                root.setName(ddlResult.getTree().getName());
                root.getChildren().addAll(ddlResult.getTree().getChildren());
                warnings.addAll(ddlResult.getWarnings());
            }
        }

        // 2. 文档部分
        if (docContent != null && !docContent.trim().isEmpty()) {
            ArchResult docResult = generateFromDocument(docContent, false);
            if (docResult.isSuccess() && docResult.getTree() != null) {
                mergeTree(root, docResult.getTree());
                warnings.addAll(docResult.getWarnings());
            }
        }

        if (root.getChildren().isEmpty()) {
            return ArchResult.builder().success(false).message("未能提取到任何架构信息").build();
        }

        String mermaidCode = renderTreeToMermaid(root);
        return ArchResult.builder()
                .success(true).message("生成成功")
                .tree(root).mermaidCode(mermaidCode)
                .warnings(warnings).extractMethod("hybrid")
                .nodeCount(countNodes(root))
                .build();
    }

    // ==================== Layer 1: DDL → 树 ====================

    private TreeNode buildTreeFromTables(List<TableModel> tables) {
        // 按前缀分组
        Map<String, List<TableModel>> groups = new LinkedHashMap<>();
        for (TableModel t : tables) {
            String prefix = extractPrefix(t.getName());
            groups.computeIfAbsent(prefix, k -> new ArrayList<>()).add(t);
        }

        // 推断系统名称
        String systemName = "数据库系统";

        TreeNode root = TreeNode.builder()
                .id("root").name(systemName).level(0)
                .children(new ArrayList<>()).build();

        for (Map.Entry<String, List<TableModel>> entry : groups.entrySet()) {
            String prefix = entry.getKey();
            List<TableModel> groupTables = entry.getValue();
            String subsystemName = prefixToSubsystemName(prefix);

            TreeNode subsystem = TreeNode.builder()
                    .id("sub_" + prefix).name(subsystemName).level(1)
                    .children(new ArrayList<>()).build();

            for (TableModel t : groupTables) {
                // 用表注释作为功能名，没有注释则用表名
                String funcName = (t.getComment() != null && !t.getComment().isEmpty())
                        ? t.getComment() : t.getName();
                subsystem.getChildren().add(TreeNode.builder()
                        .id("tbl_" + t.getName()).name(funcName).level(2)
                        .children(new ArrayList<>()).build());
            }

            root.getChildren().add(subsystem);
        }

        return root;
    }

    // ==================== Layer 1: 文档规则 → 树 ====================

    private TreeNode extractTreeByRules(String doc) {
        TreeNode root = TreeNode.builder()
                .id("root").name("系统").level(0)
                .children(new ArrayList<>()).build();

        // 尝试从一级标题提取系统名
        Matcher h1 = Pattern.compile("^#\\s+(.+)$", Pattern.MULTILINE).matcher(doc);
        if (h1.find()) {
            root.setName(h1.group(1).trim());
        }

        // 从二级标题提取子系统
        Pattern h2Pattern = Pattern.compile("^##\\s+(.+)$", Pattern.MULTILINE);
        Pattern h3Pattern = Pattern.compile("^###\\s+(.+)$", Pattern.MULTILINE);

        // 按二级标题分段
        String[] sections = doc.split("(?=^##\\s+)", Pattern.MULTILINE);

        for (String section : sections) {
            Matcher h2 = h2Pattern.matcher(section);
            if (!h2.find()) continue;

            String subsystemName = h2.group(1).trim();
            // 过滤掉非功能性标题
            if (isNonFunctionalHeading(subsystemName)) continue;

            TreeNode subsystem = TreeNode.builder()
                    .id(toId(subsystemName)).name(subsystemName).level(1)
                    .children(new ArrayList<>()).build();

            // 从三级标题提取模块
            Matcher h3 = h3Pattern.matcher(section);
            while (h3.find()) {
                String moduleName = h3.group(1).trim();
                if (!isNonFunctionalHeading(moduleName)) {
                    subsystem.getChildren().add(TreeNode.builder()
                            .id(toId(moduleName)).name(moduleName).level(2)
                            .children(new ArrayList<>()).build());
                }
            }

            // 从列表项提取功能（如果没有三级标题）
            if (subsystem.getChildren().isEmpty()) {
                Pattern listPattern = Pattern.compile("^[-*]\\s+(.+)$", Pattern.MULTILINE);
                Matcher listMatcher = listPattern.matcher(section);
                while (listMatcher.find()) {
                    String item = listMatcher.group(1).trim();
                    if (item.length() > 1 && item.length() < 50) {
                        subsystem.getChildren().add(TreeNode.builder()
                                .id(toId(item)).name(item).level(2)
                                .children(new ArrayList<>()).build());
                    }
                }
            }

            if (!subsystem.getChildren().isEmpty()) {
                root.getChildren().add(subsystem);
            }
        }

        return root;
    }

    private boolean isNonFunctionalHeading(String heading) {
        String lower = heading.toLowerCase();
        return lower.contains("目录") || lower.contains("前言") || lower.contains("附录")
                || lower.contains("参考") || lower.contains("changelog") || lower.contains("更新日志")
                || lower.contains("table of contents") || lower.contains("license");
    }

    // ==================== Layer 2: AI 提取 ====================

    private ArchResult generateFromDocumentWithAi(String docContent, List<String> warnings) {
        if (openAiService == null || !openAiService.isAvailable()) {
            return ArchResult.builder().success(false)
                    .message("AI 服务不可用，且规则提取结果不足。请确保文档包含清晰的标题层级结构。")
                    .build();
        }

        try {
            loadOpenAiConfig();

            String combinedPrompt = buildArchitecturePrompt() + "\n\n---\n\n"
                    + "请分析以下项目文档，提取系统功能结构：\n\n" + truncate(docContent, 4000);

            com.coffeeviz.llm.model.AiRequest request = com.coffeeviz.llm.model.AiRequest.builder()
                    .prompt(combinedPrompt).build();

            com.coffeeviz.llm.model.AiResponse aiResp = openAiService.generateSqlFromPrompt(request);

            if (aiResp == null || !Boolean.TRUE.equals(aiResp.getSuccess())) {
                String errMsg = aiResp != null ? aiResp.getErrorMessage() : "AI 调用失败";
                return ArchResult.builder().success(false).message(errMsg).build();
            }

            // AI 返回的内容可能在 sqlDdl 或 explanation 字段
            String content = aiResp.getSqlDdl();
            if (content == null || content.isEmpty()) content = aiResp.getExplanation();
            if (content == null || content.isEmpty()) {
                return ArchResult.builder().success(false).message("AI 返回内容为空").build();
            }

            return parseAiTreeResponse(content, warnings);

        } catch (Exception e) {
            log.error("AI 架构提取失败", e);
            return ArchResult.builder().success(false)
                    .message("AI 提取失败: " + e.getMessage()).build();
        }
    }

    private String buildArchitecturePrompt() {
        return """
                你是一个专业的系统架构师。请从用户提供的项目文档中提取系统的功能结构层级。
                
                请严格按以下 JSON 格式返回（不要包含任何其他内容，只返回 JSON）：
                
                ```json
                {
                  "name": "系统名称",
                  "children": [
                    {
                      "name": "子系统A",
                      "children": [
                        { "name": "功能模块1" },
                        { "name": "功能模块2" }
                      ]
                    },
                    {
                      "name": "子系统B",
                      "children": [
                        { "name": "功能模块3" },
                        { "name": "功能模块4" }
                      ]
                    }
                  ]
                }
                ```
                
                要求：
                1. 层级结构：系统 → 子系统 → 功能模块（最多3层）
                2. 每个子系统下至少包含2个功能模块
                3. 名称简洁明了，使用中文
                4. 只提取核心功能，不要过于细化
                """;
    }

    private ArchResult parseAiTreeResponse(String content, List<String> warnings) {
        try {
            // 提取 JSON
            String json = content;
            Matcher jsonMatcher = Pattern.compile("```json\\s*\\n(.*?)\\n```", Pattern.DOTALL).matcher(content);
            if (jsonMatcher.find()) {
                json = jsonMatcher.group(1).trim();
            } else {
                // 尝试直接解析
                int start = content.indexOf('{');
                int end = content.lastIndexOf('}');
                if (start >= 0 && end > start) {
                    json = content.substring(start, end + 1);
                }
            }

            JSONObject obj = JSON.parseObject(json);
            TreeNode root = parseJsonToTree(obj, 0);

            warnings.add("使用 AI 提取功能结构");
            String mermaidCode = renderTreeToMermaid(root);

            return ArchResult.builder()
                    .success(true).message("生成成功")
                    .tree(root).mermaidCode(mermaidCode)
                    .warnings(warnings).extractMethod("ai")
                    .nodeCount(countNodes(root))
                    .build();

        } catch (Exception e) {
            log.error("解析 AI 响应失败: {}", e.getMessage());
            return ArchResult.builder().success(false)
                    .message("AI 响应解析失败: " + e.getMessage()).build();
        }
    }

    private TreeNode parseJsonToTree(JSONObject obj, int level) {
        String name = obj.getString("name");
        if (name == null) name = "未命名";

        TreeNode node = TreeNode.builder()
                .id(toId(name) + "_" + level)
                .name(name).level(level)
                .children(new ArrayList<>()).build();

        JSONArray children = obj.getJSONArray("children");
        if (children != null) {
            for (int i = 0; i < children.size(); i++) {
                Object child = children.get(i);
                if (child instanceof JSONObject childObj) {
                    node.getChildren().add(parseJsonToTree(childObj, level + 1));
                } else if (child instanceof String childStr) {
                    node.getChildren().add(TreeNode.builder()
                            .id(toId(childStr) + "_" + (level + 1))
                            .name(childStr).level(level + 1)
                            .children(new ArrayList<>()).build());
                }
            }
        }

        return node;
    }

    // ==================== Mermaid 渲染：树状结构图 ====================

    /**
     * 将树形结构渲染为 Mermaid graph TD（自上而下的层级图）
     * 生成类似组织结构图的效果：白底黑框、直线连接
     */
    private String renderTreeToMermaid(TreeNode root) {
        StringBuilder sb = new StringBuilder();
        sb.append("graph TD\n");

        AtomicInteger idCounter = new AtomicInteger(0);
        Map<TreeNode, String> nodeIds = new HashMap<>();

        // 分配唯一 ID 并定义节点
        assignIds(root, nodeIds, idCounter);

        // 定义所有节点
        renderNodeDefinitions(sb, root, nodeIds);

        sb.append("\n");

        // 定义所有边（父→子）
        renderEdges(sb, root, nodeIds);

        sb.append("\n");

        // 统一样式：白底黑框方框风格
        sb.append("    classDef default fill:#fff,stroke:#333,stroke-width:1px,color:#333\n");
        sb.append("    classDef root fill:#f0f0f0,stroke:#333,stroke-width:2px,color:#333,font-weight:bold\n");
        sb.append("    classDef sub fill:#fff,stroke:#333,stroke-width:1.5px,color:#333,font-weight:bold\n");
        sb.append("    classDef leaf fill:#fff,stroke:#666,stroke-width:1px,color:#555\n");

        // 应用样式
        String rootId = nodeIds.get(root);
        sb.append("    class ").append(rootId).append(" root\n");

        for (TreeNode child : root.getChildren()) {
            sb.append("    class ").append(nodeIds.get(child)).append(" sub\n");
            for (TreeNode grandChild : child.getChildren()) {
                sb.append("    class ").append(nodeIds.get(grandChild)).append(" leaf\n");
                // 第四层
                for (TreeNode gc : grandChild.getChildren()) {
                    sb.append("    class ").append(nodeIds.get(gc)).append(" leaf\n");
                }
            }
        }

        return sb.toString();
    }

    private void assignIds(TreeNode node, Map<TreeNode, String> nodeIds, AtomicInteger counter) {
        nodeIds.put(node, "n" + counter.getAndIncrement());
        for (TreeNode child : node.getChildren()) {
            assignIds(child, nodeIds, counter);
        }
    }

    private void renderNodeDefinitions(StringBuilder sb, TreeNode node, Map<TreeNode, String> nodeIds) {
        String id = nodeIds.get(node);
        // 使用方括号 [] 表示矩形方框
        sb.append("    ").append(id).append("[\"").append(escapeMermaid(node.getName())).append("\"]\n");

        for (TreeNode child : node.getChildren()) {
            renderNodeDefinitions(sb, child, nodeIds);
        }
    }

    private void renderEdges(StringBuilder sb, TreeNode node, Map<TreeNode, String> nodeIds) {
        String parentId = nodeIds.get(node);
        for (TreeNode child : node.getChildren()) {
            String childId = nodeIds.get(child);
            sb.append("    ").append(parentId).append(" --> ").append(childId).append("\n");
            renderEdges(sb, child, nodeIds);
        }
    }

    private String escapeMermaid(String text) {
        if (text == null) return "";
        return text.replace("\"", "'").replace("\n", " ").replace("\r", "");
    }

    // ==================== 工具方法 ====================

    private String extractPrefix(String tableName) {
        int idx = tableName.indexOf('_');
        if (idx > 0 && idx < 6) {
            return tableName.substring(0, idx);
        }
        return "other";
    }

    private String prefixToSubsystemName(String prefix) {
        return switch (prefix.toLowerCase()) {
            case "sys" -> "系统管理子系统";
            case "biz" -> "业务管理子系统";
            case "ums", "user" -> "用户管理子系统";
            case "oms", "order", "ord" -> "订单管理子系统";
            case "pms", "product" -> "商品管理子系统";
            case "cms" -> "内容管理子系统";
            case "pay", "payment" -> "支付管理子系统";
            case "log" -> "日志管理子系统";
            case "msg", "sms" -> "消息管理子系统";
            case "act", "wf" -> "工作流子系统";
            case "rpt", "report" -> "报表管理子系统";
            default -> prefix.toUpperCase() + " 管理子系统";
        };
    }

    private String toId(String name) {
        return name.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_")
                .replaceAll("_+", "_").toLowerCase();
    }

    private int countNodes(TreeNode node) {
        if (node == null) return 0;
        int count = 1;
        for (TreeNode child : node.getChildren()) {
            count += countNodes(child);
        }
        return count;
    }

    private void mergeTree(TreeNode target, TreeNode source) {
        Set<String> existingNames = target.getChildren().stream()
                .map(TreeNode::getName).collect(Collectors.toSet());
        for (TreeNode child : source.getChildren()) {
            if (!existingNames.contains(child.getName())) {
                target.getChildren().add(child);
            }
        }
    }

    private String truncate(String text, int maxLen) {
        return text.length() > maxLen ? text.substring(0, maxLen) + "\n...(已截断)" : text;
    }

    private void loadOpenAiConfig() {
        if (configService == null) return;
        try {
            String apiKey = configService.getConfig("openai.api.key");
            String baseUrl = configService.getConfig("openai.api.base_url");
            String model = configService.getConfig("openai.model.name");
            if (openAiService instanceof OpenAiServiceImpl impl) {
                if (apiKey != null && !apiKey.isEmpty()) impl.setRuntimeApiKey(apiKey);
                if (baseUrl != null && !baseUrl.isEmpty()) impl.setRuntimeBaseUrl(baseUrl);
                if (model != null && !model.isEmpty()) impl.setRuntimeModel(model);
            }
        } catch (Exception e) {
            log.warn("加载 OpenAI 配置失败", e);
        }
    }
}
