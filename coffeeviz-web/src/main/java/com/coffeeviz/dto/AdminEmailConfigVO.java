package com.coffeeviz.dto;

import lombok.Data;

@Data
public class AdminEmailConfigVO {
    private Boolean enabled;
    private String provider;
    private String apiKey;
    private String secretKey;
    private String senderEmail;
    private String senderName;
    private String testEmail;
    private Boolean testMode;
}
