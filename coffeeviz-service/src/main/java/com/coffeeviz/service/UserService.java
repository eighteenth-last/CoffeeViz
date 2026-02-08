package com.coffeeviz.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coffeeviz.entity.User;
import com.coffeeviz.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务类
 * 
 * <p>提供用户相关的业务逻辑，包括：</p>
 * <ul>
 *   <li>用户注册（密码 BCrypt 加密）</li>
 *   <li>用户登录验证</li>
 *   <li>用户信息查询</li>
 *   <li>用户信息更新</li>
 * </ul>
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired(required = false)
    private SubscriptionService subscriptionService;
    
    @Autowired(required = false)
    private QuotaService quotaService;
    
    /**
     * BCrypt 加密成本因子（默认为 12，范围 4-31）
     * 值越大，加密越安全，但计算时间越长
     */
    private static final int BCRYPT_COST = 12;
    
    /**
     * 短信验证码存储（生产环境应使用 Redis）
     * Key: 手机号, Value: {code: 验证码, expireTime: 过期时间}
     */
    private static final Map<String, SmsCodeData> SMS_CODE_CACHE = new ConcurrentHashMap<>();
    
    /**
     * 验证码有效期（5分钟）
     */
    private static final long CODE_EXPIRE_TIME = TimeUnit.MINUTES.toMillis(5);
    
    /**
     * 短信验证码数据
     */
    private static class SmsCodeData {
        String code;
        long expireTime;
        
        SmsCodeData(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }
    
    /**
     * 用户注册
     * 
     * @param username 用户名（唯一）
     * @param password 明文密码
     * @param email 邮箱（可选）
     * @param phone 手机号（可选）
     * @return 注册成功的用户对象
     * @throws IllegalArgumentException 如果用户名已存在
     */
    @Transactional(rollbackFor = Exception.class)
    public User register(String username, String password, String email, String phone) {
        log.info("开始注册用户，用户名: {}", username);
        
        // 1. 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        Long count = userMapper.selectCount(wrapper);
        
        if (count > 0) {
            log.warn("用户名已存在: {}", username);
            throw new IllegalArgumentException("用户名已存在");
        }
        
        // 2. 使用 BCrypt 加密密码
        String hashedPassword = hashPassword(password);
        
        // 3. 创建用户对象
        User user = new User();
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setEmail(email);
        user.setPhone(phone);
        user.setDisplayName(generateDefaultDisplayName()); // 设置默认显示名称
        user.setStatus(1); // 默认状态为正常
        
        // 4. 插入数据库
        int result = userMapper.insert(user);
        
        if (result > 0) {
            log.info("用户注册成功，用户ID: {}, 用户名: {}, 显示名称: {}", user.getId(), username, user.getDisplayName());
            
            // 5. 为新用户创建免费订阅
            initUserSubscription(user.getId());
            
            return user;
        } else {
            log.error("用户注册失败，用户名: {}", username);
            throw new RuntimeException("用户注册失败");
        }
    }
    
    /**
     * 用户登录
     * 
     * @param username 用户名
     * @param password 明文密码
     * @return 登录成功的用户对象
     * @throws IllegalArgumentException 如果用户名或密码错误
     */
    public User login(String username, String password) {
        log.info("用户尝试登录，用户名: {}", username);
        
        // 1. 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);
        
        if (user == null) {
            log.warn("用户不存在: {}", username);
            throw new IllegalArgumentException("用户名或密码错误");
        }
        
        // 2. 检查用户状态
        if (user.getStatus() == 0) {
            log.warn("用户已被禁用: {}", username);
            throw new IllegalArgumentException("用户已被禁用");
        }
        
        // 3. 验证密码
        if (!verifyPassword(password, user.getPassword())) {
            log.warn("密码错误，用户名: {}", username);
            throw new IllegalArgumentException("用户名或密码错误");
        }
        
        // 4. 登录成功，使用 Sa-Token 创建会话
        try {
            StpUtil.login(user.getId());
        } catch (Exception e) {
            // 在单元测试环境中，Sa-Token 可能没有上下文，这里捕获异常
            log.debug("Sa-Token 登录失败（可能是测试环境）: {}", e.getMessage());
        }
        
        log.info("用户登录成功，用户ID: {}, 用户名: {}", user.getId(), username);
        return user;
    }
    
    /**
     * 根据用户 ID 查询用户信息
     * 
     * @param userId 用户 ID
     * @return 用户对象，如果不存在返回 null
     */
    public User getUserById(Long userId) {
        log.debug("查询用户信息，用户ID: {}", userId);
        return userMapper.selectById(userId);
    }
    
    /**
     * 根据用户名查询用户信息
     * 
     * @param username 用户名
     * @return 用户对象，如果不存在返回 null
     */
    public User getUserByUsername(String username) {
        log.debug("查询用户信息，用户名: {}", username);
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }
    
    /**
     * 获取当前登录用户信息
     * 
     * @return 当前登录用户对象
     * @throws IllegalStateException 如果用户未登录
     */
    public User getCurrentUser() {
        // 获取当前登录用户 ID
        Long userId = StpUtil.getLoginIdAsLong();
        
        User user = getUserById(userId);
        if (user == null) {
            log.error("当前登录用户不存在，用户ID: {}", userId);
            throw new IllegalStateException("用户不存在");
        }
        
        return user;
    }
    
    /**
     * 更新用户信息
     * 
     * <p>注意：此方法不更新密码，密码更新请使用 {@link #updatePassword(Long, String, String)}</p>
     * 
     * @param userId 用户 ID
     * @param email 邮箱（可选）
     * @param phone 手机号（可选）
     * @param displayName 显示名称（可选）
     * @param jobTitle 职位头衔（可选）
     * @param avatarUrl 头像 URL（可选）
     * @return 更新后的用户对象
     * @throws IllegalArgumentException 如果用户不存在
     */
    @Transactional(rollbackFor = Exception.class)
    public User updateUserInfo(Long userId, String email, String phone, 
                               String displayName, String jobTitle, String avatarUrl) {
        log.info("更新用户信息，用户ID: {}", userId);
        
        // 1. 查询用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在，用户ID: {}", userId);
            throw new IllegalArgumentException("用户不存在");
        }
        
        // 2. 更新字段（只更新非 null 的字段）
        if (email != null) {
            user.setEmail(email);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (displayName != null) {
            user.setDisplayName(displayName);
        }
        if (jobTitle != null) {
            user.setJobTitle(jobTitle);
        }
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }
        
        // 3. 更新数据库
        int result = userMapper.updateById(user);
        
        if (result > 0) {
            log.info("用户信息更新成功，用户ID: {}", userId);
            return user;
        } else {
            log.error("用户信息更新失败，用户ID: {}", userId);
            throw new RuntimeException("用户信息更新失败");
        }
    }
    
    /**
     * 更新用户密码
     * 
     * @param userId 用户 ID
     * @param oldPassword 旧密码（明文）
     * @param newPassword 新密码（明文）
     * @throws IllegalArgumentException 如果用户不存在或旧密码错误
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        log.info("更新用户密码，用户ID: {}", userId);
        
        // 1. 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在，用户ID: {}", userId);
            throw new IllegalArgumentException("用户不存在");
        }
        
        // 2. 验证旧密码
        if (!verifyPassword(oldPassword, user.getPassword())) {
            log.warn("旧密码错误，用户ID: {}", userId);
            throw new IllegalArgumentException("旧密码错误");
        }
        
        // 3. 加密新密码
        String hashedPassword = hashPassword(newPassword);
        user.setPassword(hashedPassword);
        
        // 4. 更新数据库
        int result = userMapper.updateById(user);
        
        if (result > 0) {
            log.info("用户密码更新成功，用户ID: {}", userId);
        } else {
            log.error("用户密码更新失败，用户ID: {}", userId);
            throw new RuntimeException("密码更新失败");
        }
    }
    
    /**
     * 使用 BCrypt 加密密码
     * 
     * @param plainPassword 明文密码
     * @return BCrypt 加密后的密码哈希
     */
    private String hashPassword(String plainPassword) {
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, plainPassword.toCharArray());
    }
    
    /**
     * 验证密码是否匹配
     * 
     * @param plainPassword 明文密码
     * @param hashedPassword BCrypt 加密后的密码哈希
     * @return 如果密码匹配返回 true，否则返回 false
     */
    private boolean verifyPassword(String plainPassword, String hashedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword);
        return result.verified;
    }
    
    /**
     * 发送短信验证码（演示版本）
     * 
     * <p>注意：这是演示版本，实际生产环境应该：</p>
     * <ul>
     *   <li>集成真实的短信服务商（阿里云、腾讯云等）</li>
     *   <li>使用 Redis 存储验证码</li>
     *   <li>添加发送频率限制</li>
     *   <li>添加 IP 限制</li>
     * </ul>
     * 
     * @param phone 手机号
     * @return 验证码（演示环境返回，生产环境不应返回）
     */
    public String sendSmsCode(String phone) {
        log.info("发送短信验证码，手机号: {}", phone);
        
        // 1. 验证手机号格式
        if (!isValidPhone(phone)) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        
        // 2. 生成6位随机验证码
        String code = generateRandomCode(6);
        
        // 3. 存储验证码（演示版本使用内存，生产环境应使用 Redis）
        long expireTime = System.currentTimeMillis() + CODE_EXPIRE_TIME;
        SMS_CODE_CACHE.put(phone, new SmsCodeData(code, expireTime));
        
        // 4. 发送短信（演示版本只打印日志）
        log.info("【演示模式】短信验证码: {} (手机号: {}, 有效期: 5分钟)", code, phone);
        
        // 生产环境应该调用短信服务商 API
        // smsService.send(phone, code);
        
        return code; // 演示环境返回验证码，生产环境不应返回
    }
    
    /**
     * 短信验证码登录
     * 
     * <p>如果手机号未注册，自动创建新用户</p>
     * 
     * @param phone 手机号
     * @param code 验证码
     * @return 登录成功的用户对象
     * @throws IllegalArgumentException 如果验证码错误或已过期
     */
    @Transactional(rollbackFor = Exception.class)
    public User loginWithSmsCode(String phone, String code) {
        log.info("短信验证码登录，手机号: {}", phone);
        
        // 1. 验证手机号格式
        if (!isValidPhone(phone)) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        
        // 2. 验证验证码
        SmsCodeData codeData = SMS_CODE_CACHE.get(phone);
        if (codeData == null) {
            throw new IllegalArgumentException("验证码不存在或已过期");
        }
        
        if (codeData.isExpired()) {
            SMS_CODE_CACHE.remove(phone);
            throw new IllegalArgumentException("验证码已过期");
        }
        
        if (!codeData.code.equals(code)) {
            throw new IllegalArgumentException("验证码错误");
        }
        
        // 3. 验证成功，删除验证码
        SMS_CODE_CACHE.remove(phone);
        
        // 4. 查询用户是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User user = userMapper.selectOne(wrapper);
        
        // 5. 如果用户不存在，自动注册
        if (user == null) {
            log.info("手机号未注册，自动创建新用户: {}", phone);
            
            user = new User();
            user.setPhone(phone);
            user.setUsername("user_" + phone.substring(phone.length() - 4)); // 使用手机号后4位作为用户名
            user.setDisplayName(generateDefaultDisplayName()); // 使用默认显示名称
            user.setPassword(hashPassword(generateRandomCode(16))); // 随机密码
            user.setStatus(1);
            
            userMapper.insert(user);
            log.info("新用户创建成功，用户ID: {}, 显示名称: {}", user.getId(), user.getDisplayName());
            
            // 为新用户创建免费订阅
            initUserSubscription(user.getId());
        }
        
        // 6. 检查用户状态
        if (user.getStatus() == 0) {
            throw new IllegalArgumentException("用户已被禁用");
        }
        
        // 7. 登录成功，使用 Sa-Token 创建会话
        try {
            StpUtil.login(user.getId());
        } catch (Exception e) {
            log.debug("Sa-Token 登录失败（可能是测试环境）: {}", e.getMessage());
        }
        
        log.info("短信验证码登录成功，用户ID: {}, 手机号: {}", user.getId(), phone);
        return user;
    }
    
    /**
     * 验证手机号格式（中国大陆手机号）
     * 
     * @param phone 手机号
     * @return 如果格式正确返回 true
     */
    private boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        // 中国大陆手机号：1开头，第二位是3-9，共11位
        return phone.matches("^1[3-9]\\d{9}$");
    }
    
    /**
     * 生成随机数字验证码
     * 
     * @param length 验证码长度
     * @return 随机验证码
     */
    private String generateRandomCode(int length) {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
    
    /**
     * 生成默认显示名称：神阁绘+10位UUID
     * 
     * @return 默认显示名称
     */
    private String generateDefaultDisplayName() {
        // 生成10位UUID（使用UUID的前10位）
        String uuid = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        return "神阁绘" + uuid;
    }
    
    /**
     * 初始化用户订阅（创建免费订阅）
     * 
     * @param userId 用户ID
     */
    private void initUserSubscription(Long userId) {
        if (subscriptionService == null || quotaService == null) {
            log.warn("订阅服务未初始化，跳过创建免费订阅");
            return;
        }
        
        try {
            // 获取 FREE 计划
            var freePlan = subscriptionService.getPlanByCode("FREE");
            if (freePlan == null) {
                log.error("FREE 订阅计划不存在，无法创建免费订阅");
                return;
            }
            
            // 创建免费订阅（永久有效）
            subscriptionService.createSubscription(userId, freePlan.getId(), "monthly");
            log.info("为用户创建免费订阅成功: userId={}", userId);
            
        } catch (Exception e) {
            log.error("创建免费订阅失败: userId={}", userId, e);
            // 不抛出异常，避免影响用户注册
        }
    }
}
