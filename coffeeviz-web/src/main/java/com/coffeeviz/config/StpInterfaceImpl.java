package com.coffeeviz.config;

import cn.dev33.satoken.stp.StpInterface;
import com.coffeeviz.entity.User;
import com.coffeeviz.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 权限/角色数据源实现
 * <p>
 * Sa-Token 通过此接口获取用户的角色列表和权限列表，
 * 用于 StpUtil.checkRole() / StpUtil.checkPermission() 等鉴权方法。
 * </p>
 *
 * @author CoffeeViz Team
 * @since 2.0.0
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取用户权限列表
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

    /**
     * 获取用户角色列表
     * <p>
     * 判断逻辑：username 为 "admin" 的用户拥有 admin 角色，
     * 其余用户为普通 user 角色。
     * </p>
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> roles = new ArrayList<>();
        roles.add("user");

        try {
            Long userId = Long.parseLong(loginId.toString());
            User user = userMapper.selectById(userId);
            if (user != null && "admin".equals(user.getUsername())) {
                roles.add("admin");
            }
        } catch (Exception e) {
            // 解析失败，仅返回 user 角色
        }

        return roles;
    }
}
