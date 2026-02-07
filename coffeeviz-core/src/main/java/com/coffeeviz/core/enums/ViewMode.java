package com.coffeeviz.core.enums;

/**
 * 视图模式枚举
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
public enum ViewMode {
    /**
     * 物理视图 - 显示所有字段和类型
     */
    PHYSICAL,
    
    /**
     * 逻辑视图 - 仅显示主键、外键、关键字段
     */
    LOGICAL,
    
    /**
     * 概念视图 - 仅显示表名和关系
     */
    CONCEPTUAL
}
