package com.coffeeviz.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.coffeeviz.common.Result;
import com.coffeeviz.dto.ConfigUpdateRequest;
import com.coffeeviz.dto.PasswordUpdateRequest;
import com.coffeeviz.dto.UserUpdateRequest;
import com.coffeeviz.entity.User;
import com.coffeeviz.service.ConfigService;
import com.coffeeviz.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 设置管理 Controller
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/setting")
public class SettingController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ConfigService configService;
    
    /**
     * 获取当前用户设置
     */
    @GetMapping("/user")
    public Result<User> getUserSettings() {
        log.info("获取用户设置");
        
        try {
            // 1. 获取当前用户 ID
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 2. 查询用户信息
            User user = userService.getUserById(userId);
            
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            
            // 3. 清除敏感信息
            user.setPassword(null);
            
            return Result.success(user);
            
        } catch (Exception e) {
            log.error("获取用户设置失败", e);
            return Result.error("获取用户设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新用户设置
     */
    @PutMapping("/user")
    public Result<String> updateUserSettings(@RequestBody UserUpdateRequest request) {
        log.info("更新用户设置");
        
        try {
            // 1. 获取当前用户 ID
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 2. 更新用户信息
            userService.updateUserInfo(
                userId,
                request.getEmail(),
                request.getPhone(),
                request.getDisplayName(),
                request.getJobTitle(),
                request.getAvatarUrl()
            );
            
            log.info("用户设置更新成功: userId={}", userId);
            return Result.success("用户设置更新成功", "success");
            
        } catch (Exception e) {
            log.error("更新用户设置失败", e);
            return Result.error("更新用户设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新密码
     */
    @PutMapping("/password")
    public Result<String> updatePassword(@RequestBody PasswordUpdateRequest request) {
        log.info("更新密码");
        
        try {
            // 1. 参数校验
            if (request.getOldPassword() == null || request.getOldPassword().trim().isEmpty()) {
                return Result.error(400, "旧密码不能为空");
            }
            
            if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
                return Result.error(400, "新密码不能为空");
            }
            
            if (request.getNewPassword().length() < 6) {
                return Result.error(400, "新密码长度不能少于 6 位");
            }
            
            // 2. 获取当前用户 ID
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 3. 更新密码
            userService.updatePassword(userId, request.getOldPassword(), request.getNewPassword());
            
            log.info("密码更新成功: userId={}", userId);
            return Result.success("密码更新成功", "success");
            
        } catch (Exception e) {
            log.error("更新密码失败", e);
            return Result.error("更新密码失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取系统配置
     */
    @GetMapping("/config")
    public Result<Map<String, String>> getSystemConfig() {
        log.info("获取系统配置");
        
        try {
            // 获取所有配置（敏感信息已脱敏）
            Map<String, String> configs = configService.getAllConfigs();
            
            return Result.success(configs);
            
        } catch (Exception e) {
            log.error("获取系统配置失败", e);
            return Result.error("获取系统配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取单个配置
     */
    @GetMapping("/config/{configKey}")
    public Result<String> getConfig(@PathVariable("configKey") String configKey) {
        log.info("获取配置: key={}", configKey);
        
        try {
            String value = configService.getConfig(configKey);
            
            if (value == null) {
                return Result.error(404, "配置不存在");
            }
            
            return Result.success(value);
            
        } catch (Exception e) {
            log.error("获取配置失败", e);
            return Result.error("获取配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新系统配置
     */
    @PutMapping("/config")
    public Result<String> updateSystemConfig(@RequestBody ConfigUpdateRequest request) {
        log.info("更新系统配置: {}", request);
        
        try {
            // 1. 参数校验
            if (request.getConfigs() != null && !request.getConfigs().isEmpty()) {
                // 批量配置更新
                log.info("批量更新配置，数量: {}", request.getConfigs().size());
                configService.updateConfigs(request.getConfigs());
            } else if (request.getConfigKey() != null && request.getConfigValue() != null) {
                // 单个配置更新
                log.info("更新单个配置: key={}", request.getConfigKey());
                configService.updateConfig(request.getConfigKey(), request.getConfigValue());
            } else {
                log.warn("配置更新请求参数无效: {}", request);
                return Result.error(400, "请提供配置信息");
            }
            
            log.info("系统配置更新成功");
            return Result.success("系统配置更新成功", "success");
            
        } catch (Exception e) {
            log.error("更新系统配置失败", e);
            return Result.error("更新系统配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除配置
     */
    @DeleteMapping("/config/{configKey}")
    public Result<String> deleteConfig(@PathVariable("configKey") String configKey) {
        log.info("删除配置: key={}", configKey);
        
        try {
            configService.deleteConfig(configKey);
            
            log.info("配置删除成功: key={}", configKey);
            return Result.success("配置删除成功", "success");
            
        } catch (Exception e) {
            log.error("删除配置失败", e);
            return Result.error("删除配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 初始化默认配置
     */
    @PostMapping("/config/init")
    public Result<String> initDefaultConfigs() {
        log.info("初始化默认配置");
        
        try {
            configService.initDefaultConfigs();
            
            log.info("默认配置初始化成功");
            return Result.success("默认配置初始化成功", "success");
            
        } catch (Exception e) {
            log.error("初始化默认配置失败", e);
            return Result.error("初始化默认配置失败: " + e.getMessage());
        }
    }
}
