package com.coffeeviz.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coffeeviz.entity.User;
import com.coffeeviz.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 单元测试")
class UserServiceTest {
    
    @Mock
    private UserMapper userMapper;
    
    @InjectMocks
    private UserService userService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        // 准备测试数据
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$12$hashedPassword"); // BCrypt 加密后的密码
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setStatus(1);
    }
    
    @Test
    @DisplayName("用户注册 - 成功")
    void testRegister_Success() {
        // Given
        String username = "newuser";
        String password = "password123";
        String email = "newuser@example.com";
        String phone = "13900139000";
        
        // Mock: 用户名不存在
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        
        // Mock: 插入成功
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L); // 模拟数据库生成的 ID
            return 1;
        });
        
        // When
        User result = userService.register(username, password, email, phone);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getPhone()).isEqualTo(phone);
        assertThat(result.getStatus()).isEqualTo(1);
        
        // 验证密码已加密（不等于明文）
        assertThat(result.getPassword()).isNotEqualTo(password);
        
        // 验证密码可以被验证
        BCrypt.Result verifyResult = BCrypt.verifyer().verify(password.toCharArray(), result.getPassword());
        assertThat(verifyResult.verified).isTrue();
        
        // 验证调用了 mapper 方法
        verify(userMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
        verify(userMapper, times(1)).insert(any(User.class));
    }
    
    @Test
    @DisplayName("用户注册 - 用户名已存在")
    void testRegister_UsernameExists() {
        // Given
        String username = "existinguser";
        String password = "password123";
        
        // Mock: 用户名已存在
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        
        // When & Then
        assertThatThrownBy(() -> userService.register(username, password, null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("用户名已存在");
        
        // 验证没有调用 insert
        verify(userMapper, never()).insert(any(User.class));
    }
    
    @Test
    @DisplayName("用户登录 - 成功")
    void testLogin_Success() {
        // Given
        String username = "testuser";
        String password = "password123";
        
        // 创建一个真实的 BCrypt 加密密码
        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        testUser.setPassword(hashedPassword);
        
        // Mock: 查询用户
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        
        // When
        User result = userService.login(username, password);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUser.getId());
        assertThat(result.getUsername()).isEqualTo(username);
        
        // 验证调用了 mapper 方法
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }
    
    @Test
    @DisplayName("用户登录 - 用户不存在")
    void testLogin_UserNotFound() {
        // Given
        String username = "nonexistent";
        String password = "password123";
        
        // Mock: 用户不存在
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        
        // When & Then
        assertThatThrownBy(() -> userService.login(username, password))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("用户名或密码错误");
    }
    
    @Test
    @DisplayName("用户登录 - 密码错误")
    void testLogin_WrongPassword() {
        // Given
        String username = "testuser";
        String correctPassword = "password123";
        String wrongPassword = "wrongpassword";
        
        // 创建一个真实的 BCrypt 加密密码
        String hashedPassword = BCrypt.withDefaults().hashToString(12, correctPassword.toCharArray());
        testUser.setPassword(hashedPassword);
        
        // Mock: 查询用户
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        
        // When & Then
        assertThatThrownBy(() -> userService.login(username, wrongPassword))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("用户名或密码错误");
    }
    
    @Test
    @DisplayName("用户登录 - 用户已被禁用")
    void testLogin_UserDisabled() {
        // Given
        String username = "testuser";
        String password = "password123";
        
        testUser.setStatus(0); // 禁用状态
        
        // Mock: 查询用户
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        
        // When & Then
        assertThatThrownBy(() -> userService.login(username, password))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("用户已被禁用");
    }
    
    @Test
    @DisplayName("根据用户 ID 查询用户信息")
    void testGetUserById() {
        // Given
        Long userId = 1L;
        
        // Mock: 查询用户
        when(userMapper.selectById(userId)).thenReturn(testUser);
        
        // When
        User result = userService.getUserById(userId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo(testUser.getUsername());
        
        verify(userMapper, times(1)).selectById(userId);
    }
    
    @Test
    @DisplayName("根据用户名查询用户信息")
    void testGetUserByUsername() {
        // Given
        String username = "testuser";
        
        // Mock: 查询用户
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        
        // When
        User result = userService.getUserByUsername(username);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }
    
    @Test
    @DisplayName("更新用户信息 - 成功")
    void testUpdateUserInfo_Success() {
        // Given
        Long userId = 1L;
        String newEmail = "newemail@example.com";
        String newPhone = "13900139000";
        String newDisplayName = "New Display Name";
        String newJobTitle = "Senior Developer";
        String newAvatarUrl = "https://example.com/avatar.jpg";
        
        // Mock: 查询用户
        when(userMapper.selectById(userId)).thenReturn(testUser);
        
        // Mock: 更新成功
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        
        // When
        User result = userService.updateUserInfo(userId, newEmail, newPhone, 
                                                 newDisplayName, newJobTitle, newAvatarUrl);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(newEmail);
        assertThat(result.getPhone()).isEqualTo(newPhone);
        assertThat(result.getDisplayName()).isEqualTo(newDisplayName);
        assertThat(result.getJobTitle()).isEqualTo(newJobTitle);
        assertThat(result.getAvatarUrl()).isEqualTo(newAvatarUrl);
        
        verify(userMapper, times(1)).selectById(userId);
        verify(userMapper, times(1)).updateById(any(User.class));
    }
    
    @Test
    @DisplayName("更新用户信息 - 部分字段更新")
    void testUpdateUserInfo_PartialUpdate() {
        // Given
        Long userId = 1L;
        String newEmail = "newemail@example.com";
        
        // Mock: 查询用户
        when(userMapper.selectById(userId)).thenReturn(testUser);
        
        // Mock: 更新成功
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        
        // When - 只更新 email，其他字段传 null
        User result = userService.updateUserInfo(userId, newEmail, null, null, null, null);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(newEmail);
        // 其他字段保持不变
        assertThat(result.getPhone()).isEqualTo(testUser.getPhone());
        
        verify(userMapper, times(1)).updateById(any(User.class));
    }
    
    @Test
    @DisplayName("更新用户信息 - 用户不存在")
    void testUpdateUserInfo_UserNotFound() {
        // Given
        Long userId = 999L;
        
        // Mock: 用户不存在
        when(userMapper.selectById(userId)).thenReturn(null);
        
        // When & Then
        assertThatThrownBy(() -> userService.updateUserInfo(userId, "email@test.com", null, null, null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("用户不存在");
        
        verify(userMapper, never()).updateById(any(User.class));
    }
    
    @Test
    @DisplayName("更新用户密码 - 成功")
    void testUpdatePassword_Success() {
        // Given
        Long userId = 1L;
        String oldPassword = "oldpassword123";
        String newPassword = "newpassword456";
        
        // 创建一个真实的 BCrypt 加密密码
        String hashedOldPassword = BCrypt.withDefaults().hashToString(12, oldPassword.toCharArray());
        testUser.setPassword(hashedOldPassword);
        
        // Mock: 查询用户
        when(userMapper.selectById(userId)).thenReturn(testUser);
        
        // Mock: 更新成功
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        
        // When
        userService.updatePassword(userId, oldPassword, newPassword);
        
        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper, times(1)).updateById(userCaptor.capture());
        
        User updatedUser = userCaptor.getValue();
        assertThat(updatedUser.getPassword()).isNotEqualTo(hashedOldPassword);
        
        // 验证新密码可以被验证
        BCrypt.Result verifyResult = BCrypt.verifyer().verify(newPassword.toCharArray(), updatedUser.getPassword());
        assertThat(verifyResult.verified).isTrue();
    }
    
    @Test
    @DisplayName("更新用户密码 - 旧密码错误")
    void testUpdatePassword_WrongOldPassword() {
        // Given
        Long userId = 1L;
        String correctOldPassword = "oldpassword123";
        String wrongOldPassword = "wrongpassword";
        String newPassword = "newpassword456";
        
        // 创建一个真实的 BCrypt 加密密码
        String hashedPassword = BCrypt.withDefaults().hashToString(12, correctOldPassword.toCharArray());
        testUser.setPassword(hashedPassword);
        
        // Mock: 查询用户
        when(userMapper.selectById(userId)).thenReturn(testUser);
        
        // When & Then
        assertThatThrownBy(() -> userService.updatePassword(userId, wrongOldPassword, newPassword))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("旧密码错误");
        
        verify(userMapper, never()).updateById(any(User.class));
    }
    
    @Test
    @DisplayName("更新用户密码 - 用户不存在")
    void testUpdatePassword_UserNotFound() {
        // Given
        Long userId = 999L;
        
        // Mock: 用户不存在
        when(userMapper.selectById(userId)).thenReturn(null);
        
        // When & Then
        assertThatThrownBy(() -> userService.updatePassword(userId, "oldpass", "newpass"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("用户不存在");
        
        verify(userMapper, never()).updateById(any(User.class));
    }
    
    @Test
    @DisplayName("BCrypt 密码加密验证")
    void testBCryptPasswordHashing() {
        // Given
        String plainPassword = "testPassword123!@#";
        
        // When - 注册用户（会触发密码加密）
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return 1;
        });
        
        User user = userService.register("testuser", plainPassword, null, null);
        
        // Then
        // 1. 密码不应该是明文
        assertThat(user.getPassword()).isNotEqualTo(plainPassword);
        
        // 2. 密码应该以 $2a$ 开头（BCrypt 标识）
        assertThat(user.getPassword()).startsWith("$2a$");
        
        // 3. 密码长度应该是 60 个字符（BCrypt 标准长度）
        assertThat(user.getPassword()).hasSize(60);
        
        // 4. 相同密码多次加密应该产生不同的哈希值（因为有随机盐）
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        User user2 = userService.register("testuser2", plainPassword, null, null);
        assertThat(user.getPassword()).isNotEqualTo(user2.getPassword());
        
        // 5. 但两个哈希值都应该能验证原始密码
        BCrypt.Result result1 = BCrypt.verifyer().verify(plainPassword.toCharArray(), user.getPassword());
        BCrypt.Result result2 = BCrypt.verifyer().verify(plainPassword.toCharArray(), user2.getPassword());
        assertThat(result1.verified).isTrue();
        assertThat(result2.verified).isTrue();
    }
}
