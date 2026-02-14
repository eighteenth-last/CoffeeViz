package com.coffeeviz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 额度使用记录
 */
@Data
@TableName("biz_quota_usage_log")
public class QuotaUsageLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 配额类型: sql_parse / ai_generate / repository / diagram */
    private String quotaType;

    /** 消耗数量 */
    private Integer amount;

    /** 使用场景描述 */
    private String action;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
