package com.coffeeviz.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.coffeeviz.common.Result;
import com.coffeeviz.dto.LoginRequest;
import com.coffeeviz.dto.LoginResponse;
import com.coffeeviz.dto.QrCodeResponse;
import com.coffeeviz.dto.RegisterRequest;
import com.coffeeviz.dto.WechatLoginRequest;
import com.coffeeviz.entity.User;
import com.coffeeviz.service.UserService;
import com.coffeeviz.service.WechatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证授权 Controller
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private WechatService wechatService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Long> register(@RequestBody RegisterRequest request) {
        log.info("用户注册: username={}", request.getUsername());
        
        try {
            User user = userService.register(
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                request.getPhone()
            );
            
            return Result.success("注册成功", user.getId());
            
        } catch (Exception e) {
            log.error("注册失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        log.info("用户登录: username={}", request.getUsername());
        
        try {
            // 1. 验证用户名密码
            User user = userService.login(request.getUsername(), request.getPassword());
            
            if (user == null) {
                return Result.error(401, "用户名或密码错误");
            }
            
            // 2. 登录成功，生成 Token
            StpUtil.login(user.getId());
            String token = StpUtil.getTokenValue();
            
            // 3. 构建响应
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setDisplayName(user.getDisplayName());
            userInfo.setEmail(user.getEmail());
            userInfo.setPhone(user.getPhone());
            userInfo.setAvatarUrl(user.getAvatarUrl());
            response.setUserInfo(userInfo);
            
            log.info("登录成功: userId={}, token={}", user.getId(), token);
            return Result.success("登录成功", response);
            
        } catch (Exception e) {
            log.error("登录失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            StpUtil.logout();
            
            log.info("登出成功: userId={}", userId);
            return Result.success("登出成功", "success");
            
        } catch (Exception e) {
            log.error("登出失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/userinfo")
    public Result<LoginResponse.UserInfo> getUserInfo() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            User user = userService.getUserById(userId);
            
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setDisplayName(user.getDisplayName());
            userInfo.setEmail(user.getEmail());
            userInfo.setPhone(user.getPhone());
            userInfo.setAvatarUrl(user.getAvatarUrl());
            
            return Result.success(userInfo);
            
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 生成微信登录二维码
     */
    @GetMapping("/wechat/qrcode")
    public Result<QrCodeResponse> generateWechatQrCode() {
        log.info("生成微信登录二维码");
        
        try {
            Map<String, Object> qrCodeData = wechatService.generateQrCode();
            
            QrCodeResponse response = QrCodeResponse.create(
                (String) qrCodeData.get("qrCodeId"),
                (String) qrCodeData.get("qrCodeUrl"),
                null, // 前端自己生成二维码图片
                (Long) qrCodeData.get("expireTime")
            );
            
            return Result.success("二维码生成成功", response);
            
        } catch (Exception e) {
            log.error("生成微信登录二维码失败", e);
            return Result.error("生成二维码失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查二维码扫描状态（轮询接口）
     */
    @GetMapping("/wechat/check/{qrCodeId}")
    public Result<Map<String, Object>> checkWechatQrCode(@PathVariable String qrCodeId) {
        try {
            Map<String, Object> status = wechatService.checkQrCodeStatus(qrCodeId);
            return Result.success(status);
            
        } catch (Exception e) {
            log.error("检查二维码状态失败", e);
            return Result.error("检查状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 微信扫码回调（模拟接口，实际应该由微信服务器回调）
     */
    @PostMapping("/wechat/callback")
    public Result<LoginResponse> wechatCallback(@RequestBody WechatLoginRequest request) {
        log.info("微信扫码回调: openId={}", request.getOpenId());
        
        try {
            // 处理微信登录
            Map<String, Object> loginResult = wechatService.handleWechatCallback(
                request.getCode(), // 这里用code作为qrCodeId
                request.getOpenId(),
                request.getNickname(),
                request.getAvatarUrl()
            );
            
            // 构建响应
            LoginResponse response = new LoginResponse();
            response.setToken((String) loginResult.get("token"));
            
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
            userInfo.setId((Long) loginResult.get("userId"));
            userInfo.setUsername((String) loginResult.get("username"));
            userInfo.setDisplayName(request.getNickname());
            userInfo.setAvatarUrl(request.getAvatarUrl());
            response.setUserInfo(userInfo);
            
            return Result.success("微信登录成功", response);
            
        } catch (Exception e) {
            log.error("微信登录失败", e);
            return Result.error("微信登录失败: " + e.getMessage());
        }
    }
}
