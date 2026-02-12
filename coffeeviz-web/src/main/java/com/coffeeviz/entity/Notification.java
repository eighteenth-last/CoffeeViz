package com.coffeeviz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统通知实体
 */
@Data
@TableName("sys_notification")
public class Notification {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /** 发送目标: all, pro, team, specific */
    private String target;
    
    /** 发送渠道: inbox,email,sms */
    private String channels;
    
    /** 通知标题 */
    private String title;
    
    /** 通知内容 */
    private String content;
    
    /** 发送人ID */
    private Long senderId;
    
    /** 状态: sent, failed */
    private String status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
