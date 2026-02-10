package com.coffeeviz.controller;

import com.coffeeviz.common.Result;
import com.coffeeviz.service.MinioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传 Controller
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
@CrossOrigin(origins = "*")
public class FileController {

    @Autowired(required = false)
    private MinioService minioService;

    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("上传文件: {}", file.getOriginalFilename());
        
        if (minioService == null) {
            return Result.error("MinIO service not available");
        }
        
        try {
            // 生成文件名: uploads/timestamp_filename
            String fileName = "uploads/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            
            // 上传
            String url = minioService.uploadFile(
                file.getInputStream(), 
                fileName, 
                file.getContentType(), 
                file.getSize()
            );
            
            log.info("文件上传成功: {}", url);
            return Result.success("上传成功", url);
            
        } catch (Exception e) {
            log.error("上传文件失败", e);
            return Result.error("上传文件失败: " + e.getMessage());
        }
    }
}
