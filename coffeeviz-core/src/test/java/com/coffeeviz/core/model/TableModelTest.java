package com.coffeeviz.core.model;

import com.coffeeviz.core.enums.RelationType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TableModel 单元测试
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
class TableModelTest {
    
    @Test
    void testTableModelBuilder() {
        // Given
        ColumnModel idColumn = ColumnModel.builder()
                .name("id")
                .type("BIGINT")
                .nullable(false)
                .primaryKeyPart(true)
                .autoIncrement(true)
                .build();
        
        ColumnModel nameColumn = ColumnModel.builder()
                .name("username")
                .type("VARCHAR")
                .length(50)
                .nullable(false)
                .build();
        
        PrimaryKeyModel primaryKey = PrimaryKeyModel.builder()
                .name("pk_users")
                .columns(Collections.singletonList("id"))
                .build();
        
        IndexModel index = IndexModel.builder()
                .name("idx_username")
                .columns(Collections.singletonList("username"))
                .unique(true)
                .indexType("BTREE")
                .build();
        
        // When
        TableModel table = TableModel.builder()
                .name("users")
                .comment("用户表")
                .columns(Arrays.asList(idColumn, nameColumn))
                .primaryKey(primaryKey)
                .indexes(Collections.singletonList(index))
                .tableType("BASE TABLE")
                .build();
        
        // Then
        assertThat(table.getName()).isEqualTo("users");
        assertThat(table.getComment()).isEqualTo("用户表");
        assertThat(table.getColumns()).hasSize(2);
        assertThat(table.getColumns().get(0).getName()).isEqualTo("id");
        assertThat(table.getColumns().get(1).getName()).isEqualTo("username");
        assertThat(table.getPrimaryKey()).isNotNull();
        assertThat(table.getPrimaryKey().getColumns()).containsExactly("id");
        assertThat(table.getIndexes()).hasSize(1);
        assertThat(table.getTableType()).isEqualTo("BASE TABLE");
    }
    
    @Test
    void testTableModelWithForeignKeys() {
        // Given
        ForeignKeyModel fk = ForeignKeyModel.builder()
                .name("fk_orders_user_id")
                .fromTable("orders")
                .fromColumns(Collections.singletonList("user_id"))
                .toTable("users")
                .toColumns(Collections.singletonList("id"))
                .onDelete("CASCADE")
                .onUpdate("CASCADE")
                .relationType(RelationType.ONE_TO_MANY)
                .build();
        
        // When
        TableModel table = TableModel.builder()
                .name("orders")
                .comment("订单表")
                .foreignKeys(Collections.singletonList(fk))
                .build();
        
        // Then
        assertThat(table.getForeignKeys()).hasSize(1);
        assertThat(table.getForeignKeys().get(0).getFromTable()).isEqualTo("orders");
        assertThat(table.getForeignKeys().get(0).getToTable()).isEqualTo("users");
        assertThat(table.getForeignKeys().get(0).getRelationType()).isEqualTo(RelationType.ONE_TO_MANY);
    }
    
    @Test
    void testTableModelDefaultValues() {
        // When
        TableModel table = TableModel.builder()
                .name("test_table")
                .build();
        
        // Then
        assertThat(table.getColumns()).isNotNull().isEmpty();
        assertThat(table.getForeignKeys()).isNotNull().isEmpty();
        assertThat(table.getIndexes()).isNotNull().isEmpty();
    }
}
