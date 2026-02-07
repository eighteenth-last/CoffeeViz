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
            sb.append(column.getType()).append(" ");
            sb.append(column.getName());
            
            // 主键标记
            if (column.isPrimaryKeyPart()) {
                sb.append(" PK");
            }
            
            // 非空标记
            if (!column.isNullable()) {
                sb.append(" NOT NULL");
            }
            
            // 注释
            if (column.getComment() != null && options.isShowComments()) {
                sb.append(" \"").append(column.getComment()).append("\"");
            }
            
            sb.append("\n");
        }
        
        sb.append("    }\n");
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
