package com.coffeeviz.dto;

import lombok.Data;
import java.util.List;

/**
 * 发送通知请求
 */
@Data
public class SendNotificationRequest {
    private String target;
    private List<String> channels;
    private String title;
    private String content;
}
