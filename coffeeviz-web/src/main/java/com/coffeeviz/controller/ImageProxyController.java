package com.coffeeviz.controller;

import com.coffeeviz.service.MinioService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;

/**
 * 图片代理 Controller
 * 用于代理 MinIO 图片，解决 CORS 问题
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/image")
@CrossOrigin(origins = "*")  // 添加跨域支持
public class ImageProxyController {
    
    @Autowired(required = false)
    private MinioClient minioClient;
    
    @Value("${minio.bucket-name}")
    private String bucketName;
    
    /**
     * 代理获取图片 - 支持多级路径
     * 
     * @param request HTTP 请求
     * @return 图片字节流
     */
    @GetMapping("/**")
    public ResponseEntity<byte[]> getImage(HttpServletRequest request) {
        
        // 从请求路径中提取文件路径
        String requestPath = request.getRequestURI();
        String objectName = requestPath.substring("/api/image/".length());
        
        // URL 解码，处理中文文件名
        try {
            objectName = java.net.URLDecoder.decode(objectName, "UTF-8");
        } catch (Exception e) {
            log.warn("URL 解码失败: {}", objectName);
        }
        
        log.info("代理获取图片: {}", objectName);
        
        try {
            if (minioClient == null) {
                log.error("MinIO 客户端未初始化");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
            
            // 从 MinIO 获取图片
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            
            // 读取所有字节
            byte[] imageBytes = stream.readAllBytes();
            stream.close();
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            
            // 根据文件扩展名设置 Content-Type
            if (objectName.endsWith(".png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (objectName.endsWith(".jpg") || objectName.endsWith(".jpeg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (objectName.endsWith(".svg")) {
                headers.setContentType(MediaType.valueOf("image/svg+xml"));
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            
            // 设置 CORS 头，解决 ERR_BLOCKED_BY_ORB 问题
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Cross-Origin-Resource-Policy", "cross-origin");
            
            // 设置缓存
            headers.setCacheControl("public, max-age=31536000");
            
            log.info("图片获取成功: {}, 大小: {} bytes", objectName, imageBytes.length);
            
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("获取图片失败: {}", objectName, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
