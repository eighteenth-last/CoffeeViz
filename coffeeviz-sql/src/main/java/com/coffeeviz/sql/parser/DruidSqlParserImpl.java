package com.coffeeviz.sql.parser;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLForeignKeyImpl;
import com.coffeeviz.core.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Druid SQL Parser 实现（L2 方言解析）
 * 支持 MySQL 和 PostgreSQL 特有语法
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class DruidSqlParserImpl implements SqlParser {
    
    @Override
    public ParseResult parse(String sqlText, String dialect) {
        log.info("开始使用 Druid SQL Parser 解析 SQL，方言: {}", dialect);
        
        DatabaseModel databaseModel = new DatabaseModel();
        databaseModel.setDbType(dialect);
        databaseModel.setTables(new ArrayList<>());
        
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        try {
            // 确定 Druid DbType
            DbType dbType = getDbType(dialect);
            
            // 解析 SQL 语句
            List<SQLStatement> statements = SQLUtils.parseStatements(sqlText, dbType);
            
            for (SQLStatement statement : statements) {
                if (statement instanceof SQLCreateTableStatement) {
                    try {
                        TableModel table = parseCreateTable((SQLCreateTableStatement) statement, dialect);
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
            
            log.info("Druid SQL Parser 解析成功，共解析 {} 张表", databaseModel.getTables().size());
            return ParseResult.success(databaseModel, warnings);
            
        } catch (Exception e) {
            String error = "Druid SQL Parser 解析失败: " + e.getMessage();
            log.error(error, e);
            errors.add(error);
            return ParseResult.error(error);
        }
    }
    
    @Override
    public boolean supports(String dialect) {
        return "mysql".equalsIgnoreCase(dialect) || 
               "postgres".equalsIgnoreCase(dialect) ||
               "postgresql".equalsIgnoreCase(dialect);
    }
    
    /**
     * 获取 Druid DbType
     */
    private DbType getDbType(String dialect) {
        if ("mysql".equalsIgnoreCase(dialect)) {
            return DbType.mysql;
        } else if ("postgres".equalsIgnoreCase(dialect) || "postgresql".equalsIgnoreCase(dialect)) {
            return DbType.postgresql;
        }
        return DbType.mysql; // 默认
    }
    
    /**
     * 解析 CREATE TABLE 语句
     */
    private TableModel parseCreateTable(SQLCreateTableStatement createTable, String dialect) {
        TableModel table = new TableModel();
        // 清理表名中的反引号、双引号等特殊字符
        String tableName = createTable.getTableName();
        if (tableName != null) {
            tableName = cleanIdentifier(tableName);
        }
        table.setName(tableName);
        table.setColumns(new ArrayList<>());
        table.setForeignKeys(new ArrayList<>());
        table.setIndexes(new ArrayList<>());
        
        // 解析表注释
        if (createTable.getComment() != null) {
            table.setComment(createTable.getComment().toString().replaceAll("'", ""));
        }
        
        // MySQL 特有属性
        if (createTable instanceof MySqlCreateTableStatement) {
            MySqlCreateTableStatement mysqlTable = (MySqlCreateTableStatement) createTable;
            // 可以获取 ENGINE, CHARSET 等信息
            if (mysqlTable.getTableOptions() != null) {
                // 处理表选项
            }
        }
        
        // PostgreSQL 特有属性可以在这里处理
        // Druid 对 PostgreSQL 的支持可能有限，主要依赖基础的 SQLCreateTableStatement
        
        // 解析列定义
        for (SQLTableElement element : createTable.getTableElementList()) {
            if (element instanceof SQLColumnDefinition) {
                ColumnModel column = parseColumnDefinition((SQLColumnDefinition) element);
                table.getColumns().add(column);
            } else if (element instanceof SQLPrimaryKey) {
                // 主键约束
                PrimaryKeyModel pk = parsePrimaryKey((SQLPrimaryKey) element);
                table.setPrimaryKey(pk);
                
                // 标记主键列
                for (String pkCol : pk.getColumns()) {
                    table.getColumns().stream()
                        .filter(col -> col.getName().equalsIgnoreCase(pkCol))
                        .forEach(col -> col.setPrimaryKeyPart(true));
                }
            } else if (element instanceof SQLForeignKeyConstraint) {
                // 外键约束
                ForeignKeyModel fk = parseForeignKey((SQLForeignKeyConstraint) element, table.getName());
                table.getForeignKeys().add(fk);
            } else if (element instanceof SQLUnique) {
                // 唯一约束
                IndexModel index = parseUniqueConstraint((SQLUnique) element);
                table.getIndexes().add(index);
            }
        }
        
        return table;
    }
    
    /**
     * 清理标识符（移除反引号、双引号等）
     */
    private String cleanIdentifier(String identifier) {
        if (identifier == null) {
            return null;
        }
        // 移除反引号、双引号、方括号
        return identifier.replaceAll("[`\"\\[\\]]", "").trim();
    }
    
    /**
     * 解析列定义
     */
    private ColumnModel parseColumnDefinition(SQLColumnDefinition colDef) {
        ColumnModel column = new ColumnModel();
        // 清理列名中的特殊字符
        String columnName = colDef.getColumnName();
        if (columnName != null) {
            columnName = cleanIdentifier(columnName);
        }
        column.setName(columnName);
        
        // 数据类型
        String dataType = colDef.getDataType().getName();
        column.setRawType(dataType);
        
        // 完整类型（包含长度/精度）
        column.setType(colDef.getDataType().toString());
        
        // 解析长度和精度
        if (colDef.getDataType().getArguments() != null && !colDef.getDataType().getArguments().isEmpty()) {
            try {
                if (colDef.getDataType().getArguments().size() == 1) {
                    column.setLength(Integer.parseInt(colDef.getDataType().getArguments().get(0).toString()));
                } else if (colDef.getDataType().getArguments().size() == 2) {
                    column.setPrecision(Integer.parseInt(colDef.getDataType().getArguments().get(0).toString()));
                    column.setScale(Integer.parseInt(colDef.getDataType().getArguments().get(1).toString()));
                }
            } catch (NumberFormatException e) {
                log.warn("解析列长度/精度失败: {}", e.getMessage());
            }
        }
        
        // 是否可空
        if (colDef.getConstraints() != null) {
            for (SQLColumnConstraint constraint : colDef.getConstraints()) {
                if (constraint instanceof SQLNotNullConstraint) {
                    column.setNullable(false);
                } else if (constraint instanceof SQLNullConstraint) {
                    column.setNullable(true);
                } else if (constraint instanceof SQLColumnPrimaryKey) {
                    column.setPrimaryKeyPart(true);
                }
            }
        }
        
        // 默认值
        if (colDef.getDefaultExpr() != null) {
            column.setDefaultValue(colDef.getDefaultExpr().toString());
        }
        
        // 自动增长
        if (colDef.isAutoIncrement()) {
            column.setAutoIncrement(true);
        }
        
        // 注释
        if (colDef.getComment() != null) {
            column.setComment(colDef.getComment().toString().replaceAll("'", ""));
        }
        
        return column;
    }
    
    /**
     * 解析主键
     */
    private PrimaryKeyModel parsePrimaryKey(SQLPrimaryKey pk) {
        PrimaryKeyModel primaryKey = new PrimaryKeyModel();
        
        if (pk.getName() != null) {
            primaryKey.setName(cleanIdentifier(pk.getName().getSimpleName()));
        }
        
        List<String> columns = new ArrayList<>();
        for (SQLSelectOrderByItem item : pk.getColumns()) {
            columns.add(cleanIdentifier(item.getExpr().toString()));
        }
        primaryKey.setColumns(columns);
        
        return primaryKey;
    }
    
    /**
     * 解析外键
     */
    private ForeignKeyModel parseForeignKey(SQLForeignKeyConstraint fk, String fromTable) {
        ForeignKeyModel foreignKey = new ForeignKeyModel();
        
        if (fk.getName() != null) {
            foreignKey.setName(cleanIdentifier(fk.getName().getSimpleName()));
        }
        
        foreignKey.setFromTable(fromTable);
        
        // FROM 列
        List<String> fromColumns = new ArrayList<>();
        for (SQLExpr column : fk.getReferencingColumns()) {
            fromColumns.add(cleanIdentifier(column.toString()));
        }
        foreignKey.setFromColumns(fromColumns);
        
        // TO 表
        if (fk.getReferencedTable() != null) {
            foreignKey.setToTable(cleanIdentifier(fk.getReferencedTable().toString()));
        }
        
        // TO 列
        List<String> toColumns = new ArrayList<>();
        for (SQLExpr column : fk.getReferencedColumns()) {
            toColumns.add(cleanIdentifier(column.toString()));
        }
        foreignKey.setToColumns(toColumns);
        
        // ON DELETE / ON UPDATE - simplified approach
        // Druid API may vary by version, so we'll leave these as null for now
        // They can be extracted from the SQL text if needed
        
        return foreignKey;
    }
    
    /**
     * 解析唯一约束
     */
    private IndexModel parseUniqueConstraint(SQLUnique unique) {
        IndexModel index = new IndexModel();
        
        if (unique.getName() != null) {
            index.setName(cleanIdentifier(unique.getName().getSimpleName()));
        }
        
        List<String> columns = new ArrayList<>();
        for (SQLSelectOrderByItem item : unique.getColumns()) {
            columns.add(cleanIdentifier(item.getExpr().toString()));
        }
        index.setColumns(columns);
        index.setUnique(true);
        
        return index;
    }
}
