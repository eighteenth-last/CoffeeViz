package com.coffeeviz.core.renderer;

import com.coffeeviz.core.model.DatabaseModel;
import com.coffeeviz.core.model.RenderOptions;

/**
 * Mermaid 渲染器接口
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
public interface MermaidRenderer {
    
    /**
     * 渲染 SchemaModel 为 Mermaid ER 图代码
     * 
     * @param model 数据库模型
     * @param options 渲染选项
     * @return Mermaid 代码
     */
    String render(DatabaseModel model, RenderOptions options);
}
