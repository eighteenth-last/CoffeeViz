package com.coffeeviz.core.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ColumnModel 单元测试
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
class ColumnModelTest {
    
    @Test
    void testColumnModelBuilder() {
        // When
        ColumnModel column = ColumnModel.builder()
                .name("id")
                .type("BIGINT")
                .rawType("BIGINT(20)")
                .nullable(false)
                .primaryKeyPart(true)
                .autoIncrement(true)
                .comment("主键ID")
                .build();
        
        // Then
        assertThat(column.getName()).isEqualTo("id");
        assertThat(column.getType()).isEqualTo("BIGINT");
        assertThat(column.getRawType()).isEqualTo("BIGINT(20)");
        assertThat(column.isNullable()).isFalse();
        assertThat(column.isPrimaryKeyPart()).isTrue();
        assertThat(column.isAutoIncrement()).isTrue();
        assertThat(column.getComment()).isEqualTo("主键ID");
    }
    
    @Test
    void testVarcharColumn() {
        // When
        ColumnModel column = ColumnModel.builder()
                .name("username")
                .type("VARCHAR")
                .length(50)
                .nullable(false)
                .defaultValue("''")
                .comment("用户名")
                .build();
        
        // Then
        assertThat(column.getName()).isEqualTo("username");
        assertThat(column.getType()).isEqualTo("VARCHAR");
        assertThat(column.getLength()).isEqualTo(50);
        assertThat(column.isNullable()).isFalse();
        assertThat(column.getDefaultValue()).isEqualTo("''");
    }
    
    @Test
    void testDecimalColumn() {
        // When
        ColumnModel column = ColumnModel.builder()
                .name("price")
                .type("DECIMAL")
                .precision(10)
                .scale(2)
                .nullable(false)
                .defaultValue("0.00")
                .comment("价格")
                .build();
        
        // Then
        assertThat(column.getName()).isEqualTo("price");
        assertThat(column.getType()).isEqualTo("DECIMAL");
        assertThat(column.getPrecision()).isEqualTo(10);
        assertThat(column.getScale()).isEqualTo(2);
        assertThat(column.getDefaultValue()).isEqualTo("0.00");
    }
    
    @Test
    void testNullableColumn() {
        // When
        ColumnModel column = ColumnModel.builder()
                .name("description")
                .type("TEXT")
                .nullable(true)
                .build();
        
        // Then
        assertThat(column.isNullable()).isTrue();
        assertThat(column.isPrimaryKeyPart()).isFalse();
        assertThat(column.isAutoIncrement()).isFalse();
    }
}
