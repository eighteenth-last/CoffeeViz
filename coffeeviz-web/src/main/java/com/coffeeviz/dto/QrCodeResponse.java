package com.coffeeviz.dto;

import lombok.Data;

/**
 * 二维码响应
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
public class QrCodeResponse {
    
    /**
     * 二维码唯一标识
     */
    private String qrCodeId;
    
    /**
     * 二维码内容（URL）
     */
    private String qrCodeUrl;
    
    /**
     * 二维码图片（Base64）
     */
    private String qrCodeImage;
    
    /**
     * 过期时间（秒）
     */
    private Long expireTime;
    
    public static QrCodeResponse create(String qrCodeId, String qrCodeUrl, String qrCodeImage, Long expireTime) {
        QrCodeResponse response = new QrCodeResponse();
        response.setQrCodeId(qrCodeId);
        response.setQrCodeUrl(qrCodeUrl);
        response.setQrCodeImage(qrCodeImage);
        response.setExpireTime(expireTime);
        return response;
    }
}
