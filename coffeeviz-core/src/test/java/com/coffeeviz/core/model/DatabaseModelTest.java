package com.coffeeviz.core.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DatabaseModel 单元测试
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
class DatabaseModelTest {
    
    @Test
    void testDatabaseModelBuilder() {
        // Given
        Map<String, String> metadata = new HashMap<>();
        metadata.put("version", "8.0.32");
        metadata.put("charset", "utf8mb4");
        
        TableModel table1 = TableModel.builder()
                .name("users")
                .comment("用户表")
                .build();
        
        TableModel table2 = TableModel.builder()
                .name("orders")
                .comment("订单表")
                .build();
        
        // When
        DatabaseModel model = DatabaseModel.builder()
                .dbType("mysql")
                .schemaName("test_db")
                .tables(Arrays.asList(table1, table2))
                .metadata(metadata)
                .build();
        
        // Then
        assertThat(model.getDbType()).isEqualTo("mysql");
        assertThat(model.getSchemaName()).isEqualTo("test_db");
        assertThat(model.getTables()).hasSize(2);
        assertThat(model.getTables().get(0).getName()).isEqualTo("users");
        assertThat(model.getTables().get(1).getName()).isEqualTo("orders");
        assertThat(model.getMetadata()).containsEntry("version", "8.0.32");
        assertThat(model.getMetadata()).containsEntry("charset", "utf8mb4");
    }
    
    @Test
    void testDatabaseModelDefaultValues() {
        // When
        DatabaseModel model = DatabaseModel.builder()
                .dbType("postgres")
                .schemaName("public")
                .build();
        
        // Then
        assertThat(model.getTables()).isNotNull().isEmpty();
        assertThat(model.getViews()).isNotNull().isEmpty();
        assertThat(model.getMetadata()).isNotNull().isEmpty();
    }
    
    @Test
    void testDatabaseModelWithViews() {
        // Given
        ViewModel view = ViewModel.builder()
                .name("user_summary")
                .comment("用户摘要视图")
                .build();
        
        // When
        DatabaseModel model = DatabaseModel.builder()
                .dbType("mysql")
                .schemaName("test_db")
                .views(Arrays.asList(view))
                .build();
        
        // Then
        assertThat(model.getViews()).hasSize(1);
        assertThat(model.getViews().get(0).getName()).isEqualTo("user_summary");
    }
}
