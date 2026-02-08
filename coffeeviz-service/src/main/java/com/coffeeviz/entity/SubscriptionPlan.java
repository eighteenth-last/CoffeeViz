package com.coffeeviz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订阅计划实体
 * 
 * @author CoffeeViz Team
 * @since 2.0.0
 */
@Data
@TableName("biz_subscription_plan")
public class SubscriptionPlan {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String planCode;
    
    private String planName;
    
    private String planNameEn;
    
    private String description;
    
    private BigDecimal priceMonthly;
    
    private BigDecimal priceYearly;
    
    private Integer maxRepositories;
    
    private Integer maxDiagramsPerRepo;
    
    private Integer maxSqlSizeMb;
    
    private Integer supportJdbc;
    
    private Integer supportAi;
    
    private Integer supportExport;
    
    private Integer supportTeam;
    
    private Integer prioritySupport;
    
    private String features;
    
    private Integer sortOrder;
    
    private String status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
