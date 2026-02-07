package com.coffeeviz.core.model;

import com.coffeeviz.core.enums.LayoutDirection;
import com.coffeeviz.core.enums.ViewMode;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RenderOptions 单元测试
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
class RenderOptionsTest {
    
    @Test
    void testDefaultRenderOptions() {
        // When
        RenderOptions options = RenderOptions.builder().build();
        
        // Then
        assertThat(options.getViewMode()).isEqualTo(ViewMode.PHYSICAL);
        assertThat(options.getDirection()).isEqualTo(LayoutDirection.TB);
        assertThat(options.isShowComments()).isTrue();
        assertThat(options.isGroupBySchema()).isFalse();
    }
    
    @Test
    void testCustomRenderOptions() {
        // Given
        Set<String> includeTables = new HashSet<>();
        includeTables.add("users");
        includeTables.add("orders");
        
        Set<String> excludeTables = new HashSet<>();
        excludeTables.add("temp_table");
        
        // When
        RenderOptions options = RenderOptions.builder()
                .viewMode(ViewMode.LOGICAL)
                .includeTables(includeTables)
                .excludeTables(excludeTables)
                .relationDepth(2)
                .direction(LayoutDirection.LR)
                .showComments(false)
                .groupBySchema(true)
                .tablePrefix("sys_")
                .dialect("mysql")
                .build();
        
        // Then
        assertThat(options.getViewMode()).isEqualTo(ViewMode.LOGICAL);
        assertThat(options.getIncludeTables()).hasSize(2);
        assertThat(options.getIncludeTables()).contains("users", "orders");
        assertThat(options.getExcludeTables()).hasSize(1);
        assertThat(options.getExcludeTables()).contains("temp_table");
        assertThat(options.getRelationDepth()).isEqualTo(2);
        assertThat(options.getDirection()).isEqualTo(LayoutDirection.LR);
        assertThat(options.isShowComments()).isFalse();
        assertThat(options.isGroupBySchema()).isTrue();
        assertThat(options.getTablePrefix()).isEqualTo("sys_");
        assertThat(options.getDialect()).isEqualTo("mysql");
    }
    
    @Test
    void testConceptualViewMode() {
        // When
        RenderOptions options = RenderOptions.builder()
                .viewMode(ViewMode.CONCEPTUAL)
                .build();
        
        // Then
        assertThat(options.getViewMode()).isEqualTo(ViewMode.CONCEPTUAL);
    }
    
    @Test
    void testLeftToRightLayout() {
        // When
        RenderOptions options = RenderOptions.builder()
                .direction(LayoutDirection.LR)
                .build();
        
        // Then
        assertThat(options.getDirection()).isEqualTo(LayoutDirection.LR);
    }
}
