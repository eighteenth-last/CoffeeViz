package com.coffeeviz.jdbc.parser;

import com.coffeeviz.core.model.*;
import com.coffeeviz.jdbc.config.JdbcConfig;
import com.coffeeviz.jdbc.model.ConnectionTestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;

/**
 * JDBC 元数据解析器实现
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class JdbcMetadataParserImpl implements JdbcMetadataParser {
    
    @Override
    public ParseResult parseFromDatabase(JdbcConfig config) {
        long startTime = System.currentTimeMillis();
        List<String> warnings = new ArrayList<>();
        
        try (Connection conn = createConnection(config)) {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = conn.getCatalog();
            String schema = config.getSchemaName() != null ? config.getSchemaName() : catalog;
            
            log.info("开始解析数据库元数据：{}", schema);
            
            // 创建数据库模型
            DatabaseModel databaseModel = DatabaseModel.builder()
                    .dbType(config.getDbType())
                    .schemaName(schema)
                    .build();
            
            // 添加元数据
            databaseModel.getMetadata().put("version", metaData.getDatabaseProductVersion());
            databaseModel.getMetadata().put("product", metaData.getDatabaseProductName());
            
            // 获取所有表
            ResultSet tables = metaData.getTables(catalog, schema, "%", new String[]{"TABLE"});
            
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                String tableComment = tables.getString("REMARKS");
                
                log.debug("解析表：{}", tableName);
                
                TableModel table = TableModel.builder()
                        .name(tableName)
                        .comment(tableComment)
                        .tableType("BASE TABLE")
                        .build();
                
                // 解析列
                parseColumns(metaData, catalog, schema, tableName, table, warnings);
                
                // 解析主键
                parsePrimaryKey(metaData, catalog, schema, tableName, table, warnings);
                
                // 解析外键
                parseForeignKeys(metaData, catalog, schema, tableName, table, warnings);
                
                // 解析索引
                parseIndexes(metaData, catalog, schema, tableName, table, warnings);
                
                databaseModel.getTables().add(table);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("解析完成，共 {} 张表，耗时 {} ms", databaseModel.getTables().size(), duration);
            
            return ParseResult.success(databaseModel, warnings);
            
        } catch (SQLException e) {
            log.error("JDBC 元数据解析失败", e);
            return ParseResult.error("JDBC 元数据解析失败：" + e.getMessage());
        }
    }
    
    @Override
    public ConnectionTestResult testConnection(JdbcConfig config) {
        long startTime = System.currentTimeMillis();
        
        try (Connection conn = createConnection(config)) {
            DatabaseMetaData metaData = conn.getMetaData();
            String dbVersion = metaData.getDatabaseProductName() + " " + 
                             metaData.getDatabaseProductVersion();
            
            // 统计表数量
            String catalog = conn.getCatalog();
            String schema = config.getSchemaName() != null ? config.getSchemaName() : catalog;
            ResultSet tables = metaData.getTables(catalog, schema, "%", new String[]{"TABLE"});
            
            int tableCount = 0;
            while (tables.next()) {
                tableCount++;
            }
            
            long duration = System.currentTimeMillis() - startTime;
            
            return ConnectionTestResult.success(
                    "连接成功",
                    tableCount,
                    dbVersion,
                    duration
            );
            
        } catch (SQLException e) {
            log.error("数据库连接测试失败", e);
            return ConnectionTestResult.failure("连接失败：" + e.getMessage());
        }
    }
    
    /**
     * 创建数据库连接
     */
    private Connection createConnection(JdbcConfig config) throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", config.getUsername());
        props.setProperty("password", config.getPassword());
        props.setProperty("connectTimeout", String.valueOf(config.getTimeout() * 1000));
        
        Connection conn = DriverManager.getConnection(config.getJdbcUrl(), props);
        conn.setReadOnly(config.isReadOnly());
        
        return conn;
    }
    
    /**
     * 解析列信息
     */
    private void parseColumns(DatabaseMetaData metaData, String catalog, String schema,
                             String tableName, TableModel table, List<String> warnings) {
        try {
            ResultSet columns = metaData.getColumns(catalog, schema, tableName, "%");
            
            while (columns.next()) {
                String typeName = columns.getString("TYPE_NAME");
                int columnSize = columns.getInt("COLUMN_SIZE");
                int decimalDigits = columns.getInt("DECIMAL_DIGITS");
                
                // 构建完整的类型字符串
                String fullType = buildFullTypeString(typeName, columnSize, decimalDigits);
                
                ColumnModel column = ColumnModel.builder()
                        .name(columns.getString("COLUMN_NAME"))
                        .type(fullType)  // 完整类型（包含长度）
                        .rawType(typeName)  // 原始类型名
                        .length(columnSize)
                        .precision(decimalDigits > 0 ? columnSize : null)
                        .scale(decimalDigits > 0 ? decimalDigits : null)
                        .nullable(columns.getInt("NULLABLE") == DatabaseMetaData.columnNullable)
                        .defaultValue(columns.getString("COLUMN_DEF"))
                        .comment(columns.getString("REMARKS"))
                        .autoIncrement("YES".equalsIgnoreCase(columns.getString("IS_AUTOINCREMENT")))
                        .build();
                
                table.getColumns().add(column);
            }
        } catch (SQLException e) {
            warnings.add("解析表 " + tableName + " 的列信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 构建完整的类型字符串（包含长度/精度）
     */
    private String buildFullTypeString(String typeName, int columnSize, int decimalDigits) {
        // 不需要长度的类型
        Set<String> noLengthTypes = Set.of(
            "INT", "INTEGER", "BIGINT", "SMALLINT", "TINYINT",
            "DATE", "DATETIME", "TIMESTAMP", "TIME",
            "TEXT", "LONGTEXT", "MEDIUMTEXT", "TINYTEXT",
            "BLOB", "LONGBLOB", "MEDIUMBLOB", "TINYBLOB",
            "BOOLEAN", "BOOL"
        );
        
        String upperTypeName = typeName.toUpperCase();
        
        // 如果是不需要长度的类型，直接返回
        if (noLengthTypes.contains(upperTypeName)) {
            return typeName;
        }
        
        // DECIMAL/NUMERIC 类型需要精度和小数位
        if (upperTypeName.contains("DECIMAL") || upperTypeName.contains("NUMERIC")) {
            if (decimalDigits > 0) {
                return typeName + "(" + columnSize + "," + decimalDigits + ")";
            } else if (columnSize > 0) {
                return typeName + "(" + columnSize + ")";
            }
        }
        
        // VARCHAR/CHAR 等字符类型需要长度
        if (upperTypeName.contains("CHAR") || upperTypeName.contains("BINARY")) {
            if (columnSize > 0) {
                return typeName + "(" + columnSize + ")";
            }
        }
        
        return typeName;
    }
    
    /**
     * 解析主键
     */
    private void parsePrimaryKey(DatabaseMetaData metaData, String catalog, String schema,
                                 String tableName, TableModel table, List<String> warnings) {
        try {
            ResultSet primaryKeys = metaData.getPrimaryKeys(catalog, schema, tableName);
            
            List<String> pkColumns = new ArrayList<>();
            String pkName = null;
            
            while (primaryKeys.next()) {
                if (pkName == null) {
                    pkName = primaryKeys.getString("PK_NAME");
                }
                pkColumns.add(primaryKeys.getString("COLUMN_NAME"));
            }
            
            if (!pkColumns.isEmpty()) {
                table.setPrimaryKey(PrimaryKeyModel.builder()
                        .name(pkName)
                        .columns(pkColumns)
                        .build());
                
                // 标记主键列
                for (ColumnModel column : table.getColumns()) {
                    if (pkColumns.contains(column.getName())) {
                        column.setPrimaryKeyPart(true);
                    }
                }
            }
        } catch (SQLException e) {
            warnings.add("解析表 " + tableName + " 的主键失败：" + e.getMessage());
        }
    }
    
    /**
     * 解析外键
     */
    private void parseForeignKeys(DatabaseMetaData metaData, String catalog, String schema,
                                  String tableName, TableModel table, List<String> warnings) {
        try {
            ResultSet foreignKeys = metaData.getImportedKeys(catalog, schema, tableName);
            
            Map<String, ForeignKeyModel> fkMap = new HashMap<>();
            
            while (foreignKeys.next()) {
                String fkName = foreignKeys.getString("FK_NAME");
                String fromColumn = foreignKeys.getString("FKCOLUMN_NAME");
                String toTable = foreignKeys.getString("PKTABLE_NAME");
                String toColumn = foreignKeys.getString("PKCOLUMN_NAME");
                
                ForeignKeyModel fk = fkMap.computeIfAbsent(fkName, k -> 
                    ForeignKeyModel.builder()
                            .name(fkName)
                            .fromTable(tableName)
                            .toTable(toTable)
                            .fromColumns(new ArrayList<>())
                            .toColumns(new ArrayList<>())
                            .build()
                );
                
                fk.getFromColumns().add(fromColumn);
                fk.getToColumns().add(toColumn);
            }
            
            table.getForeignKeys().addAll(fkMap.values());
            
        } catch (SQLException e) {
            warnings.add("解析表 " + tableName + " 的外键失败：" + e.getMessage());
        }
    }
    
    /**
     * 解析索引
     */
    private void parseIndexes(DatabaseMetaData metaData, String catalog, String schema,
                             String tableName, TableModel table, List<String> warnings) {
        try {
            ResultSet indexes = metaData.getIndexInfo(catalog, schema, tableName, false, false);
            
            Map<String, IndexModel> indexMap = new HashMap<>();
            
            while (indexes.next()) {
                String indexName = indexes.getString("INDEX_NAME");
                
                // 跳过主键索引
                if (indexName == null || "PRIMARY".equalsIgnoreCase(indexName)) {
                    continue;
                }
                
                String columnName = indexes.getString("COLUMN_NAME");
                boolean unique = !indexes.getBoolean("NON_UNIQUE");
                
                IndexModel index = indexMap.computeIfAbsent(indexName, k ->
                    IndexModel.builder()
                            .name(indexName)
                            .unique(unique)
                            .columns(new ArrayList<>())
                            .build()
                );
                
                index.getColumns().add(columnName);
            }
            
            table.getIndexes().addAll(indexMap.values());
            
        } catch (SQLException e) {
            warnings.add("解析表 " + tableName + " 的索引失败：" + e.getMessage());
        }
    }
}
