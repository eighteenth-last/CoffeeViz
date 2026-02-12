package com.coffeeviz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_email_log")
public class EmailLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String templateCode;
    private String toEmail;
    private String subject;
    private String content;
    private String target;
    private String status;
    private String errorMsg;
    private Long senderId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
