package com.coffeeviz.sql.parser;

import com.coffeeviz.core.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式降级解析器（L3）
 * 当 JSqlParser 和 Druid 都失败时使用
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class RegexFallbackParser implements SqlParser {
    
    // CREATE TABLE 语句模式
    private static final Pattern CREATE_TABLE_PATTERN = Pattern.compile(
        "CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?([`\\w]+)\\s*\\((.*?)\\)(?:\\s*ENGINE\\s*=\\s*\\w+)?(?:\\s*DEFAULT\\s+CHARSET\\s*=\\s*\\w+)?(?:\\s*COMMENT\\s*=?\\s*'([^']*)')?",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    
    // 列定义模式
    private static final Pattern COLUMN_PATTERN = Pattern.compile(
        "([`\\w]+)\\s+(\\w+(?:\\([^)]+\\))?)(?:\\s+(NOT\\s+NULL|NULL))?(?:\\s+DEFAULT\\s+([^,\\s]+))?(?:\\s+AUTO_INCREMENT)?(?:\\s+COMMENT\\s+'([^']*)')?",
        Pattern.CASE_INSENSITIVE
    );
    
    // 主键模式
    private static final Pattern PRIMARY_KEY_PATTERN = Pattern.compile(
        "PRIMARY\\s+KEY\\s*\\(([^)]+)\\)",
        Pattern.CASE_INSENSITIVE
    );
    
    // 外键模式
    private static final Pattern FOREIGN_KEY_PATTERN = Pattern.compile(
        "(?:CONSTRAINT\\s+([`\\w]+)\\s+)?FOREIGN\\s+KEY\\s*\\(([^)]+)\\)\\s*REFERENCES\\s+([`\\w]+)\\s*\\(([^)]+)\\)(?:\\s+ON\\s+DELETE\\s+(CASCADE|SET\\s+NULL|RESTRICT|NO\\s+ACTION))?(?:\\s+ON\\s+UPDATE\\s+(CASCADE|SET\\s+NULL|RESTRICT|NO\\s+ACTION))?",
        Pattern.CASE_INSENSITIVE
    );
    
    // 索引模式
    private static final Pattern INDEX_PATTERN = Pattern.compile(
        "(UNIQUE\\s+)?(?:KEY|INDEX)\\s+([`\\w]+)\\s*\\(([^)]+)\\)",
        Pattern.CASE_INSENSITIVE
    );
    
    @Override
    public ParseResult parse(String sqlText, String dialect) {
        log.info("开始使用正则表达式降级解析 SQL，方言: {}", dialect);
        
        DatabaseModel databaseModel = new DatabaseModel();
        databaseModel.setDbType(dialect);
        databaseModel.setTables(new ArrayList<>());
        
        List<String> warnings = new ArrayList<>();
        warnings.add("使用降级解析策略，可能无法识别所有语法特性");
        
        try {
            // 移除注释
            String cleanedSql = removeComments(sqlText);
            
            // 查找所有 CREATE TABLE 语句
            Matcher tableMatcher = CREATE_TABLE_PATTERN.matcher(cleanedSql);
            
            while (tableMatcher.find()) {
                try {
                    String tableName = cleanName(tableMatcher.group(1));
                    String tableBody = tableMatcher.group(2);
                    String tableComment = tableMatcher.group(3);
                    
                    TableModel table = parseTable(tableName, tableBody, tableComment);
                    databaseModel.getTables().add(table);
                } catch (Exception e) {
                    String error = "解析表失败: " + e.getMessage();
                    log.warn(error, e);
                    warnings.add(error);
                }
            }
            
            if (databaseModel.getTables().isEmpty()) {
                return ParseResult.error("未找到任何表定义");
            }
            
            log.info("正则表达式解析成功，共解析 {} 张表", databaseModel.getTables().size());
            return ParseResult.success(databaseModel, warnings);
            
        } catch (Exception e) {
            String error = "正则表达式解析失败: " + e.getMessage();
            log.error(error, e);
            return ParseResult.error(error);
        }
    }
    
    @Override
    public boolean supports(String dialect) {
        // 降级解析器支持所有方言
        return true;
    }
    
    /**
     * 解析表
     */
    private TableModel parseTable(String tableName, String tableBody, String tableComment) {
        TableModel table = new TableModel();
        table.setName(tableName);
        table.setComment(tableComment);
        table.setColumns(new ArrayList<>());
        table.setForeignKeys(new ArrayList<>());
        table.setIndexes(new ArrayList<>());
        
        // 分割表体为行
        String[] lines = tableBody.split(",(?![^()]*\\))");
        
        for (String line : lines) {
            line = line.trim();
            
            // 检查是否是主键
            Matcher pkMatcher = PRIMARY_KEY_PATTERN.matcher(line);
            if (pkMatcher.find()) {
                PrimaryKeyModel pk = parsePrimaryKey(pkMatcher.group(1));
                table.setPrimaryKey(pk);
                
                // 标记主键列
                for (String pkCol : pk.getColumns()) {
                    table.getColumns().stream()
                        .filter(col -> col.getName().equalsIgnoreCase(pkCol))
                        .forEach(col -> col.setPrimaryKeyPart(true));
                }
                continue;
            }
            
            // 检查是否是外键
            Matcher fkMatcher = FOREIGN_KEY_PATTERN.matcher(line);
            if (fkMatcher.find()) {
                ForeignKeyModel fk = parseForeignKey(fkMatcher, tableName);
                table.getForeignKeys().add(fk);
                continue;
            }
            
            // 检查是否是索引
            Matcher indexMatcher = INDEX_PATTERN.matcher(line);
            if (indexMatcher.find()) {
                IndexModel index = parseIndex(indexMatcher);
                table.getIndexes().add(index);
                continue;
            }
            
            // 尝试解析为列定义
            Matcher colMatcher = COLUMN_PATTERN.matcher(line);
            if (colMatcher.find()) {
                ColumnModel column = parseColumn(colMatcher);
                table.getColumns().add(column);
            }
        }
        
        return table;
    }
    
    /**
     * 解析列
     */
    private ColumnModel parseColumn(Matcher matcher) {
        ColumnModel column = new ColumnModel();
        column.setName(cleanName(matcher.group(1)));
        column.setType(matcher.group(2));
        column.setRawType(extractBaseType(matcher.group(2)));
        
        // 可空性
        String nullability = matcher.group(3);
        if (nullability != null) {
            column.setNullable(!nullability.toUpperCase().contains("NOT NULL"));
        } else {
            column.setNullable(true); // 默认可空
        }
        
        // 默认值
        if (matcher.group(4) != null) {
            column.setDefaultValue(matcher.group(4));
        }
        
        // 注释
        if (matcher.group(5) != null) {
            column.setComment(matcher.group(5));
        }
        
        // 检查是否自动增长
        String fullLine = matcher.group(0);
        if (fullLine.toUpperCase().contains("AUTO_INCREMENT") || 
            fullLine.toUpperCase().contains("AUTOINCREMENT")) {
            column.setAutoIncrement(true);
        }
        
        return column;
    }
    
    /**
     * 解析主键
     */
    private PrimaryKeyModel parsePrimaryKey(String columnsStr) {
        PrimaryKeyModel pk = new PrimaryKeyModel();
        
        List<String> columns = new ArrayList<>();
        for (String col : columnsStr.split(",")) {
            columns.add(cleanName(col.trim()));
        }
        pk.setColumns(columns);
        
        return pk;
    }
    
    /**
     * 解析外键
     */
    private ForeignKeyModel parseForeignKey(Matcher matcher, String fromTable) {
        ForeignKeyModel fk = new ForeignKeyModel();
        
        if (matcher.group(1) != null) {
            fk.setName(cleanName(matcher.group(1)));
        }
        
        fk.setFromTable(fromTable);
        
        // FROM 列
        List<String> fromColumns = new ArrayList<>();
        for (String col : matcher.group(2).split(",")) {
            fromColumns.add(cleanName(col.trim()));
        }
        fk.setFromColumns(fromColumns);
        
        // TO 表
        fk.setToTable(cleanName(matcher.group(3)));
        
        // TO 列
        List<String> toColumns = new ArrayList<>();
        for (String col : matcher.group(4).split(",")) {
            toColumns.add(cleanName(col.trim()));
        }
        fk.setToColumns(toColumns);
        
        // ON DELETE
        if (matcher.group(5) != null) {
            fk.setOnDelete(matcher.group(5).toUpperCase().replace(" ", "_"));
        }
        
        // ON UPDATE
        if (matcher.group(6) != null) {
            fk.setOnUpdate(matcher.group(6).toUpperCase().replace(" ", "_"));
        }
        
        return fk;
    }
    
    /**
     * 解析索引
     */
    private IndexModel parseIndex(Matcher matcher) {
        IndexModel index = new IndexModel();
        
        boolean isUnique = matcher.group(1) != null;
        index.setUnique(isUnique);
        
        index.setName(cleanName(matcher.group(2)));
        
        List<String> columns = new ArrayList<>();
        for (String col : matcher.group(3).split(",")) {
            columns.add(cleanName(col.trim()));
        }
        index.setColumns(columns);
        
        return index;
    }
    
    /**
     * 移除 SQL 注释
     */
    private String removeComments(String sql) {
        // 移除单行注释
        sql = sql.replaceAll("--[^\n]*", "");
        // 移除多行注释
        sql = sql.replaceAll("/\\*.*?\\*/", "");
        return sql;
    }
    
    /**
     * 清理名称（移除反引号等）
     */
    private String cleanName(String name) {
        return name.replaceAll("[`'\"]", "").trim();
    }
    
    /**
     * 提取基础类型（去除长度/精度）
     */
    private String extractBaseType(String fullType) {
        int idx = fullType.indexOf('(');
        if (idx > 0) {
            return fullType.substring(0, idx);
        }
        return fullType;
    }
}
