package com.coffeeviz.core.renderer;

import com.coffeeviz.core.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mermaid ER 渲染器字段解析测试
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
class MermaidErRendererFieldTest {
    
    private MermaidErRenderer renderer;
    private DatabaseModel databaseModel;
    private RenderOptions options;
    
    @BeforeEach
    void setUp() {
        renderer = new MermaidErRenderer();
        databaseModel = new DatabaseModel();
        databaseModel.setDbType("mysql");
        databaseModel.setTables(new ArrayList<>());
        
        options = new RenderOptions();
        options.setShowComments(true);
    }
    
    @Test
    void testRenderWithFullTypeInfo() {
        // 创建测试表
        TableModel table = createTestTable();
        databaseModel.getTables().add(table);
        
        // 渲染
        String result = renderer.render(databaseModel, options);
        
        // 验证结果
        System.out.println("=== 渲染结果 ===");
        System.out.println(result);
        
        // 验证主键标识
        assertTrue(result.contains("bigint id PK"), "应该包含主键标识 PK");
        
        // 验证外键标识
        assertTrue(result.contains("bigint user_id FK"), "应该包含外键标识 FK");
        
        // 验证完整类型（包含长度）
        assertTrue(result.contains("varchar(100) project_name"), "应该包含 varchar(100)");
        assertTrue(result.contains("varchar(20) db_type"), "应该包含 varchar(20)");
        assertTrue(result.contains("varchar(255) password"), "应该包含 varchar(255)");
        
        // 验证 DECIMAL 类型（使用下划线代替逗号）
        assertTrue(result.contains("decimal(10_2) price"), "应该包含 decimal(10_2)");
        
        // 验证注释（应该合并约束和注释）
        assertTrue(result.contains("\"NOT NULL, 用户ID\""), "应该合并 NOT NULL 和注释");
        assertTrue(result.contains("\"NOT NULL, 项目名称\""), "应该合并 NOT NULL 和注释");
        
        // 验证不需要长度的类型
        assertTrue(result.contains("int table_count"), "int 类型不应该有长度");
        assertTrue(result.contains("datetime create_time"), "datetime 类型不应该有长度");
        assertTrue(result.contains("text description"), "text 类型不应该有长度");
        
        // 验证不应该有多个独立的注释字符串
        assertFalse(result.contains("\"NOT NULL\" \""), "不应该有多个独立的注释字符串");
    }
    
    @Test
    void testRenderWithForeignKeyRelation() {
        // 创建两个关联表
        TableModel userTable = createUserTable();
        TableModel projectTable = createProjectTable();
        
        databaseModel.getTables().add(userTable);
        databaseModel.getTables().add(projectTable);
        
        // 渲染
        String result = renderer.render(databaseModel, options);
        
        System.out.println("=== 外键关系渲染结果 ===");
        System.out.println(result);
        
        // 验证外键关系
        assertTrue(result.contains("sys_user ||--o{ biz_project"), "应该包含一对多关系");
        assertTrue(result.contains("user_id"), "应该包含外键列名");
        
        // 验证不应该有多个独立的注释字符串
        assertFalse(result.contains("\"NOT NULL\" \""), "不应该有多个独立的注释字符串");
    }
    
    @Test
    void testEscapeComment() {
        TableModel table = new TableModel();
        table.setName("test_table");
        table.setColumns(new ArrayList<>());
        
        // 创建包含特殊字符的注释
        ColumnModel column = new ColumnModel();
        column.setName("description");
        column.setType("text");
        column.setRawType("text");
        column.setComment("这是一个\"测试\"注释\n包含换行");
        column.setNullable(true);
        
        table.getColumns().add(column);
        databaseModel.getTables().add(table);
        
        // 渲染
        String result = renderer.render(databaseModel, options);
        
        System.out.println("=== 注释转义测试 ===");
        System.out.println(result);
        
        // 验证注释被正确转义
        assertTrue(result.contains("这是一个'测试'注释"), "双引号应该被转义为单引号");
        assertFalse(result.contains("注释\n包含"), "换行符应该被替换为空格");
    }
    
    @Test
    void testPrimaryKeyAndForeignKeyConflict() {
        // 测试字段既是主键又是外键的情况（应该只显示 PK）
        TableModel parentTable = new TableModel();
        parentTable.setName("parent_table");
        parentTable.setColumns(new ArrayList<>());
        parentTable.setForeignKeys(new ArrayList<>());
        
        ColumnModel parentId = new ColumnModel();
        parentId.setName("id");
        parentId.setType("bigint");
        parentId.setRawType("bigint");
        parentId.setPrimaryKeyPart(true);
        parentId.setNullable(false);
        parentTable.getColumns().add(parentId);
        
        TableModel childTable = new TableModel();
        childTable.setName("child_table");
        childTable.setColumns(new ArrayList<>());
        childTable.setForeignKeys(new ArrayList<>());
        
        // 创建一个既是主键又是外键的字段
        ColumnModel childId = new ColumnModel();
        childId.setName("parent_id");
        childId.setType("bigint");
        childId.setRawType("bigint");
        childId.setPrimaryKeyPart(true);  // 是主键
        childId.setNullable(false);
        childTable.getColumns().add(childId);
        
        // 添加外键关系
        ForeignKeyModel fk = new ForeignKeyModel();
        fk.setName("fk_child_parent");
        fk.setFromTable("child_table");
        fk.setFromColumns(List.of("parent_id"));
        fk.setToTable("parent_table");
        fk.setToColumns(List.of("id"));
        childTable.getForeignKeys().add(fk);
        
        databaseModel.getTables().add(parentTable);
        databaseModel.getTables().add(childTable);
        
        // 渲染
        String result = renderer.render(databaseModel, options);
        
        System.out.println("=== PK+FK 冲突测试 ===");
        System.out.println(result);
        
        // 验证：应该只显示 PK，不显示 FK（因为 Mermaid 不支持同时显示）
        assertTrue(result.contains("bigint parent_id PK"), "应该包含 PK 标记");
        assertFalse(result.contains("PK FK"), "不应该同时包含 PK 和 FK");
        
        // 验证外键关系仍然存在
        assertTrue(result.contains("parent_table ||--o{ child_table"), "应该包含外键关系");
    }
    
    /**
     * 创建测试表（模拟 biz_project）
     */
    private TableModel createTestTable() {
        TableModel table = new TableModel();
        table.setName("biz_project");
        table.setColumns(new ArrayList<>());
        table.setForeignKeys(new ArrayList<>());
        
        // 主键
        ColumnModel id = new ColumnModel();
        id.setName("id");
        id.setType("bigint");
        id.setRawType("bigint");
        id.setPrimaryKeyPart(true);
        id.setNullable(false);
        id.setComment("主键ID");
        table.getColumns().add(id);
        
        // 外键
        ColumnModel userId = new ColumnModel();
        userId.setName("user_id");
        userId.setType("bigint");
        userId.setRawType("bigint");
        userId.setNullable(false);
        userId.setComment("用户ID");
        table.getColumns().add(userId);
        
        // VARCHAR 类型
        ColumnModel projectName = new ColumnModel();
        projectName.setName("project_name");
        projectName.setType("varchar(100)");
        projectName.setRawType("varchar");
        projectName.setLength(100);
        projectName.setNullable(false);
        projectName.setComment("项目名称");
        table.getColumns().add(projectName);
        
        ColumnModel dbType = new ColumnModel();
        dbType.setName("db_type");
        dbType.setType("varchar(20)");
        dbType.setRawType("varchar");
        dbType.setLength(20);
        dbType.setNullable(true);
        dbType.setComment("数据库类型");
        table.getColumns().add(dbType);
        
        ColumnModel password = new ColumnModel();
        password.setName("password");
        password.setType("varchar(255)");
        password.setRawType("varchar");
        password.setLength(255);
        password.setNullable(false);
        password.setComment("密码");
        table.getColumns().add(password);
        
        // TEXT 类型
        ColumnModel description = new ColumnModel();
        description.setName("description");
        description.setType("text");
        description.setRawType("text");
        description.setNullable(true);
        description.setComment("项目描述");
        table.getColumns().add(description);
        
        // INT 类型
        ColumnModel tableCount = new ColumnModel();
        tableCount.setName("table_count");
        tableCount.setType("int");
        tableCount.setRawType("int");
        tableCount.setNullable(true);
        tableCount.setComment("表数量");
        table.getColumns().add(tableCount);
        
        // DATETIME 类型
        ColumnModel createTime = new ColumnModel();
        createTime.setName("create_time");
        createTime.setType("datetime");
        createTime.setRawType("datetime");
        createTime.setNullable(true);
        createTime.setComment("创建时间");
        table.getColumns().add(createTime);
        
        // DECIMAL 类型
        ColumnModel price = new ColumnModel();
        price.setName("price");
        price.setType("decimal(10_2)");  // 使用下划线格式
        price.setRawType("decimal");
        price.setPrecision(10);
        price.setScale(2);
        price.setNullable(true);
        price.setComment("价格");
        table.getColumns().add(price);
        
        // 添加外键
        ForeignKeyModel fk = new ForeignKeyModel();
        fk.setName("fk_project_user");
        fk.setFromTable("biz_project");
        fk.setFromColumns(List.of("user_id"));
        fk.setToTable("sys_user");
        fk.setToColumns(List.of("id"));
        table.getForeignKeys().add(fk);
        
        return table;
    }
    
    /**
     * 创建用户表
     */
    private TableModel createUserTable() {
        TableModel table = new TableModel();
        table.setName("sys_user");
        table.setColumns(new ArrayList<>());
        table.setForeignKeys(new ArrayList<>());
        
        ColumnModel id = new ColumnModel();
        id.setName("id");
        id.setType("bigint");
        id.setRawType("bigint");
        id.setPrimaryKeyPart(true);
        id.setNullable(false);
        table.getColumns().add(id);
        
        ColumnModel username = new ColumnModel();
        username.setName("username");
        username.setType("varchar(50)");
        username.setRawType("varchar");
        username.setLength(50);
        username.setNullable(false);
        table.getColumns().add(username);
        
        return table;
    }
    
    /**
     * 创建项目表
     */
    private TableModel createProjectTable() {
        TableModel table = new TableModel();
        table.setName("biz_project");
        table.setColumns(new ArrayList<>());
        table.setForeignKeys(new ArrayList<>());
        
        ColumnModel id = new ColumnModel();
        id.setName("id");
        id.setType("bigint");
        id.setRawType("bigint");
        id.setPrimaryKeyPart(true);
        id.setNullable(false);
        table.getColumns().add(id);
        
        ColumnModel userId = new ColumnModel();
        userId.setName("user_id");
        userId.setType("bigint");
        userId.setRawType("bigint");
        userId.setNullable(false);
        table.getColumns().add(userId);
        
        // 添加外键
        ForeignKeyModel fk = new ForeignKeyModel();
        fk.setName("fk_project_user");
        fk.setFromTable("biz_project");
        fk.setFromColumns(List.of("user_id"));
        fk.setToTable("sys_user");
        fk.setToColumns(List.of("id"));
        table.getForeignKeys().add(fk);
        
        return table;
    }
}
