package com.coffeeviz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 团队成员实体
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("biz_team_member")
public class TeamMember {
    
    /**
     * 成员ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 团队ID
     */
    private Long teamId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 角色: owner-所有者, member-成员
     */
    private String role;
    
    /**
     * 团队内昵称
     */
    private String nickname;
    
    /**
     * 加入时间
     */
    private LocalDateTime joinTime;
    
    /**
     * 加入方式: create-创建, invite-邀请
     */
    private String joinSource;
    
    /**
     * 使用的邀请码
     */
    private String inviteCode;
    
    /**
     * 状态: active-活跃, inactive-停用
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    // 关联查询字段
    @TableField(exist = false)
    private String username;
    
    @TableField(exist = false)
    private String email;
    
    @TableField(exist = false)
    private String avatarUrl;
    
    @TableField(exist = false)
    private String displayName;
}
