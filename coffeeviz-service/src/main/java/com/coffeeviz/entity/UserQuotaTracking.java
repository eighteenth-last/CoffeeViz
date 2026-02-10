package com.coffeeviz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户配额跟踪实体
 * 
 * @author CoffeeViz Team
 * @since 2.0.0
 */
@Data
@TableName("biz_user_quota_tracking")
public class UserQuotaTracking {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 计划配额ID
     */
    private Long planQuotaId;
    
    /**
     * 配额类型
     */
    private String quotaType;
    
    /**
     * 当前配额限制
     */
    private Integer quotaLimit;
    
    /**
     * 已使用配额
     */
    private Integer quotaUsed;
    
    /**
     * 上次重置时间
     */
    private LocalDateTime lastResetTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
