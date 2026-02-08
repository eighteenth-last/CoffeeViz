package com.coffeeviz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户使用配额实体
 */
@Data
@TableName("biz_usage_quota")
public class UsageQuota {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String quotaType;
    
    private Integer quotaLimit;
    
    private Integer quotaUsed;
    
    private String resetCycle;
    
    private LocalDateTime lastResetTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
