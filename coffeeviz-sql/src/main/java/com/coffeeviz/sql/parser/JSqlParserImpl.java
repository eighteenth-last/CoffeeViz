package com.coffeeviz.sql.parser;

import com.coffeeviz.core.model.*;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ForeignKeyIndex;
import net.sf.jsqlparser.statement.create.table.Index;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JSqlParser 实现（L1 精确解析）
 * 支持标准 SQL 语法
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class JSqlParserImpl implements SqlParser {
    
    @Override
    public ParseResult parse(String sqlText, String dialect) {
        log.info("开始使用 JSqlParser 解析 SQL，方言: {}", dialect);
        
        DatabaseModel databaseModel = new DatabaseModel();
        databaseModel.setDbType(dialect);
        databaseModel.setTables(new ArrayList<>());
        
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        try {
            // 解析 SQL 语句
            Statements statements = CCJSqlParserUtil.parseStatements(sqlText);
            
            if (statements == null || statements.getStatements() == null) {
                return ParseResult.error("无法解析 SQL 语句");
            }
            
            for (Statement statement : statements.getStatements()) {
                if (statement instanceof CreateTable) {
                    try {
                        TableModel table = parseCreateTable((CreateTable) statement, dialect);
                        databaseModel.getTables().add(table);
                    } catch (Exception e) {
                        String error = "解析表失败: " + e.getMessage();
                        log.warn(error, e);
                        warnings.add(error);
                    }
                }
            }
            
            if (databaseModel.getTables().isEmpty()) {
                return ParseResult.error("未找到任何表定义");
            }
            
            log.info("JSqlParser 解析成功，共解析 {} 张表", databaseModel.getTables().size());
            return ParseResult.success(databaseModel, warnings);
            
        } catch (Exception e) {
            String error = "JSqlParser 解析失败: " + e.getMessage();
            log.debug(error); // 降级为 DEBUG，因为这是正常的多层解析策略
            errors.add(error);
            return ParseResult.error(error);
        }
    }
    
    @Override
    public boolean supports(String dialect) {
        // JSqlParser 支持标准 SQL 和大部分方言
        return "mysql".equalsIgnoreCase(dialect) || 
               "postgres".equalsIgnoreCase(dialect) ||
               "postgresql".equalsIgnoreCase(dialect) ||
               "auto".equalsIgnoreCase(dialect);
    }
    
    /**
     * 解析 CREATE TABLE 语句
     */
    private TableModel parseCreateTable(CreateTable createTable, String dialect) {
        TableModel table = new TableModel();
        table.setName(createTable.getTable().getName());
        table.setColumns(new ArrayList<>());
        table.setForeignKeys(new ArrayList<>());
        table.setIndexes(new ArrayList<>());
        
        // 解析列定义
        if (createTable.getColumnDefinitions() != null) {
            for (ColumnDefinition colDef : createTable.getColumnDefinitions()) {
                ColumnModel column = parseColumnDefinition(colDef);
                table.getColumns().add(column);
            }
        }
        
        // 解析表级约束
        if (createTable.getIndexes() != null) {
            for (Index index : createTable.getIndexes()) {
                if (index.getType() != null && index.getType().equalsIgnoreCase("PRIMARY KEY")) {
                    // 主键
                    PrimaryKeyModel pk = new PrimaryKeyModel();
                    pk.setName(index.getName());
                    pk.setColumns(index.getColumnsNames());
                    table.setPrimaryKey(pk);
                    
                    // 标记主键列
                    for (String pkCol : pk.getColumns()) {
                        table.getColumns().stream()
                            .filter(col -> col.getName().equalsIgnoreCase(pkCol))
                            .forEach(col -> col.setPrimaryKeyPart(true));
                    }
                } else if (index instanceof ForeignKeyIndex) {
                    // 外键
                    ForeignKeyIndex fkIndex = (ForeignKeyIndex) index;
                    ForeignKeyModel fk = parseForeignKey(fkIndex, table.getName());
                    table.getForeignKeys().add(fk);
                } else {
                    // 普通索引
                    IndexModel indexModel = parseIndex(index);
                    table.getIndexes().add(indexModel);
                }
            }
        }
        
        return table;
    }
    
    /**
     * 解析列定义
     */
    private ColumnModel parseColumnDefinition(ColumnDefinition colDef) {
        ColumnModel column = new ColumnModel();
        column.setName(colDef.getColumnName());
        
        // 数据类型
        String dataType = colDef.getColDataType().getDataType();
        List<String> argumentsStringList = colDef.getColDataType().getArgumentsStringList();
        
        if (argumentsStringList != null && !argumentsStringList.isEmpty()) {
            if (argumentsStringList.size() == 1) {
                column.setType(dataType + "(" + argumentsStringList.get(0) + ")");
                column.setLength(parseInteger(argumentsStringList.get(0)));
            } else if (argumentsStringList.size() == 2) {
                column.setType(dataType + "(" + argumentsStringList.get(0) + "," + argumentsStringList.get(1) + ")");
                column.setPrecision(parseInteger(argumentsStringList.get(0)));
                column.setScale(parseInteger(argumentsStringList.get(1)));
            }
        } else {
            column.setType(dataType);
        }
        
        column.setRawType(dataType);
        
        // 列约束
        column.setNullable(true); // 默认可空
        
        if (colDef.getColumnSpecs() != null) {
            String allSpecs = String.join(" ", colDef.getColumnSpecs()).toUpperCase();
            
            // 检查 NOT NULL
            if (allSpecs.contains("NOT") && allSpecs.contains("NULL")) {
                // 确保是 NOT NULL 而不是单独的 NULL
                if (allSpecs.contains("NOT NULL") || allSpecs.contains("NOTNULL") || 
                    (allSpecs.indexOf("NOT") < allSpecs.indexOf("NULL"))) {
                    column.setNullable(false);
                }
            } else if (allSpecs.contains("NULL") && !allSpecs.contains("NOT")) {
                column.setNullable(true);
            }
            
            // 检查主键
            if (allSpecs.contains("PRIMARY KEY") || allSpecs.contains("PRIMARYKEY") || allSpecs.contains("PRIMARY_KEY")) {
                column.setPrimaryKeyPart(true);
            }
            
            // 检查自动增长
            if (allSpecs.contains("AUTO_INCREMENT") || allSpecs.contains("AUTOINCREMENT") || allSpecs.contains("AUTO INCREMENT")) {
                column.setAutoIncrement(true);
            }
            
            // 提取默认值
            if (allSpecs.contains("DEFAULT")) {
                int defaultIdx = allSpecs.indexOf("DEFAULT");
                if (defaultIdx >= 0) {
                    String afterDefault = allSpecs.substring(defaultIdx + 7).trim();
                    String[] parts = afterDefault.split("\\s+");
                    if (parts.length > 0) {
                        column.setDefaultValue(parts[0]);
                    }
                }
            }
            
            // 提取注释
            if (allSpecs.contains("COMMENT")) {
                String specsStr = String.join(" ", colDef.getColumnSpecs());
                int commentIdx = specsStr.toUpperCase().indexOf("COMMENT");
                if (commentIdx >= 0) {
                    String afterComment = specsStr.substring(commentIdx + 7).trim();
                    // 移除引号
                    if (afterComment.startsWith("'") || afterComment.startsWith("\"")) {
                        int endQuote = afterComment.indexOf(afterComment.charAt(0), 1);
                        if (endQuote > 0) {
                            column.setComment(afterComment.substring(1, endQuote));
                        }
                    }
                }
            }
        }
        
        return column;
    }
    
    /**
     * 解析外键
     */
    private ForeignKeyModel parseForeignKey(ForeignKeyIndex fkIndex, String fromTable) {
        ForeignKeyModel fk = new ForeignKeyModel();
        fk.setName(fkIndex.getName());
        fk.setFromTable(fromTable);
        fk.setFromColumns(fkIndex.getColumnsNames());
        
        if (fkIndex.getTable() != null) {
            fk.setToTable(fkIndex.getTable().getName());
        }
        
        if (fkIndex.getReferencedColumnNames() != null) {
            fk.setToColumns(fkIndex.getReferencedColumnNames());
        }
        
        // ON DELETE / ON UPDATE - JSqlParser may not fully support these
        // We'll try to extract from the index name or leave them null
        
        return fk;
    }
    
    /**
     * 解析索引
     */
    private IndexModel parseIndex(Index index) {
        IndexModel indexModel = new IndexModel();
        indexModel.setName(index.getName());
        indexModel.setColumns(index.getColumnsNames());
        
        if (index.getType() != null) {
            String type = index.getType().toUpperCase();
            indexModel.setUnique(type.contains("UNIQUE"));
            indexModel.setIndexType(type);
        }
        
        return indexModel;
    }
    
    /**
     * 提取引用动作（CASCADE/SET NULL/RESTRICT）
     */
    private String extractReferenceAction(String refOption, String keyword) {
        int idx = refOption.indexOf(keyword);
        if (idx >= 0) {
            String after = refOption.substring(idx + keyword.length()).trim();
            String[] parts = after.split("\\s+");
            if (parts.length > 0) {
                return parts[0];
            }
        }
        return "RESTRICT";
    }
    
    /**
     * 解析整数
     */
    private Integer parseInteger(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
