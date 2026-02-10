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
 * 团队邀请链接实体
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("biz_team_invitation")
public class TeamInvitation {
    
    /**
     * 邀请ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 团队ID
     */
    private Long teamId;
    
    /**
     * 邀请码（UUID）
     */
    private String inviteCode;
    
    /**
     * 完整邀请链接
     */
    private String inviteUrl;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
    
    /**
     * 最大使用次数（0=无限制）
     */
    private Integer maxUses;
    
    /**
     * 已使用次数
     */
    private Integer usedCount;
    
    /**
     * 状态: active-有效, disabled-禁用, expired-已过期
     */
    private String status;
    
    /**
     * 过期时间（NULL=永久有效）
     */
    private LocalDateTime expireTime;
    
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
    private String teamName;
    
    @TableField(exist = false)
    private String creatorName;
}
