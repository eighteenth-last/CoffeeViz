package com.coffeeviz.core.model;

import com.coffeeviz.core.enums.LayoutDirection;
import com.coffeeviz.core.enums.ViewMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 渲染选项
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenderOptions {
    
    /**
     * 视图模式（PHYSICAL、LOGICAL、CONCEPTUAL）
     */
    @Builder.Default
    private ViewMode viewMode = ViewMode.PHYSICAL;
    
    /**
     * 包含的表（null 表示全部）
     */
    private Set<String> includeTables;
    
    /**
     * 排除的表
     */
    private Set<String> excludeTables;
    
    /**
     * 关系深度（null 表示全部）
     */
    private Integer relationDepth;
    
    /**
     * 布局方向（TB 或 LR）
     */
    @Builder.Default
    private LayoutDirection direction = LayoutDirection.TB;
    
    /**
     * 是否显示注释
     */
    @Builder.Default
    private boolean showComments = true;
    
    /**
     * 是否按 Schema 分组
     */
    @Builder.Default
    private boolean groupBySchema = false;
    
    /**
     * 按表前缀分组（如 "sys_"、"biz_"）
     */
    private String tablePrefix;
    
    /**
     * SQL 方言（mysql、postgres、auto）
     */
    private String dialect;
}
