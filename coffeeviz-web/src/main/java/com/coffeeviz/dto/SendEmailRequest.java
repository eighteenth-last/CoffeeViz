package com.coffeeviz.dto;

import lombok.Data;

@Data
public class SendEmailRequest {
    /** 发送目标: all / pro / specific */
    private String target;
    /** 指定用户邮箱（target=specific 时使用，逗号分隔） */
    private String emails;
    /** 使用的模板编码 */
    private String templateCode;
    /** 邮件主题（覆盖模板默认主题） */
    private String subject;
    /** 邮件正文（覆盖模板默认内容） */
    private String content;
}
