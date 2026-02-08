package com.coffeeviz.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 对象存储服务
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class MinioService {
    
    @Autowired
    private MinioClient minioClient;
    
    @Value("${minio.bucket-name}")
    private String bucketName;
    
    @Value("${minio.endpoint}")
    private String endpoint;
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    /**
     * 初始化：确保存储桶存在
     */
    @PostConstruct
    public void init() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            
            if (!exists) {
                log.info("创建 MinIO 存储桶: {}", bucketName);
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                
                log.info("MinIO 存储桶创建成功");
            } else {
                log.info("MinIO 存储桶已存在: {}", bucketName);
            }
            
            // 设置存储桶为公开读取（使用简化的策略）
            String policy = String.format("""
                    {
                        "Version": "2012-10-17",
                        "Statement": [
                            {
                                "Effect": "Allow",
                                "Principal": {"AWS": ["*"]},
                                "Action": ["s3:GetObject"],
                                "Resource": ["arn:aws:s3:::%s/*"]
                            }
                        ]
                    }
                    """, bucketName);
            
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(policy)
                            .build()
            );
            
            log.info("MinIO 存储桶策略设置成功");
            
        } catch (Exception e) {
            log.error("初始化 MinIO 存储桶失败", e);
        }
    }
    
    /**
     * 上传文件
     * 
     * @param content 文件内容
     * @param fileName 文件名
     * @param contentType 内容类型
     * @return 文件访问 URL
     */
    public String uploadFile(String content, String fileName, String contentType) {
        try {
            byte[] bytes = content.getBytes();
            InputStream inputStream = new ByteArrayInputStream(bytes);
            
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, bytes.length, -1)
                            .contentType(contentType)
                            .build()
            );
            
            // 返回公开访问 URL
            String fileUrl = endpoint + "/" + bucketName + "/" + fileName;
            log.info("文件上传成功: {}", fileUrl);
            return fileUrl;
            
        } catch (Exception e) {
            log.error("上传文件失败: {}", fileName, e);
            throw new RuntimeException("上传文件失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 上传文件（InputStream）
     * 
     * @param inputStream 输入流
     * @param fileName 文件名
     * @param contentType 内容类型
     * @param size 文件大小
     * @return 文件访问 URL（通过后端代理）
     */
    public String uploadFile(InputStream inputStream, String fileName, String contentType, long size) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );
            
            // 返回代理 URL（通过后端接口访问）
            // 格式: http://localhost:8080/api/image/{folder}/{fileName}
            String[] parts = fileName.split("/", 2);
            String fileUrl = "http://localhost:" + serverPort + "/api/image/" + fileName;
            
            log.info("文件上传成功: {}", fileUrl);
            return fileUrl;
            
        } catch (Exception e) {
            log.error("上传文件失败: {}", fileName, e);
            throw new RuntimeException("上传文件失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 删除文件
     * 
     * @param fileName 文件名
     */
    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            log.info("文件删除成功: {}", fileName);
        } catch (Exception e) {
            log.error("删除文件失败: {}", fileName, e);
            throw new RuntimeException("删除文件失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取文件访问 URL（通过后端代理）
     * 
     * @param fileName 文件名
     * @return 文件访问 URL
     */
    public String getFileUrl(String fileName) {
        return "http://localhost:" + serverPort + "/api/image/" + fileName;
    }
    
    /**
     * 获取文件预签名 URL（临时访问）
     * 
     * @param fileName 文件名
     * @param expiry 过期时间（秒）
     * @return 预签名 URL
     */
    public String getPresignedUrl(String fileName, int expiry) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileName)
                            .expiry(expiry, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            log.error("获取预签名 URL 失败: {}", fileName, e);
            throw new RuntimeException("获取预签名 URL 失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 检查文件是否存在
     * 
     * @param fileName 文件名
     * @return 是否存在
     */
    public boolean fileExists(String fileName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
