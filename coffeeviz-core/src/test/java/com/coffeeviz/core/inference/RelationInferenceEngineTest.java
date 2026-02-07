package com.coffeeviz.core.inference;

import com.coffeeviz.core.enums.RelationType;
import com.coffeeviz.core.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RelationInferenceEngine 单元测试
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
class RelationInferenceEngineTest {
    
    private RelationInferenceEngine engine;
    private InferenceConfig config;
    
    @BeforeEach
    void setUp() {
        engine = new RelationInferenceEngine();
        config = InferenceConfig.builder()
                .enableNamingConvention(false)
                .identifyJunctionTables(true)
                .build();
    }
    
    @Test
    void testInferOneToManyRelation() {
        // 创建 users 表
        TableModel users = TableModel.builder()
                .name("users")
                .primaryKey(PrimaryKeyModel.builder()
                        .columns(Collections.singletonList("id"))
                        .build())
                .columns(Arrays.asList(
                        ColumnModel.builder().name("id").type("BIGINT").primaryKeyPart(true).build(),
                        ColumnModel.builder().name("username").type("VARCHAR").build()
                ))
                .foreignKeys(new ArrayList<>())
                .indexes(new ArrayList<>())
                .build();
        
        // 创建 orders 表，有外键指向 users
        ForeignKeyModel fk = ForeignKeyModel.builder()
                .name("fk_orders_user_id")
                .fromTable("orders")
                .fromColumns(Collections.singletonList("user_id"))
                .toTable("users")
                .toColumns(Collections.singletonList("id"))
                .build();
        
        TableModel orders = TableModel.builder()
                .name("orders")
                .primaryKey(PrimaryKeyModel.builder()
                        .columns(Collections.singletonList("id"))
                        .build())
                .columns(Arrays.asList(
                        ColumnModel.builder().name("id").type("BIGINT").primaryKeyPart(true).build(),
                        ColumnModel.builder().name("user_id").type("BIGINT").build()
                ))
                .foreignKeys(new ArrayList<>(Collections.singletonList(fk)))
                .indexes(new ArrayList<>())
                .build();
        
        DatabaseModel model = DatabaseModel.builder()
                .tables(Arrays.asList(users, orders))
                .build();
        
        // 执行推断
        engine.inferRelations(model);
        
        // 验证：应该推断为 ONE_TO_MANY
        assertEquals(RelationType.ONE_TO_MANY, fk.getRelationType());
    }
    
    @Test
    void testInferOneToOneRelationWithPrimaryKey() {
        // 创建 users 表
        TableModel users = TableModel.builder()
                .name("users")
                .primaryKey(PrimaryKeyModel.builder()
                        .columns(Collections.singletonList("id"))
                        .build())
                .columns(Collections.singletonList(
                        ColumnModel.builder().name("id").type("BIGINT").primaryKeyPart(true).build()
                ))
                .foreignKeys(new ArrayList<>())
                .indexes(new ArrayList<>())
                .build();
        
        // 创建 profiles 表，外键是主键的一部分
        ForeignKeyModel fk = ForeignKeyModel.builder()
                .name("fk_profiles_user_id")
                .fromTable("profiles")
                .fromColumns(Collections.singletonList("user_id"))
                .toTable("users")
                .toColumns(Collections.singletonList("id"))
                .build();
        
        TableModel profiles = TableModel.builder()
                .name("profiles")
                .primaryKey(PrimaryKeyModel.builder()
                        .columns(Collections.singletonList("user_id"))
                        .build())
                .columns(Collections.singletonList(
                        ColumnModel.builder().name("user_id").type("BIGINT").primaryKeyPart(true).build()
                ))
                .foreignKeys(new ArrayList<>(Collections.singletonList(fk)))
                .indexes(new ArrayList<>())
                .build();
        
        DatabaseModel model = DatabaseModel.builder()
                .tables(Arrays.asList(users, profiles))
                .build();
        
        // 执行推断
        engine.inferRelations(model);
        
        // 验证：应该推断为 ONE_TO_ONE（外键是主键）
        assertEquals(RelationType.ONE_TO_ONE, fk.getRelationType());
    }
    
    @Test
    void testInferOneToOneRelationWithUniqueIndex() {
        // 创建 users 表
        TableModel users = TableModel.builder()
                .name("users")
                .primaryKey(PrimaryKeyModel.builder()
                        .columns(Collections.singletonList("id"))
                        .build())
                .columns(Collections.singletonList(
                        ColumnModel.builder().name("id").type("BIGINT").primaryKeyPart(true).build()
                ))
                .foreignKeys(new ArrayList<>())
                .indexes(new ArrayList<>())
                .build();
        
        // 创建 profiles 表，外键有唯一索引
        ForeignKeyModel fk = ForeignKeyModel.builder()
                .name("fk_profiles_user_id")
                .fromTable("profiles")
                .fromColumns(Collections.singletonList("user_id"))
                .toTable("users")
                .toColumns(Collections.singletonList("id"))
                .build();
        
        IndexModel uniqueIndex = IndexModel.builder()
                .name("uk_user_id")
                .columns(Collections.singletonList("user_id"))
                .unique(true)
                .build();
        
        TableModel profiles = TableModel.builder()
                .name("profiles")
                .primaryKey(PrimaryKeyModel.builder()
                        .columns(Collections.singletonList("id"))
                        .build())
                .columns(Arrays.asList(
                        ColumnModel.builder().name("id").type("BIGINT").primaryKeyPart(true).build(),
                        ColumnModel.builder().name("user_id").type("BIGINT").build()
                ))
                .foreignKeys(new ArrayList<>(Collections.singletonList(fk)))
                .indexes(new ArrayList<>(Collections.singletonList(uniqueIndex)))
                .build();
        
        DatabaseModel model = DatabaseModel.builder()
                .tables(Arrays.asList(users, profiles))
                .build();
        
        // 执行推断
        engine.inferRelations(model);
        
        // 验证：应该推断为 ONE_TO_ONE（有唯一索引）
        assertEquals(RelationType.ONE_TO_ONE, fk.getRelationType());
    }
    
    @Test
    void testIdentifyJunctionTable() {
        // 创建 users 表
        TableModel users = TableModel.builder()
                .name("users")
                .primaryKey(PrimaryKeyModel.builder()
                        .columns(Collections.singletonList("id"))
                        .build())
                .columns(Collections.singletonList(
                        ColumnModel.builder().name("id").type("BIGINT").primaryKeyPart(true).build()
                ))
                .foreignKeys(new ArrayList<>())
                .indexes(new ArrayList<>())
                .build();
        
        // 创建 roles 表
        TableModel roles = TableModel.builder()
                .name("roles")
                .primaryKey(PrimaryKeyModel.builder()
                        .columns(Collections.singletonList("id"))
                        .build())
                .columns(Collections.singletonList(
                        ColumnModel.builder().name("id").type("BIGINT").primaryKeyPart(true).build()
                ))
                .foreignKeys(new ArrayList<>())
                .indexes(new ArrayList<>())
                .build();
        
        // 创建中间表 user_roles
        ForeignKeyModel fk1 = ForeignKeyModel.builder()
                .name("fk_user_roles_user_id")
                .fromTable("user_roles")
                .fromColumns(Collections.singletonList("user_id"))
                .toTable("users")
                .toColumns(Collections.singletonList("id"))
                .build();
        
        ForeignKeyModel fk2 = ForeignKeyModel.builder()
                .name("fk_user_roles_role_id")
                .fromTable("user_roles")
                .fromColumns(Collections.singletonList("role_id"))
                .toTable("roles")
                .toColumns(Collections.singletonList("id"))
                .build();
        
        TableModel userRoles = TableModel.builder()
                .name("user_roles")
                .primaryKey(PrimaryKeyModel.builder()
                        .columns(Arrays.asList("user_id", "role_id"))
                        .build())
                .columns(Arrays.asList(
                        ColumnModel.builder().name("user_id").type("BIGINT").primaryKeyPart(true).build(),
                        ColumnModel.builder().name("role_id").type("BIGINT").primaryKeyPart(true).build()
                ))
                .foreignKeys(new ArrayList<>(Arrays.asList(fk1, fk2)))
                .indexes(new ArrayList<>())
                .build();
        
        DatabaseModel model = DatabaseModel.builder()
                .tables(Arrays.asList(users, roles, userRoles))
                .build();
        
        // 执行推断
        engine.inferRelations(model);
        
        // 验证：应该识别为中间表
        assertEquals("JUNCTION", userRoles.getTableType());
        assertEquals(RelationType.MANY_TO_MANY, fk1.getRelationType());
        assertEquals(RelationType.MANY_TO_MANY, fk2.getRelationType());
    }
    
    @Test
    void testNamingConventionInference() {
        // 启用命名约定推断
        config.setEnableNamingConvention(true);
        
        // 创建 users 表
        TableModel users = TableModel.builder()
                .name("users")
                .primaryKey(PrimaryKeyModel.builder()
                        .columns(Collections.singletonList("id"))
                        .build())
                .columns(Collections.singletonList(
                        ColumnModel.builder().name("id").type("BIGINT").primaryKeyPart(true).build()
                ))
                .foreignKeys(new ArrayList<>())
                .indexes(new ArrayList<>())
                .build();
        
        // 创建 posts 表，有 user_id 列但没有显式外键
        TableModel posts = TableModel.builder()
                .name("posts")
                .primaryKey(PrimaryKeyModel.builder()
                        .columns(Collections.singletonList("id"))
                        .build())
                .columns(Arrays.asList(
                        ColumnModel.builder().name("id").type("BIGINT").primaryKeyPart(true).build(),
                        ColumnModel.builder().name("user_id").type("BIGINT").build()
                ))
                .foreignKeys(new ArrayList<>())
                .indexes(new ArrayList<>())
                .build();
        
        DatabaseModel model = DatabaseModel.builder()
                .tables(Arrays.asList(users, posts))
                .build();
        
        // 执行推断
        engine.inferRelations(model);
        
        // 验证：应该推断出隐式外键
        assertEquals(1, posts.getForeignKeys().size());
        ForeignKeyModel inferredFk = posts.getForeignKeys().get(0);
        assertEquals("posts", inferredFk.getFromTable());
        assertEquals("users", inferredFk.getToTable());
        assertEquals("user_id", inferredFk.getFromColumns().get(0));
        assertEquals(RelationType.ONE_TO_MANY, inferredFk.getRelationType());
    }
    
    @Test
    void testNullModelHandling() {
        // 测试空模型
        assertDoesNotThrow(() -> engine.inferRelations(null));
        
        DatabaseModel emptyModel = DatabaseModel.builder().build();
        assertDoesNotThrow(() -> engine.inferRelations(emptyModel));
    }
    
    @Test
    void testTableWithoutForeignKeys() {
        TableModel users = TableModel.builder()
                .name("users")
                .primaryKey(PrimaryKeyModel.builder()
                        .columns(Collections.singletonList("id"))
                        .build())
                .columns(Collections.singletonList(
                        ColumnModel.builder().name("id").type("BIGINT").primaryKeyPart(true).build()
                ))
                .foreignKeys(new ArrayList<>())
                .indexes(new ArrayList<>())
                .build();
        
        DatabaseModel model = DatabaseModel.builder()
                .tables(Collections.singletonList(users))
                .build();
        
        // 执行推断
        assertDoesNotThrow(() -> engine.inferRelations(model));
        
        // 验证：没有外键，不应该有变化
        assertTrue(users.getForeignKeys().isEmpty());
    }
}
