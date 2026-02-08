package com.coffeeviz.core.renderer;

import com.coffeeviz.core.enums.RelationType;
import com.coffeeviz.core.enums.ViewMode;
import com.coffeeviz.core.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mermaid ER 图渲染器
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class MermaidErRenderer implements MermaidRenderer {
    
    @Override
    public String render(DatabaseModel model, RenderOptions options) {
        log.info("开始渲染 Mermaid ER 图，视图模式：{}", options.getViewMode());
        
        StringBuilder sb = new StringBuilder();
        sb.append("erDiagram\n");
        
        // 1. 过滤表
        List<TableModel> tables = filterTables(model.getTables(), options);
        
        // 2. 渲染表定义
        for (TableModel table : tables) {
            renderTable(sb, table, options);
        }
        
        // 3. 渲染关系
        for (TableModel table : tables) {
            renderRelations(sb, table, options);
        }
        
        log.info("Mermaid ER 图渲染完成，共 {} 张表", tables.size());
        return sb.toString();
    }
    
    /**
     * 过滤表
     */
    private List<TableModel> filterTables(List<TableModel> tables, RenderOptions options) {
        return tables.stream()
                .filter(table -> {
                    // 包含表过滤
                    if (options.getIncludeTables() != null && !options.getIncludeTables().isEmpty()) {
                        return options.getIncludeTables().contains(table.getName());
                    }
                    // 排除表过滤
                    if (options.getExcludeTables() != null) {
                        return !options.getExcludeTables().contains(table.getName());
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 渲染表定义
     */
    private void renderTable(StringBuilder sb, TableModel table, RenderOptions options) {
        sb.append("    ").append(table.getName()).append(" {\n");
        
        List<ColumnModel> columns = filterColumns(table.getColumns(), options);
        
        for (ColumnModel column : columns) {
            sb.append("        ");
            
            // 完整的数据类型（包含长度/精度）
            String fullType = buildFullType(column);
            sb.append(fullType).append(" ");
            
            sb.append(column.getName());
            
            // 主键标记（使用 PK 关键字）
            // 注意：如果字段既是主键又是外键，只显示 PK（Mermaid 语法限制）
            if (column.isPrimaryKeyPart()) {
                sb.append(" PK");
            }
            // 外键标记（使用 FK 关键字）- 仅当不是主键时才添加
            else if (isForeignKeyColumn(table, column.getName())) {
                sb.append(" FK");
            }
            
            // 构建注释内容（合并约束信息和字段注释）
            String commentText = buildCommentText(column, options);
            if (commentText != null && !commentText.isEmpty()) {
                sb.append(" \"").append(commentText).append("\"");
            }
            
            sb.append("\n");
        }
        
        sb.append("    }\n");
    }
    
    /**
     * 构建注释文本（合并约束信息和字段注释）
     */
    private String buildCommentText(ColumnModel column, RenderOptions options) {
        StringBuilder comment = new StringBuilder();
        
        // 添加约束信息
        if (!column.isNullable() && !column.isPrimaryKeyPart()) {
            comment.append("NOT NULL");
        }
        
        // 添加字段注释
        if (column.getComment() != null && !column.getComment().isEmpty() && options.isShowComments()) {
            if (comment.length() > 0) {
                comment.append(", ");
            }
            comment.append(escapeComment(column.getComment()));
        }
        
        return comment.toString();
    }
    
    /**
     * 构建完整的数据类型（包含长度/精度）
     * 注意：Mermaid ER 图语法对类型定义有限制，需要进行转换
     */
    private String buildFullType(ColumnModel column) {
        String rawType = column.getRawType() != null ? column.getRawType() : column.getType();
        
        // 如果已经包含长度信息，直接返回（但需要处理特殊字符）
        if (column.getType() != null && column.getType().contains("(")) {
            // 移除空格并替换逗号为下划线，避免 Mermaid 解析错误
            return column.getType()
                    .replaceAll("\\s+", "")  // 移除所有空格
                    .replace(",", "_");       // 替换逗号为下划线
        }
        
        // 根据长度/精度构建完整类型
        if (column.getPrecision() != null && column.getScale() != null) {
            // DECIMAL(10_2) 类型 - 使用下划线代替逗号
            return rawType + "(" + column.getPrecision() + "_" + column.getScale() + ")";
        } else if (column.getLength() != null && column.getLength() > 0) {
            // VARCHAR(255) 类型
            return rawType + "(" + column.getLength() + ")";
        }
        
        return rawType;
    }
    
    /**
     * 判断是否为外键列
     */
    private boolean isForeignKeyColumn(TableModel table, String columnName) {
        return table.getForeignKeys().stream()
                .anyMatch(fk -> fk.getFromColumns().contains(columnName));
    }
    
    /**
     * 转义注释中的特殊字符
     * Mermaid ER 图对注释内容有严格限制，需要移除或转义特殊字符
     */
    private String escapeComment(String comment) {
        if (comment == null || comment.isEmpty()) {
            return "";
        }
        
        return comment
                .replace("\"", "'")      // 双引号替换为单引号
                .replace("\n", " ")      // 换行符替换为空格
                .replace("\r", "")       // 移除回车符
                .replace("\t", " ")      // 制表符替换为空格
                .replaceAll("\\s+", " ") // 多个空格合并为一个
                .trim();
    }
    
    /**
     * 过滤列
     */
    private List<ColumnModel> filterColumns(List<ColumnModel> columns, RenderOptions options) {
        ViewMode viewMode = options.getViewMode();
        
        if (viewMode == ViewMode.CONCEPTUAL) {
            // 概念视图：不显示任何列
            return List.of();
        } else if (viewMode == ViewMode.LOGICAL) {
            // 逻辑视图：仅显示主键和外键
            return columns.stream()
                    .filter(col -> col.isPrimaryKeyPart() || col.getName().endsWith("_id"))
                    .collect(Collectors.toList());
        } else {
            // 物理视图：显示所有列
            return columns;
        }
    }
    
    /**
     * 渲染关系
     */
    private void renderRelations(StringBuilder sb, TableModel table, RenderOptions options) {
        for (ForeignKeyModel fk : table.getForeignKeys()) {
            String relationship = getRelationshipSymbol(fk.getRelationType());
            
            sb.append("    ");
            sb.append(fk.getToTable());
            sb.append(" ");
            sb.append(relationship);
            sb.append(" ");
            sb.append(fk.getFromTable());
            sb.append(" : \"");
            sb.append(String.join(",", fk.getFromColumns()));
            sb.append("\"\n");
        }
    }
    
    /**
     * 获取关系符号
     */
    private String getRelationshipSymbol(RelationType type) {
        if (type == null) {
            return "||--o{";
        }
        
        return switch (type) {
            case ONE_TO_ONE -> "||--||";
            case ONE_TO_MANY -> "||--o{";
            case MANY_TO_MANY -> "}o--o{";
        };
    }
}
