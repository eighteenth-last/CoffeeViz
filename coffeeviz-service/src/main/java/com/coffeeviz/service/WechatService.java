package com.coffeeviz.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coffeeviz.entity.User;
import com.coffeeviz.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 微信登录服务
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class WechatService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String QR_CODE_PREFIX = "qrcode:";
    private static final String QR_SCAN_PREFIX = "qrscan:";
    private static final long QR_CODE_EXPIRE_SECONDS = 300; // 5分钟
    
    /**
     * 生成登录二维码
     * 
     * @return 二维码信息
     */
    public Map<String, Object> generateQrCode() {
        // 生成唯一的二维码ID
        String qrCodeId = UUID.randomUUID().toString().replace("-", "");
        
        // 构建二维码URL（实际应用中应该是微信公众号的授权URL）
        String qrCodeUrl = buildWechatAuthUrl(qrCodeId);
        
        // 存储二维码状态到Redis
        if (redisTemplate != null) {
            Map<String, Object> qrCodeData = new HashMap<>();
            qrCodeData.put("status", "pending"); // pending, scanned, confirmed, expired
            qrCodeData.put("createTime", System.currentTimeMillis());
            
            redisTemplate.opsForValue().set(
                QR_CODE_PREFIX + qrCodeId, 
                qrCodeData, 
                QR_CODE_EXPIRE_SECONDS, 
                TimeUnit.SECONDS
            );
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("qrCodeId", qrCodeId);
        result.put("qrCodeUrl", qrCodeUrl);
        result.put("expireTime", QR_CODE_EXPIRE_SECONDS);
        
        log.info("生成登录二维码: qrCodeId={}", qrCodeId);
        return result;
    }
    
    /**
     * 检查二维码扫描状态
     * 
     * @param qrCodeId 二维码ID
     * @return 状态信息
     */
    public Map<String, Object> checkQrCodeStatus(String qrCodeId) {
        Map<String, Object> result = new HashMap<>();
        
        if (redisTemplate == null) {
            result.put("status", "error");
            result.put("message", "Redis未配置");
            return result;
        }
        
        // 从Redis获取二维码状态
        Object qrCodeData = redisTemplate.opsForValue().get(QR_CODE_PREFIX + qrCodeId);
        
        if (qrCodeData == null) {
            result.put("status", "expired");
            result.put("message", "二维码已过期");
            return result;
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) qrCodeData;
        String status = (String) data.get("status");
        
        result.put("status", status);
        
        // 如果已确认登录，返回token
        if ("confirmed".equals(status)) {
            String token = (String) data.get("token");
            Long userId = (Long) data.get("userId");
            result.put("token", token);
            result.put("userId", userId);
            
            // 删除二维码数据
            redisTemplate.delete(QR_CODE_PREFIX + qrCodeId);
        }
        
        return result;
    }
    
    /**
     * 微信扫码回调处理
     * 
     * @param qrCodeId 二维码ID
     * @param openId 微信OpenID
     * @param nickname 微信昵称
     * @param avatarUrl 微信头像
     * @return 登录结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> handleWechatCallback(String qrCodeId, String openId, String nickname, String avatarUrl) {
        log.info("处理微信扫码回调: qrCodeId={}, openId={}", qrCodeId, openId);
        
        Map<String, Object> result = new HashMap<>();
        
        // 1. 查找或创建用户
        User user = findOrCreateUserByOpenId(openId, nickname, avatarUrl);
        
        // 2. 生成登录token
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        
        // 3. 更新二维码状态
        if (redisTemplate != null) {
            Map<String, Object> qrCodeData = new HashMap<>();
            qrCodeData.put("status", "confirmed");
            qrCodeData.put("token", token);
            qrCodeData.put("userId", user.getId());
            qrCodeData.put("confirmTime", System.currentTimeMillis());
            
            redisTemplate.opsForValue().set(
                QR_CODE_PREFIX + qrCodeId,
                qrCodeData,
                60, // 确认后保留60秒供前端获取
                TimeUnit.SECONDS
            );
        }
        
        result.put("success", true);
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        
        log.info("微信登录成功: userId={}, username={}", user.getId(), user.getUsername());
        return result;
    }
    
    /**
     * 根据OpenID查找或创建用户
     */
    private User findOrCreateUserByOpenId(String openId, String nickname, String avatarUrl) {
        // 使用OpenID作为用户名查找用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, "wx_" + openId);
        
        User user = userMapper.selectOne(wrapper);
        
        if (user == null) {
            // 创建新用户
            user = new User();
            user.setUsername("wx_" + openId);
            user.setPassword(""); // 微信登录不需要密码
            user.setDisplayName(nickname != null ? nickname : "微信用户");
            user.setAvatarUrl(avatarUrl);
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            
            userMapper.insert(user);
            log.info("创建微信用户: userId={}, openId={}", user.getId(), openId);
        } else {
            // 更新用户信息
            if (nickname != null) {
                user.setDisplayName(nickname);
            }
            if (avatarUrl != null) {
                user.setAvatarUrl(avatarUrl);
            }
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
        }
        
        return user;
    }
    
    /**
     * 构建微信授权URL
     * 实际应用中应该使用微信公众号的授权URL
     */
    private String buildWechatAuthUrl(String qrCodeId) {
        // 这里返回一个模拟的URL，实际应该是：
        // https://open.weixin.qq.com/connect/qrconnect?appid=YOUR_APPID&redirect_uri=YOUR_CALLBACK&response_type=code&scope=snsapi_login&state=STATE#wechat_redirect
        
        String appId = "YOUR_WECHAT_APPID"; // 从配置中读取
        String redirectUri = "http://localhost:8080/api/auth/wechat/callback";
        
        return String.format(
            "https://open.weixin.qq.com/connect/qrconnect?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_login&state=%s#wechat_redirect",
            appId, redirectUri, qrCodeId
        );
    }
}
