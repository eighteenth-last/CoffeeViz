package com.coffeeviz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 计划配额实体
 * 
 * @author CoffeeViz Team
 * @since 2.0.0
 */
@Data
@TableName("biz_plan_quota")
public class PlanQuota {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 订阅计划ID
     */
    private Long planId;
    
    /**
     * 配额类型（repository/diagram/sql_parse/ai_generate）
     */
    private String quotaType;
    
    /**
     * 配额限制（-1表示无限制）
     */
    private Integer quotaLimit;
    
    /**
     * 重置周期（daily/monthly/yearly/never）
     */
    private String resetCycle;
    
    /**
     * 配额描述
     */
    private String description;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
