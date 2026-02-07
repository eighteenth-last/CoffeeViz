package com.coffeeviz.core.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ParseResult 单元测试
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
class ParseResultTest {
    
    @Test
    void testSuccessResult() {
        // Given
        DatabaseModel model = DatabaseModel.builder()
                .dbType("mysql")
                .schemaName("test_db")
                .build();
        
        // When
        ParseResult result = ParseResult.success(model);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getDatabaseModel()).isEqualTo(model);
        assertThat(result.getMessage()).isEqualTo("解析成功");
        assertThat(result.getWarnings()).isEmpty();
        assertThat(result.getErrors()).isEmpty();
    }
    
    @Test
    void testSuccessResultWithWarnings() {
        // Given
        DatabaseModel model = DatabaseModel.builder()
                .dbType("mysql")
                .schemaName("test_db")
                .build();
        
        // When
        ParseResult result = ParseResult.success(model, 
                Arrays.asList("警告1：表 users 缺少主键", "警告2：字段长度过大"));
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getDatabaseModel()).isEqualTo(model);
        assertThat(result.getWarnings()).hasSize(2);
        assertThat(result.getWarnings()).contains("警告1：表 users 缺少主键");
    }
    
    @Test
    void testErrorResult() {
        // When
        ParseResult result = ParseResult.error("解析失败：SQL 语法错误");
        
        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).isEqualTo("解析失败：SQL 语法错误");
        assertThat(result.getDatabaseModel()).isNull();
    }
    
    @Test
    void testErrorResultWithErrors() {
        // When
        ParseResult result = ParseResult.error("解析失败", 
                Arrays.asList("错误1：无法识别的语法", "错误2：表名重复"));
        
        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).isEqualTo("解析失败");
        assertThat(result.getErrors()).hasSize(2);
        assertThat(result.getErrors()).contains("错误1：无法识别的语法");
    }
    
    @Test
    void testAddWarning() {
        // Given
        DatabaseModel model = DatabaseModel.builder().build();
        ParseResult result = ParseResult.success(model);
        
        // When
        result.addWarning("新增警告");
        
        // Then
        assertThat(result.getWarnings()).hasSize(1);
        assertThat(result.getWarnings()).contains("新增警告");
    }
    
    @Test
    void testAddError() {
        // Given
        ParseResult result = ParseResult.error("初始错误");
        
        // When
        result.addError("新增错误");
        
        // Then
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors()).contains("新增错误");
    }
    
    @Test
    void testBuilder() {
        // Given
        DatabaseModel model = DatabaseModel.builder().build();
        
        // When
        ParseResult result = ParseResult.builder()
                .databaseModel(model)
                .success(true)
                .message("自定义消息")
                .warnings(Arrays.asList("警告1"))
                .errors(Arrays.asList("错误1"))
                .build();
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).isEqualTo("自定义消息");
        assertThat(result.getWarnings()).hasSize(1);
        assertThat(result.getErrors()).hasSize(1);
    }
}
