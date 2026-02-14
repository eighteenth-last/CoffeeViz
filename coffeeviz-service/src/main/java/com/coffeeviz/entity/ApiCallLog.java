package com.coffeeviz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * API 调用日志
 */
@Data
@TableName("sys_api_call_log")
public class ApiCallLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID（未登录为 null） */
    private Long userId;

    /** 请求方法 GET/POST/PUT/DELETE */
    private String method;

    /** 请求路径 */
    private String path;

    /** HTTP 状态码 */
    private Integer statusCode;

    /** 响应耗时（毫秒） */
    private Long duration;

    /** 客户端 IP */
    private String clientIp;

    /** 调用日期（用于按天统计） */
    private java.time.LocalDate callDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
