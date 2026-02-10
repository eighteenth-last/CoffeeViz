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
 * 团队实体
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("biz_team")
public class Team {
    
    /**
     * 团队ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 团队名称
     */
    private String teamName;
    
    /**
     * 团队唯一标识
     */
    private String teamCode;
    
    /**
     * 团队描述
     */
    private String description;
    
    /**
     * 团队头像
     */
    private String avatarUrl;
    
    /**
     * 所有者ID
     */
    private Long ownerId;
    
    /**
     * 绑定的架构归档库ID
     */
    private Long repositoryId;
    
    /**
     * 成员数量
     */
    private Integer memberCount;
    
    /**
     * 最大成员数
     */
    private Integer maxMembers;
    
    /**
     * 状态: active-活跃, suspended-暂停, deleted-已删除
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
    private String ownerName;
    
    @TableField(exist = false)
    private String repositoryName;
    
    @TableField(exist = false)
    private String userRole; // 当前用户在团队中的角色
}
