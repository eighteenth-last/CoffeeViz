package com.coffeeviz.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.coffeeviz.common.Result;
import com.coffeeviz.dto.LoginRequest;
import com.coffeeviz.dto.LoginResponse;
import com.coffeeviz.dto.QrCodeResponse;
import com.coffeeviz.dto.RegisterRequest;
import com.coffeeviz.dto.SmsCodeRequest;
import com.coffeeviz.dto.SmsLoginRequest;
import com.coffeeviz.dto.WechatLoginRequest;
import com.coffeeviz.entity.User;
import com.coffeeviz.service.UserService;
import com.coffeeviz.service.WechatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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
    
    @Autowired(required = false)
    private com.coffeeviz.service.EmailService emailService;
    
    /**
     * 邮箱验证码缓存（生产环境应使用 Redis）
     */
    private static final Map<String, EmailCodeData> EMAIL_CODE_CACHE = new ConcurrentHashMap<>();
    private static final long EMAIL_CODE_EXPIRE = TimeUnit.MINUTES.toMillis(5);
    private static final long EMAIL_CODE_SEND_INTERVAL = TimeUnit.SECONDS.toMillis(60);
    
    private static class EmailCodeData {
        String code;
        long expireTime;
        long sendTime;
        
        EmailCodeData(String code, long expireTime, long sendTime) {
            this.code = code;
            this.expireTime = expireTime;
            this.sendTime = sendTime;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
        
        boolean canResend() {
            return System.currentTimeMillis() - sendTime > EMAIL_CODE_SEND_INTERVAL;
        }
    }
    
    /**
     * 发送邮箱验证码（注册用）
     */
    @PostMapping("/email/send-code")
    public Result<String> sendEmailCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return Result.error("邮箱不能为空");
        }
        
        // 简单邮箱格式校验
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            return Result.error("邮箱格式不正确");
        }
        
        // 检查邮箱是否已被注册
        try {
            User existingUser = userService.getUserByEmail(email);
            if (existingUser != null) {
                return Result.error("该邮箱已被注册");
            }
        } catch (Exception ignored) {}
        
        // 检查发送频率
        EmailCodeData existing = EMAIL_CODE_CACHE.get(email);
        if (existing != null && !existing.canResend()) {
            return Result.error("发送过于频繁，请稍后再试");
        }
        
        // 生成 6 位验证码
        String code = String.format("%06d", new Random().nextInt(1000000));
        long now = System.currentTimeMillis();
        EMAIL_CODE_CACHE.put(email, new EmailCodeData(code, now + EMAIL_CODE_EXPIRE, now));
        
        // 发送验证码邮件
        try {
            if (emailService != null) {
                String subject = "【CoffeeViz】邮箱验证码";
                String content = "<div style='font-family:sans-serif;max-width:480px;margin:0 auto;padding:24px;'>"
                        + "<h2 style='color:#d97706;'>CoffeeViz 邮箱验证</h2>"
                        + "<p>您的验证码为：</p>"
                        + "<div style='font-size:32px;font-weight:bold;letter-spacing:8px;color:#d97706;padding:16px 0;'>" + code + "</div>"
                        + "<p style='color:#888;font-size:13px;'>验证码 5 分钟内有效，请勿泄露给他人。</p>"
                        + "<p style='color:#888;font-size:13px;'>如非本人操作，请忽略此邮件。</p>"
                        + "</div>";
                emailService.sendSingleEmail(email, subject, content, "email_verify");
                log.info("邮箱验证码已发送: email={}, code={}", email, code);
            } else {
                log.warn("邮件服务未配置，验证码: {} (邮箱: {})", code, email);
            }
        } catch (Exception e) {
            log.error("发送邮箱验证码失败: email={}", email, e);
            return Result.error("验证码发送失败，请稍后重试");
        }
        
        return Result.success("验证码已发送", null);
    }
    
    /**
     * 用户注册（需要邮箱验证码）
     */
    @PostMapping("/register")
    public Result<Long> register(@RequestBody RegisterRequest request) {
        log.info("用户注册: username={}, email={}", request.getUsername(), request.getEmail());
        
        try {
            // 1. 校验必填字段
            if (request.getUsername() == null || request.getUsername().isBlank()) {
                return Result.error("用户名不能为空");
            }
            if (request.getPassword() == null || request.getPassword().isBlank()) {
                return Result.error("密码不能为空");
            }
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                return Result.error("邮箱不能为空");
            }
            if (request.getEmailCode() == null || request.getEmailCode().isBlank()) {
                return Result.error("邮箱验证码不能为空");
            }
            
            // 2. 验证邮箱验证码
            EmailCodeData codeData = EMAIL_CODE_CACHE.get(request.getEmail());
            if (codeData == null) {
                return Result.error("请先获取邮箱验证码");
            }
            if (codeData.isExpired()) {
                EMAIL_CODE_CACHE.remove(request.getEmail());
                return Result.error("验证码已过期，请重新获取");
            }
            if (!codeData.code.equals(request.getEmailCode())) {
                return Result.error("验证码错误");
            }
            
            // 3. 验证通过，删除验证码
            EMAIL_CODE_CACHE.remove(request.getEmail());
            
            // 4. 注册用户
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
    public Result<Map<String, Object>> checkWechatQrCode(@PathVariable("qrCodeId") String qrCodeId) {
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
    
    /**
     * 发送短信验证码
     */
    @PostMapping("/sms/send")
    public Result<String> sendSmsCode(@RequestBody SmsCodeRequest request) {
        log.info("发送短信验证码: phone={}", request.getPhone());
        
        try {
            String code = userService.sendSmsCode(request.getPhone());
            
            // 演示环境返回验证码（生产环境不应返回）
            log.info("【演示模式】验证码已生成: {}", code);
            
            return Result.success("验证码已发送", null);
            
        } catch (Exception e) {
            log.error("发送验证码失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 短信验证码登录
     */
    @PostMapping("/sms/login")
    public Result<LoginResponse> loginWithSms(@RequestBody SmsLoginRequest request) {
        log.info("短信验证码登录: phone={}", request.getPhone());
        
        try {
            // 1. 验证验证码并登录
            User user = userService.loginWithSmsCode(request.getPhone(), request.getCode());
            
            if (user == null) {
                return Result.error(401, "验证码错误");
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
            
            log.info("短信验证码登录成功: userId={}, phone={}", user.getId(), request.getPhone());
            return Result.success("登录成功", response);
            
        } catch (Exception e) {
            log.error("短信验证码登录失败", e);
            return Result.error(e.getMessage());
        }
    }
}
